/* Modules section (student view): read-only. Shows each module's study-material PDFs ("View PDF" opens the file in a new tab)
   and the YouTube videos saved for that module in Resources. Nothing here adds, edits or deletes modules. */
Views.modules = async function (el, isCurrent) {
  let modules = [];
  let videos = {};            // moduleId -> [resource] (YouTube videos with a stored URL)
  const openIds = new Set();   // module rows the student has opened
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
        ? `<button type="button" class="btn btn-sm btn-primary pdf-view" data-open="${UI.escape(file.fileId)}" data-title="${UI.escape(file.title || label)}"><i class="bi bi-book"></i> View PDF</button>`
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

  /* Each module is one row: an arrow and "Module N: Title". Click it to open the study material and videos. */
  function card(m) {
    const name = `Module ${m.moduleNumber}: ${m.name}`;
    const pdfs = [];
    if (m.syllabus) pdfs.push(pdfItem({ ...m.syllabus, title: "Syllabus" }, "Syllabus", name));
    (m.materials || []).forEach(f => pdfs.push(pdfItem(f, "Study material", name)));
    const vids = videos[m.id] || [];
    const count = pdfs.length ? `<span class="module-count">${pdfs.length} PDF${pdfs.length === 1 ? "" : "s"}</span>` : "";
    return `<details class="module-row" data-module="${UI.escape(m.id)}"${openIds.has(m.id) ? " open" : ""}>
      <summary><span class="module-arrow" aria-hidden="true"></span><span class="module-name">Module ${m.moduleNumber}: ${UI.escape(m.name)}</span>${count}</summary>
      <div class="module-body">
        ${m.description ? `<p class="text-muted small mb-2">${UI.escape(m.description)}</p>` : ""}
        <div class="section-label">PDF study material</div>
        ${pdfs.join("") || `<div class="text-muted small">No PDF study material available yet.</div>`}
        ${vids.length ? `<div class="section-label mt-3">YouTube videos</div>${vids.map(videoItem).join("")}` : ""}
      </div>
    </details>`;
  }

  function draw() {
    if (!isCurrent()) return;
    const head = UI.pageHead("Modules", "Click a module to open its study material and videos.");
    let body;
    if (!modules.length) {
      body = `<div class="card-ds">${UI.empty("bi-collection", "No modules available yet.", true)}</div>`;
    } else {
      const bySubject = {};
      modules.forEach(m => (bySubject[m.subject] = bySubject[m.subject] || []).push(m));
      body = Object.entries(bySubject).map(([subject, list]) =>
        `<h4 class="subject-title">${UI.escape(subject)}</h4><div class="module-list mb-4">${list.map(card).join("")}</div>`).join("");
    }
    el.innerHTML = head + body;
  }

  el.addEventListener("click", (e) => {
    const b = e.target.closest("[data-open]");
    if (b) PdfViewer.open(b.dataset.open, b.dataset.title);       // page-flip book reader
  });

  el.addEventListener("toggle", (e) => {          // remember which rows are open, so a redraw keeps them open
    const d = e.target;
    if (d && d.classList && d.classList.contains("module-row")) { d.open ? openIds.add(d.dataset.module) : openIds.delete(d.dataset.module); }
  }, true);

  /* One quick request asks which stored PDFs are gone; they then show "Unavailable". The page does not wait for it. */
  async function checkFiles() {
    const ids = [];
    modules.forEach(m => { if (m.syllabus) ids.push(m.syllabus.fileId); (m.materials || []).forEach(f => ids.push(f.fileId)); });
    if (!ids.length) return false;
    try {
      const gone = await API.get("/api/files/missing?ids=" + ids.map(encodeURIComponent).join(","));
      gone.forEach(id => missing.add(id));
      return gone.length > 0;
    } catch (e) { return false; }   // network trouble: leave the links usable
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
    [modules] = await Promise.all([API.get("/api/modules"), loadVideos()]);
    draw();                                     // show everything straight away
    if (await checkFiles()) draw();             // then mark any PDF whose file has gone missing
  } catch (err) {
    if (isCurrent()) el.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
  }
};
