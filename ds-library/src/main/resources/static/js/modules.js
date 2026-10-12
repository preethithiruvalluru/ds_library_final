/* Modules section (student view): read-only. Shows each module's study-material PDFs ("View PDF" opens the file in a new tab)
   and the YouTube videos saved for that module in Resources. Nothing here adds, edits or deletes modules. */
Views.modules = async function (el, isCurrent) {
  let modules = [];
  let videos = {};            // moduleId -> [resource] (YouTube videos with a stored URL)
  const missing = new Set();  // fileIds whose stored file can not be found

  const fileUrl = (id) => `/api/files/${encodeURIComponent(id)}`;

  function pdfItem(file, label, moduleName) {
    const mb = file.size ? `${(file.size / 1048576).toFixed(1)} MB` : "";
    const ok = !missing.has(file.fileId);
    return `<div class="pdf-item${ok ? "" : " is-missing"}">
      <div class="pdf-icon" aria-hidden="true"><i class="bi bi-file-earmark-pdf-fill"></i></div>
      <div class="pdf-info">
        <div class="pdf-title">${UI.escape(file.title || label)}</div>
        <div class="pdf-meta">${UI.escape(moduleName)}${mb ? " · " + mb : ""}</div>
      </div>
      ${ok
        ? `<a class="btn btn-sm btn-primary pdf-view" href="${fileUrl(file.fileId)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-eye"></i> View PDF</a>`
        : `<span class="badge text-bg-light border pdf-view" title="The file could not be found">Unavailable</span>`}
    </div>`;
  }

  function videoItem(v) {
    return `<div class="video-item">
      <div class="video-icon" aria-hidden="true"><i class="bi bi-youtube"></i></div>
      <div class="pdf-info"><div class="pdf-title">${UI.escape(v.title)}</div></div>
      <a class="btn btn-sm btn-danger pdf-view" href="${UI.escape(v.url)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-play-fill"></i> Watch Video</a>
    </div>`;
  }

  function card(m) {
    const name = `Module ${m.moduleNumber}: ${m.name}`;
    const pdfs = [];
    if (m.syllabus) pdfs.push(pdfItem({ ...m.syllabus, title: "Syllabus" }, "Syllabus", name));
    (m.materials || []).forEach(f => pdfs.push(pdfItem(f, "Study material", name)));
    const vids = videos[m.id] || [];
    return `<div class="col-12 col-xl-6"><div class="card-ds module-card">
      <span class="badge bg-primary mb-1">Module ${m.moduleNumber}</span>
      <h5 class="mb-1">${UI.escape(m.name)}</h5>
      ${m.description ? `<p class="text-muted small mb-2">${UI.escape(m.description)}</p>` : ""}
      <div class="section-label">PDF study material</div>
      ${pdfs.join("") || `<div class="text-muted small">No PDF study material available yet.</div>`}
      ${vids.length ? `<div class="section-label mt-3">YouTube videos</div>${vids.map(videoItem).join("")}` : ""}
    </div></div>`;
  }

  function draw() {
    if (!isCurrent()) return;
    const head = UI.pageHead("Modules", "Study material and videos, module by module.");
    let body;
    if (!modules.length) {
      body = `<div class="card-ds">${UI.empty("bi-collection", "No modules available yet.", true)}</div>`;
    } else {
      const bySubject = {};
      modules.forEach(m => (bySubject[m.subject] = bySubject[m.subject] || []).push(m));
      body = Object.entries(bySubject).map(([subject, list]) =>
        `<h4 class="subject-title">${UI.escape(subject)}</h4><div class="row g-3 mb-4">${list.map(card).join("")}</div>`).join("");
    }
    el.innerHTML = head + body;
  }

  /* Ask the server whether each stored PDF is really there, so a broken file shows "Unavailable" instead of an error page. */
  async function checkFiles() {
    const ids = [];
    modules.forEach(m => { if (m.syllabus) ids.push(m.syllabus.fileId); (m.materials || []).forEach(f => ids.push(f.fileId)); });
    await Promise.all(ids.map(async id => {
      try { const r = await fetch(fileUrl(id), { method: "HEAD" }); if (!r.ok) missing.add(id); }
      catch (e) { /* network trouble: leave the link usable */ }
    }));
  }

  async function loadVideos() {
    videos = {};
    try {
      const list = await API.get("/api/resources?type=YOUTUBE");
      list.forEach(v => { if (v.moduleId && v.url) (videos[v.moduleId] = videos[v.moduleId] || []).push(v); });
    } catch (e) { /* videos are optional: show the modules without them */ }
  }

  el.innerHTML = UI.loading();
  try {
    modules = await API.get("/api/modules");
    await Promise.all([checkFiles(), loadVideos()]);
    draw();
  } catch (err) {
    if (isCurrent()) el.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
  }
};
