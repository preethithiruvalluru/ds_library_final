/* Section views. In later phases each one will load its data from the Spring Boot API.
   For now they show the layout and honest empty states (no fake data). */
const Sections = {
  home() {
    const stats = [
      ["Modules", "bi-collection"], ["Running Notes", "bi-journal-text"],
      ["Lab Experiments", "bi-clipboard-data"], ["Study Resources", "bi-link-45deg"]
    ].map(([label, icon]) => `
      <div class="col-6 col-xl-3"><div class="card-ds stat-card">
        <div class="stat-icon"><i class="bi ${icon}"></i></div>
        <div><div class="stat-value" data-stat="${label}">–</div><div class="stat-label">${label}</div></div>
      </div></div>`).join("");

    const quick = [
      ["#modules", "bi-collection", "Modules"], ["#notes", "bi-journal-text", "Notes"],
      ["#experiments", "bi-clipboard-data", "Experiments"], ["#editor", "bi-code-slash", "Code Editor"]
    ].map(([href, icon, text]) => `<a class="btn btn-outline-primary" href="${href}"><i class="bi ${icon}"></i> ${text}</a>`).join("");

    const panel = (title, icon, msg) => `<div class="col-lg-4"><div class="card-ds"><h5>${title}</h5>${UI.empty(icon, msg)}</div></div>`;

    return `
      <div class="welcome-banner mb-4">
        <h3>Welcome to the Data Science Digital Library</h3>
        <p>Your notes, lab experiments, tools and study material in one place.</p>
      </div>
      <div class="row g-3 mb-4">${stats}</div>
      <div class="card-ds mb-4"><h5>Quick access</h5><div class="d-flex flex-wrap gap-2">${quick}</div></div>
      <div class="row g-3">
        ${panel("Recently added notes", "bi-journal", "No notes yet.")}
        ${panel("Recently added experiments", "bi-clipboard-data", "No experiments yet.")}
        ${panel("Recently uploaded PDFs", "bi-file-earmark-pdf", "No PDFs uploaded yet.")}
      </div>`;
  },

  modules() {
    return UI.pageHead("Modules", "Syllabus and study material, module by module.",
        `<button class="btn btn-primary" disabled><i class="bi bi-plus-lg"></i> Add Module</button>`)
      + `<div class="card-ds">${UI.empty("bi-collection", "No modules yet. Add your first module to get started.", true)}</div>`;
  },

  notes() {
    return UI.pageHead("Running Notes", "Write, search and organise your class notes.",
        `<button class="btn btn-primary" disabled><i class="bi bi-plus-lg"></i> New Note</button>`)
      + `<div class="row g-2 mb-3">
          <div class="col-md-6"><input class="form-control" placeholder="Search notes..." aria-label="Search notes" disabled></div>
          <div class="col-md-3"><select class="form-select" aria-label="Filter by module" disabled><option>All modules</option></select></div>
          <div class="col-md-3"><select class="form-select" aria-label="Filter by subject" disabled><option>All subjects</option></select></div>
        </div>`
      + `<div class="card-ds">${UI.empty("bi-journal-text", "No notes yet.", true)}</div>`;
  },

  experiments() {
    return UI.pageHead("Lab Experiments", "Aim, code, output and result for every experiment.",
        `<button class="btn btn-primary" disabled><i class="bi bi-plus-lg"></i> Add Experiment</button>`)
      + `<div class="row g-2 mb-3">
          <div class="col-md-8"><input class="form-control" placeholder="Search experiments..." aria-label="Search experiments" disabled></div>
          <div class="col-md-4"><select class="form-select" aria-label="Filter by module" disabled><option>All modules</option></select></div>
        </div>`
      + `<div class="card-ds">${UI.empty("bi-clipboard-data", "No experiments yet.", true)}</div>`;
  },

  tools() {
    return UI.pageHead("Tools", "A directory of useful Data Science tools.",
        `<button class="btn btn-primary" disabled><i class="bi bi-plus-lg"></i> Add Tool</button>`)
      + `<div class="card-ds">${UI.empty("bi-tools", "No tools yet. Categories will appear here once tools are added.", true)}</div>`;
  },

  resources() {
    return UI.pageHead("Resources", "PDFs, links, documentation, tutorials and videos.",
        `<button class="btn btn-primary" disabled><i class="bi bi-plus-lg"></i> Add Resource</button>`)
      + `<div class="card-ds">${UI.empty("bi-link-45deg", "No resources yet.", true)}</div>`;
  },

  editor() {
    const langs = ["Python", "Java", "C", "C++", "JavaScript", "R", "SQL"].map(l => `<option>${l}</option>`).join("");
    return UI.pageHead("Code Editor", "Write and save code. Running code needs the secure runner (set up in a later phase).")
      + `<div class="card-ds">
          <div class="d-flex flex-wrap gap-2 mb-3">
            <select class="form-select w-auto" aria-label="Language">${langs}</select>
            <button class="btn btn-success" disabled><i class="bi bi-play-fill"></i> Run</button>
            <button class="btn btn-outline-primary" disabled><i class="bi bi-save"></i> Save</button>
            <button class="btn btn-outline-primary" disabled><i class="bi bi-folder2-open"></i> Load</button>
            <button class="btn btn-outline-danger" disabled><i class="bi bi-eraser"></i> Clear</button>
          </div>
          <div class="border rounded p-5 text-center text-muted mb-3" style="min-height:240px">Monaco editor will load here (Phase 9)</div>
          <div class="row g-3">
            <div class="col-md-6"><label class="form-label">Input</label><textarea class="form-control" rows="4" disabled></textarea></div>
            <div class="col-md-6"><label class="form-label">Output</label><pre class="form-control bg-dark text-light mb-0" style="min-height:104px">Runner not configured yet.</pre></div>
          </div>
        </div>`;
  }
};
