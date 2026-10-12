/* Student profile page: shows the ID card from /api/profile and lets you edit it. */
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

    /* ---- left: resume ---- */
    const mb = p.resume && p.resume.size ? ` · ${(p.resume.size / 1048576).toFixed(1)} MB` : "";
    const resumeBlock = p.resume
      ? `<div class="resume-file">
           <div class="resume-icon" aria-hidden="true"><i class="bi bi-file-earmark-pdf-fill"></i></div>
           <div class="resume-info"><strong>${UI.escape(p.resume.filename || "Resume.pdf")}</strong><small>PDF${mb}</small></div>
         </div>
         <div class="resume-actions">
           <a class="btn btn-primary" href="/api/files/${encodeURIComponent(p.resume.fileId)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-eye"></i> View Resume</a>
           <a class="btn btn-outline-primary" href="/api/files/${encodeURIComponent(p.resume.fileId)}?download=true"><i class="bi bi-download"></i> Download Resume</a>
         </div>`
      : `<div class="resume-empty"><i class="bi bi-file-earmark-person"></i><p>No resume uploaded yet.</p><small>Use <strong>Edit</strong> to upload your resume (PDF).</small></div>`;
    const skills = (p.skills || []).map(s => `<span class="id-skill">${UI.escape(s)}</span>`).join("");
    const contacts = [
      p.email ? `<a class="id-social" href="mailto:${UI.escape(p.email)}"><i class="bi bi-envelope-fill"></i> ${UI.escape(p.email)}</a>` : "",
      p.githubUrl ? `<a class="id-social" href="${UI.escape(p.githubUrl)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-github"></i> GitHub</a>` : "",
      p.linkedinUrl ? `<a class="id-social" href="${UI.escape(p.linkedinUrl)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-linkedin"></i> LinkedIn</a>` : ""
    ].join("");
    const extra = (skills ? `<div class="id-skills"><small>Skills</small><div>${skills}</div></div>` : "")
      + (contacts ? `<div class="id-socials">${contacts}</div>` : "");

    /* ---- right: ID card (only fields that have a value) ---- */
    const field = (label, v) => v ? `<div class="idc-field"><small>${label}</small><span>${UI.escape(v)}</span></div>` : "";
    const fields = [
      field("Department", p.department), field("Program", p.program), field("Academic year", p.academicYear),
      field("Semester", p.semester), field("Section", p.section)
    ].join("");

    content.innerHTML = `
      <div class="page-head profile-wide">
        <div><h2>My Profile</h2><p>${p.name ? "Your resume and student ID card." : "Nothing added yet. Click Edit to fill it in."}</p></div>
        <button class="btn btn-primary" id="editProfile"><i class="bi bi-pencil"></i> Edit</button>
      </div>
      <div class="profile-cols">
        <section class="profile-col-resume" aria-labelledby="resumeHead">
          <div class="card-ds resume-card">
            <h4 id="resumeHead" class="resume-heading"><i class="bi bi-file-earmark-person-fill"></i> My Resume</h4>
            ${resumeBlock}
            ${extra}
          </div>
        </section>
        <section class="profile-col-id" aria-label="Student ID card">
          <div class="id-card">
            <div class="id-card-top">
              <img src="img/ds-logo.svg" alt="Data Science Digital Library logo" class="id-logo">
              <div class="id-college"><strong>${"Data Science Digital Library"}</strong><small>Student ID Card</small></div>
            </div>
            <div class="id-hero">
              <div class="id-photo-wrap">${photo}</div>
              <h3 class="id-name">${p.name ? UI.escape(p.name) : `<span class="text-muted">Student name</span>`}</h3>
              ${p.rollNumber ? `<span class="id-roll"><i class="bi bi-upc-scan"></i> ${UI.escape(p.rollNumber)}</span>` : ""}
            </div>
            <div class="id-body">
              ${fields ? `<div class="idc-grid">${fields}</div>` : `<p class="text-muted small text-center mb-0">No details added yet.</p>`}
            </div>
            <div class="id-foot"></div>
          </div>
        </section>
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
          ${text("program", "Course / Program", p.program, 'maxlength="100" placeholder="e.g. B.Tech"')}
          ${text("academicYear", "Academic year", p.academicYear, 'maxlength="30" placeholder="e.g. 2nd year"')}
          ${text("semester", "Current semester", p.semester, 'maxlength="30" placeholder="e.g. 4"')}
          <div class="col-12"><label class="form-label" for="p_college">College name</label>
            <input class="form-control" id="p_college" name="college" maxlength="150" value="${UI.escape(p.college)}"></div>
          ${text("mentorName", "Faculty / mentor name", p.mentorName, 'maxlength="100"')}
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
        ["name", "rollNumber", "department", "section", "program", "academicYear", "semester", "college", "mentorName", "email", "githubUrl", "linkedinUrl"]
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
  });

  async function init() {
    content.innerHTML = UI.loading();
    try {
      p = await API.get("/api/profile");
      draw();
    } catch (err) {
      content.innerHTML = `<div class="alert alert-danger">${UI.escape(err.message)}</div>`;
    }
  }
  init();
})();
