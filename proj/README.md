# Interview Prep Hub — Spring Boot + MySQL + Thymeleaf

A single Spring Boot app hosting six interview-prep tabs: DSA, DBMS, Azure, Java &
Microservices, Docker + CI/CD + Jenkins, and a Misc page where you write your own
Q&A. Every tab's progress (reviewed/solved, important/revision, star rating) is
stored in MySQL instead of the browser, so it follows you across devices.

## Pages / tabs

| URL       | Content                                                            |
|-----------|---------------------------------------------------------------------|
| `/`       | Home hub linking to every tracker                                   |
| `/dsa`    | 108 pattern-based LeetCode questions (solved / revision / rating)   |
| `/dbms`   | 11 DBMS & SQL interview questions (reviewed / important / rating)   |
| `/azure`  | 38 Microsoft Azure interview questions                              |
| `/java`   | 156 Java & Microservices questions, grouped by category             |
| `/docker` | 69 Docker + CI/CD + Jenkins questions (3 YOE level)                  |
| `/misc`   | Your own questions — full CRUD (add, edit, delete)                  |

A dark tab bar pinned to the top of every page lets you jump between them.

## What's stored in the database

Two tables, both holding only small numeric/text fields you actually generate —
none of the built-in question text is duplicated into the database:

**`tracker_progress`** — one row per (tracker, question) once you interact with it:

| column   | type    | meaning                                             |
|----------|---------|------------------------------------------------------|
| tracker  | varchar | `dsa`, `dbms`, `azure`, `java`, or `docker`           |
| item_id  | int     | the question's number on that page                   |
| done     | int     | 0/1 — solved (DSA) or reviewed (the rest)             |
| flagged  | int     | 0/1 — revision (DSA) or important (the rest)          |
| rating   | int     | 0–5 star rating                                       |

A question with no row yet is simply treated as done=0/flagged=0/rating=0 by the
page — rows are created lazily the first time you tick something.

**`misc_question`** — your own Q&A entries, written entirely by you:

| column     | type     | meaning                          |
|------------|----------|-----------------------------------|
| title      | varchar  | the question you typed            |
| answer     | text     | your own answer                   |
| done       | int      | reviewed flag                     |
| flagged    | int      | important flag                    |
| rating     | int      | 0–5 star rating                   |
| created_at | datetime | when you added it                 |

## Prerequisites

- Java 17+
- Maven 3.8+
- A running MySQL server (5.7+ or 8.x)

## 1. Configure MySQL

Edit `src/main/resources/application.properties`, or set these as environment
variables (useful for deployment — see below):

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dsa_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

You don't need to create the schema yourself — `createDatabaseIfNotExist=true`
creates it on first connection, and `spring.jpa.hibernate.ddl-auto=update` creates
both tables automatically on startup.

## 2. Run it

```bash
mvn spring-boot:run
```

Or build a jar and run that:

```bash
mvn clean package
java -jar target/dsa-tracker-1.0.0.jar
```

## 3. Open it

Visit **http://localhost:8080/** — the home hub links to every tab. Ticking a
checkbox, flagging something important, or setting a star rating saves instantly
to MySQL via the API below; reloading (or opening from another device) pulls the
same values back.

## API summary

| Method | Path                          | Purpose                                              |
|--------|-------------------------------|--------------------------------------------------------|
| GET    | /api/progress/{tracker}       | All progress rows for that tracker (dsa/dbms/azure/java/docker) |
| PUT    | /api/progress/{tracker}/{id}  | Upsert one question's done/flagged/rating              |
| POST   | /api/progress/{tracker}/reset | Wipe all progress rows for that tracker only            |
| GET    | /api/misc                     | List your own questions                                 |
| POST   | /api/misc                     | Create a question `{title, answer}`                     |
| PUT    | /api/misc/{id}                | Update title/answer/done/flagged/rating                 |
| DELETE | /api/misc/{id}                | Delete a question                                       |

## Deploying it for free (usable from any device)

There's no platform in 2026 offering a permanently free, always-on Spring Boot +
MySQL combo — but for personal use, a $0 setup works fine with two known trade-offs:

- Render's free web service **sleeps after 15 minutes idle** — the first request
  after a break takes ~30-60 seconds to wake back up.
- Free MySQL hosts like db4free.net are explicitly "testing only" — occasional
  downtime or a reset is possible. Fine for personal use, not for anything critical.

### 1. Get a free MySQL database (db4free.net)

1. Go to **https://www.db4free.net/** and click "Sign up".
2. Pick a database name, username, and password. Submit — you'll get a confirmation
   email with an activation link; click it.
3. Note down: host `db4free.net`, port `3306`, your database name, username, password.

(Alternative: **freedb.tech** works the same way if db4free.net is down for maintenance.)

### 2. Push this project to GitHub

```bash
cd interview-prep-hub
git init
git add .
git commit -m "Initial commit"
```
Create a new empty repo on GitHub, then follow its "push an existing repository"
instructions to push this code there.

### 3. Deploy to Render

1. Go to **https://render.com** and sign up (no card required for the free tier).
2. **New +** → **Web Service** → connect your GitHub repo.
3. Render will detect the `Dockerfile` automatically — leave "Docker" as the
   environment/runtime.
4. Under **Environment Variables**, add:

   | Key                          | Value                                                                                          |
   |------------------------------|--------------------------------------------------------------------------------------------------|
   | `SPRING_DATASOURCE_URL`      | `jdbc:mysql://db4free.net:3306/<your-db-name>?useSSL=false&serverTimezone=UTC`                    |
   | `SPRING_DATASOURCE_USERNAME` | `<your db4free.net username>`                                                                     |
   | `SPRING_DATASOURCE_PASSWORD` | `<your db4free.net password>`                                                                     |

   (You don't need to set `PORT` — Render injects it automatically, and
   `application.properties` already reads it.)
5. Choose the **Free** instance type, click **Create Web Service**.
6. Wait for the build/deploy logs to finish (a few minutes the first time).

### 4. Open it from any device

Render gives you a public URL like `https://interview-prep-hub.onrender.com` —
open that on your phone, laptop, or any browser, from anywhere. Progress is shared
across all devices since it's all reading/writing the same MySQL database.
