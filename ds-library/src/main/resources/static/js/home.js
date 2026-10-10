/* Home: just the welcome banner (it greets you by name once the profile is filled in).
   Section links and the totals live in the menu (see app.js), not on this page. */
Views.home = async function (el, isCurrent) {
  el.innerHTML = UI.loading();
  const profile = await API.get("/api/profile").catch(() => null);
  if (!isCurrent()) return;

  const who = profile && profile.name ? `Welcome, ${UI.escape(profile.name)}` : "Welcome to the Data Science Digital Library";
  el.innerHTML = `
    <div class="welcome-banner">
      <h3>${who}</h3>
      <p>Everything for your DS lab and course is here. Open the menu (the button at the top left) to pick a section.</p>
    </div>`;
};
