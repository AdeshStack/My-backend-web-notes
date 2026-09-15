# Local CI/CD with Jenkins + Docker Desktop

Two separate Jenkins pipelines, both defined as files in this repo:

- **`Jenkinsfile`** — the CI pipeline. Runs on every commit: checkout → unit tests →
  build Docker image → push to Docker Hub.
- **`Jenkinsfile.deploy`** — the CD pipeline. Run manually (or trigger after CI):
  pull the latest image from Docker Hub → stop the old container → run the new one,
  wired to **your own local MySQL**.

Keeping them separate mirrors how real teams split "build & publish an artifact" from
"deploy that artifact somewhere" — two different concerns, two different triggers.

## 0. Prerequisites

- Docker Desktop running, and `docker version` works from wherever Jenkins runs its
  shell steps (this is the main thing to get right — see the note below if Jenkins
  itself runs inside a container).
- A Docker Hub account (free) — https://hub.docker.com
- Jenkins plugins installed: **Docker Pipeline**, **Pipeline: Stage View**,
  **Credentials Binding** (the last one is usually installed by default).

> **If your Jenkins itself runs as a Docker container** (rather than installed natively),
> mounting `/var/run/docker.sock` lets it talk to Docker Desktop's daemon, but the
> `-v "$WORKSPACE":/app` volume mount in the CI pipeline's test stage only works cleanly
> if Jenkins's workspace path is also a real path *on the host* (not just inside the
> Jenkins container). If you hit volume-mount weirdness, the simplest fix for local
> learning is running Jenkins natively (the official installer for Windows/Mac) instead
> of containerized — then `$WORKSPACE` is already a real host path and everything "just
> works." This exact class of problem — a path or hostname meaning something different
> inside vs. outside a container — is the same idea behind the `host.docker.internal`
> question below.

## 1. Set up your local MySQL for container access

By default, MySQL only accepts connections from `localhost`, and Docker containers are
*not* localhost from MySQL's point of view. Create a dedicated app user that can connect
from anywhere:

```sql
CREATE USER 'appuser'@'%' IDENTIFIED BY 'your-chosen-password';
GRANT ALL PRIVILEGES ON dsa_tracker.* TO 'appuser'@'%';
FLUSH PRIVILEGES;
```

Also make sure MySQL is actually listening on more than just `127.0.0.1` — in
`my.cnf`/`my.ini`, `bind-address` should be `0.0.0.0` (or commented out), then restart
MySQL.

## 2. Add credentials in Jenkins

**Manage Jenkins → Credentials → System → Global credentials → Add Credentials**, twice:

| Kind | ID | Username | Password |
|---|---|---|---|
| Username with password | `dockerhub-creds` | your Docker Hub username | a Docker Hub **access token** (Docker Hub → Account Settings → Security → New Access Token) — not your real password |
| Username with password | `local-mysql-creds` | `appuser` | the password you set in step 1 |

## 3. Edit the two Jenkinsfiles

In both `Jenkinsfile` and `Jenkinsfile.deploy`, replace `YOUR_DOCKERHUB_USERNAME` with
your actual Docker Hub username (so the image name resolves to a repo you can push to).

## 4. Create the CI pipeline job

1. Jenkins → **New Item** → name it `interview-prep-hub-ci` → type **Pipeline** → OK.
2. Under **Pipeline**, set Definition to **Pipeline script from SCM**, SCM = **Git**,
   point it at this repo's URL/branch.
3. Script Path: `Jenkinsfile` (default, leave as-is).
4. Save, then **Build Now**. Watch the stages run: test → docker build → docker push.
5. Check Docker Hub — you should see a new repo/tag appear after a successful run.

Optional: add a GitHub webhook (or just poll SCM on a schedule) so this triggers
automatically on every push, instead of clicking Build Now each time.

## 5. Create the CD pipeline job

1. **New Item** → name it `interview-prep-hub-deploy` → type **Pipeline** → OK.
2. Same SCM setup as above, but Script Path: `Jenkinsfile.deploy`.
3. Save, then **Build with Parameters** → leave `IMAGE_TAG` as `latest` (or pin it to a
   specific build number from the CI job) → run.
4. It pulls the image, stops any previous container named `interview-prep-hub`, and
   starts the new one on port 8080, pointed at your local MySQL.
5. Open **http://localhost:8080/** — same MySQL data whether you got there via this
   container or via a plain local run (next section).

## 6. Running locally *without* Docker, against the same MySQL

Since `application.properties` already reads its datasource settings from environment
variables (with `localhost:3306` / `root`/`root` as the fallback default), a plain local
run just needs matching env vars if your credentials differ from the defaults:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/dsa_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME=appuser
export SPRING_DATASOURCE_PASSWORD=your-chosen-password

mvn spring-boot:run
```

Notice the only difference between this and the CD pipeline's `docker run` command is
`localhost` vs `host.docker.internal` — because this process runs directly on your
machine, real `localhost` already reaches your local MySQL. Inside the container, it
wouldn't.

## The pipeline at a glance

```
git push
   │
   ▼
[CI: Jenkinsfile]
   checkout → mvn test → docker build → docker push  →  Docker Hub
                                                             │
                                                             ▼
                                          [CD: Jenkinsfile.deploy] (manual trigger)
                                          docker pull → stop old → run new
                                                             │
                                                             ▼
                                          http://localhost:8080  (your local MySQL)
```
