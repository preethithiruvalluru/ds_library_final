/* Registry of data-driven section views (each file adds Views.name = function (el, isCurrent) {...}). */
const Views = {};

/* Small reusable UI helpers (toasts, empty states, loaders). */
const UI = {
  escape(s) {
    return String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  },

  empty(icon, message, big = false) {
    return `<div class="empty-state ${big ? "big" : ""}"><i class="bi ${icon}"></i>${message}</div>`;
  },

  loading() {
    return `<div class="text-center py-5"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>`;
  },

  toast(message, type = "success") {
    const el = document.createElement("div");
    el.className = `alert alert-${type} shadow-sm mb-0`;
    el.setAttribute("role", "alert");
    el.textContent = message;
    document.getElementById("toasts").appendChild(el);
    setTimeout(() => el.remove(), 3500);
  },

  /* Bootstrap modal with a form. onSubmit(formElement) may be async; throw an Error to show it in the modal. */
  formModal({ title, body, submitText = "Save", size = "", onSubmit, onOpen }) {
    const wrap = document.createElement("div");
    wrap.className = "modal fade";
    wrap.tabIndex = -1;
    wrap.innerHTML = `<div class="modal-dialog ${size} modal-dialog-scrollable"><form class="modal-content" novalidate>
      <div class="modal-header"><h5 class="modal-title">${UI.escape(title)}</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>
      <div class="modal-body">${body}<div class="alert alert-danger d-none mt-3 mb-0" role="alert"></div></div>
      <div class="modal-footer"><button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancel</button>
        <button type="submit" class="btn btn-primary">${UI.escape(submitText)}</button></div></form></div>`;
    document.body.appendChild(wrap);
    const modal = new bootstrap.Modal(wrap);
    const form = wrap.querySelector("form");
    const errBox = wrap.querySelector(".alert");
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      errBox.classList.add("d-none");
      form.classList.add("was-validated");
      if (!form.checkValidity()) return;
      const btn = form.querySelector("[type=submit]");
      btn.disabled = true;
      try {
        await onSubmit(form);
        modal.hide();
      } catch (err) {
        errBox.textContent = err.message;
        errBox.classList.remove("d-none");
      } finally {
        btn.disabled = false;
      }
    });
    wrap.addEventListener("hidden.bs.modal", () => { modal.dispose(); wrap.remove(); });
    if (onOpen) onOpen(form);
    modal.show();
    setTimeout(() => form.querySelector("input,textarea,select")?.focus(), 300);
  },

  /* One GitHub repository (data from /api/github/...). The link is only used if it really points to github.com. */
  repoCard(r) {
    const href = /^https:\/\/github\.com\//.test(r.htmlUrl || "") ? r.htmlUrl : "#";
    const when = r.pushedAt ? new Date(r.pushedAt).toLocaleDateString() : "";
    return `<div class="repo-card">
      <div class="d-flex flex-wrap justify-content-between gap-2">
        <a href="${UI.escape(href)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-github"></i> <strong>${UI.escape(r.fullName)}</strong></a>
        ${r.fork ? `<span class="badge text-bg-light border">fork</span>` : ""}
      </div>
      <p class="small mb-1">${UI.escape(r.description) || "<span class='text-muted'>No description.</span>"}</p>
      <div class="small text-muted d-flex flex-wrap gap-3">
        ${r.language ? `<span><i class="bi bi-circle-fill"></i> ${UI.escape(r.language)}</span>` : ""}
        <span title="Stars"><i class="bi bi-star"></i> ${Number(r.stars) || 0}</span>
        <span title="Forks"><i class="bi bi-diagram-2"></i> ${Number(r.forks) || 0}</span>
        <span title="Open issues"><i class="bi bi-exclamation-circle"></i> ${Number(r.openIssues) || 0}</span>
        ${when ? `<span>Last push ${UI.escape(when)}</span>` : ""}
      </div></div>`;
  },

  pageHead(title, subtitle, actionHtml = "") {
    return `<div class="page-head"><div><h2>${title}</h2><p>${subtitle}</p></div><div>${actionHtml}</div></div>`;
  }
};
