#!/usr/bin/env node
/* Copies the content of your LOCAL portal to an ONLINE copy of the portal (no Docker, no database tools needed).
 *
 *   node tools/copy-data.js https://your-app.onrender.com            copy everything
 *   node tools/copy-data.js https://your-app.onrender.com --preview  only show what would be copied
 *
 * Needs Node 18+ and your local portal running at http://localhost:8085 (change with the SOURCE environment variable).
 * It copies: profile (with photo and resume), modules with their PDFs, resources, lab experiments and notes.
 * (The tools list is created by the portal itself on a new database.)
 * To avoid duplicates it refuses to run when the online portal already has modules; add --force to run anyway.
 */
const SOURCE = (process.env.SOURCE || "http://localhost:8085").replace(/\/$/, "");
const args = process.argv.slice(2);
const TARGET = (args.find(a => /^https?:\/\//.test(a)) || "").replace(/\/$/, "");
const PREVIEW = args.includes("--preview");
const FORCE = args.includes("--force");

if (!TARGET) {
  console.log("Usage: node tools/copy-data.js https://your-online-portal [--preview] [--force]");
  process.exit(1);
}

async function json(base, path, options) {
  const res = await fetch(base + path, options);
  const text = await res.text();
  if (!res.ok) throw new Error(`${options && options.method || "GET"} ${base}${path} -> ${res.status} ${text.slice(0, 200)}`);
  return text ? JSON.parse(text) : null;
}
const send = (path, method, body) => json(TARGET, path, { method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });

async function download(fileId) {
  const res = await fetch(`${SOURCE}/api/files/${encodeURIComponent(fileId)}`);
  if (!res.ok) throw new Error(`could not read file ${fileId} from the local portal (${res.status})`);
  return { bytes: Buffer.from(await res.arrayBuffer()), type: res.headers.get("content-type") || "application/octet-stream" };
}
async function upload(path, file, filename, extra = {}) {
  const fd = new FormData();
  fd.append("file", new Blob([file.bytes], { type: file.type }), filename);
  for (const [k, v] of Object.entries(extra)) fd.append(k, v);
  return json(TARGET, path, { method: "POST", body: fd });
}

(async () => {
  console.log(`From ${SOURCE}\n  to ${TARGET}${PREVIEW ? "   (preview only, nothing is changed)" : ""}\n`);
  await json(SOURCE, "/api/health");
  await json(TARGET, "/api/health");

  const [profile, modules, resources, experiments, notes] = await Promise.all(
    ["/api/profile", "/api/modules", "/api/resources", "/api/experiments", "/api/notes"].map(p => json(SOURCE, p)));
  const pdfCount = modules.reduce((n, m) => n + (m.syllabus ? 1 : 0) + (m.materials || []).length, 0);
  console.log(`Local portal has: profile${profile.photo ? " + photo" : ""}${profile.resume ? " + resume" : ""}, ${modules.length} modules with ${pdfCount} PDFs, `
    + `${resources.length} resources, ${experiments.length} lab experiments, ${notes.length} notes.\n`);
  if (PREVIEW) { console.log("Preview finished. Run again without --preview to copy."); return; }

  const existing = await json(TARGET, "/api/modules");
  if (existing.length && !FORCE) {
    console.log(`The online portal already has ${existing.length} modules. Stopping so nothing is duplicated (use --force to copy anyway).`);
    process.exitCode = 2;
    return;
  }

  // profile
  await send("/api/profile", "PUT", {
    name: profile.name, rollNumber: profile.rollNumber, department: profile.department, section: profile.section, program: profile.program,
    academicYear: profile.academicYear, semester: profile.semester, admissionYear: profile.admissionYear, validTill: profile.validTill,
    college: profile.college, mentorName: profile.mentorName, email: profile.email, githubUrl: profile.githubUrl,
    linkedinUrl: profile.linkedinUrl, skills: profile.skills || [] });
  if (profile.photo) await upload("/api/profile/photo", await download(profile.photo.fileId), profile.photo.filename || "photo.png");
  if (profile.resume) await upload("/api/profile/resume", await download(profile.resume.fileId), profile.resume.filename || "resume.pdf");
  console.log("Profile copied.");

  // modules and their PDFs
  const moduleIds = {};
  for (const m of modules) {
    const created = await send("/api/modules", "POST", { moduleNumber: m.moduleNumber, name: m.name, subject: m.subject, description: m.description || "" });
    moduleIds[m.id] = created.id;
    if (m.syllabus) await upload(`/api/modules/${created.id}/syllabus`, await download(m.syllabus.fileId), m.syllabus.filename || "syllabus.pdf");
    for (const f of m.materials || []) {
      await upload(`/api/modules/${created.id}/materials`, await download(f.fileId), f.filename || "material.pdf", { title: f.title || "" });
    }
    console.log(`Module ${m.moduleNumber} copied (${(m.syllabus ? 1 : 0) + (m.materials || []).length} PDFs).`);
  }

  // resources, experiments, notes (items that carry an uploaded file are reported, not copied)
  let skipped = 0;
  for (const r of resources) {
    if (r.file) { skipped++; console.log(`  skipped resource with an uploaded file: ${r.title}`); continue; }
    await send("/api/resources", "POST", { title: r.title, description: r.description || "", type: r.type, subject: r.subject || "", moduleId: moduleIds[r.moduleId] || "", url: r.url || "", tags: r.tags || [] });
  }
  console.log(`${resources.length - skipped} resources copied.`);
  for (const e of experiments) {
    await send("/api/experiments", "POST", { experimentNumber: e.experimentNumber, title: e.title, aim: e.aim || "", requirements: e.requirements || "", procedure: e.procedure || "",
      sourceCode: e.sourceCode || "", language: e.language || "python", outputText: e.outputText || "", result: e.result || "", moduleId: moduleIds[e.moduleId] || "", githubRepoUrl: e.githubRepoUrl || "" });
    if (e.outputImage) console.log(`  note: the output image of experiment ${e.experimentNumber} was not copied`);
  }
  console.log(`${experiments.length} lab experiments copied.`);
  for (const n of notes) {
    await send("/api/notes", "POST", { title: n.title, subject: n.subject || "", moduleId: moduleIds[n.moduleId] || "", content: n.content || "", tags: n.tags || [] });
  }
  console.log(`${notes.length} notes copied.\n\nDone. Open ${TARGET} to check it.`);
})().catch(e => { console.error("\nStopped: " + e.message); process.exitCode = 1; });
