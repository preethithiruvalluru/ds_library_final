/* Home dashboard: every number and list comes from the backend (/api/dashboard and /api/profile). */
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

  const n = (count, one, many) => `${count} ${count === 1 ? one : many}`;
  const tiles = [
    ["#modules", "bi-collection", "t-red", "Modules", n(d.counts.modules, "module", "modules"), "Syllabus and study PDFs"],
    ["#notes", "bi-journal-text", "t-purple", "Running Notes", n(d.counts.notes, "note", "notes"), "Write and search your notes"],
    ["#experiments", "bi-clipboard-data", "t-blue", "Lab Experiments", n(d.counts.experiments, "experiment", "experiments"), "Aim, code, output and result"],
    ["#resources", "bi-link-45deg", "t-green", "Resources", n(d.counts.resources, "resource", "resources"), "PDFs, links and videos"],
    ["#tools", "bi-tools", "t-amber", "Tools", n(d.counts.tools, "tool", "tools"), "Data Science tool directory"],
    ["#editor", "bi-code-slash", "t-teal", "Code Editor", "", "Practice coding"]
  ].map(([href, icon, color, title, count, sub]) => `
    <div class="col-6 col-md-4"><a class="tile" href="${href}">
      <i class="bi bi-arrow-right tile-arrow"></i>
      <div class="tile-icon ${color}"><i class="bi ${icon}"></i></div>
      <div class="tile-title">${title}</div>
      ${count ? `<div class="tile-count">${count}</div>` : ""}
      <div class="tile-sub">${sub}</div>
    </a></div>`).join("");

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
      <p>Everything for your DS lab and course is here: modules, notes, lab experiments, tools and resources.</p>
    </div>
    <div class="row g-3 mb-4">${tiles}</div>
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
