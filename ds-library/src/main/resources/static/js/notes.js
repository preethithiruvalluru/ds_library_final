/* Running Notes: create, view, edit, delete, search, and filter by module / subject / tag. */
Views.notes = async function (el, isCurrent) {
  let modules = [], subjects = [], tags = [], notes = [];
  const filters = { q: "", moduleId: "", subject: "", tag: "" };
  let timer;

  const moduleName = (id) => { const m = modules.find(x => x.id === id); return m ? `Module ${m.moduleNumber}: ${m.name}` : ""; };
  const opts = (list, selected, allLabel) => `<option value="">${allLabel}</option>` +
    list.map(([v, t]) => `<option value="${UI.escape(v)}" ${v === selected ? "selected" : ""}>${UI.escape(t)}</option>`).join("");

  function shell() {
    el.innerHTML = UI.pageHead("Running Notes", "Write, search and organise your class notes.",
        `<button class="btn btn-primary" id="addNote"><i class="bi bi-plus-lg"></i> New Note</button>`) + `
      <div class="row g-2 mb-3">
        <div class="col-lg-5"><input class="form-control" id="fq" type="search" placeholder="Search title, content or tags..." aria-label="Search notes" value="${UI.escape(filters.q)}"></div>
        <div class="col-6 col-lg-3"><select class="form-select" id="fmod" aria-label="Filter by module">${opts(modules.map(m => [m.id, `Module ${m.moduleNumber}: ${m.name}`]), filters.moduleId, "All modules")}</select></div>
        <div class="col-6 col-lg-2"><select class="form-select" id="fsub" aria-label="Filter by subject">${opts(subjects.map(s => [s, s]), filters.subject, "All subjects")}</select></div>
        <div class="col-12 col-lg-2"><select class="form-select" id="ftag" aria-label="Filter by tag">${opts(tags.map(t => [t, "#" + t]), filters.tag, "All tags")}</select></div>
      </div>
      <div id="noteList"></div>`;
  }

  function drawList() {
    if (!isCurrent()) return;
    const box = el.querySelector("#noteList");
    if (!box) return;
    if (!notes.length) {
      const filtered = Object.values(filters).some(Boolean);
      box.innerHTML = `<div class="card-ds">${UI.empty("bi-journal-text", filtered ? "No notes match your search or filters." : "No notes yet. Click “New Note” to write your first one.", true)}</div>`;
      return;
    }
    box.innerHTML = `<div class="row g-3">${notes.map(n => `
      <div class="col-md-6 col-xl-4"><div class="card-ds note-card">
        <div class="d-flex justify-content-between align-items-start gap-2">
          <h5 class="mb-1 note-title" role="button" tabindex="0" data-view="${n.id}">${UI.escape(n.title)}</h5>
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-primary" data-edit="${n.id}" aria-label="Edit note"><i class="bi bi-pencil"></i></button>
            <button class="btn btn-outline-danger" data-del="${n.id}" aria-label="Delete note"><i class="bi bi-trash"></i></button>
          </div></div>
        <div class="small text-muted mb-2">${[n.subject, moduleName(n.moduleId)].filter(Boolean).map(UI.escape).join(" · ") || "No subject"}</div>
        <p class="note-preview">${UI.escape(n.content)}</p>
        <div class="mb-2">${(n.tags || []).map(t => `<span class="badge text-bg-light border me-1">#${UI.escape(t)}</span>`).join("")}</div>
        <div class="small text-muted mt-auto">Updated ${new Date(n.updatedAt).toLocaleString()}</div>
      </div></div>`).join("")}</div>`;
  }

  async function loadNotes() {
    const p = new URLSearchParams();
    Object.entries(filters).forEach(([k, v]) => v && p.set(k, v));
    notes = await API.get("/api/notes?" + p);
    drawList();
  }

  async function loadLookups() {
    [subjects, tags] = await Promise.all([API.get("/api/notes/subjects"), API.get("/api/notes/tags")]);
  }

  async function refreshAll() {
    await loadLookups();
    // Dropped filter values (e.g. a tag that no longer exists) are cleared.
    if (filters.subject && !subjects.includes(filters.subject)) filters.subject = "";
    if (filters.tag && !tags.includes(filters.tag)) filters.tag = "";
    shell();
    await loadNotes();
  }

  function noteForm(n) {
    n = n || { title: "", subject: "", moduleId: "", content: "", tags: [] };
    UI.formModal({
      title: n.id ? "Edit note" : "New note",
      body: `
        <div class="mb-3"><label class="form-label" for="nTitle">Title</label>
          <input class="form-control" id="nTitle" name="title" maxlength="200" required value="${UI.escape(n.title)}"></div>
        <div class="row g-2 mb-3">
          <div class="col-md-6"><label class="form-label" for="nSub">Subject</label>
            <input class="form-control" id="nSub" name="subject" maxlength="150" list="nSubList" value="${UI.escape(n.subject)}">
            <datalist id="nSubList">${subjects.map(s => `<option value="${UI.escape(s)}">`).join("")}</datalist></div>
          <div class="col-md-6"><label class="form-label" for="nMod">Module</label>
            <select class="form-select" id="nMod" name="moduleId">${opts(modules.map(m => [m.id, `Module ${m.moduleNumber}: ${m.name}`]), n.moduleId || "", "No module")}</select></div>
        </div>
        <div class="mb-3"><label class="form-label" for="nContent">Content</label>
          <textarea class="form-control" id="nContent" name="content" rows="8" maxlength="50000" required>${UI.escape(n.content)}</textarea></div>
        <div class="mb-1"><label class="form-label" for="nTags">Tags (comma separated)</label>
          <input class="form-control" id="nTags" name="tags" placeholder="pandas, dataframe, exam" value="${UI.escape((n.tags || []).join(", "))}"></div>`,
      onSubmit: async (f) => {
        const data = {
          title: f.title.value, subject: f.subject.value, moduleId: f.moduleId.value, content: f.content.value,
          tags: f.tags.value.split(",").map(t => t.trim()).filter(Boolean)
        };
        if (n.id) await API.put(`/api/notes/${n.id}`, data); else await API.post("/api/notes", data);
        UI.toast(n.id ? "Note updated." : "Note saved.");
        await refreshAll();
      }
    });
  }

  function viewNote(n) {
    const wrap = document.createElement("div");
    wrap.className = "modal fade";
    wrap.tabIndex = -1;
    wrap.innerHTML = `<div class="modal-dialog modal-lg modal-dialog-scrollable"><div class="modal-content">
      <div class="modal-header"><h5 class="modal-title">${UI.escape(n.title)}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <div class="small text-muted mb-3">${[n.subject, moduleName(n.moduleId)].filter(Boolean).map(UI.escape).join(" · ")}<br>
          Created ${new Date(n.createdAt).toLocaleString()} · Updated ${new Date(n.updatedAt).toLocaleString()}</div>
        <div class="note-full">${UI.escape(n.content)}</div>
        <div class="mt-3">${(n.tags || []).map(t => `<span class="badge text-bg-light border me-1">#${UI.escape(t)}</span>`).join("")}</div>
      </div></div></div>`;
    document.body.appendChild(wrap);
    const modal = new bootstrap.Modal(wrap);
    wrap.addEventListener("hidden.bs.modal", () => { modal.dispose(); wrap.remove(); });
    modal.show();
  }

  el.addEventListener("input", (e) => {
    if (e.target.id !== "fq") return;
    clearTimeout(timer);
    timer = setTimeout(() => { filters.q = e.target.value; loadNotes().catch(err => UI.toast(err.message, "danger")); }, 300);
  });
  el.addEventListener("change", (e) => {
    const map = { fmod: "moduleId", fsub: "subject", ftag: "tag" };
    if (!map[e.target.id]) return;
    filters[map[e.target.id]] = e.target.value;
    loadNotes().catch(err => UI.toast(err.message, "danger"));
  });
  el.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && e.target.dataset.view) e.target.click();
  });
  el.addEventListener("click", async (e) => {
    const t = e.target.closest("[data-view],[data-edit],[data-del],#addNote");
    if (!t) return;
    try {
      if (t.id === "addNote") noteForm();
      else if (t.dataset.view) viewNote(notes.find(n => n.id === t.dataset.view));
      else if (t.dataset.edit) noteForm(notes.find(n => n.id === t.dataset.edit));
      else if (t.dataset.del) {
        if (!confirm("Delete this note? This cannot be undone.")) return;
        await API.del(`/api/notes/${t.dataset.del}`);
        UI.toast("Note deleted.");
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
