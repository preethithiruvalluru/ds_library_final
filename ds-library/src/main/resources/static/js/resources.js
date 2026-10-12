/* Resources: PDFs, links, documentation, tutorials, YouTube videos and study material. */
Views.resources = async function (el, isCurrent) {
  const TYPES = {
    PDF: ["PDF", "bi-file-earmark-pdf"], LINK: ["Website link", "bi-link-45deg"], DOCUMENTATION: ["Documentation", "bi-book"],
    TUTORIAL: ["Tutorial", "bi-mortarboard"], YOUTUBE: ["YouTube video", "bi-youtube"], MATERIAL: ["Learning material", "bi-collection"]
  };
  let modules = [], subjects = [], tags = [], items = [];
  const filters = { q: "", type: "", subject: "", moduleId: "", tag: "" };
  let timer;

  const moduleName = (id) => { const m = modules.find(x => x.id === id); return m ? `Module ${m.moduleNumber}: ${m.name}` : ""; };
  const opts = (list, selected, allLabel) => `<option value="">${allLabel}</option>` +
    list.map(([v, t]) => `<option value="${UI.escape(v)}" ${v === selected ? "selected" : ""}>${UI.escape(t)}</option>`).join("");

  function shell() {
    el.innerHTML = UI.pageHead("Resources", "PDFs, links, documentation, tutorials and videos.") + `
      <div class="row g-2 mb-3">
        <div class="col-lg-4"><input class="form-control" id="fq" type="search" placeholder="Search title, description or tags..." aria-label="Search resources" value="${UI.escape(filters.q)}"></div>
        <div class="col-6 col-lg-2"><select class="form-select" id="ftype" aria-label="Filter by type">${opts(Object.entries(TYPES).map(([k, v]) => [k, v[0]]), filters.type, "All types")}</select></div>
        <div class="col-6 col-lg-2"><select class="form-select" id="fsub" aria-label="Filter by subject">${opts(subjects.map(s => [s, s]), filters.subject, "All subjects")}</select></div>
        <div class="col-6 col-lg-2"><select class="form-select" id="fmod" aria-label="Filter by module">${opts(modules.map(m => [m.id, `Module ${m.moduleNumber}: ${m.name}`]), filters.moduleId, "All modules")}</select></div>
        <div class="col-6 col-lg-2"><select class="form-select" id="ftag" aria-label="Filter by tag">${opts(tags.map(t => [t, "#" + t]), filters.tag, "All tags")}</select></div>
      </div>
      <div id="resList"></div>`;
  }

  function actions(r) {
    if (r.type === "PDF") {
      return r.file
        ? `<button class="btn btn-sm btn-primary" data-open="${UI.escape(r.file.fileId)}" data-title="${UI.escape(r.title)}"><i class="bi bi-book"></i> Read</button>
           <a class="btn btn-sm btn-outline-secondary" href="/api/files/${encodeURIComponent(r.file.fileId)}?download=true"><i class="bi bi-download"></i> Download</a>`
        : `<span class="text-muted small">No PDF uploaded yet.</span>`;
    }
    const open = `<a class="btn btn-sm btn-outline-primary" href="${UI.escape(r.url)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-box-arrow-up-right"></i> Open</a>`;
    if (r.type === "YOUTUBE" && r.videoId) {
      return `<button class="btn btn-sm btn-primary" data-watch="${r.id}"><i class="bi bi-play-circle"></i> Watch here</button>${open}`;
    }
    return open;
  }

  function drawList() {
    if (!isCurrent()) return;
    const box = el.querySelector("#resList");
    if (!box) return;
    if (!items.length) {
      const filtered = Object.values(filters).some(Boolean);
      box.innerHTML = `<div class="card-ds">${UI.empty("bi-link-45deg", filtered ? "No resources match your search or filters." : "Resources will appear here.", true)}</div>`;
      return;
    }
    box.innerHTML = `<div class="row g-3">${items.map(r => `
      <div class="col-md-6 col-xl-4"><div class="card-ds note-card">
        <div class="d-flex justify-content-between align-items-start gap-2">
          <div><span class="badge text-bg-light border mb-1"><i class="bi ${(TYPES[r.type] || TYPES.LINK)[1]}"></i> ${(TYPES[r.type] || TYPES.LINK)[0]}</span>
            <h5 class="mb-1">${UI.escape(r.title)}</h5></div></div>
        <div class="small text-muted mb-2">${[r.subject, moduleName(r.moduleId)].filter(Boolean).map(UI.escape).join(" · ") || "No subject"}</div>
        <p class="note-preview">${UI.escape(r.description)}</p>
        <div class="mb-2">${(r.tags || []).map(t => `<span class="badge text-bg-light border me-1">#${UI.escape(t)}</span>`).join("")}</div>
        <div class="d-flex flex-wrap gap-2 mt-auto">${actions(r)}</div>
        <div class="small text-muted mt-2">Added ${new Date(r.createdAt).toLocaleDateString()}</div>
      </div></div>`).join("")}</div>`;
  }

  async function load() {
    const p = new URLSearchParams();
    Object.entries(filters).forEach(([k, v]) => v && p.set(k, v));
    items = await API.get("/api/resources?" + p);
    drawList();
  }

  async function refreshAll() {
    [subjects, tags] = await Promise.all([API.get("/api/resources/subjects"), API.get("/api/resources/tags")]);
    if (filters.subject && !subjects.includes(filters.subject)) filters.subject = "";
    if (filters.tag && !tags.includes(filters.tag)) filters.tag = "";
    shell();
    await load();
  }

  /* The video only loads after the user clicks "Watch here" (privacy-friendly youtube-nocookie player). */
  function watch(r) {
    const wrap = document.createElement("div");
    wrap.className = "modal fade";
    wrap.tabIndex = -1;
    wrap.innerHTML = `<div class="modal-dialog modal-lg modal-dialog-centered"><div class="modal-content">
      <div class="modal-header"><h5 class="modal-title">${UI.escape(r.title)}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body"><div class="ratio ratio-16x9"><iframe title="${UI.escape(r.title)}"
        src="https://www.youtube-nocookie.com/embed/${encodeURIComponent(r.videoId)}" allow="fullscreen; picture-in-picture" allowfullscreen
        referrerpolicy="strict-origin-when-cross-origin" sandbox="allow-scripts allow-same-origin allow-presentation allow-popups"></iframe></div>
        <p class="small text-muted mt-2 mb-0">If the video does not play here, <a href="${UI.escape(r.url)}" target="_blank" rel="noopener noreferrer">open it on YouTube</a>.</p></div>
      </div></div>`;
    document.body.appendChild(wrap);
    const modal = new bootstrap.Modal(wrap);
    wrap.addEventListener("hidden.bs.modal", () => { modal.dispose(); wrap.remove(); });   // removing the iframe stops playback
    modal.show();
  }

  function resForm(r) {
    r = r || { title: "", description: "", type: "LINK", subject: "", moduleId: "", url: "", tags: [] };
    UI.formModal({
      title: r.id ? "Edit resource" : "Add resource",
      body: `
        <div class="mb-3"><label class="form-label" for="rTitle">Title</label>
          <input class="form-control" id="rTitle" name="title" maxlength="200" required value="${UI.escape(r.title)}"></div>
        <div class="row g-2 mb-3">
          <div class="col-md-6"><label class="form-label" for="rType">Type</label>
            <select class="form-select" id="rType" name="type">${Object.entries(TYPES).map(([k, v]) => `<option value="${k}" ${k === r.type ? "selected" : ""}>${v[0]}</option>`).join("")}</select></div>
          <div class="col-md-6"><label class="form-label" for="rSub">Subject</label>
            <input class="form-control" id="rSub" name="subject" maxlength="150" list="rSubList" value="${UI.escape(r.subject)}">
            <datalist id="rSubList">${subjects.map(s => `<option value="${UI.escape(s)}">`).join("")}</datalist></div>
        </div>
        <div class="mb-3"><label class="form-label" for="rMod">Related module</label>
          <select class="form-select" id="rMod" name="moduleId">${opts(modules.map(m => [m.id, `Module ${m.moduleNumber}: ${m.name}`]), r.moduleId || "", "No module")}</select></div>
        <div class="mb-3" data-for="url"><label class="form-label" for="rUrl">URL</label>
          <input class="form-control" id="rUrl" name="url" type="url" maxlength="500" placeholder="https://..." value="${UI.escape(r.url)}">
          <div class="form-text" data-yt-hint hidden>Paste a normal YouTube video link (youtube.com/watch?v=... or youtu.be/...).</div></div>
        <div class="mb-3" data-for="file"><label class="form-label" for="rFile">PDF file (max 25 MB)</label>
          <input class="form-control" id="rFile" name="file" type="file" accept="application/pdf,.pdf">
          ${r.file ? `<div class="form-text">Current file: ${UI.escape(r.file.filename)}. Choose a new file to replace it.</div>` : ""}</div>
        <div class="mb-3"><label class="form-label" for="rDesc">Description</label>
          <textarea class="form-control" id="rDesc" name="description" rows="3" maxlength="1000">${UI.escape(r.description)}</textarea></div>
        <div class="mb-1"><label class="form-label" for="rTags">Tags (comma separated)</label>
          <input class="form-control" id="rTags" name="tags" placeholder="pandas, beginner" value="${UI.escape((r.tags || []).join(", "))}"></div>`,
      onOpen: (f) => {
        const sync = () => {
          const pdf = f.type.value === "PDF";
          f.querySelector("[data-for=url]").hidden = pdf;
          f.querySelector("[data-for=file]").hidden = !pdf;
          f.url.required = !pdf;
          f.querySelector("[data-yt-hint]").hidden = f.type.value !== "YOUTUBE";
        };
        f.type.addEventListener("change", sync);
        sync();
      },
      onSubmit: async (f) => {
        const file = f.file.files[0];
        const isPdf = f.type.value === "PDF";
        if (isPdf && file && file.size > 25 * 1048576) throw new Error("File is larger than 25 MB.");
        if (isPdf && !file && !r.file) throw new Error("Please choose a PDF file to upload.");
        const data = {
          title: f.title.value, description: f.description.value, type: f.type.value, subject: f.subject.value, moduleId: f.moduleId.value,
          url: isPdf ? "" : f.url.value, tags: f.tags.value.split(",").map(t => t.trim()).filter(Boolean)
        };
        const saved = r.id ? await API.put(`/api/resources/${r.id}`, data) : await API.post("/api/resources", data);
        if (isPdf && file) {
          try {
            const fd = new FormData();
            fd.append("file", file);
            await API.post(`/api/resources/${saved.id}/file`, fd);
          } catch (err) {
            UI.toast("The resource was saved, but the PDF was not uploaded: " + err.message, "warning");
          }
        }
        UI.toast(r.id ? "Resource updated." : "Resource added.");
        await refreshAll();
      }
    });
  }

  const filterMap = { ftype: "type", fsub: "subject", fmod: "moduleId", ftag: "tag" };
  el.addEventListener("input", (e) => {
    if (e.target.id !== "fq") return;
    clearTimeout(timer);
    timer = setTimeout(() => { filters.q = e.target.value; load().catch(err => UI.toast(err.message, "danger")); }, 300);
  });
  el.addEventListener("change", (e) => {
    if (!filterMap[e.target.id]) return;
    filters[filterMap[e.target.id]] = e.target.value;
    load().catch(err => UI.toast(err.message, "danger"));
  });
  el.addEventListener("click", async (e) => {
    const t = e.target.closest("[data-open],[data-watch],[data-edit],[data-del],#addRes");
    if (!t) return;
    try {
      if (t.id === "addRes") resForm();
      else if (t.dataset.open) PdfViewer.open(t.dataset.open, t.dataset.title);
      else if (t.dataset.watch) watch(items.find(x => x.id === t.dataset.watch));
      else if (t.dataset.edit) resForm(items.find(x => x.id === t.dataset.edit));
      else if (t.dataset.del) {
        if (!confirm("Delete this resource? This cannot be undone.")) return;
        await API.del(`/api/resources/${t.dataset.del}`);
        UI.toast("Resource deleted.");
        await refreshAll();
      }
    } catch (err) {
      UI.toast(err.message, "danger");
    }
  });

  el.innerHTML = UI.loading();
  try {
    modules = await API.get("/api/modules");
    await refreshAll();
  } catch (err) {
    if (isCurrent()) el.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
  }
};
