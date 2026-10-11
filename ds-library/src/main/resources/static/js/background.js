/* Animated page background: a glowing network of connected nodes (a "data graph") with faint data symbols floating up in the MBU colours.
   - Sits behind everything (a fixed canvas, never blocks clicks).
   - Nodes near the mouse pointer link to it with gold lines.
   - Pauses while the tab is hidden, uses fewer nodes on small screens, and shows one still picture
     instead of moving when the visitor has asked their device for reduced motion. */
(function () {
  const reduceMotion = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const canvas = document.createElement("canvas");
  canvas.id = "bgCanvas";
  canvas.setAttribute("aria-hidden", "true");
  document.body.prepend(canvas);
  const ctx = canvas.getContext("2d");
  if (!ctx) return;

  const CRIMSON = "179, 18, 31";
  const GOLD = "240, 160, 32";
  let W = 0, H = 0, nodes = [], glyphs = [], linkDist = 150, raf = 0, last = 0;
  const GLYPHS = ["0", "1", "01", "10", "Σ", "μ", "σ", "β", "π", "∞", "</>", "{ }", "x²", "%", "≈", "∑"];
  const pointer = { x: -9999, y: -9999, active: false };

  function makeNode() {
    const speed = 8 + Math.random() * 16;                 // pixels per second: slow and calm
    const angle = Math.random() * Math.PI * 2;
    return {
      x: Math.random() * W, y: Math.random() * H,
      vx: Math.cos(angle) * speed, vy: Math.sin(angle) * speed,
      r: 1.6 + Math.random() * 2.4,
      gold: Math.random() < 0.18
    };
  }

  function makeGlyph(anywhere) {
    return {
      x: Math.random() * W, y: anywhere ? Math.random() * H : H + 30,
      vy: -(10 + Math.random() * 16), sway: Math.random() * Math.PI * 2, swaySpeed: 0.3 + Math.random() * 0.5,
      size: 14 + Math.random() * 18, text: GLYPHS[Math.floor(Math.random() * GLYPHS.length)],
      gold: Math.random() < 0.35, alpha: 0.10 + Math.random() * 0.14
    };
  }

  function resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    W = window.innerWidth;
    H = window.innerHeight;
    canvas.width = Math.round(W * dpr);
    canvas.height = Math.round(H * dpr);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    linkDist = W < 700 ? 115 : 165;
    const wanted = Math.round(Math.min(95, Math.max(26, (W * H) / 13000)));
    while (nodes.length < wanted) nodes.push(makeNode());
    if (nodes.length > wanted) nodes.length = wanted;
    const wantedGlyphs = W < 700 ? 8 : 16;
    while (glyphs.length < wantedGlyphs) glyphs.push(makeGlyph(true));
    if (glyphs.length > wantedGlyphs) glyphs.length = wantedGlyphs;
    nodes.forEach(n => { if (n.x > W) n.x = Math.random() * W; if (n.y > H) n.y = Math.random() * H; });
    if (reduceMotion) draw();
  }

  function step(dt) {
    for (const g of glyphs) {
      g.y += g.vy * dt;
      g.sway += g.swaySpeed * dt;
      g.x += Math.sin(g.sway) * 6 * dt;
      if (g.y < -40) Object.assign(g, makeGlyph(false));
    }
    for (const n of nodes) {
      n.x += n.vx * dt;
      n.y += n.vy * dt;
      if (n.x < -10) n.x = W + 10; else if (n.x > W + 10) n.x = -10;   // wrap around the edges
      if (n.y < -10) n.y = H + 10; else if (n.y > H + 10) n.y = -10;
    }
  }

  function draw() {
    ctx.clearRect(0, 0, W, H);
    ctx.textAlign = "center"; ctx.textBaseline = "middle";
    for (const g of glyphs) {
      ctx.font = `600 ${g.size.toFixed(0)}px Consolas, "Courier New", monospace`;
      ctx.fillStyle = `rgba(${g.gold ? GOLD : CRIMSON}, ${g.alpha.toFixed(3)})`;
      ctx.fillText(g.text, g.x, g.y);
    }
    ctx.lineWidth = 1;
    for (let i = 0; i < nodes.length; i++) {
      const a = nodes[i];
      for (let j = i + 1; j < nodes.length; j++) {
        const b = nodes[j];
        const dx = a.x - b.x, dy = a.y - b.y;
        const d2 = dx * dx + dy * dy;
        if (d2 < linkDist * linkDist) {
          const alpha = (1 - Math.sqrt(d2) / linkDist) * 0.36;
          ctx.strokeStyle = `rgba(${CRIMSON}, ${alpha.toFixed(3)})`;
          ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke();
        }
      }
      if (pointer.active) {
        const dx = a.x - pointer.x, dy = a.y - pointer.y;
        const d = Math.sqrt(dx * dx + dy * dy);
        if (d < linkDist * 1.4) {
          ctx.strokeStyle = `rgba(${GOLD}, ${((1 - d / (linkDist * 1.4)) * 0.55).toFixed(3)})`;
          ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(pointer.x, pointer.y); ctx.stroke();
        }
      }
    }
    for (const n of nodes) {
      const rgb = n.gold ? GOLD : CRIMSON;
      const halo = ctx.createRadialGradient(n.x, n.y, 0, n.x, n.y, n.r * 5);
      halo.addColorStop(0, `rgba(${rgb}, ${n.gold ? 0.32 : 0.22})`);
      halo.addColorStop(1, `rgba(${rgb}, 0)`);
      ctx.fillStyle = halo;
      ctx.beginPath(); ctx.arc(n.x, n.y, n.r * 5, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = `rgba(${rgb}, ${n.gold ? 0.85 : 0.6})`;
      ctx.beginPath(); ctx.arc(n.x, n.y, n.r, 0, Math.PI * 2); ctx.fill();
    }
  }

  function frame(t) {
    const dt = Math.min((t - last) / 1000, 0.05);        // never jump far after a pause
    last = t;
    step(dt);
    draw();
    raf = requestAnimationFrame(frame);
  }

  function start() {
    if (reduceMotion || raf) return;
    last = performance.now();
    raf = requestAnimationFrame(frame);
  }
  function stop() {
    if (raf) { cancelAnimationFrame(raf); raf = 0; }
  }

  window.addEventListener("resize", resize);
  document.addEventListener("visibilitychange", () => (document.hidden ? stop() : start()));
  window.addEventListener("pointermove", (e) => { pointer.x = e.clientX; pointer.y = e.clientY; pointer.active = true; }, { passive: true });
  document.addEventListener("pointerleave", () => { pointer.active = false; });

  resize();
  if (reduceMotion) draw(); else start();
})();
