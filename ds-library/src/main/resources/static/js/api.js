/* Thin wrapper around fetch() for the Spring Boot REST API. Errors become readable messages.
   After every successful change (POST, PUT, DELETE) a "data-changed" event is fired so the menu totals can refresh. */
const API = {
  async request(method, url, body) {
    const opts = { method, headers: {} };
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
    let data = null;
    if (res.status !== 204) {
      try { data = await res.json(); } catch (e) { /* no JSON body */ }
    }
    if (!res.ok) {
      let msg = (data && data.message) || `Request failed (${res.status})`;
      if (data && data.fieldErrors) msg += ": " + Object.values(data.fieldErrors).join("; ");
      throw new Error(msg);
    }
    if (method !== "GET") window.dispatchEvent(new Event("data-changed"));
    return data;
  },
  get: (url) => API.request("GET", url),
  post: (url, body) => API.request("POST", url, body),
  put: (url, body) => API.request("PUT", url, body),
  del: (url) => API.request("DELETE", url)
};
