# FileBot — Fork with HTTP Server

This is a fork of the FileBot source code (v4.8.0), with a built-in HTTP REST API and web UI added on top.

## What's new

Unlike the original CLI-only FileBot, this fork embeds a lightweight HTTP server (`com.sun.net.httpserver`, JDK 17 built-in) that exposes FileBot's core operations via REST endpoints and serves a browser-based interface.

### Features

- **REST API** — All major FileBot operations are available as JSON endpoints:
  - `POST /api/rename` — Rename files via TheTVDB / TheMovieDB / AniDB
  - `POST /api/subtitles` — Fetch subtitles from OpenSubtitles
  - `POST /api/extract` — Extract archives (RAR, 7z, ZIP)
  - `POST /api/mediainfo` — Read media file properties
  - `POST /api/list` — List episodes from online databases
  - `POST /api/check` — Compute / verify SFV, SHA1, SHA256, MD5
  - `POST /api/revert` — Revert previously renamed files
  - `POST /api/script` — Run FileBot scripts (e.g. AMC)
  - `GET/POST /api/settings` — Read / update working directory and language
  - `GET /api/status` — Server health check
  - `GET /api/log` — Live log output (poll-based SSE)
  - `GET /api/files` — List directory contents

- **Web UI** — Full browser interface with sidebar navigation, form-based API calls, live output panel, and connection status indicator

- **i18n** — English and Italian translations; can be extended with additional `web/i18n/*.json` files

- **Server flags** — `-http` starts the server, `--port N` sets the port (default 5454)

- **Thread safety** — Write operations (rename, subtitles, extract, check, revert, script) are serialised on a single-thread executor

## Build

Requires JDK 17+ and Apache Ant.

```sh
ant fatjar
```

Output: `dist/FileBot_5.1.0.jar`

## Run

```sh
java -jar dist/FileBot_5.1.0.jar -http --port 5454
```

Open http://localhost:5454 in your browser.

## Fork base

Original fork point preserved in the [`fork-point`](https://github.com/rednoah/filebot/tree/fork-point) branch.

## License

Same license as the original project.
