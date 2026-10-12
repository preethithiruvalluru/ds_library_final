/* Study Bot: a round button in the bottom-right corner that opens a chat window.
   It works offline from a ready-made list of answers: the server compares the words of the question with the stored
   answers (/api/chat). No AI and no API key are involved.
   Answers are shown through a small, safe formatter: all HTML is escaped first, so nothing in a reply can run as code. */
(function () {
  const STORAGE_KEY = "dsl-chat-v2";
  const MAX_CHARS = 300;
  const GREETING = "Hi! I'm the Study Bot. Type a doubt about your subjects (Python, SQL, statistics, machine learning, lab work...) or about this portal and the college, and I'll look for the answer.";
  const FALLBACK_STARTERS = ["What is Python?", "What is overfitting?", "What are the types of SQL joins?", "What is Pandas?"];

  let history = [];       // [{role: "user"|"assistant", content, suggestions?}]
  let status = null;      // {enabled, answers, starters} once checked
  let busy = false;

  try {
    history = JSON.parse(sessionStorage.getItem(STORAGE_KEY) || "[]")
      .filter(m => m && (m.role === "user" || m.role === "assistant") && typeof m.content === "string")
      .map(m => ({ role: m.role, content: m.content, suggestions: Array.isArray(m.suggestions) ? m.suggestions.filter(s => typeof s === "string") : [] }));
  } catch (e) { history = []; }

  /* ---------- safe formatting ---------- */
  function render(src) {
    const blocks = [], inlines = [];
    let text = String(src).replace(/```[a-zA-Z0-9+#_-]*\n?([\s\S]*?)```/g, (_, code) => {
      blocks.push(`<pre class="chat-code"><code>${UI.escape(code.replace(/\n$/, ""))}</code></pre>`);
      return `\u0000${blocks.length - 1}\u0000`;
    });
    text = UI.escape(text).replace(/`([^`\n]+)`/g, (_, code) => {
      inlines.push(`<code>${code}</code>`);
      return `\u0001${inlines.length - 1}\u0001`;
    }).replace(/\*\*([^*\n]+)\*\*/g, "<strong>$1</strong>").replace(/(^|[\s(])\*([^*\n]+)\*(?=[\s).,!?]|$)/g, "$1<em>$2</em>");

    const out = [];
    let list = null, para = [];
    const closeList = () => { if (list) { out.push(`</${list}>`); list = null; } };
    const flush = () => { if (para.length) { out.push("<p>" + para.join("<br>") + "</p>"); para = []; } };
    for (const line of text.split("\n")) {
      let m;
      if ((m = line.match(/^\u0000(\d+)\u0000$/))) { flush(); closeList(); out.push(blocks[+m[1]]); continue; }
      if ((m = line.match(/^\s*[-*•]\s+(.*)$/))) { flush(); if (list !== "ul") { closeList(); out.push("<ul>"); list = "ul"; } out.push("<li>" + m[1] + "</li>"); continue; }
      if ((m = line.match(/^\s*\d+[.)]\s+(.*)$/))) { flush(); if (list !== "ol") { closeList(); out.push("<ol>"); list = "ol"; } out.push("<li>" + m[1] + "</li>"); continue; }
      if ((m = line.match(/^#{1,4}\s+(.*)$/))) { flush(); closeList(); out.push("<p><strong>" + m[1] + "</strong></p>"); continue; }
      if (!line.trim()) { flush(); closeList(); continue; }
      closeList();
      para.push(line);
    }
    flush(); closeList();
    return out.join("")
      .replace(/\u0000(\d+)\u0000/g, (_, n) => blocks[+n] || "")
      .replace(/\u0001(\d+)\u0001/g, (_, n) => inlines[+n] || "");
  }

  /* ---------- build the window ---------- */
  const fab = document.createElement("button");
  fab.type = "button";
  fab.className = "chat-fab";
  fab.id = "chatFab";
  fab.setAttribute("aria-label", "Open the Study Bot");
  fab.setAttribute("aria-expanded", "false");
  fab.setAttribute("aria-controls", "chatPanel");
  fab.innerHTML = `<i class="bi bi-chat-dots-fill" aria-hidden="true"></i><span class="chat-fab-label">Ask Bot</span>`;

  const panel = document.createElement("section");
  panel.className = "chat-panel";
  panel.id = "chatPanel";
  panel.setAttribute("role", "dialog");
  panel.setAttribute("aria-label", "Study Bot");
  panel.hidden = true;
  panel.innerHTML = `
    <header class="chat-head">
      <div class="chat-avatar" aria-hidden="true"><i class="bi bi-robot"></i></div>
      <div class="chat-title"><strong>Study Bot</strong><small>Subject doubts &amp; college help</small></div>
      <button type="button" class="chat-icon-btn" id="chatNew" aria-label="Start a new chat" title="New chat"><i class="bi bi-arrow-counterclockwise"></i></button>
      <button type="button" class="chat-icon-btn" id="chatClose" aria-label="Close the chat" title="Close"><i class="bi bi-x-lg"></i></button>
    </header>
    <div class="chat-body" id="chatBody" aria-live="polite"></div>
    <div class="chat-chips" id="chatChips"></div>
    <form class="chat-form" id="chatForm" autocomplete="off">
      <label class="visually-hidden" for="chatInput">Type your question</label>
      <textarea id="chatInput" rows="1" maxlength="${MAX_CHARS}" placeholder="Type your doubt here..."></textarea>
      <button type="submit" class="chat-send" id="chatSend" aria-label="Send"><i class="bi bi-send-fill"></i></button>
    </form>
    <div class="chat-foot">Works offline from a ready-made list of answers. If it can't find one, try different words.</div>`;
  document.body.append(fab, panel);

  const body = panel.querySelector("#chatBody");
  const chips = panel.querySelector("#chatChips");
  const form = panel.querySelector("#chatForm");
  const input = panel.querySelector("#chatInput");
  const sendBtn = panel.querySelector("#chatSend");

  function chipButton(label) {
    const b = document.createElement("button");
    b.type = "button";
    b.className = "chat-chip";
    b.textContent = label;
    b.addEventListener("click", () => send(label));
    return b;
  }

  function bubble(role, content, extraClass, suggestions) {
    const el = document.createElement("div");
    el.className = `chat-msg chat-${role}${extraClass ? " " + extraClass : ""}`;
    if (role === "assistant") el.innerHTML = render(content); else el.textContent = content;
    if (role === "assistant" && suggestions && suggestions.length) {
      const box = document.createElement("div");
      box.className = "chat-suggest";
      suggestions.forEach(s => box.appendChild(chipButton(s)));
      el.appendChild(box);
    }
    body.appendChild(el);
    body.scrollTop = body.scrollHeight;
    return el;
  }

  function save() {
    try { sessionStorage.setItem(STORAGE_KEY, JSON.stringify(history.slice(-20))); } catch (e) { /* storage may be blocked */ }
  }

  function drawAll() {
    body.innerHTML = "";
    bubble("assistant", GREETING);
    history.forEach((m, i) => bubble(m.role, m.content, "", i === history.length - 1 ? m.suggestions : null));
    if (status && !status.enabled) {
      bubble("assistant", "The study bot is not available right now. You can still use the modules, notes, tools and resources.", "chat-notice");
    }
    chips.innerHTML = "";
    if (!history.length && (!status || status.enabled)) {
      const starters = status && status.starters && status.starters.length ? status.starters : FALLBACK_STARTERS;
      starters.slice(0, 6).forEach(s => chips.appendChild(chipButton(s)));
    }
    const off = !!status && !status.enabled;
    input.disabled = off;
    sendBtn.disabled = off || busy;
    input.placeholder = off ? "The bot is not available yet" : "Type your doubt here...";
  }

  async function loadStatus() {
    try {
      const r = await fetch("/api/chat/status");
      status = r.ok ? await r.json() : { enabled: false };
    } catch (e) {
      status = { enabled: false };
    }
    drawAll();
  }

  function setBusy(on) {
    busy = on;
    sendBtn.disabled = on || (status && !status.enabled);
    input.disabled = on || (status && !status.enabled);
  }

  async function send(text) {
    text = (text || "").trim();
    if (!text || busy || (status && !status.enabled)) return;
    if (text.length > MAX_CHARS) { bubble("assistant", `That message is too long. Please keep it under ${MAX_CHARS} characters.`, "chat-error"); return; }

    chips.innerHTML = "";
    body.querySelectorAll(".chat-suggest").forEach(el => el.remove());   // old suggestions are replaced by the new reply's
    history.push({ role: "user", content: text, suggestions: [] });
    bubble("user", text);
    input.value = "";
    autoGrow();
    setBusy(true);

    try {
      const res = await fetch("/api/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text })
      });
      let data = null;
      try { data = await res.json(); } catch (e) { /* no JSON body */ }
      if (!res.ok) throw new Error((data && data.message) || `Something went wrong (${res.status}).`);
      const suggestions = Array.isArray(data.suggestions) ? data.suggestions : [];
      history.push({ role: "assistant", content: data.reply, suggestions });
      bubble("assistant", data.reply, data.matched ? "" : "chat-nomatch", suggestions);
    } catch (err) {
      history.pop();                          // the failed question is not kept, so asking again works
      const msg = err instanceof TypeError ? "I could not reach the server. Please check that the portal is running." : err.message;
      bubble("assistant", msg, "chat-error");
      input.value = text;                     // give the question back so it can be sent again
      autoGrow();
    } finally {
      save();
      setBusy(false);
      if (!input.disabled) input.focus();
    }
  }

  function autoGrow() {
    input.style.height = "auto";
    input.style.height = Math.min(input.scrollHeight, 120) + "px";
  }

  /* ---------- open / close ---------- */
  function open() {
    panel.hidden = false;
    fab.classList.add("is-open");
    fab.setAttribute("aria-expanded", "true");
    fab.setAttribute("aria-label", "Close the Study Bot");
    if (status === null) { drawAll(); loadStatus(); } else { drawAll(); }
    setTimeout(() => { if (!input.disabled) input.focus(); }, 50);
  }
  function close() {
    panel.hidden = true;
    fab.classList.remove("is-open");
    fab.setAttribute("aria-expanded", "false");
    fab.setAttribute("aria-label", "Open the Study Bot");
    fab.focus();
  }

  fab.addEventListener("click", () => (panel.hidden ? open() : close()));
  panel.querySelector("#chatClose").addEventListener("click", close);
  panel.querySelector("#chatNew").addEventListener("click", () => { history = []; save(); drawAll(); input.focus(); });
  form.addEventListener("submit", (e) => { e.preventDefault(); send(input.value); });
  input.addEventListener("input", autoGrow);
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey && !e.isComposing) { e.preventDefault(); send(input.value); }
  });
  document.addEventListener("keydown", (e) => { if (e.key === "Escape" && !panel.hidden) close(); });
})();
