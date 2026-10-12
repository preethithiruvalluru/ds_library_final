/* Lab Experiments: create, view, edit, delete, search/filter, highlighted code with a copy button. */
Views.experiments = async function (el, isCurrent) {
  const LANGS = { python: "Python", java: "Java", c: "C", cpp: "C++", javascript: "JavaScript", r: "R", sql: "SQL", plaintext: "Other / plain text" };
  const HLJS_JS = "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js";
  const HLJS_CSS = "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css";
  let modules = [], items = [];
  const filters = { q: "", moduleId: "", language: "" };
  let timer;

  const moduleName = (id) => { const m = modules.find(x => x.id === id); return m ? `Module ${m.moduleNumber}: ${m.name}` : ""; };
  const opts = (list, selected, allLabel) => `<option value="">${allLabel}</option>` +
    list.map(([v, t]) => `<option value="${UI.escape(v)}" ${v === selected ? "selected" : ""}>${UI.escape(t)}</option>`).join("");

  /* highlight.js is loaded once, only when a code block is first shown; without it the code is shown as plain text. */
  let hlPromise;
  function loadHighlighter() {
    if (!hlPromise) {
      hlPromise = new Promise((resolve) => {
        const css = document.createElement("link");
        css.rel = "stylesheet"; css.href = HLJS_CSS;
        document.head.appendChild(css);
        const s = document.createElement("script");
        s.src = HLJS_JS;
        s.onload = () => resolve(true);
        s.onerror = () => resolve(false);
        document.head.appendChild(s);
      });
    }
    return hlPromise;
  }

  function shell() {
    el.innerHTML = UI.pageHead("Lab Experiments", "Aim, code, output and result for every experiment.") + `
      <div class="row g-2 mb-3">
        <div class="col-lg-6"><input class="form-control" id="fq" type="search" placeholder="Search number, title, aim or code..." aria-label="Search experiments" value="${UI.escape(filters.q)}"></div>
        <div class="col-6 col-lg-3"><select class="form-select" id="fmod" aria-label="Filter by module">${opts(modules.map(m => [m.id, `Module ${m.moduleNumber}: ${m.name}`]), filters.moduleId, "All modules")}</select></div>
        <div class="col-6 col-lg-3"><select class="form-select" id="flang" aria-label="Filter by language">${opts(Object.entries(LANGS), filters.language, "All languages")}</select></div>
      </div>
      <div id="expList"></div>`;
  }

  function drawList() {
    if (!isCurrent()) return;
    const box = el.querySelector("#expList");
    if (!box) return;
    if (!items.length) {
      const filtered = Object.values(filters).some(Boolean);
      box.innerHTML = `<div class="card-ds">${UI.empty("bi-clipboard-data", filtered ? "No experiments match your search or filters." : "Experiments will appear here.", true)}</div>`;
      return;
    }
    box.innerHTML = `<div class="row g-3">${items.map(x => `
      <div class="col-md-6 col-xl-4"><div class="card-ds note-card">
        <div class="d-flex justify-content-between align-items-start gap-2">
          <div><span class="badge bg-primary mb-1">Exp ${UI.escape(x.experimentNumber)}</span>
            <span class="badge text-bg-light border mb-1">${UI.escape(LANGS[x.language] || x.language)}</span>
            <h5 class="mb-1 note-title" role="button" tabindex="0" data-view="${x.id}">${UI.escape(x.title)}</h5></div></div>
        <div class="small text-muted mb-2">${UI.escape(moduleName(x.moduleId)) || "No module"}</div>
        <p class="note-preview">${UI.escape(x.aim) || "<span class='text-muted'>No aim written.</span>"}</p>
        <div class="d-flex flex-wrap gap-2 mt-auto">
          <button class="btn btn-sm btn-primary" data-view="${x.id}"><i class="bi bi-eye"></i> View</button>
          ${x.githubRepoUrl ? `<a class="btn btn-sm btn-outline-dark" href="${UI.escape(x.githubRepoUrl)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-github"></i> GitHub</a>` : ""}
        </div>
      </div></div>`).join("")}</div>`;
  }

  async function load() {
    const p = new URLSearchParams();
    Object.entries(filters).forEach(([k, v]) => v && p.set(k, v));
    items = await API.get("/api/experiments?" + p);
    drawList();
  }

  async function refreshAll() {
    shell();
    await load();
  }

  const section = (title, text) => text ? `<h6 class="section-label">${title}</h6><div class="note-full mb-3">${UI.escape(text)}</div>` : "";

  async function viewExp(x) {
    const wrap = document.createElement("div");
    wrap.className = "modal fade";
    wrap.tabIndex = -1;
    wrap.innerHTML = `<div class="modal-dialog modal-xl modal-dialog-scrollable"><div class="modal-content">
      <div class="modal-header"><h5 class="modal-title">Experiment ${UI.escape(x.experimentNumber)}: ${UI.escape(x.title)}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">
        <div class="small text-muted mb-3">${[LANGS[x.language], moduleName(x.moduleId)].filter(Boolean).map(UI.escape).join(" · ")}
          <br>Created ${new Date(x.createdAt).toLocaleString()} · Updated ${new Date(x.updatedAt).toLocaleString()}</div>
        ${section("Aim", x.aim)}${section("Requirements", x.requirements)}${section("Procedure / Algorithm", x.procedure)}
        ${x.sourceCode ? `<div class="d-flex justify-content-between align-items-center"><h6 class="section-label mb-1">Source code</h6>
            <button class="btn btn-sm btn-outline-secondary" data-copy><i class="bi bi-clipboard"></i> Copy code</button></div>
          <pre class="code-block"><code class="hljs"></code></pre>` : ""}
        ${x.outputText ? `<h6 class="section-label">Output</h6><pre class="output-block"></pre>` : ""}
        ${x.outputImage ? `<h6 class="section-label">Output image</h6>
          <img class="img-fluid border rounded mb-3" src="/api/files/${encodeURIComponent(x.outputImage.fileId)}" alt="Output of experiment ${UI.escape(x.experimentNumber)}">` : ""}
        ${section("Result", x.result)}
        ${x.githubRepoUrl ? `<a class="btn btn-outline-dark" href="${UI.escape(x.githubRepoUrl)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-github"></i> Open GitHub repository</a>
          <div class="mt-3" data-repo-info><span class="text-muted small">Loading repository details from GitHub...</span></div>` : ""}
      </div></div></div>`;
    document.body.appendChild(wrap);
    // Live repository details from the GitHub API (through our server). A failure never blocks the experiment.
    const repoBox = wrap.querySelector("[data-repo-info]");
    if (repoBox) {
      API.get("/api/github/repo?url=" + encodeURIComponent(x.githubRepoUrl))
        .then(r => { if (wrap.isConnected) repoBox.innerHTML = UI.repoCard(r); })
        .catch(err => { if (wrap.isConnected) repoBox.innerHTML = `<span class="text-muted small"><i class="bi bi-info-circle"></i> Repository details are not available: ${UI.escape(err.message)}</span>`; });
    }
    // Code and output are inserted as text so nothing typed in them can run as HTML.
    const codeEl = wrap.querySelector("code");
    if (codeEl) codeEl.textContent = x.sourceCode;
    const outEl = wrap.querySelector(".output-block");
    if (outEl) outEl.textContent = x.outputText;
    const modal = new bootstrap.Modal(wrap);
    wrap.addEventListener("hidden.bs.modal", () => { modal.dispose(); wrap.remove(); });
    wrap.addEventListener("click", async (e) => {
      const b = e.target.closest("[data-copy]");
      if (!b) return;
      try {
        await navigator.clipboard.writeText(x.sourceCode);
      } catch (err) {
        const ta = document.createElement("textarea");
        ta.value = x.sourceCode;
        document.body.appendChild(ta);
        ta.select();
        const ok = document.execCommand("copy");
        ta.remove();
        if (!ok) { UI.toast("Could not copy. Select the code and press Ctrl+C.", "danger"); return; }
      }
      b.innerHTML = `<i class="bi bi-check2"></i> Copied`;
      setTimeout(() => { b.innerHTML = `<i class="bi bi-clipboard"></i> Copy code`; }, 1800);
    });
    modal.show();
    if (codeEl && await loadHighlighter() && window.hljs && wrap.isConnected) {
      const lang = window.hljs.getLanguage(x.language) ? x.language : null;
      const r = lang ? window.hljs.highlight(x.sourceCode, { language: lang, ignoreIllegals: true }) : window.hljs.highlightAuto(x.sourceCode);
      codeEl.innerHTML = r.value;   // hljs escapes the code itself
    }
  }

  function expForm(x) {
    x = x || { experimentNumber: String(items.reduce((a, b) => Math.max(a, parseInt(b.experimentNumber) || 0), 0) + 1), title: "", aim: "", requirements: "", procedure: "",
      sourceCode: "", language: "python", outputText: "", result: "", moduleId: "", githubRepoUrl: "" };
    const area = (name, label, rows, val, extra = "") => `<div class="mb-3"><label class="form-label" for="x_${name}">${label}</label>
      <textarea class="form-control ${extra}" id="x_${name}" name="${name}" rows="${rows}">${UI.escape(val)}</textarea></div>`;
    UI.formModal({
      title: x.id ? "Edit experiment" : "Add experiment",
      size: "modal-xl",
      body: `
        <div class="row g-2 mb-3">
          <div class="col-md-3"><label class="form-label" for="x_num">Experiment no.</label>
            <input class="form-control" id="x_num" name="experimentNumber" maxlength="20" required value="${UI.escape(x.experimentNumber)}"></div>
          <div class="col-md-9"><label class="form-label" for="x_title">Title</label>
            <input class="form-control" id="x_title" name="title" maxlength="200" required value="${UI.escape(x.title)}"></div>
        </div>
        <div class="row g-2 mb-3">
          <div class="col-md-6"><label class="form-label" for="x_mod">Related module</label>
            <select class="form-select" id="x_mod" name="moduleId">${opts(modules.map(m => [m.id, `Module ${m.moduleNumber}: ${m.name}`]), x.moduleId || "", "No module")}</select></div>
          <div class="col-md-6"><label class="form-label" for="x_lang">Programming language</label>
            <select class="form-select" id="x_lang" name="language">${Object.entries(LANGS).map(([v, t]) => `<option value="${v}" ${v === x.language ? "selected" : ""}>${t}</option>`).join("")}</select></div>
        </div>
        ${area("aim", "Aim", 2, x.aim)}${area("requirements", "Requirements", 2, x.requirements)}
        ${area("procedure", "Procedure / Algorithm", 4, x.procedure)}
        ${area("sourceCode", "Source code", 10, x.sourceCode, "code-input")}
        ${area("outputText", "Output (text)", 4, x.outputText, "code-input")}
        <div class="mb-3"><label class="form-label" for="x_img">Output image (PNG, JPEG, GIF or WebP, max 5 MB)</label>
          <input class="form-control" id="x_img" name="image" type="file" accept="image/png,image/jpeg,image/gif,image/webp">
          ${x.outputImage ? `<div class="form-check mt-2"><input class="form-check-input" type="checkbox" id="x_rmimg" name="removeImage">
            <label class="form-check-label" for="x_rmimg">Remove current image (${UI.escape(x.outputImage.filename)})</label></div>` : ""}</div>
        ${area("result", "Result", 2, x.result)}
        <div class="mb-1"><label class="form-label" for="x_gh">GitHub repository URL (optional)</label>
          <input class="form-control" id="x_gh" name="githubRepoUrl" type="url" maxlength="300" placeholder="https://github.com/user/repo" value="${UI.escape(x.githubRepoUrl)}"></div>`,
      onSubmit: async (f) => {
        const img = f.image.files[0];
        if (img && img.size > 5 * 1048576) throw new Error("Image is larger than 5 MB.");
        if (img && !/^image\/(png|jpeg|gif|webp)$/.test(img.type)) throw new Error("Only PNG, JPEG, GIF or WebP images are allowed.");
        const data = {};
        ["experimentNumber", "title", "moduleId", "language", "aim", "requirements", "procedure", "sourceCode", "outputText", "result", "githubRepoUrl"]
          .forEach(k => { data[k] = f[k].value; });
        const saved = x.id ? await API.put(`/api/experiments/${x.id}`, data) : await API.post("/api/experiments", data);
        let imageProblem = null;
        try {
          if (img) {
            const fd = new FormData();
            fd.append("file", img);
            await API.post(`/api/experiments/${saved.id}/output-image`, fd);
          } else if (f.removeImage && f.removeImage.checked) {
            await API.del(`/api/experiments/${saved.id}/output-image`);
          }
        } catch (err) {
          imageProblem = err.message;
        }
        UI.toast(x.id ? "Experiment updated." : "Experiment saved.");
        if (imageProblem) UI.toast("The experiment was saved, but the image was not: " + imageProblem, "warning");
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
    const map = { fmod: "moduleId", flang: "language" };
    if (!map[e.target.id]) return;
    filters[map[e.target.id]] = e.target.value;
    load().catch(err => UI.toast(err.message, "danger"));
  });
  el.addEventListener("keydown", (e) => { if (e.key === "Enter" && e.target.dataset.view) e.target.click(); });
  el.addEventListener("click", async (e) => {
    const t = e.target.closest("[data-view],[data-edit],[data-del],#addExp");
    if (!t) return;
    try {
      if (t.id === "addExp") expForm();
      else if (t.dataset.view) viewExp(items.find(x => x.id === t.dataset.view));
      else if (t.dataset.edit) expForm(items.find(x => x.id === t.dataset.edit));
      else if (t.dataset.del) {
        if (!confirm("Delete this experiment? This cannot be undone.")) return;
        await API.del(`/api/experiments/${t.dataset.del}`);
        UI.toast("Experiment deleted.");
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
