/* Code Editor: Monaco editor + saved code (MongoDB).
   Running code:
   - JavaScript and Python run in a Web Worker INSIDE YOUR BROWSER (Python via Pyodide). Nothing runs on the server.
   - Java, C, C++, R and SQL cannot run yet: they need a secure server-side runner that is not configured.
   No output is ever faked: what you see is what the program really printed. */
(function () {
  const MONACO_BASE = "https://cdn.jsdelivr.net/npm/monaco-editor@0.52.2/min";
  const RUN_LIMIT_MS = 10000;
  const MAX_OUTPUT = 200000;

  const LANGS = {
    python: { label: "Python", monaco: "python", runs: "browser" },
    javascript: { label: "JavaScript", monaco: "javascript", runs: "browser" },
    java: { label: "Java", monaco: "java", runs: null },
    c: { label: "C", monaco: "c", runs: null },
    cpp: { label: "C++", monaco: "cpp", runs: null },
    r: { label: "R", monaco: "r", runs: null },
    sql: { label: "SQL", monaco: "sql", runs: null }
  };
  const STARTERS = {
    python: 'name = input("Your name: ")\nprint("Hello,", name)\n',
    javascript: 'const name = readLine();\nconsole.log("Hello, " + name);\n',
    java: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}\n',
    c: '#include <stdio.h>\n\nint main(void) {\n    printf("Hello, World!\\n");\n    return 0;\n}\n',
    cpp: '#include <iostream>\n\nint main() {\n    std::cout << "Hello, World!" << std::endl;\n    return 0;\n}\n',
    r: 'cat("Hello, World!\\n")\n',
    sql: "SELECT 'Hello, World!' AS greeting;\n"
  };

  /* ---------- Web Worker sources (written as real functions, then turned into blobs) ---------- */
  function jsWorkerMain() {
    self.onmessage = (e) => {
      const lines = String(e.data.stdin || "").split(/\r?\n/);
      let li = 0, size = 0;
      const send = (stream, text) => { size += text.length; if (size <= 200000) postMessage({ type: "out", stream, text }); };
      const fmt = (args) => args.map(x => {
        if (typeof x === "string") return x;
        try { return typeof x === "object" ? JSON.stringify(x) : String(x); } catch (_) { return String(x); }
      }).join(" ");
      console.log = console.info = console.debug = (...a) => send("stdout", fmt(a) + "\n");
      console.warn = console.error = (...a) => send("stderr", fmt(a) + "\n");
      // The code is yours and runs only in this tab, but we still block network access from inside the worker.
      for (const k of ["fetch", "XMLHttpRequest", "WebSocket", "EventSource", "importScripts"]) {
        try { Object.defineProperty(self, k, { value: undefined, configurable: false }); } catch (_) { /* already locked */ }
      }
      self.readLine = () => (li < lines.length ? lines[li++] : null);
      self.addEventListener("unhandledrejection", (ev) => postMessage({ type: "error", text: String(ev.reason && ev.reason.stack || ev.reason) }));
      try {
        (0, eval)(e.data.code);
      } catch (err) {
        postMessage({ type: "error", text: String(err && err.stack || err) });
      }
      setTimeout(() => postMessage({ type: "done" }), 200);   // short grace period for timers started by the script
    };
  }

  function pyWorkerMain() {
    let py = null, lines = [], li = 0;
    const send = (stream, text) => postMessage({ type: "out", stream, text });
    self.onmessage = async (e) => {
      if (e.data.type !== "run") return;
      lines = String(e.data.stdin || "").split(/\r?\n/);
      li = 0;
      try {
        await py.runPythonAsync(e.data.code);
        postMessage({ type: "done" });
      } catch (err) {
        postMessage({ type: "error", text: String(err && err.message || err) });
      }
    };
    (async () => {
      importScripts("https://cdn.jsdelivr.net/pyodide/v0.26.4/full/pyodide.js");
      py = await loadPyodide();
      py.setStdout({ batched: (s) => send("stdout", s + "\n") });
      py.setStderr({ batched: (s) => send("stderr", s + "\n") });
      py.setStdin({ stdin: () => (li < lines.length ? lines[li++] : undefined) });
      // Block network access for user code now that the runtime is loaded.
      for (const k of ["fetch", "XMLHttpRequest", "WebSocket", "EventSource", "importScripts"]) {
        try { Object.defineProperty(self, k, { value: undefined, configurable: false }); } catch (_) { /* already locked */ }
      }
      postMessage({ type: "ready" });
    })().catch((err) => postMessage({ type: "fatal", text: String(err && err.message || err) }));
  }

  const workerUrl = (fn) => URL.createObjectURL(new Blob(["(" + fn.toString() + ")()"], { type: "text/javascript" }));

  /* ---------- Monaco loader ---------- */
  let monacoPromise;
  function loadMonaco() {
    if (!monacoPromise) {
      monacoPromise = new Promise((resolve, reject) => {
        const s = document.createElement("script");
        s.src = MONACO_BASE + "/vs/loader.js";
        s.onload = () => {
          window.MonacoEnvironment = {
            getWorkerUrl: () => URL.createObjectURL(new Blob(
              [`self.MonacoEnvironment={baseUrl:'${MONACO_BASE}/'};importScripts('${MONACO_BASE}/vs/base/worker/workerMain.js');`],
              { type: "text/javascript" }))
          };
          window.require.config({ paths: { vs: MONACO_BASE + "/vs" } });
          window.require(["vs/editor/editor.main"], () => resolve(window.monaco), reject);
        };
        s.onerror = () => { monacoPromise = null; reject(new Error("Could not load the code editor. Check your internet connection.")); };
        document.head.appendChild(s);
      });
    }
    return monacoPromise;
  }

  /* ---------- Runner (browser sandbox) ---------- */
  const Runner = {
    worker: null, kind: null, timer: null, pyReady: false,
    stop() {
      clearTimeout(this.timer);
      if (this.worker) { this.worker.terminate(); this.worker = null; }
      this.pyReady = false;
      this.kind = null;
    }
  };

  let state = null;   // the currently mounted editor page

  function teardown() {
    Runner.stop();
    if (state) {
      try { state.editor.dispose(); } catch (e) { /* already gone */ }
      state = null;
    }
  }
  window.addEventListener("hashchange", () => { if (location.hash !== "#editor") teardown(); });

  Views.editor = async function (el, isCurrent) {
    teardown();
    let langKey = "python", currentId = null;
    let serverReady = {}, serverMessage = "";
    // "browser" = runs in this tab; "server" = runs in the code-runner (Docker sandbox); null = cannot run yet.
    const runMode = (k) => LANGS[k].runs === "browser" ? "browser" : (serverReady[k] ? "server" : null);
    const drafts = {};   // unsaved text per language, so switching language does not lose your work

    el.innerHTML = UI.pageHead("Code Editor", "Write and run code. JavaScript and Python run in your browser.") + `
      <div class="card-ds">
        <div class="alert alert-info py-2 small" id="runNote"></div>
        <div class="d-flex flex-wrap gap-2 mb-3 align-items-center">
          <label class="visually-hidden" for="lang">Language</label>
          <select class="form-select w-auto" id="lang">${Object.entries(LANGS).map(([k, v]) => `<option value="${k}">${v.label}</option>`).join("")}</select>
          <button class="btn btn-success" id="runBtn"><i class="bi bi-play-fill"></i> Run</button>
          <button class="btn btn-outline-secondary" id="stopBtn" disabled><i class="bi bi-stop-fill"></i> Stop</button>
          <button class="btn btn-outline-primary" id="loadBtn"><i class="bi bi-folder2-open"></i> Load</button>
          <button class="btn btn-outline-danger" id="clearBtn"><i class="bi bi-eraser"></i> Clear</button>
          <span class="text-muted small ms-auto" id="current"></span>
        </div>
        <div id="monacoBox" class="monaco-box border rounded">${UI.loading()}</div>
        <div class="row g-3 mt-1">
          <div class="col-md-5"><label class="form-label" for="stdin">Input (one value per line)</label>
            <textarea class="form-control code-input" id="stdin" rows="6" spellcheck="false"></textarea></div>
          <div class="col-md-7"><label class="form-label" for="out">Output console</label>
            <pre class="output-block console-box" id="out" aria-live="polite"></pre>
            <div class="alert alert-danger mt-2 mb-0 d-none" id="errBox" role="alert"></div></div>
        </div>
      </div>`;

    const $ = (id) => el.querySelector("#" + id);
    const out = $("out"), errBox = $("errBox");
    let monaco;
    try {
      monaco = await loadMonaco();
    } catch (err) {
      if (isCurrent()) $("monacoBox").innerHTML = `<div class="alert alert-danger m-3">${UI.escape(err.message)}</div>`;
      return;
    }
    if (!isCurrent()) return;
    try {
      const r = await API.get("/api/execute/languages");
      serverMessage = r.message || "";
      (r.serverLanguages || []).forEach(l => { if (l.ready) serverReady[l.id] = true; });
    } catch (err) {
      serverMessage = "Could not check the code runner: " + err.message;
    }
    if (!isCurrent()) return;
    $("monacoBox").innerHTML = "";
    const editor = monaco.editor.create($("monacoBox"), {
      value: STARTERS[langKey], language: LANGS[langKey].monaco, automaticLayout: true, minimap: { enabled: false },
      fontSize: 14, scrollBeyondLastLine: false, tabSize: 4
    });
    state = { editor };

    const updateUi = () => {
      const l = LANGS[langKey];
      const mode = runMode(langKey);
      $("runBtn").disabled = !mode;
      $("runBtn").title = mode ? "Run" : "This language cannot run yet";
      if (mode === "browser") {
        $("runNote").innerHTML = `<i class="bi bi-shield-check"></i> ${l.label} runs <strong>inside your browser</strong> in an isolated worker (${RUN_LIMIT_MS / 1000}s limit, no network). Nothing is run on the server.`;
      } else if (mode === "server") {
        $("runNote").innerHTML = `<i class="bi bi-shield-lock"></i> ${l.label} runs on the server inside a locked-down Docker container (no network, memory and time limits).`;
      } else {
        $("runNote").innerHTML = `<i class="bi bi-info-circle"></i> ${l.label} can be written and saved here, but <strong>cannot be run yet</strong>. ${UI.escape(serverMessage)}`;
      }
      $("current").textContent = currentId ? "Editing a saved snippet" : "";
    };
    const showError = (msg) => { errBox.textContent = msg; errBox.classList.toggle("d-none", !msg); };
    const append = (stream, text) => {
      if (out.textContent.length > MAX_OUTPUT) return;
      const span = document.createElement("span");
      span.className = stream === "stderr" ? "text-warning" : "";
      span.textContent = text;
      out.appendChild(span);
      out.scrollTop = out.scrollHeight;
    };
    const setRunning = (running) => {
      $("runBtn").disabled = running || !runMode(langKey);
      $("stopBtn").disabled = !running;
    };

    function finish(note) {
      clearTimeout(Runner.timer);
      if (Runner.kind === "js") Runner.stop();   // JS workers are one-shot; the Python runtime stays loaded
      if (note) append("stderr", note);
      setRunning(false);
    }

    function handle(msg) {
      if (!isCurrent()) return;
      if (msg.type === "out") append(msg.stream, msg.text);
      else if (msg.type === "error") { showError(msg.text); finish(); }
      else if (msg.type === "done") finish();
    }

    function startTimer() {
      clearTimeout(Runner.timer);
      Runner.timer = setTimeout(() => {
        const wasPy = Runner.kind === "py";
        Runner.stop();
        showError(`Stopped: the program ran longer than ${RUN_LIMIT_MS / 1000} seconds.` + (wasPy ? " (The Python runtime will reload on the next run.)" : ""));
        setRunning(false);
      }, RUN_LIMIT_MS);
    }

    function run() {
      out.textContent = "";
      showError("");
      const code = editor.getValue(), stdin = $("stdin").value;
      if (runMode(langKey) === "server") { runOnServer(code, stdin); return; }
      if (langKey === "javascript") {
        Runner.stop();
        Runner.kind = "js";
        Runner.worker = new Worker(workerUrl(jsWorkerMain));
        Runner.worker.onmessage = (e) => handle(e.data);
        Runner.worker.onerror = (e) => { showError(e.message || "The program crashed."); finish(); };
        setRunning(true);
        startTimer();
        Runner.worker.postMessage({ code, stdin });
      } else if (langKey === "python") {
        setRunning(true);
        const go = () => {
          startTimer();
          Runner.worker.postMessage({ type: "run", code, stdin });
        };
        if (Runner.kind === "py" && Runner.pyReady) { go(); return; }
        Runner.stop();
        Runner.kind = "py";
        append("stderr", "Loading the Python runtime (first run only, about 10 MB)...\n");
        Runner.worker = new Worker(workerUrl(pyWorkerMain));
        Runner.worker.onmessage = (e) => {
          const m = e.data;
          if (m.type === "ready") { Runner.pyReady = true; out.textContent = ""; go(); }
          else if (m.type === "fatal") { showError("Could not start Python: " + m.text + " (this needs an internet connection)."); Runner.stop(); setRunning(false); }
          else handle(m);
        };
        Runner.worker.onerror = (e) => { showError(e.message || "The Python runtime crashed."); Runner.stop(); setRunning(false); };
      }
    }

    async function runOnServer(code, stdin) {
      out.textContent = "";
      showError("");
      setRunning(true);
      try {
        const r = await API.post("/api/execute", { language: langKey, code, stdin });
        if (!isCurrent()) return;
        if (r.stdout) append("stdout", r.stdout);
        if (r.stderr) append("stderr", r.stderr);
        if (r.timedOut) showError("Stopped: the program took longer than 20 seconds.");
        else if (r.exitCode !== 0) append("stderr", `\n[Finished with exit code ${r.exitCode}]\n`);
        if (r.truncated) append("stderr", "\n[Output was cut short at 100 KB]\n");
      } catch (err) {
        if (isCurrent()) showError(err.message);
      } finally {
        if (isCurrent()) setRunning(false);
      }
    }

    function setLanguage(key, text) {
      drafts[langKey] = editor.getValue();
      langKey = key;
      monaco.editor.setModelLanguage(editor.getModel(), LANGS[key].monaco);
      editor.setValue(text != null ? text : (drafts[key] != null ? drafts[key] : STARTERS[key]));
      updateUi();
    }

    /* ----- saved code ----- */
    function saveDialog() {
      const code = editor.getValue();
      if (!code.trim()) { UI.toast("There is no code to save.", "warning"); return; }
      UI.formModal({
        title: currentId ? "Save changes" : "Save code",
        body: `<div class="mb-3"><label class="form-label" for="sTitle">Title</label>
            <input class="form-control" id="sTitle" name="title" maxlength="120" required value="${UI.escape(state.title || "")}"></div>
          ${currentId ? `<div class="form-check"><input class="form-check-input" type="checkbox" id="sCopy" name="asCopy">
            <label class="form-check-label" for="sCopy">Save as a new copy instead of replacing</label></div>` : ""}`,
        onSubmit: async (f) => {
          const data = { title: f.title.value, language: langKey, code, stdin: $("stdin").value };
          const saved = currentId && !(f.asCopy && f.asCopy.checked)
            ? await API.put(`/api/code/${currentId}`, data) : await API.post("/api/code", data);
          currentId = saved.id;
          state.title = saved.title;
          updateUi();
          UI.toast("Code saved.");
        }
      });
    }

    async function loadDialog() {
      let list;
      try { list = await API.get("/api/code"); } catch (err) { UI.toast(err.message, "danger"); return; }
      const wrap = document.createElement("div");
      wrap.className = "modal fade";
      wrap.tabIndex = -1;
      const render = () => {
        wrap.querySelector(".modal-body").innerHTML = list.length
          ? `<div class="list-group">${list.map(s => `
              <div class="list-group-item d-flex justify-content-between align-items-center gap-2">
                <button class="btn btn-link text-start p-0 flex-grow-1" data-load="${s.id}">
                  <strong>${UI.escape(s.title)}</strong><br><small class="text-muted">${UI.escape((LANGS[s.language] || {}).label || s.language)} · ${new Date(s.updatedAt).toLocaleString()}</small></button>
              </div>`).join("")}</div>`
          : UI.empty("bi-folder2-open", "No saved code yet.");
      };
      wrap.innerHTML = `<div class="modal-dialog modal-dialog-scrollable"><div class="modal-content">
        <div class="modal-header"><h5 class="modal-title">Load saved code</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
        <div class="modal-body"></div></div></div>`;
      document.body.appendChild(wrap);
      render();
      const modal = new bootstrap.Modal(wrap);
      wrap.addEventListener("hidden.bs.modal", () => { modal.dispose(); wrap.remove(); });
      wrap.addEventListener("click", async (e) => {
        const b = e.target.closest("[data-load],[data-del]");
        if (!b) return;
        try {
          if (b.dataset.load) {
            const s = list.find(x => x.id === b.dataset.load);
            if (!LANGS[s.language]) throw new Error("This snippet uses an unknown language.");
            $("lang").value = s.language;
            setLanguage(s.language, s.code);
            $("stdin").value = s.stdin || "";
            currentId = s.id;
            state.title = s.title;
            updateUi();
            modal.hide();
          } else if (b.dataset.del) {
            if (!confirm("Delete this saved code?")) return;
            await API.del(`/api/code/${b.dataset.del}`);
            if (currentId === b.dataset.del) { currentId = null; state.title = ""; updateUi(); }
            list = list.filter(x => x.id !== b.dataset.del);
            render();
            UI.toast("Deleted.");
          }
        } catch (err) { UI.toast(err.message, "danger"); }
      });
      modal.show();
    }

    $("lang").addEventListener("change", (e) => { currentId = null; state.title = ""; setLanguage(e.target.value); });
    $("runBtn").addEventListener("click", run);
    $("stopBtn").addEventListener("click", () => { Runner.stop(); showError("Stopped by you."); setRunning(false); });
    $("loadBtn").addEventListener("click", loadDialog);
    $("clearBtn").addEventListener("click", () => {
      if (editor.getValue().trim() && !confirm("Clear the editor? Unsaved code will be lost.")) return;
      editor.setValue("");
      $("stdin").value = "";
      out.textContent = "";
      showError("");
      currentId = null;
      state.title = "";
      updateUi();
    });
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, () => { if (!$("runBtn").disabled) run(); });
    updateUi();
  };
})();
