/* Student profile page: shows the ID card from /api/profile and lets the admin edit it. */
(function () {
  const content = document.getElementById("content");
  let p = null;

  const val = (s) => s ? UI.escape(s) : `<span class="text-muted">Not added</span>`;
  const row = (label, html) => `<div class="id-row"><span>${label}</span><span class="text-end">${html}</span></div>`;
  const link = (url, text) => url ? `<a href="${UI.escape(url)}" target="_blank" rel="noopener noreferrer">${UI.escape(text || url)}</a>` : val("");

  function draw() {
    const photo = p.photo
      ? `<img class="id-photo" src="/api/files/${encodeURIComponent(p.photo.fileId)}" alt="Profile photo of ${UI.escape(p.name || "student")}">`
      : `<div class="id-photo id-photo-empty" role="img" aria-label="No profile photo"><i class="bi bi-person-fill"></i></div>`;
    const mentor = [p.mentorName, p.mentorDesignation].filter(Boolean).join(", ");
    const resume = p.resume
      ? `<button class="btn btn-sm btn-primary" data-resume><i class="bi bi-book"></i> View</button>
         <a class="btn btn-sm btn-outline-secondary" href="/api/files/${encodeURIComponent(p.resume.fileId)}?download=true"><i class="bi bi-download"></i></a>`
      : val("");
    const skills = (p.skills || []).length
      ? (p.skills || []).map(s => `<span class="badge text-bg-light border me-1 mb-1">${UI.escape(s)}</span>`).join("")
      : val("");

    content.innerHTML = `
      <div class="page-head" style="max-width:560px;margin-inline:auto">
        <div><h2>Student Profile</h2><p>${p.name ? "Your student ID card." : "Nothing added yet. Log in as admin and click Edit to fill it in."}</p></div>
        <button class="btn btn-primary admin-only" id="editProfile"><i class="bi bi-pencil"></i> Edit</button>
      </div>
      <div class="id-card">
        <div class="id-card-top">
          <img src="img/logo-placeholder.svg" alt="" width="40" height="40" class="rounded">
          <div><strong>${UI.escape(p.college || "College name not set")}</strong><br><small>Student ID Card</small></div>
        </div>
        <div class="d-flex align-items-start">
          ${photo}
          <div class="pt-3 pe-3"><h4 class="mb-0">${p.name ? UI.escape(p.name) : `<span class="text-muted">Student name</span>`}</h4>
            <span class="text-muted">Roll No: ${p.rollNumber ? UI.escape(p.rollNumber) : "—"}</span></div>
        </div>
        <div class="id-body mt-3">
          ${row("Branch / Department", val(p.department))}
          ${row("Section", val(p.section))}
          ${row("Mentor", mentor ? UI.escape(mentor) : val(""))}
          ${row("Mentor email", p.mentorEmail ? link("mailto:" + p.mentorEmail, p.mentorEmail) : val(""))}
          ${row("Email", p.email ? link("mailto:" + p.email, p.email) : val(""))}
          ${row("GitHub", link(p.githubUrl))}
          ${row("LinkedIn", link(p.linkedinUrl))}
          ${row("Resume", resume)}
          <div class="id-row d-block"><span class="d-block mb-1">Skills</span><div>${skills}</div></div>
        </div>
      </div>
      ${p.githubUrl ? `<div class="repo-section"><h5><i class="bi bi-github"></i> GitHub repositories</h5><div id="ghRepos"><span class="text-muted small">Loading from GitHub...</span></div></div>` : ""}`;
    if (p.githubUrl) loadRepos();
  }

  /* Public repositories of the GitHub account in the profile, fetched through our server. */
  async function loadRepos() {
    const box = document.getElementById("ghRepos");
    try {
      const r = await API.get("/api/github/profile-repos");
      if (!document.getElementById("ghRepos")) return;
      box.innerHTML = r.repositories.length
        ? r.repositories.map(UI.repoCard).join("")
        : `<span class="text-muted small">${UI.escape(r.username)} has no public repositories yet.</span>`;
    } catch (err) {
      if (box) box.innerHTML = `<span class="text-muted small"><i class="bi bi-info-circle"></i> Repositories are not available: ${UI.escape(err.message)}</span>`;
    }
  }

  function edit() {
    const text = (name, label, value, attrs = "") => `<div class="col-md-6"><label class="form-label" for="p_${name}">${label}</label>
      <input class="form-control" id="p_${name}" name="${name}" value="${UI.escape(value)}" ${attrs}></div>`;
    UI.formModal({
      title: "Edit profile",
      size: "modal-lg",
      body: `
        <div class="row g-3">
          ${text("name", "Student name", p.name, 'maxlength="100"')}
          ${text("rollNumber", "Student ID / Roll number", p.rollNumber, 'maxlength="40"')}
          ${text("department", "Branch / Department", p.department, 'maxlength="100"')}
          ${text("section", "Section", p.section, 'maxlength="20"')}
          <div class="col-12"><label class="form-label" for="p_college">College name</label>
            <input class="form-control" id="p_college" name="college" maxlength="150" value="${UI.escape(p.college)}"></div>
          ${text("mentorName", "Faculty / mentor name", p.mentorName, 'maxlength="100"')}
          ${text("mentorDesignation", "Mentor designation", p.mentorDesignation, 'maxlength="100"')}
          ${text("mentorEmail", "Mentor email", p.mentorEmail, 'type="email" maxlength="120"')}
          ${text("email", "Your email", p.email, 'type="email" maxlength="120"')}
          ${text("githubUrl", "GitHub profile URL", p.githubUrl, 'type="url" placeholder="https://github.com/username" pattern="^$|^https://github\\.com/[A-Za-z0-9_.\\-]+/?$"')}
          ${text("linkedinUrl", "LinkedIn profile URL", p.linkedinUrl, 'type="url" placeholder="https://www.linkedin.com/in/username" maxlength="200"')}
          <div class="col-12"><label class="form-label" for="p_skills">Skills (comma separated)</label>
            <input class="form-control" id="p_skills" name="skills" placeholder="Python, SQL, Pandas" value="${UI.escape((p.skills || []).join(", "))}"></div>
          <div class="col-md-6"><label class="form-label" for="p_photo">Profile photo (PNG, JPEG, GIF or WebP, max 5 MB)</label>
            <input class="form-control" id="p_photo" name="photo" type="file" accept="image/png,image/jpeg,image/gif,image/webp">
            ${p.photo ? `<div class="form-check mt-2"><input class="form-check-input" type="checkbox" id="p_rmphoto" name="removePhoto">
              <label class="form-check-label" for="p_rmphoto">Remove current photo</label></div>` : ""}</div>
          <div class="col-md-6"><label class="form-label" for="p_resume">Resume (PDF, max 25 MB)</label>
            <input class="form-control" id="p_resume" name="resume" type="file" accept="application/pdf,.pdf">
            ${p.resume ? `<div class="form-check mt-2"><input class="form-check-input" type="checkbox" id="p_rmresume" name="removeResume">
              <label class="form-check-label" for="p_rmresume">Remove current resume (${UI.escape(p.resume.filename)})</label></div>` : ""}</div>
        </div>`,
      onSubmit: async (f) => {
        const photo = f.photo.files[0], resume = f.resume.files[0];
        if (photo && photo.size > 5 * 1048576) throw new Error("Photo is larger than 5 MB.");
        if (resume && resume.size > 25 * 1048576) throw new Error("Resume is larger than 25 MB.");
        const data = {};
        ["name", "rollNumber", "department", "section", "college", "mentorName", "mentorDesignation", "mentorEmail", "email", "githubUrl", "linkedinUrl"]
          .forEach(k => { data[k] = f[k].value; });
        data.skills = f.skills.value.split(",").map(s => s.trim()).filter(Boolean);
        await API.put("/api/profile", data);

        const problems = [];
        const send = async (path, file, remove) => {
          try {
            if (file) { const fd = new FormData(); fd.append("file", file); await API.post(path, fd); }
            else if (remove) await API.del(path);
          } catch (err) { problems.push(err.message); }
        };
        await send("/api/profile/photo", photo, f.removePhoto && f.removePhoto.checked);
        await send("/api/profile/resume", resume, f.removeResume && f.removeResume.checked);
        UI.toast("Profile saved.");
        if (problems.length) UI.toast("Saved, but a file was not uploaded: " + problems.join(" "), "warning");
        p = await API.get("/api/profile");
        draw();
      }
    });
  }

  content.addEventListener("click", (e) => {
    if (e.target.closest("#editProfile")) edit();
    else if (e.target.closest("[data-resume]")) PdfViewer.open(p.resume.fileId, "Resume");
  });
  document.getElementById("authBtn").addEventListener("click", () => Auth.toggle());

  async function init() {
    Auth.apply();
    content.innerHTML = UI.loading();
    try {
      p = await API.get("/api/profile");
      draw();
    } catch (err) {
      content.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
    }
    Auth.refresh();
  }
  init();
})();
