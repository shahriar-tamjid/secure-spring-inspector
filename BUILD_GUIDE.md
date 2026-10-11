# SecureSpring Inspector — Build Guide

This file is a file-by-file map of the repository so a beginner can study it from a clean checkout.

## Phase A — Final architecture

The project uses a single Maven module. Spring Boot provides dependency injection and the executable JAR, but the application runs with `WebApplicationType.NONE` so it remains a CLI. The flow is:

```text
Target project
  -> ProjectScanner
  -> ConfigFileParser for application.properties/application.yml/application.yaml
  -> SecurityRule implementations
  -> RuleEngine
  -> InspectionResult
  -> ConsoleReporter or JsonReporter
```

No database, frontend, network calls, target-code execution, ML, or LLM API exists in the runtime.

## Source files

| Path | Purpose |
|---|---|
| `src/main/java/com/example/securespringinspector/SecureSpringInspectorApplication.java` | Spring Boot entry point and process exit handling. |
| `src/main/java/com/example/securespringinspector/cli/InspectorCommandLineRunner.java` | Reads CLI arguments, invokes scanning, selects the reporter, and sets exit codes. |
| `src/main/java/com/example/securespringinspector/model/Severity.java` | HIGH/MEDIUM/LOW severity enum. |
| `src/main/java/com/example/securespringinspector/model/Finding.java` | Immutable finding record with evidence and remediation. |
| `src/main/java/com/example/securespringinspector/model/ConfigEntry.java` | Normalised key/value evidence with line number. |
| `src/main/java/com/example/securespringinspector/model/FileType.java` | Supported file categories. |
| `src/main/java/com/example/securespringinspector/model/InspectionResult.java` | Complete scan result and summary helpers. |
| `src/main/java/com/example/securespringinspector/parser/ConfigFileParser.java` | Small deterministic parser for simple properties/YAML mappings. |
| `src/main/java/com/example/securespringinspector/scanner/ProjectScanner.java` | Validates the target, discovers supported files, reads content, and collects warnings. |
| `src/main/java/com/example/securespringinspector/scanner/ScannedFile.java` | In-memory representation of one file. |
| `src/main/java/com/example/securespringinspector/scanner/ScannedProject.java` | In-memory representation of the target project. |
| `src/main/java/com/example/securespringinspector/scanner/ProjectScanException.java` | Input/runtime exception used by the CLI. |
| `src/main/java/com/example/securespringinspector/rule/SecurityRule.java` | Common rule contract. |
| `src/main/java/com/example/securespringinspector/rule/RuleEngine.java` | Owns the ten enabled rules and de-duplicates/sorts findings. |
| `src/main/java/com/example/securespringinspector/rule/RuleSupport.java` | Small helper methods shared by rules. |
| `src/main/java/com/example/securespringinspector/rule/DebugModeRule.java` | SEC-001 implementation. |
| `src/main/java/com/example/securespringinspector/rule/WildcardCorsRule.java` | SEC-002 implementation. |
| `src/main/java/com/example/securespringinspector/rule/BroadActuatorExposureRule.java` | SEC-003 implementation. |
| `src/main/java/com/example/securespringinspector/rule/HardCodedSecretRule.java` | SEC-004 implementation. |
| `src/main/java/com/example/securespringinspector/rule/InsecureHttpUrlRule.java` | SEC-005 implementation. |
| `src/main/java/com/example/securespringinspector/rule/WeakCookieSecurityRule.java` | SEC-006 implementation. |
| `src/main/java/com/example/securespringinspector/rule/DisabledCsrfRule.java` | SEC-007 implementation. |
| `src/main/java/com/example/securespringinspector/rule/SensitiveActuatorEndpointRule.java` | SEC-008 implementation. |
| `src/main/java/com/example/securespringinspector/rule/UnsafeUploadDirectoryRule.java` | SEC-009 implementation. |
| `src/main/java/com/example/securespringinspector/rule/MissingCookieSecurityRule.java` | SEC-010 implementation. |
| `src/main/java/com/example/securespringinspector/report/Reporter.java` | Reporter abstraction. |
| `src/main/java/com/example/securespringinspector/report/ConsoleReporter.java` | Human-readable console output. |
| `src/main/java/com/example/securespringinspector/report/JsonReporter.java` | Minimal standard-library JSON output. |

## Test files

| Path | Purpose |
|---|---|
| `src/test/java/com/example/securespringinspector/ConfigurationRulesTest.java` | Positive/negative tests for configuration rules. |
| `src/test/java/com/example/securespringinspector/SourceRulesTest.java` | Positive/negative tests for Java-source rules. |
| `src/test/java/com/example/securespringinspector/ConfigFileParserTest.java` | YAML nesting, comments, and malformed-input behaviour. |
| `src/test/java/com/example/securespringinspector/ProjectScannerIntegrationTest.java` | End-to-end vulnerable/secure fixture scan and missing-path behaviour. |

## Other important files

- `pom.xml` — Maven build and dependency management.
- `.mvn/wrapper/maven-wrapper.properties`, `mvnw`, `mvnw.cmd` — lightweight project-local Maven bootstrap scripts so Maven does not need to be installed globally.
- `.github/workflows/build.yml` — clean CI test/build job.
- `examples/sample-vulnerable-app/` — deterministic insecure fixture.
- `examples/sample-secure-app/` — corrected fixture expected to produce zero findings.
- `docs/DEFENCE_GUIDE.md` — interview/application study notes.
- `docs/DEMO_SCRIPT.md` — five-minute demonstration script.
- `LICENSE` — MIT licence.
- `.gitignore` — standard repository exclusions.

Every file above is committed as a complete file in this repository; there are no placeholder snippets.
