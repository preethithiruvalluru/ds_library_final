/* Modules section: list, add/edit/delete modules, upload and read syllabus / study-material PDFs. */
Views.modules = async function (el, isCurrent) {
  let modules;

  function pdfRow(file, moduleId, kind) {
    const mb = (file.size / 1048576).toFixed(1);
    return `<div class="file-row">
      <button class="btn btn-link p-0 text-start text-truncate" data-open="${UI.escape(file.fileId)}" data-title="${UI.escape(file.title)}">
        <i class="bi bi-file-earmark-pdf text-danger"></i> ${UI.escape(file.title)} <small class="text-muted">(${mb} MB)</small></button>
      <button class="btn btn-sm btn-outline-danger" data-del-file="${UI.escape(file.fileId)}" data-module="${UI.escape(moduleId)}" data-kind="${kind}"
        aria-label="Delete ${UI.escape(file.title)}"><i class="bi bi-trash"></i></button></div>`;
  }

  function card(m) {
    const syllabus = m.syllabus
      ? pdfRow({ ...m.syllabus, title: "Syllabus" }, m.id, "syllabus")
      : `<div class="text-muted small">No syllabus uploaded.</div>`;
    const materials = (m.materials || []).map(f => pdfRow(f, m.id, "material")).join("")
      || `<div class="text-muted small">No study material yet.</div>`;
    return `<div class="col-md-6 col-xl-4"><div class="card-ds module-card">
      <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
        <div><span class="badge bg-primary mb-1">Module ${m.moduleNumber}</span><h5 class="mb-0">${UI.escape(m.name)}</h5></div>
        <div class="btn-group btn-group-sm">
          <button class="btn btn-outline-primary" data-edit="${m.id}" aria-label="Edit module"><i class="bi bi-pencil"></i></button>
          <button class="btn btn-outline-danger" data-del="${m.id}" aria-label="Delete module"><i class="bi bi-trash"></i></button>
        </div></div>
      <p class="text-muted small mb-2">${UI.escape(m.description) || "No description."}</p>
      <div class="section-label">Syllabus</div>${syllabus}
      <button class="btn btn-sm btn-outline-primary mt-1 mb-2" data-up-syllabus="${m.id}"><i class="bi bi-upload"></i> ${m.syllabus ? "Replace" : "Upload"} syllabus</button>
      <div class="section-label">Study material</div>${materials}
      <button class="btn btn-sm btn-outline-primary mt-1" data-up-material="${m.id}"><i class="bi bi-plus-lg"></i> Add material</button>
    </div></div>`;
  }

  function draw() {
    if (!isCurrent()) return;
    const head = UI.pageHead("Modules", "Syllabus and study material, module by module.",
      `<button class="btn btn-primary" id="addModule"><i class="bi bi-plus-lg"></i> Add Module</button>`);
    let body;
    if (!modules.length) {
      body = `<div class="card-ds">${UI.empty("bi-collection", "No modules yet. Click “Add Module” to create your first one.", true)}</div>`;
    } else {
      const bySubject = {};
      modules.forEach(m => (bySubject[m.subject] = bySubject[m.subject] || []).push(m));
      body = Object.entries(bySubject).map(([subject, list]) =>
        `<h4 class="subject-title">${UI.escape(subject)}</h4><div class="row g-3 mb-4">${list.map(card).join("")}</div>`).join("");
    }
    el.innerHTML = head + body;
  }

  async function reload() {
    modules = await API.get("/api/modules");
    draw();
  }

  function moduleForm(m) {
    m = m || { moduleNumber: (modules.reduce((x, y) => Math.max(x, y.moduleNumber), 0) + 1), name: "", subject: "", description: "" };
    UI.formModal({
      title: m.id ? "Edit module" : "Add module",
      body: `
        <div class="mb-3"><label class="form-label" for="fNum">Module number</label>
          <input class="form-control" id="fNum" name="moduleNumber" type="number" min="1" required value="${m.moduleNumber}"></div>
        <div class="mb-3"><label class="form-label" for="fName">Module name</label>
          <input class="form-control" id="fName" name="name" maxlength="150" required value="${UI.escape(m.name)}"></div>
        <div class="mb-3"><label class="form-label" for="fSub">Subject</label>
          <input class="form-control" id="fSub" name="subject" maxlength="150" required list="subjectList" value="${UI.escape(m.subject)}">
          <datalist id="subjectList">${[...new Set(modules.map(x => x.subject))].map(s => `<option value="${UI.escape(s)}">`).join("")}</datalist></div>
        <div class="mb-1"><label class="form-label" for="fDesc">Description (optional)</label>
          <textarea class="form-control" id="fDesc" name="description" rows="3" maxlength="2000">${UI.escape(m.description)}</textarea></div>`,
      onSubmit: async (f) => {
        const data = { moduleNumber: Number(f.moduleNumber.value), name: f.name.value, subject: f.subject.value, description: f.description.value };
        if (m.id) await API.put(`/api/modules/${m.id}`, data); else await API.post("/api/modules", data);
        UI.toast(m.id ? "Module updated." : "Module added.");
        await reload();
      }
    });
  }

  function uploadForm(moduleId, kind) {
    const isMat = kind === "material";
    UI.formModal({
      title: isMat ? "Add study material" : "Upload syllabus",
      submitText: "Upload",
      body: `${isMat ? `<div class="mb-3"><label class="form-label" for="fTitle">Title</label>
          <input class="form-control" id="fTitle" name="title" maxlength="150" placeholder="e.g. Unit 1 slides"></div>` : ""}
        <div class="mb-1"><label class="form-label" for="fFile">PDF file (max 25 MB)</label>
          <input class="form-control" id="fFile" name="file" type="file" accept="application/pdf,.pdf" required></div>`,
      onSubmit: async (f) => {
        const file = f.file.files[0];
        if (file.size > 25 * 1048576) throw new Error("File is larger than 25 MB.");
        const fd = new FormData();
        fd.append("file", file);
        if (isMat) fd.append("title", f.title.value);
        await API.post(`/api/modules/${moduleId}/${isMat ? "materials" : "syllabus"}`, fd);
        UI.toast("PDF uploaded.");
        await reload();
      }
    });
  }

  el.addEventListener("click", async (e) => {
    const t = e.target.closest("button");
    if (!t) return;
    try {
      if (t.id === "addModule") moduleForm();
      else if (t.dataset.edit) moduleForm(modules.find(m => m.id === t.dataset.edit));
      else if (t.dataset.open) PdfViewer.open(t.dataset.open, t.dataset.title);
      else if (t.dataset.upSyllabus) uploadForm(t.dataset.upSyllabus, "syllabus");
      else if (t.dataset.upMaterial) uploadForm(t.dataset.upMaterial, "material");
      else if (t.dataset.del) {
        if (!confirm("Delete this module and all its PDFs? This cannot be undone.")) return;
        await API.del(`/api/modules/${t.dataset.del}`);
        UI.toast("Module deleted.");
        await reload();
      } else if (t.dataset.delFile) {
        if (!confirm("Delete this PDF?")) return;
        const path = t.dataset.kind === "syllabus" ? "syllabus" : `materials/${t.dataset.delFile}`;
        await API.del(`/api/modules/${t.dataset.module}/${path}`);
        UI.toast("PDF deleted.");
        await reload();
      }
    } catch (err) {
      UI.toast(err.message, "danger");
    }
  });

  el.innerHTML = UI.loading();
  try {
    await reload();
  } catch (err) {
    if (isCurrent()) el.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
  }
};
