/* Home: welcome banner and the "recently added" lists. Everything comes from the backend.
   Section links and the totals live in the menu (see app.js), not on this page. */
Views.home = async function (el, isCurrent) {
  el.innerHTML = UI.loading();
  let d, profile = null;
  try {
    [d, profile] = await Promise.all([API.get("/api/dashboard"), API.get("/api/profile").catch(() => null)]);
  } catch (err) {
    if (isCurrent()) el.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
    return;
  }
  if (!isCurrent()) return;

  const date = (iso) => iso ? new Date(iso).toLocaleDateString() : "";
  const panel = (title, items, emptyIcon, emptyMsg) => `<div class="col-lg-4"><div class="card-ds"><h5>${title}</h5>
    ${items.length ? `<ul class="recent-list">${items.join("")}</ul>` : UI.empty(emptyIcon, emptyMsg)}</div></div>`;

  const notes = d.recentNotes.map(x => `<li><a href="#notes" class="text-decoration-none">${UI.escape(x.title)}</a>
    <small class="text-muted">${date(x.createdAt)}</small></li>`);
  const exps = d.recentExperiments.map(e => `<li><a href="#experiments" class="text-decoration-none">${e.experimentNumber ? "Exp " + UI.escape(e.experimentNumber) + ": " : ""}${UI.escape(e.title)}</a></li>`);
  const pdfs = d.recentPdfs.map(p => `<li><button class="btn btn-link p-0 text-start" data-open="${UI.escape(p.fileId)}" data-title="${UI.escape(p.title)}">
    <i class="bi bi-file-earmark-pdf text-danger"></i> ${UI.escape(p.title)}</button> <small class="text-muted">${UI.escape(p.moduleName)}</small></li>`);

  const who = profile && profile.name ? `Welcome, ${UI.escape(profile.name)}` : "Welcome to the Data Science Digital Library";
  el.innerHTML = `
    <div class="welcome-banner mb-4">
      <h3>${who}</h3>
      <p>Everything for your DS lab and course is here. Pick a section from the menu to get started.</p>
    </div>
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
