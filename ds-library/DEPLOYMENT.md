# Deployment guide

Three ways to run the portal, from simplest to "on the internet".

## 1. On your own computer (development)

See `README.md`. You need JDK 17+, Maven and a MongoDB.

## 2. Docker Compose (app + MongoDB together)

Needs Docker Desktop running.

```powershell
$env:ADMIN_PASSWORD = "choose-a-long-password"
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
| `ADMIN_PASSWORD` | a long password only you know |
| `COOKIE_SECURE` | `true` (the site is served over HTTPS) |
| `FORWARD_HEADERS` | `native` |
| `GITHUB_TOKEN` | optional, a read-only token |

3. Deploy. When the log shows `Started DsLibraryApplication`, open the address Render gives you.
4. Log in with **Admin login** (username `admin`) and add your content.

### What to expect on a free host
- The service goes to sleep after a period without visitors, so the first page after a break can take about a minute to load.
- Memory is small (512 MB). The Dockerfile already limits Java to a 300 MB heap. Very large PDFs (near 25 MB) are fine one at a time.
- **Java, C and C++ running will not work on a free host**, because it needs Docker inside the host.
  JavaScript and Python keep working (they run in the visitor's browser). The editor then shows Java, C and C++ as "cannot be run yet", which is accurate.
  To get server-side running online you need a small VPS where you can run Docker, with `code-runner` started next to the app (see `../code-runner/README.md`).

## Before you share the link: checklist

- [ ] `ADMIN_PASSWORD` is long (12+ characters) and not used anywhere else.
- [ ] `COOKIE_SECURE=true` and the address starts with `https://`.
- [ ] `FORWARD_HEADERS=native` on a proxied host, so the login attempt limit counts each visitor separately.
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
| Login works locally but not online | `COOKIE_SECURE=true` needs HTTPS. Open the `https://` address, not `http://` |
| "Too many failed attempts" for everyone | Behind a proxy without `FORWARD_HEADERS=native` all visitors look like one address. Set it |
| Upload fails with "too large" | PDFs are limited to 25 MB, images to 5 MB |
| GitHub cards say "rate limit reached" | Set `GITHUB_TOKEN`, or wait. Results are cached for 10 minutes |
| Editor will not load | The editor, PDF reader and Python load libraries from CDNs, so the visitor needs internet access |
