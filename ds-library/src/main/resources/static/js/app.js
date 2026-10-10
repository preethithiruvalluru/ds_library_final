/* Menu routing: shows the section that matches the URL hash (#home, #notes, ...).
   Sections with real data (Views.xxx) load from the API; the others still show a placeholder layout. */
(function () {
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
  window.addEventListener("DOMContentLoaded", render);
})();
