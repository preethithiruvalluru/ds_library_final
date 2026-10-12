/* Tools directory: cards grouped by category, loaded from the backend (/api/tools). */
Views.tools = async function (el, isCurrent) {
  const CATEGORY_ORDER = ["Programming Languages", "Data Analysis", "Machine Learning", "Data Visualization",
    "Databases", "Notebook Environments", "Version Control", "Development Tools"];
  const CATEGORY_ICON = {
    "Programming Languages": "bi-code-slash", "Data Analysis": "bi-table", "Machine Learning": "bi-cpu",
    "Data Visualization": "bi-bar-chart-line", "Databases": "bi-database", "Notebook Environments": "bi-journal-code",
    "Version Control": "bi-git", "Development Tools": "bi-wrench-adjustable"
  };
  // Only real YouTube playlist links become buttons (the server checks this too).
  const YT_PLAYLIST = /^https:\/\/(www\.)?youtube\.com\/playlist\?list=[A-Za-z0-9_-]+$/;
  let tools = [];
  const filters = { q: "", category: "" };
  let timer;

  const categories = () => {
    const all = [...new Set([...CATEGORY_ORDER, ...tools.map(t => t.category)])];
    return all;
  };

  function shell(allCats) {
    el.innerHTML = UI.pageHead("Tools", "A directory of useful Data Science tools.") + `
      <div class="row g-2 mb-3">
        <div class="col-md-8"><input class="form-control" id="fq" type="search" placeholder="Search tools..." aria-label="Search tools" value="${UI.escape(filters.q)}"></div>
        <div class="col-md-4"><select class="form-select" id="fcat" aria-label="Filter by category"><option value="">All categories</option>
          ${allCats.map(c => `<option ${c === filters.category ? "selected" : ""}>${UI.escape(c)}</option>`).join("")}</select></div>
      </div>
      <div id="toolList"></div>`;
  }

  function drawList() {
    if (!isCurrent()) return;
    const box = el.querySelector("#toolList");
    if (!box) return;
    if (!tools.length) {
      const filtered = filters.q || filters.category;
      box.innerHTML = `<div class="card-ds">${UI.empty("bi-tools", filtered ? "No tools match your search." : "No tools available yet.", true)}</div>`;
      return;
    }
    const groups = {};
    tools.forEach(t => (groups[t.category] = groups[t.category] || []).push(t));
    const ordered = Object.keys(groups).sort((a, b) => {
      const ia = CATEGORY_ORDER.indexOf(a), ib = CATEGORY_ORDER.indexOf(b);
      return (ia < 0 ? 99 : ia) - (ib < 0 ? 99 : ib) || a.localeCompare(b);
    });
    box.innerHTML = ordered.map(cat => `
      <h4 class="subject-title"><i class="bi ${CATEGORY_ICON[cat] || "bi-tools"}"></i> ${UI.escape(cat)}</h4>
      <div class="row g-3 mb-4">${groups[cat].map(t => `
        <div class="col-sm-6 col-xl-4"><div class="card-ds tool-card">
          <div class="d-flex align-items-center gap-3 mb-2">
            <div class="stat-icon"><i class="bi ${UI.escape(t.icon || CATEGORY_ICON[cat] || "bi-tools")}"></i></div>
            <h5 class="mb-0 flex-grow-1">${UI.escape(t.name)}</h5></div>
          <p class="text-muted small mb-3">${UI.escape(t.description) || "No description."}</p>
          <div class="d-flex flex-wrap gap-2 mt-auto">
            <a class="btn btn-sm btn-outline-primary" href="${UI.escape(t.officialUrl)}" target="_blank" rel="noopener noreferrer">
              <i class="bi bi-box-arrow-up-right"></i> Official website</a>
            ${YT_PLAYLIST.test(t.playlistUrl || "") ? `<a class="btn btn-sm btn-youtube" href="${UI.escape(t.playlistUrl)}" target="_blank" rel="noopener noreferrer"
              title="Best YouTube playlist to learn ${UI.escape(t.name)}"><i class="bi bi-youtube"></i> YouTube playlist</a>` : ""}
          </div>
        </div></div>`).join("")}</div>`).join("");
  }

  async function load() {
    const p = new URLSearchParams();
    Object.entries(filters).forEach(([k, v]) => v && p.set(k, v));
    tools = await API.get("/api/tools?" + p);
    drawList();
  }

  async function refreshAll() {
    const everything = await API.get("/api/tools");   // full list so the category dropdown stays complete
    const cats = [...new Set([...CATEGORY_ORDER, ...everything.map(t => t.category)])];
    shell(cats);
    await load();
  }

  function toolForm(t) {
    t = t || { name: "", category: "", description: "", officialUrl: "", icon: "" };
    UI.formModal({
      title: t.id ? "Edit tool" : "Add tool",
      body: `
        <div class="mb-3"><label class="form-label" for="tName">Tool name</label>
          <input class="form-control" id="tName" name="name" maxlength="80" required value="${UI.escape(t.name)}"></div>
        <div class="mb-3"><label class="form-label" for="tCat">Category</label>
          <input class="form-control" id="tCat" name="category" maxlength="60" required list="tCatList" value="${UI.escape(t.category)}">
          <datalist id="tCatList">${categories().map(c => `<option value="${UI.escape(c)}">`).join("")}</datalist></div>
        <div class="mb-3"><label class="form-label" for="tDesc">Short description</label>
          <textarea class="form-control" id="tDesc" name="description" rows="2" maxlength="500">${UI.escape(t.description)}</textarea></div>
        <div class="mb-3"><label class="form-label" for="tUrl">Official website</label>
          <input class="form-control" id="tUrl" name="officialUrl" type="url" maxlength="500" required placeholder="https://..." value="${UI.escape(t.officialUrl)}"></div>
        <div class="mb-3"><label class="form-label" for="tPlaylist">YouTube playlist (optional)</label>
          <input class="form-control" id="tPlaylist" name="playlistUrl" type="url" maxlength="120" placeholder="https://www.youtube.com/playlist?list=..."
            pattern="^$|^https://(www\\.)?youtube\\.com/playlist\\?list=[A-Za-z0-9_\\-]+$" value="${UI.escape(t.playlistUrl || "")}">
          <div class="form-text">Open a playlist on YouTube and paste its address (it must contain <code>playlist?list=</code>).</div></div>
        <div class="mb-1"><label class="form-label" for="tIcon">Icon (optional, a Bootstrap Icons class)</label>
          <input class="form-control" id="tIcon" name="icon" pattern="^$|^bi-[a-z0-9-]+$" placeholder="bi-database" value="${UI.escape(t.icon)}">
          <div class="form-text">Find names at icons.getbootstrap.com. Leave empty to use the category icon.</div></div>`,
      onSubmit: async (f) => {
        const data = { name: f.name.value, category: f.category.value, description: f.description.value, officialUrl: f.officialUrl.value, playlistUrl: f.playlistUrl.value.trim(), icon: f.icon.value.trim() };
        if (t.id) await API.put(`/api/tools/${t.id}`, data); else await API.post("/api/tools", data);
        UI.toast(t.id ? "Tool updated." : "Tool added.");
        await refreshAll();
      }
    });
  }

  el.addEventListener("input", (e) => {
    if (e.target.id !== "fq") return;
    clearTimeout(timer);
    timer = setTimeout(() => { filters.q = e.target.value; load().catch(err => UI.toast(err.message, "danger")); }, 300);
  });
  el.addEventListener("change", (e) => {
    if (e.target.id !== "fcat") return;
    filters.category = e.target.value;
    load().catch(err => UI.toast(err.message, "danger"));
  });
  el.addEventListener("click", async (e) => {
    const t = e.target.closest("[data-edit],[data-del],#addTool");
    if (!t) return;
    try {
      if (t.id === "addTool") toolForm();
      else if (t.dataset.edit) toolForm(tools.find(x => x.id === t.dataset.edit));
      else if (t.dataset.del) {
        if (!confirm("Delete this tool?")) return;
        await API.del(`/api/tools/${t.dataset.del}`);
        UI.toast("Tool deleted.");
        await refreshAll();
      }
    } catch (err) {
      UI.toast(err.message, "danger");
    }
  });

  el.innerHTML = UI.loading();
  try {
    await refreshAll();
  } catch (err) {
    if (isCurrent()) el.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
  }
};
