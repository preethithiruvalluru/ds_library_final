/* Book-style PDF reader: PDF.js renders each page, StPageFlip gives the page-turn effect. */
const PdfViewer = {
  PDFJS_URL: "https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.min.js",
  WORKER_URL: "https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js",
  FLIP_URL: "https://cdn.jsdelivr.net/npm/page-flip@2.0.7/dist/js/page-flip.browser.js",
  MAX_PAGES: 300,
  _libs: null,
  _active: null,

  _script(src) {
    return new Promise((resolve, reject) => {
      const s = document.createElement("script");
      s.src = src;
      s.onload = resolve;
      s.onerror = () => reject(new Error("Could not load the PDF reader library. Check your internet connection."));
      document.head.appendChild(s);
    });
  },

  _loadLibs() {
    if (!this._libs) {
      this._libs = Promise.all([this._script(this.PDFJS_URL), this._script(this.FLIP_URL)]).then(() => {
        window.pdfjsLib.GlobalWorkerOptions.workerSrc = this.WORKER_URL;
      }).catch(e => { this._libs = null; throw e; });
    }
    return this._libs;
  },

  async open(fileId, title) {
    this.close();
    const root = document.createElement("div");
    root.className = "pdf-viewer";
    root.setAttribute("role", "dialog");
    root.setAttribute("aria-label", "PDF reader");
    root.innerHTML = `
      <div class="pdf-bar">
        <div class="pdf-title text-truncate">${UI.escape(title || "Document")}</div>
        <div class="pdf-controls">
          <button class="btn btn-sm btn-light" data-act="prev" aria-label="Previous page" title="Previous"><i class="bi bi-chevron-left"></i></button>
          <span class="pdf-page" aria-live="polite">–</span>
          <button class="btn btn-sm btn-light" data-act="next" aria-label="Next page" title="Next"><i class="bi bi-chevron-right"></i></button>
          <button class="btn btn-sm btn-light" data-act="zoomout" aria-label="Zoom out" title="Zoom out"><i class="bi bi-zoom-out"></i></button>
          <button class="btn btn-sm btn-light" data-act="zoomin" aria-label="Zoom in" title="Zoom in"><i class="bi bi-zoom-in"></i></button>
          <a class="btn btn-sm btn-light" href="/api/files/${encodeURIComponent(fileId)}?download=true" aria-label="Download" title="Download"><i class="bi bi-download"></i></a>
          <button class="btn btn-sm btn-light" data-act="full" aria-label="Full screen" title="Full screen"><i class="bi bi-fullscreen"></i></button>
          <button class="btn btn-sm btn-danger" data-act="close" aria-label="Close reader" title="Close"><i class="bi bi-x-lg"></i></button>
        </div>
      </div>
      <div class="pdf-stage">
        <div class="pdf-status"><div class="spinner-border text-light" role="status"></div><div class="pdf-status-text">Loading...</div></div>
        <div class="pdf-zoom"><div class="flipbook"></div></div>
      </div>`;
    document.body.appendChild(root);
    document.body.classList.add("pdf-open");

    const state = { root, zoom: 1, flip: null, closed: false, doc: null, keyHandler: null };
    this._active = state;
    const $ = (sel) => root.querySelector(sel);
    const statusEl = $(".pdf-status"), statusText = $(".pdf-status-text");
    const pageLabel = $(".pdf-page");
    const zoomEl = $(".pdf-zoom"), book = $(".flipbook");

    const applyZoom = () => { zoomEl.style.transform = `scale(${state.zoom})`; };
    root.addEventListener("click", (e) => {
      const btn = e.target.closest("[data-act]");
      if (!btn) return;
      const act = btn.dataset.act;
      if (act === "close") this.close();
      else if (act === "prev" && state.flip) state.flip.flipPrev();
      else if (act === "next" && state.flip) state.flip.flipNext();
      else if (act === "zoomin") { state.zoom = Math.min(2.5, state.zoom + 0.25); applyZoom(); }
      else if (act === "zoomout") { state.zoom = Math.max(0.5, state.zoom - 0.25); applyZoom(); }
      else if (act === "full") {
        if (document.fullscreenElement) document.exitFullscreen();
        else if (root.requestFullscreen) root.requestFullscreen().catch(() => UI.toast("Full screen is not available here.", "warning"));
      }
    });
    state.keyHandler = (e) => {
      if (e.key === "Escape" && !document.fullscreenElement) this.close();
      else if (e.key === "ArrowLeft" && state.flip) state.flip.flipPrev();
      else if (e.key === "ArrowRight" && state.flip) state.flip.flipNext();
    };
    document.addEventListener("keydown", state.keyHandler);

    try {
      await this._loadLibs();
      const doc = await window.pdfjsLib.getDocument({ url: `/api/files/${encodeURIComponent(fileId)}` }).promise;
      if (state.closed) { doc.destroy(); return; }
      state.doc = doc;
      const total = Math.min(doc.numPages, this.MAX_PAGES);
      const images = [];
      let ratio = 1.414;
      for (let n = 1; n <= total; n++) {
        if (state.closed) return;
        statusText.textContent = `Preparing page ${n} of ${total}...`;
        const page = await doc.getPage(n);
        const base = page.getViewport({ scale: 1 });
        if (n === 1) ratio = base.height / base.width;
        const viewport = page.getViewport({ scale: Math.min(2, 900 / base.width) });
        const canvas = document.createElement("canvas");
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        await page.render({ canvasContext: canvas.getContext("2d"), viewport }).promise;
        images.push(canvas.toDataURL("image/jpeg", 0.85));
        canvas.width = canvas.height = 0;
      }
      if (state.closed) return;
      const W = 450, H = Math.round(W * ratio);
      const flip = new St.PageFlip(book, {
        width: W, height: H, size: "stretch",
        minWidth: 220, maxWidth: 800, minHeight: 300, maxHeight: 1200,
        showCover: true, usePortrait: true, mobileScrollSupport: false, maxShadowOpacity: 0.4
      });
      flip.loadFromImages(images);
      state.flip = flip;
      const update = () => { pageLabel.textContent = `Page ${flip.getCurrentPageIndex() + 1} / ${flip.getPageCount()}`; };
      flip.on("flip", update);
      update();
      statusEl.remove();
      if (doc.numPages > total) UI.toast(`Showing the first ${total} of ${doc.numPages} pages.`, "info");
    } catch (e) {
      if (!state.closed) {
        statusEl.innerHTML = `<div class="text-center text-light"><i class="bi bi-exclamation-triangle fs-1"></i><p class="mt-2">Could not open this PDF.<br><small>${UI.escape(e.message)}</small></p></div>`;
      }
    }
  },

  close() {
    const s = this._active;
    if (!s) return;
    s.closed = true;
    if (document.fullscreenElement) document.exitFullscreen().catch(() => {});
    document.removeEventListener("keydown", s.keyHandler);
    try { if (s.flip) s.flip.destroy(); } catch (e) { /* already gone */ }
    if (s.doc) s.doc.destroy();
    s.root.remove();
    document.body.classList.remove("pdf-open");
    this._active = null;
  }
};
