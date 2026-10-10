/* Sidebar routing: shows the section that matches the URL hash (#home, #notes, ...).
   Sections with real data (Views.xxx) load from the API; the others still show a placeholder layout. */
(function () {
  const sidebar = document.getElementById("sidebar");
  const backdrop = document.getElementById("backdrop");

  function setMenu(open) {
    sidebar.classList.toggle("open", open);
    backdrop.classList.toggle("show", open);
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
    setMenu(false);
    window.scrollTo(0, 0);
  }

  document.getElementById("menuToggle").addEventListener("click", () => setMenu(!sidebar.classList.contains("open")));
  backdrop.addEventListener("click", () => setMenu(false));
  window.addEventListener("hashchange", render);
  window.addEventListener("DOMContentLoaded", render);
})();
