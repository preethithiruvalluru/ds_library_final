# Deployment guide

Three ways to run the portal, from simplest to "on the internet".

> **There is no login.** Anyone who can open the site can add, change and delete modules, notes, experiments, resources,
> the profile and uploaded files. That is fine on your own computer or a private network.
> **If you put it on the internet, strangers can edit or wipe your content.** Only do that if you are comfortable with it
> (for example a throw-away demo), keep regular backups, and never connect the code runner to a public site
> (anyone could then run programs on that machine). If you later need a public read-only site, ask for a "view only" mode.

## 1. On your own computer (development)

See `README.md`. You need JDK 17+, Maven and a MongoDB.

## 2. Docker Compose (app + MongoDB together)

Needs Docker Desktop running.

```powershell
cd ds-library
docker compose up --build
```

Open http://localhost:8085. Stop with `docker compose down`. Data lives in the `mongo-data` volume and survives restarts
(`docker compose down -v` deletes it too).

## 3. On the internet: MongoDB Atlas + a Docker web host

The recipe below uses **MongoDB Atlas** (free database) and **Render** (free web hosting from a Dockerfile).
Any host that can run a Docker image works the same way (Railway, Fly.io, a VPS...).
Hosting sites change their screens often, so look for the matching setting names rather than exact buttons.

### Step A: put the code on GitHub
The host builds from your repository. Push the whole project (it contains both `ds-library` and `code-runner`) to a GitHub repository.
Before pushing, check that no real password or token is in any file. `.env` and `target/` are already ignored.
Because the `Dockerfile` is inside `ds-library`, tell the host to use **`ds-library` as the root directory** (Render calls this "Root Directory").

### Step B: create the database (MongoDB Atlas)
1. Create a free cluster (the free tier gives 512 MB, shared by your data **and** uploaded files).
2. **Database Access:** create a user with a long random password. If the password contains symbols such as `@ : / ?`, replace each by its URL-encoded form (for example `@` becomes `%40`).
3. **Network Access:** free web hosts do not have a fixed IP address, so you will probably need to allow `0.0.0.0/0`.
   This is acceptable only because the database still needs the long password. Never use a weak password with this setting.
4. Copy the connection string. It looks like
   `mongodb+srv://USER:PASSWORD@cluster0.xxxxx.mongodb.net/dslibrary?retryWrites=true&w=majority`
   (add `/dslibrary` before the `?` so your data goes into a database called `dslibrary`).

### Step C: create the web service (Render)
1. New **Web Service**, connect your GitHub repository, choose **Docker** as the runtime. It finds the `Dockerfile` by itself.
2. Add these **environment variables** (never put them in the code):

| Name | Value |
|---|---|
| `MONGODB_URI` | the Atlas connection string from step B |
| `GITHUB_TOKEN` | optional, a read-only token |
| `ANTHROPIC_API_KEY` | optional: turns on the AI assistant (keep it secret; costs API credit) |
| `CHAT_DAILY_LIMIT` | optional: the most AI questions the whole site accepts per day (default 500) |

3. Deploy. When the log shows `Started DsLibraryApplication`, open the address Render gives you.
4. Add your content.

### What to expect on a free host
- The service goes to sleep after a period without visitors, so the first page after a break can take about a minute to load.
- Memory is small (512 MB). The Dockerfile already limits Java to a 300 MB heap. Very large PDFs (near 25 MB) are fine one at a time.
- **Java, C and C++ running will not work on a free host**, because it needs Docker inside the host.
  JavaScript and Python keep working (they run in the visitor's browser). The editor then shows Java, C and C++ as "cannot be run yet", which is accurate.
  To get server-side running online you need a small VPS where you can run Docker, with `code-runner` started next to the app (see `../code-runner/README.md`).

## Before you share the link: checklist

- [ ] You understand that **anyone with the link can add, change and delete content** (there is no login). Share the link only with people you trust, or keep the site on your own computer.
- [ ] The code runner is **not** connected to a public site.
- [ ] If you turned on the AI assistant: your API key is only in the host's environment variables, `CHAT_DAILY_LIMIT` is set to something you are happy to pay for, and `college-info.md` contains only official information.
- [ ] The Atlas database user has a long random password; no secrets are in your GitHub repository.
- [ ] You looked at the Profile page: everything on it (including emails) is **public**. Leave out anything you do not want strangers to read.
- [ ] Atlas backups are on, or you run `mongodump` now and then (uploaded files live in the same database, in GridFS).

## Updating the live site
Push your changes to GitHub; the host rebuilds and redeploys. Data stays in Atlas.

## Troubleshooting

| Problem | Likely cause and fix |
|---|---|
| App starts but pages show "Cannot reach the server" or data errors | `MONGODB_URI` is wrong, or Atlas Network Access blocks the host. Check `/api/health` |
| `/api/health` says database DOWN | Same as above. Check the password encoding and the cluster address |
| Upload fails with "too large" | PDFs are limited to 25 MB, images to 5 MB |
| GitHub cards say "rate limit reached" | Set `GITHUB_TOKEN`, or wait. Results are cached for 10 minutes |
| Editor will not load | The editor, PDF reader and Python load libraries from CDNs, so the visitor needs internet access |
