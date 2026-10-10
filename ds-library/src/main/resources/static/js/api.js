/* Thin wrapper around fetch() for the Spring Boot REST API. Errors become readable messages. */
const API = {
  csrf() {
    const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
    return m ? decodeURIComponent(m[1]) : null;
  },
  async request(method, url, body) {
    const opts = { method, headers: {} };
    if (method !== "GET") {
      const token = API.csrf();
      if (token) opts.headers["X-XSRF-TOKEN"] = token;
    }
    if (body instanceof FormData) {
      opts.body = body;
    } else if (body !== undefined) {
      opts.headers["Content-Type"] = "application/json";
      opts.body = JSON.stringify(body);
    }
    let res;
    try {
      res = await fetch(url, opts);
    } catch (e) {
      throw new Error("Cannot reach the server. Is the application running?");
    }
    if (res.status === 204) return null;
    let data = null;
    try { data = await res.json(); } catch (e) { /* no JSON body */ }
    if (!res.ok) {
      if (res.status === 401 && !url.includes("/auth/login")) throw new Error("Please log in as admin to do this.");
      if (res.status === 403) throw new Error("Security check failed. Please refresh the page and try again.");
      let msg = (data && data.message) || `Request failed (${res.status})`;
      if (data && data.fieldErrors) msg += ": " + Object.values(data.fieldErrors).join("; ");
      throw new Error(msg);
    }
    return data;
  },
  get: (url) => API.request("GET", url),
  post: (url, body) => API.request("POST", url, body),
  put: (url, body) => API.request("PUT", url, body),
  del: (url) => API.request("DELETE", url)
};

/* Admin login state. Elements with class "admin-only" are visible only while logged in (the server enforces it too). */
const Auth = {
  admin: false,

  apply() {
    document.body.classList.toggle("is-admin", this.admin);
    const btn = document.getElementById("authBtn");
    if (btn) btn.innerHTML = this.admin
      ? `<i class="bi bi-box-arrow-right"></i> <span class="label">Logout</span>`
      : `<i class="bi bi-shield-lock"></i> <span class="label">Admin login</span>`;
  },

  async refresh() {
    try { this.admin = !!(await API.get("/api/auth/me")).authenticated; } catch (e) { this.admin = false; }
    this.apply();
  },

  changed() {
    this.apply();
    window.dispatchEvent(new Event("hashchange"));   // redraw the current section
  },

  login() {
    UI.formModal({
      title: "Admin login",
      submitText: "Log in",
      body: `<div class="mb-3"><label class="form-label" for="aUser">Username</label>
          <input class="form-control" id="aUser" name="username" autocomplete="username" required></div>
        <div class="mb-1"><label class="form-label" for="aPass">Password</label>
          <input class="form-control" id="aPass" name="password" type="password" autocomplete="current-password" required></div>`,
      onSubmit: async (f) => {
        await API.post("/api/auth/login", { username: f.username.value, password: f.password.value });
        this.admin = true;
        UI.toast("Logged in.");
        this.changed();
      }
    });
  },

  async logout() {
    try { await API.post("/api/auth/logout"); } catch (e) { /* session may already be gone */ }
    this.admin = false;
    UI.toast("Logged out.");
    this.changed();
  },

  toggle() { return this.admin ? this.logout() : this.login(); }
};
