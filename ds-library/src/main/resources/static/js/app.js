/* Menu routing: shows the section that matches the URL hash (#home, #notes, ...).
   Sections with real data (Views.xxx) load from the API; the others still show a placeholder layout.
   The menu starts CLOSED every time the site is opened; the button in the header opens and closes it. */
(function () {
  const toggle = document.getElementById("menuToggle");

  function setCollapsed(collapsed) {
    document.body.classList.toggle("sidebar-collapsed", collapsed);
    toggle.setAttribute("aria-expanded", String(!collapsed));
    const label = collapsed ? "Open menu" : "Close menu";
    toggle.setAttribute("aria-label", label);
    toggle.title = label;
  }

  // Start closed, without playing the slide animation on page load.
  document.body.classList.add("no-anim");
  setCollapsed(true);
  requestAnimationFrame(() => requestAnimationFrame(() => document.body.classList.remove("no-anim")));

  toggle.addEventListener("click", () => setCollapsed(!document.body.classList.contains("sidebar-collapsed")));

  /* Live totals shown as small badges in the menu (from /api/dashboard). A failure just leaves the badges as they were. */
  let countTimer;
  function refreshCounts() {
    clearTimeout(countTimer);
    countTimer = setTimeout(async () => {
      try {
        const d = await API.get("/api/dashboard");
        document.querySelectorAll("[data-count]").forEach(b => {
          const n = d.counts[b.dataset.count];
          b.textContent = typeof n === "number" ? String(n) : "";
        });
      } catch (e) { /* ignore: the menu still works without the numbers */ }
    }, 150);
  }

  function render() {
    PdfViewer.close();
    const route = (location.hash || "#home").slice(1);
    const name = Sections[route] ? route : "home";
    // Fresh container each time so old click handlers never pile up.
    const old = document.getElementById("content");
    const content = old.cloneNode(false);
    old.replaceWith(content);
    const isCurrent = () => document.getElementById("content") === content;
    if (Views[name]) {
      Views[name](content, isCurrent);
    } else {
      content.innerHTML = Sections[name]();
    }
    document.querySelectorAll(".nav-item-link").forEach(a => a.classList.toggle("active", a.dataset.route === name));
    // On phones the menu is a sideways-scrolling bar: keep the current item in view.
    const active = document.querySelector(".nav-item-link.active");
    if (active && active.scrollIntoView) active.scrollIntoView({ inline: "center", block: "nearest" });
    window.scrollTo(0, 0);
  }

  window.addEventListener("hashchange", render);
  window.addEventListener("data-changed", refreshCounts);
  window.addEventListener("DOMContentLoaded", () => { render(); refreshCounts(); });
})();
