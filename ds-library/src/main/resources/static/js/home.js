/* Home: just the welcome banner (no personal name is shown here).
   Section links and the totals live in the menu (see app.js), not on this page. */
Views.home = async function (el, isCurrent) {
  el.innerHTML = `
    <section class="hero" aria-label="Welcome">
      <div>
        <h3>Turning Data into Intelligence</h3>
        <p>Discover meaningful patterns, generate powerful insights, and make data-driven decisions with confidence.</p>
        <p class="hero-flow">Explore Data <span aria-hidden="true">&rarr;</span> Discover Insights <span aria-hidden="true">&rarr;</span> Drive Innovation</p>
        <a class="btn" href="#modules">Browse Modules</a>
      </div>
      <svg class="hero-art" viewBox="0 0 320 220" role="img" aria-label="Data analytics illustration">
        <rect x="20" y="30" width="190" height="130" rx="10" fill="#fff" opacity=".95"/>
        <rect x="36" y="46" width="60" height="8" rx="4" fill="#cdb6fa"/>
        <rect x="40" y="120" width="22" height="28" rx="3" fill="#9455f4"/>
        <rect x="70" y="100" width="22" height="48" rx="3" fill="#7a3fe0"/>
        <rect x="100" y="84" width="22" height="64" rx="3" fill="#9455f4"/>
        <rect x="130" y="64" width="22" height="84" rx="3" fill="#5a22b5"/>
        <rect x="160" y="76" width="22" height="72" rx="3" fill="#9455f4"/>
        <path d="M44 112 L81 92 L111 78 L141 56 L171 68" fill="none" stroke="#2b1458" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
        <g fill="#2b1458"><circle cx="44" cy="112" r="4"/><circle cx="81" cy="92" r="4"/><circle cx="111" cy="78" r="4"/><circle cx="141" cy="56" r="4"/><circle cx="171" cy="68" r="4"/></g>
        <g stroke="#e9dcff" stroke-width="2" fill="none"><path d="M240 60 L276 96 L246 142 L286 166"/><path d="M276 96 L300 56"/></g>
        <g fill="#fff"><circle cx="240" cy="60" r="9"/><circle cx="276" cy="96" r="11"/><circle cx="246" cy="142" r="8"/><circle cx="286" cy="166" r="9"/><circle cx="300" cy="56" r="7"/></g>
        <rect x="60" y="172" width="130" height="10" rx="5" fill="#fff" opacity=".6"/>
      </svg>
    </section>`;
};
