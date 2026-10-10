/* Home dashboard: every number and list comes from GET /api/dashboard. */
Views.home = async function (el, isCurrent) {
  el.innerHTML = UI.loading();
  let d;
  try {
    d = await API.get("/api/dashboard");
  } catch (err) {
    if (isCurrent()) el.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
    return;
  }
  if (!isCurrent()) return;

  const stats = [
    ["Modules", "bi-collection", d.counts.modules], ["Running Notes", "bi-journal-text", d.counts.notes],
    ["Lab Experiments", "bi-clipboard-data", d.counts.experiments], ["Study Resources", "bi-link-45deg", d.counts.resources]
  ].map(([label, icon, n]) => `
    <div class="col-6 col-xl-3"><div class="card-ds stat-card">
      <div class="stat-icon"><i class="bi ${icon}"></i></div>
      <div><div class="stat-value">${n}</div><div class="stat-label">${label}</div></div>
    </div></div>`).join("");

  const quick = [
    ["#modules", "bi-collection", "Modules"], ["#notes", "bi-journal-text", "Notes"],
    ["#experiments", "bi-clipboard-data", "Experiments"], ["#resources", "bi-link-45deg", "Resources"], ["#editor", "bi-code-slash", "Code Editor"]
  ].map(([href, icon, text]) => `<a class="btn btn-outline-primary" href="${href}"><i class="bi ${icon}"></i> ${text}</a>`).join("");

  const date = (iso) => iso ? new Date(iso).toLocaleDateString() : "";
  const panel = (title, items, emptyIcon, emptyMsg) => `<div class="col-lg-4"><div class="card-ds"><h5>${title}</h5>
    ${items.length ? `<ul class="recent-list">${items.join("")}</ul>` : UI.empty(emptyIcon, emptyMsg)}</div></div>`;

  const notes = d.recentNotes.map(n => `<li><a href="#notes" class="text-decoration-none">${UI.escape(n.title)}</a>
    <small class="text-muted">${date(n.createdAt)}</small></li>`);
  const exps = d.recentExperiments.map(e => `<li><a href="#experiments" class="text-decoration-none">${e.experimentNumber ? "Exp " + UI.escape(e.experimentNumber) + ": " : ""}${UI.escape(e.title)}</a></li>`);
  const pdfs = d.recentPdfs.map(p => `<li><button class="btn btn-link p-0 text-start" data-open="${UI.escape(p.fileId)}" data-title="${UI.escape(p.title)}">
    <i class="bi bi-file-earmark-pdf text-danger"></i> ${UI.escape(p.title)}</button> <small class="text-muted">${UI.escape(p.moduleName)}</small></li>`);

  el.innerHTML = `
    <div class="welcome-banner mb-4">
      <h3>Welcome to the Data Science Digital Library</h3>
      <p>Your notes, lab experiments, tools and study material in one place.</p>
    </div>
    <div class="row g-3 mb-4">${stats}</div>
    <div class="card-ds mb-4"><h5>Quick access</h5><div class="d-flex flex-wrap gap-2">${quick}</div></div>
    <div class="row g-3">
      ${panel("Recently added notes", notes, "bi-journal", "No notes yet.")}
      ${panel("Recently added experiments", exps, "bi-clipboard-data", "No experiments yet.")}
      ${panel("Recently uploaded PDFs", pdfs, "bi-file-earmark-pdf", "No PDFs uploaded yet.")}
    </div>`;
  el.addEventListener("click", (e) => {
    const b = e.target.closest("[data-open]");
    if (b) PdfViewer.open(b.dataset.open, b.dataset.title);
  });
};
