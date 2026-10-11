# SecureSpring Inspector

A lightweight, deterministic static-analysis tool for checking selected Spring Boot configuration and Java source files for common security misconfigurations.

> Educational scope: this repository demonstrates a small, explainable security-analysis pipeline. It is not a commercial vulnerability scanner, formal-verification system, or guarantee of application security.

## 1. Project Overview

SecureSpring Inspector accepts a local Spring Boot project directory, discovers a small set of supported files, extracts simple configuration entries, applies ten security rules, and produces a human-readable or JSON report.

The runtime is deliberately small:

```text
Target Project
    |
    v
File Discovery
    |
    v
Configuration / Source Reading
    |
    v
Deterministic Security Rules
    |
    v
Findings Collector
    |
    +------> Console Reporter
    |
    +------> JSON Reporter
```

The project is CLI-first and runs without a database or web server.

## 2. Motivation

Backend engineers regularly work with application configuration, authentication settings, management endpoints, URLs and secrets. A small deterministic checker is a useful way to learn how security properties can be turned into repeatable engineering checks.

This project is intentionally small enough that the important decisions can be studied and defended without pretending to implement an industrial scanner.

## 3. Problem Statement

Common application-security mistakes can exist in configuration before an application is deployed. Examples include debug mode being explicitly enabled, wildcard CORS, broad actuator exposure, literal password/secret-like values, insecure non-local HTTP URLs, weakened session-cookie settings, explicit CSRF disablement, sensitive management endpoints, risky upload-directory paths, and missing explicit cookie-security settings in the defined demo profile.

SecureSpring Inspector checks these patterns before deployment without executing the target application.

## 4. Cybersecurity and Software Assurance Relevance

### Cybersecurity

The project turns a list of security properties into deterministic checks that identify potentially unsafe configuration and source patterns.

### Backend Engineering

The project is a Java/Spring Boot application using Maven, dependency injection, filesystem access, typed models, a small rule engine, reporting, input validation, exception handling and JUnit tests.

### Software Assurance

The project demonstrates a small form of static analysis and program-analysis thinking: define a property, inspect artefacts, evaluate a rule, produce traceable evidence and test both positive and negative cases. It does **not** claim formal verification, theorem proving, symbolic execution, data-flow analysis, or industrial-grade static analysis.

## 5. Scope

The scanner currently reads:

- `application.properties`
- `application.yml`
- `application.yaml`
- `pom.xml` (discovered as a supported project file, but not currently analysed by a rule)
- Java source files under project `src` directories

The configuration parser intentionally supports simple mapping-style properties/YAML. It does not attempt to implement the full YAML specification.

The project avoids databases, message queues, microservices, cloud SDKs, complex frontends, ML, LLM APIs and target-application execution.

## 6. What the Tool Detects

The initial rule set contains ten deterministic rules:

| ID | Rule | Severity | Main evidence |
|---|---|---|---|
| SEC-001 | Debug Mode Enabled | LOW | `debug=true` |
| SEC-002 | Wildcard CORS | MEDIUM | CORS origin set to `*` |
| SEC-003 | Broad Actuator Exposure | MEDIUM | `management.endpoints.web.exposure.include=*` |
| SEC-004 | Hard-coded Password/Secret-like Property | HIGH | Literal value for a secret-like key |
| SEC-005 | Insecure HTTP URL in Supported Configuration | MEDIUM | Non-local `http://` URL |
| SEC-006 | Weak Cookie Security Setting | MEDIUM | Session cookie `secure=false` or `http-only=false` |
| SEC-007 | Disabled CSRF Protection | HIGH | A small set of explicit Spring Security disable patterns |
| SEC-008 | Exposed Management Endpoint Configuration | MEDIUM | Explicit exposure of `env`, `beans`, `configprops`, `heapdump`, or `threaddump` |
| SEC-009 | Unsafe File/Upload Configuration | MEDIUM | Upload directory points to parent/root-style path |
| SEC-010 | Missing Explicit Security Configuration for Demo Case | LOW | Missing explicit Secure + HttpOnly session-cookie settings |

## 7. Threat / Security Perspective

The project focuses on configuration and source patterns that can weaken security posture if they are accidentally committed or deployed.

A finding is not automatically proof that the application is exploitable. Each rule is intentionally narrow, and the report exposes the evidence file and line where a match was found when line information is reliable.

Threat perspective, in simple terms:

- insecure configuration can expand the attack surface;
- literal credentials can create credential disclosure risk;
- weak browser/session settings can reduce protection around sessions;
- management endpoint exposure can disclose operational information;
- insecure transport configuration can expose traffic to interception;
- unsafe storage paths can cross intended filesystem boundaries.

## 8. Architecture

The implementation follows five small stages.

### 1. File discovery

`ProjectScanner` validates the target directory, ignores `.git` and `target`, applies a 1 MB per-file size limit, and reads only recognised files.

### 2. Parsing

`ConfigFileParser` turns simple properties/YAML mappings into `ConfigEntry` objects containing a dotted key, value, file and line number. Java source is retained as lines because only a few explicit text patterns are required.

### 3. Rule evaluation

Each class implementing `SecurityRule` owns one security property. The `RuleEngine` evaluates all ten enabled rules.

### 4. Findings

A finding contains rule ID, rule name, severity, file, line number when available, message, description and recommendation.

### 5. Reporting

The CLI prints either a console report or JSON report. Findings are de-duplicated and sorted by severity and evidence location.

## 9. Project Structure

```text
SecureSpring-Inspector/
├── .github/
│   └── workflows/
│       └── build.yml
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── docs/
│   ├── DEFENCE_GUIDE.md
│   └── DEMO_SCRIPT.md
├── examples/
│   ├── sample-secure-app/
│   └── sample-vulnerable-app/
├── src/
│   ├── main/java/com/example/securespringinspector/
│   │   ├── cli/
│   │   ├── model/
│   │   ├── parser/
│   │   ├── report/
│   │   ├── rule/
│   │   └── scanner/
│   └── test/java/com/example/securespringinspector/
├── .gitignore
├── BUILD_GUIDE.md
├── LICENSE
├── README.md
├── mvnw
├── mvnw.cmd
└── pom.xml
```

`BUILD_GUIDE.md` provides a file-by-file study map and identifies the purpose of every source and test file.

## 10. Technologies Used

- Java 21
- Spring Boot 3.4.5
- Maven
- JUnit 5 through Spring Boot test support
- Java standard-library filesystem, collections, regex and JSON-string escaping
- Git / GitHub
- GitHub Actions

Spring Boot is used for dependency injection and packaging rather than to create a web API.

## 11. How the Analysis Works

The scanner follows this sequence:

1. validate the target directory;
2. discover supported files;
3. read text as UTF-8;
4. parse simple properties/YAML mappings;
5. retain Java source lines for explicit source-pattern checks;
6. evaluate all ten rules;
7. remove duplicate findings;
8. sort findings by severity, rule ID and evidence location;
9. report the result.

### Configuration matching

Properties use `key=value` (or simple `key:value` forms), while YAML indentation is converted into dotted keys such as:

```text
server.servlet.session.cookie.secure=true
```

The YAML parser is intentionally limited. Complex YAML features such as anchors, advanced sequences, multi-document semantics and arbitrary tags are not interpreted.

### Source matching

Java rules currently use a very small set of explicit line patterns rather than a full Java parser. Comments that occupy an entire line are ignored.

## 12. Rule Catalogue

### SEC-001 — Debug Mode Enabled

**What is checked:** `debug=true`, `debug=yes`, or `debug=1` in supported configuration.

**Why it matters:** Debug-oriented behaviour can expose diagnostics or implementation information that is not appropriate for production.

**Detected pattern:** Exact configuration key `debug` with a truthy value.

**Vulnerable example:**

```properties
debug=true
```

**Corrected example:**

```properties
debug=false
```

**Severity rationale:** LOW because the scanner only observes the explicit configuration flag and cannot determine the application's exact debugging behaviour.

**Limitations / false positives:** Some controlled development environments intentionally enable debug mode.

### SEC-002 — Wildcard CORS

**What is checked:** Configuration keys containing both `cors` and `origin` with value `*`, plus an obvious Java `setAllowedOrigins(..."*"...)` pattern.

**Why it matters:** Wildcard browser-origin access can be much broader than necessary.

**Detected patterns:**

```properties
app.cors.allowed-origins=*
```

or an obvious Java wildcard call.

**Corrected example:**

```properties
app.cors.allowed-origins=https://example.com
```

**Severity rationale:** MEDIUM because origin requirements are application-specific but a wildcard is a clear configuration pattern worth reviewing.

**Limitations / false positives:** The scanner does not understand every CORS library, framework abstraction or runtime environment.

### SEC-003 — Broad Actuator Exposure

**What is checked:** `management.endpoints.web.exposure.include=*`.

**Why it matters:** Broad management endpoint exposure can increase the externally reachable management surface.

**Vulnerable example:**

```properties
management.endpoints.web.exposure.include=*
```

**Corrected example:**

```properties
management.endpoints.web.exposure.include=health,info
```

**Severity rationale:** MEDIUM because actual impact depends on network access controls and the endpoints used.

**Limitations / false positives:** The scanner cannot determine whether the management port is private or protected by an upstream network boundary.

### SEC-004 — Hard-coded Password/Secret-like Property

**What is checked:** Keys containing common secret-like words such as `password`, `secret`, `token`, `api-key` or `access-key` with a literal value.

**Why it matters:** Credentials and secrets should not normally be committed directly into source configuration.

**Vulnerable example:**

```properties
app.datasource.password=demo-secret-123
```

**Corrected example:**

```properties
app.datasource.password=${DB_PASSWORD}
```

**Severity rationale:** HIGH because a literal credential can become a source-control disclosure problem.

**Limitations / false positives:** A value may be a harmless demo string or intentionally non-secret. The rule only knows the key/value text.

### SEC-005 — Insecure HTTP URL in Supported Configuration

**What is checked:** Non-local literal `http://` URLs in supported configuration. Common loopback URLs are excluded.

**Why it matters:** HTTPS protects service traffic against network interception and tampering.

**Vulnerable example:**

```properties
remote.service.url=http://example.com/api
```

**Corrected example:**

```properties
remote.service.url=https://example.com/api
```

**Severity rationale:** MEDIUM because local/test HTTP can be intentional and network architecture matters.

**Limitations / false positives:** The scanner cannot know whether a URL is protected by a trusted local network or reverse proxy.

### SEC-006 — Weak Cookie Security Setting

**What is checked:** Explicit `server.servlet.session.cookie.secure=false` or `server.servlet.session.cookie.http-only=false`.

**Why it matters:** Session-cookie flags are important defence-in-depth controls for browser-based sessions.

**Vulnerable example:**

```properties
server.servlet.session.cookie.secure=false
server.servlet.session.cookie.http-only=false
```

**Corrected example:**

```properties
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
```

**Severity rationale:** MEDIUM because the correct setting depends on deployment and client architecture.

**Limitations / false positives:** The scanner does not infer whether the application uses browser sessions, whether HTTPS terminates before the application, or whether cookies are configured elsewhere.

### SEC-007 — Disabled CSRF Protection

**What is checked:** A small set of explicit source patterns such as `csrf().disable()`, `csrf(AbstractHttpConfigurer::disable)`, and `csrf(csrf -> csrf.disable())`.

**Why it matters:** CSRF protection can be important for state-changing requests in browser/session-based applications.

**Vulnerable example:**

```java
http.csrf(csrf -> csrf.disable());
```

**Corrected example:** Keep CSRF protection enabled unless the application's security model explicitly justifies otherwise.

**Severity rationale:** HIGH because blindly disabling a protection can remove an important defence.

**Limitations / false positives:** Stateless APIs may intentionally disable CSRF depending on their authentication model. The rule cannot reason about that full context.

### SEC-008 — Exposed Management Endpoint Configuration

**What is checked:** Explicit exposure of `env`, `beans`, `configprops`, `heapdump` or `threaddump` through `management.endpoints.web.exposure.include`.

**Why it matters:** These endpoints can reveal operational or application-internal information.

**Vulnerable example:**

```properties
management.endpoints.web.exposure.include=health,env
```

**Corrected example:**

```properties
management.endpoints.web.exposure.include=health,info
```

**Severity rationale:** MEDIUM because actual exposure impact depends on authentication and network reachability.

**Limitations / false positives:** The scanner cannot determine whether a management endpoint is only reachable from a trusted operator network.

### SEC-009 — Unsafe File/Upload Configuration

**What is checked:** An upload-directory configuration whose value is obviously a parent-directory traversal or filesystem-root-style path, such as `../uploads`, `../../uploads`, `/`, or `C:/`.

**Why it matters:** Storage locations should remain within an intended application-owned boundary.

**Vulnerable example:**

```properties
app.upload.directory=../uploads
```

**Corrected example:**

```properties
app.upload.directory=uploads
```

**Severity rationale:** MEDIUM because the pattern is a clear configuration smell, but the complete file-handling logic is outside the scanner's scope.

**Limitations / false positives:** The rule does not inspect file-name sanitisation, path canonicalisation or runtime access controls.

### SEC-010 — Missing Explicit Security Configuration for Demo Case

**What is checked:** Whether both session-cookie security properties are explicitly present in supported configuration.

**Why it matters:** For this educational demo, the rule makes an important security assumption visible and testable instead of silently relying on defaults.

**Vulnerable example:**

```properties
spring.application.name=demo
```

**Corrected example:**

```properties
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
```

**Severity rationale:** LOW because this is a narrow completeness check, not a universal Spring security requirement.

**Limitations / false positives:** Different applications may rely on secure framework defaults, use another session mechanism, or configure cookies in code.

## 13. Example Vulnerable Project

`examples/sample-vulnerable-app` is a static fixture, not a production service. It intentionally contains insecure patterns for demonstration.

Notable values include:

```properties
debug=true
app.cors.allowed-origins=*
management.endpoints.web.exposure.include=*
app.datasource.password=demo-secret-123
remote.service.url=http://example.com/api
server.servlet.session.cookie.secure=false
server.servlet.session.cookie.http-only=false
app.upload.directory=../uploads
```

Its Java fixture also contains an explicit CSRF-disable pattern.

The scanner counts below must be understood as results produced by this repository's deterministic test fixture. They are not benchmark results and do not claim to represent real-world vulnerability rates.

## 14. Example Secure Project

`examples/sample-secure-app` is the corrected fixture. It uses explicit secure cookie settings, a specific CORS origin, HTTPS, a secret placeholder, a narrow actuator list, and a local upload directory.

It is expected to produce zero findings under the rules in this repository.

## 15. Example Output

A scan of the included vulnerable fixture currently produces this deterministic result:

```text
SecureSpring Inspector
======================
Target: ...\examples\sample-vulnerable-app
Files analysed: 3
Rules evaluated: 10

HIGH   SEC-004  Hard-coded secret-like value detected (application.properties:6)
HIGH   SEC-007  Spring Security CSRF protection is explicitly disabled (src/main/java/com/example/samplevulnerable/config/SecurityConfig.java:6)
MEDIUM SEC-002  Wildcard CORS origin is configured (application.properties:4)
MEDIUM SEC-003  Actuator web exposure includes all endpoints (*) (application.properties:5)
MEDIUM SEC-005  Non-local HTTP URL detected; HTTPS is not configured (application.properties:7)
MEDIUM SEC-006  Session cookie security is explicitly weakened (application.properties:8)
MEDIUM SEC-006  Session cookie security is explicitly weakened (application.properties:9)
MEDIUM SEC-009  Upload directory points outside an obviously local application-owned path (application.properties:10)
LOW    SEC-001  Debug mode is explicitly enabled (application.properties:3)

Summary
-------
High: 2
Medium: 6
Low: 1
Passed rules: 2
Fail threshold: LOW
```

The exact counts above are produced by the repository's deterministic demo fixture and are not benchmark results.

For machine-readable output:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="examples/sample-vulnerable-app --format=json"
```

## 16. Local Installation

### Windows — recommended path

Install Git and Java 21. Maven itself is not required: `mvnw.cmd` bootstraps the pinned Maven distribution into your user Maven cache on first use.

Check Java and Git:

```powershell
java -version
git --version
```

Clone the repository:

```powershell
git clone https://github.com/YOUR-USERNAME/securespring-inspector.git
cd securespring-inspector
```

The placeholder GitHub URL is intentionally not claimed to be an existing repository. Replace `YOUR-USERNAME` after publishing your own repository.

## 17. Running the Project

### Recommended packaged-JAR path

Build:

```powershell
.\mvnw.cmd clean package
```

Run the vulnerable fixture:

```powershell
java -jar target\securespring-inspector-0.0.1-SNAPSHOT.jar examples\sample-vulnerable-app
```

Run the secure fixture:

```powershell
java -jar target\securespring-inspector-0.0.1-SNAPSHOT.jar examples\sample-secure-app
```

JSON:

```powershell
java -jar target\securespring-inspector-0.0.1-SNAPSHOT.jar examples\sample-vulnerable-app --format=json
```

Change the exit-code threshold:

```powershell
java -jar target\securespring-inspector-0.0.1-SNAPSHOT.jar examples\sample-vulnerable-app --fail-on=HIGH
```

### Run directly with Spring Boot Maven plugin

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="examples/sample-vulnerable-app"
```

### Exit codes

```text
0 = no findings at or above the selected threshold
1 = at least one finding at or above the selected threshold
2 = scanner/runtime/input error
```

## 18. Running Tests

Run all tests:

```powershell
.\mvnw.cmd test
```

The test suite covers:

- positive and negative rule cases;
- YAML nesting and comments;
- malformed YAML handling;
- vulnerable fixture scanning;
- secure fixture scanning;
- missing target paths;
- unsupported files being ignored;
- duplicate-looking input being handled by distinct finding collection.

## 19. Building the JAR

```powershell
.\mvnw.cmd clean package
```

The executable JAR is created under:

```text
target/securespring-inspector-0.0.1-SNAPSHOT.jar
```

No target project code is executed by the scanner. The JAR is simply given a directory to inspect.

## 20. Running with Docker (if used)

Docker is intentionally not required for the first version because this is a CLI tool and the Maven/Java workflow is already reproducible.

A Docker image can be added later without changing the analysis architecture. The current repository does not include a Dockerfile so that there is no fake deployment surface.

## 21. Deployment Guide

### Preferred public verification model

1. Push the repository to GitHub.
2. Let GitHub Actions run `./mvnw test` and `./mvnw -DskipTests package`.
3. Optionally publish the executable JAR as a GitHub Release after you have verified it.

No cloud backend is necessary for this project.

### GitHub publishing

After creating an empty repository on GitHub:

```powershell
git init
git add .
git commit -m "Build SecureSpring Inspector"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/securespring-inspector.git
git push -u origin main
```

Replace the placeholder URL with the real repository you own.

### Releasing the JAR

After a successful build:

```powershell
.\mvnw.cmd clean package
```

Upload `target\securespring-inspector-0.0.1-SNAPSHOT.jar` to a GitHub Release if desired. Do not call it a security-certified release; it is simply a reproducible educational artefact.

## 22. Reproducibility

A clean-machine verification can be performed with:

```powershell
git clone <your-real-repository-url>
cd securespring-inspector
java -version
.\mvnw.cmd test
.\mvnw.cmd clean package
java -jar target\securespring-inspector-0.0.1-SNAPSHOT.jar examples\sample-vulnerable-app
java -jar target\securespring-inspector-0.0.1-SNAPSHOT.jar examples\sample-secure-app
```

The project does not depend on a database, local cloud credentials, runtime target services, or a hidden external API.

The project-local Maven bootstrap wrapper pins the Maven distribution used to build the project and downloads it only on first use. The source tree pins Java and Spring Boot versions through `pom.xml`.

## 23. Design Decisions

### Why CLI-first?

A CLI keeps the main idea visible. The input is a directory and the output is a report.

### Why deterministic rules?

Deterministic rules are easy to test, reproduce and defend. The same evidence should produce the same result.

### Why a line-oriented parser?

The first version only needs simple configuration patterns. A tiny parser keeps the implementation understandable and avoids making the project look like a framework.

### Why Java source text instead of a full AST?

Only a few explicit Java patterns are required. A full Java parser would add complexity and create a stronger claim than this educational scope needs.

### Why Spring Boot?

Spring Boot is the ecosystem being inspected and also provides a clean way to package a Java CLI with dependency injection. The application explicitly disables the web environment.

### Why no database?

A scan is a stateless transformation from files to findings. Persistent storage is not required.

### Why no cloud deployment?

A public CLI repository with CI is a more coherent deployment story than inventing a cloud backend merely for appearance.

### Why no LLM at runtime?

The core security decision must remain explainable and reproducible. LLM assistance may be useful during development, but it is intentionally outside the product.

## 24. Limitations

This project does not:

- execute the target application;
- guarantee that a project is secure;
- parse all Java syntax or semantics;
- perform AST, control-flow or data-flow analysis;
- understand every Spring Security configuration style;
- resolve environment-specific runtime behaviour;
- inspect binary dependencies for CVEs;
- prove security properties formally;
- replace a professional application-security review.

The YAML parser supports simple mapping structures only. Complex YAML documents may be skipped with a warning rather than causing the whole scan to fail.

## 25. False Positives / False Negatives

### False positives

A finding can be a false positive because real deployment context matters. Examples:

- HTTP can be intentional for a trusted local network.
- CSRF may be disabled intentionally in a stateless design.
- A secret-like value may be a non-secret demo constant.
- A management endpoint may be protected by a separate network boundary.
- A parent-directory upload path may be safe in a particular controlled environment.

### False negatives

The scanner can miss a problem when:

- the same setting is expressed through an unsupported syntax;
- a value is constructed dynamically;
- security logic is spread across multiple Java classes;
- secrets are encoded, concatenated or loaded through a custom mechanism;
- a security property is set outside the files this version scans.

The README intentionally describes these boundaries instead of hiding them.

## 26. Security Considerations

The scanner reads files from a directory supplied by the user. It does not execute target code, invoke Maven in the target project, start the target application, make outbound requests, or interpret uploaded project contents as executable instructions.

The project has a 1 MB per-file read limit and skips `.git` and `target` directories. Unsupported files are ignored.

The vulnerable sample is intentionally insecure configuration for a test fixture. It is not intended to be deployed.

## 27. Future Improvements

Reasonable next steps, only if they remain explainable:

1. replace line-oriented Java matching with a real Java parser and retain evidence mapping;
2. add a small suppression/ignore mechanism with explicit justification;
3. expand rule metadata and allow a configurable rule set;
4. improve YAML support with a dedicated parser while preserving line evidence;
5. add a minimal SARIF reporter for CI integration;
6. add more security fixtures and regression tests.

Future work should not be described as already implemented.

## 28. What I Learned

This project is intended to demonstrate practical learning around:

- static analysis as a repeatable security-checking process;
- rule design and deterministic decisions;
- evidence and line-level traceability;
- false-positive / false-negative trade-offs;
- secure configuration thinking;
- Java/Spring Boot modular backend design;
- test-driven validation of security rules;
- reproducible builds with Maven and CI.

## 29. Author

**Sharif MD Shahriar Tamjid**

Software Engineer II | Backend / Enterprise Systems

- GitHub: https://github.com/shahriar-tamjid
- LinkedIn: https://linkedin.com/in/shahriar-tamjid/

This project is an educational portfolio project built to demonstrate backend engineering, cybersecurity awareness, and an introductory software-assurance mindset.

## 30. Licence

MIT License. See `LICENSE`.
