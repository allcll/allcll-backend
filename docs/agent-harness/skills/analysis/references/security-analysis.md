# Security Analysis Rubric

Use this reference for credential, token, local configuration, admin endpoint, logging, SSE token, external integration, and secret-handling reviews.

## Non-Negotiable Boundary

Do not read or output secret values. Treat these as content-prohibited for this project:

- `src/main/resources/application-local.yml`
- `.env`
- credentials files
- service account JSON files
- token files
- private keys
- local-only config containing admin tokens, Google credentials, portal credentials, or production URLs

Security analysis should work from file names, config classes, environment variable names, property binding names, call paths, `.gitignore`/policy state, logging code, endpoint behavior, and repository metadata without exposing values.

## Risk Categories

Check these categories before writing conclusions:

- Secret storage: local config, dev/prod config ownership, service account files, `.env`, generated artifacts.
- Secret loading: `@ConfigurationProperties`, direct resource loading, environment variables, Google Sheet clients, login/session config.
- Secret propagation: API responses, exception messages, logs, DTO serialization, query strings, SSE event data, cookies, headers.
- Access control: admin endpoints, interceptors, token validation, bypass paths, test-only or local-only branches.
- Retention and cleanup: emitter/token storage, session state, cache lifetime, stale entries, failed-send cleanup.
- Commit policy: ignored files, generated benchmark artifacts, copied transcripts, redaction in docs.

## Safe Evidence Sources

Prefer these evidence sources:

- Code that references property names or config beans without value content.
- Controller/service methods that accept, return, log, or store tokens.
- Exception handlers and logging utilities.
- `.gitignore` and committed docs that describe sensitive-file policy.
- File names and directory layout, not file contents, for sensitive assets.

If a necessary proof would require reading a sensitive file, stop and report the exact non-sensitive evidence that is missing. Ask for redacted metadata or a safe inventory of keys/categories instead of opening the file.

## Finding Standard

For each security finding include:

- Location: code path, file name, method, or policy document.
- Exposure path: where the secret or token could move from source to sink.
- Severity rationale: likelihood, blast radius, privilege level, persistence, or user impact.
- Evidence used: non-sensitive code/policy evidence.
- Evidence avoided: name the sensitive file/category not opened, if relevant.
- Confirmation needed: safe command, redacted sample, test, log review, or config inventory.
- Follow-up route: bug-fix for behavior changes, refactoring for safe cleanup, or ask-user for policy decisions.

## Overclaim Guard

Do not claim that secrets are safe because no value was seen. Say "not proven exposed from the non-sensitive paths reviewed" or "requires safe config inventory" when applicable.
