/* Student profile page: shows the ID card from /api/profile and lets you edit it. */
(function () {
  const content = document.getElementById("content");
  let p = null;

  const val = (s) => s ? UI.escape(s) : `<span class="text-muted">Not added</span>`;
  const row = (label, html) => `<div class="id-row"><span>${label}</span><span class="text-end">${html}</span></div>`;
  const link = (url, text) => url ? `<a href="${UI.escape(url)}" target="_blank" rel="noopener noreferrer">${UI.escape(text || url)}</a>` : val("");

  function draw() {
    const photo = p.photo
      ? `<img class="sid-img" src="/api/files/${encodeURIComponent(p.photo.fileId)}" alt="Profile photo of ${UI.escape(p.name || "student")}">`
      : `<div class="sid-img sid-img-empty" role="img" aria-label="No profile photo"><i class="bi bi-person-fill"></i></div>`;

    /* ---- left: resume ---- */
    const resumeBlock = p.resume
      ? `<div class="resume-pages" id="resumePages" aria-label="Resume"><div class="text-muted small p-3">Loading resume...</div></div>`
      : `<div class="resume-empty"><i class="bi bi-file-earmark-person"></i><p>No resume uploaded yet.</p><small>Use <strong>Edit</strong> to upload your resume (PDF).</small></div>`;
    const skills = (p.skills || []).map(s => `<span class="id-skill">${UI.escape(s)}</span>`).join("");
    const contacts = [
      p.email ? `<a class="id-social" href="mailto:${UI.escape(p.email)}"><i class="bi bi-envelope-fill"></i> ${UI.escape(p.email)}</a>` : "",
      p.githubUrl ? `<a class="id-social" href="${UI.escape(p.githubUrl)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-github"></i> GitHub</a>` : "",
      p.linkedinUrl ? `<a class="id-social" href="${UI.escape(p.linkedinUrl)}" target="_blank" rel="noopener noreferrer"><i class="bi bi-linkedin"></i> LinkedIn</a>` : ""
    ].join("");
    const extra = (skills ? `<div class="id-skills"><small>Skills</small><div>${skills}</div></div>` : "")
      + (contacts ? `<div class="id-socials">${contacts}</div>` : "");
    const aboutCard = extra ? `<div class="card-ds mt-3 profile-links">${extra}</div>` : "";

    /* ---- right: ID card (only fields that have a value) ---- */
    const idRow = (label, v) => v ? `<div><dt>${label}</dt><dd>${UI.escape(v)}</dd></div>` : "";
    const idRows = [
      idRow("Registration No.", p.rollNumber), idRow("Course", p.program), idRow("Year of Admission", p.admissionYear), idRow("Mentor", p.mentorName)
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
          </div>
        </section>
        <section class="profile-col-id" aria-label="Student ID card">
          <div class="sid" aria-label="Student ID card">
            <div class="sid-top">
              <img src="img/ds-logo.svg" alt="Data Science Digital Library logo" class="sid-logo">
              <div class="sid-brand"><strong>MBU</strong><span>MOHAN BABU UNIVERSITY</span></div>
            </div>
            <div class="sid-photo">${photo}</div>
            <div class="sid-name">${p.name ? UI.escape(p.name.toUpperCase()) : "STUDENT NAME"}</div>
            <dl class="sid-rows">${idRows}</dl>
            ${p.validTill ? `<div class="sid-valid" title="Valid till">Valid till ${UI.escape(p.validTill)}</div>` : ""}
            <div class="sid-foot">DREAM. BELIEVE. ACHIEVE.</div>
          </div>
          ${aboutCard}
        </section>
      </div>`;
    if (p.resume) renderResume(p.resume.fileId);
  }

  /* Draws every page of the resume PDF straight into the page (PDF.js), sized to the column. */
  async function renderResume(fileId) {
    const box = document.getElementById("resumePages");
    try {
      await PdfViewer._loadLibs();
      const doc = await window.pdfjsLib.getDocument({ url: `/api/files/${encodeURIComponent(fileId)}` }).promise;
      if (!document.getElementById("resumePages")) return;
      box.innerHTML = "";
      const width = box.clientWidth || 600;
      const dpr = Math.min(window.devicePixelRatio || 1, 2);
      for (let n = 1; n <= Math.min(doc.numPages, 10); n++) {
        const page = await doc.getPage(n);
        const base = page.getViewport({ scale: 1 });
        const viewport = page.getViewport({ scale: (width / base.width) * dpr });
        const canvas = document.createElement("canvas");
        canvas.className = "resume-page";
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        canvas.style.width = "100%";
        box.appendChild(canvas);
        await page.render({ canvasContext: canvas.getContext("2d"), viewport }).promise;
      }
    } catch (err) {
      if (box) box.innerHTML = `<div class="text-muted small p-3"><i class="bi bi-info-circle"></i> The resume could not be shown here. Please try again later. (${UI.escape(err.message)})</div>`;
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
          ${text("admissionYear", "Year of admission", p.admissionYear, 'maxlength="30" placeholder="e.g. 2024-2025"')}
          ${text("validTill", "ID valid till", p.validTill, 'maxlength="30" placeholder="e.g. 30-06-2028"')}
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
        ["name", "rollNumber", "department", "section", "program", "academicYear", "semester", "admissionYear", "validTill", "college", "mentorName", "email", "githubUrl", "linkedinUrl"]
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
