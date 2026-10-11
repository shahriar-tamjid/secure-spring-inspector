# SecureSpring Inspector — Defence Guide

Use these answers as study notes. They describe the implementation in this repository and deliberately avoid claims beyond it.

## Why is this static analysis?

The tool inspects source/configuration files without running the target application. It compares extracted configuration values and selected Java source lines against deterministic rules.

## Why are the rules deterministic?

A rule has a fixed input pattern and a fixed decision. For example, `debug=true` is a finding; `debug=false` is not. The same files therefore produce the same result.

## Static vs dynamic analysis

Static analysis examines artefacts such as source code and configuration without executing the application. Dynamic analysis observes a running system. This project is intentionally static and does not launch the target application.

## What is a false positive?

A false positive is a reported finding that is not actually a security problem in the application's real context. For example, an HTTP URL may intentionally point to a local test endpoint; this scanner already excludes common loopback URLs but cannot understand every environment-specific exception.

## Why can this tool miss vulnerabilities?

It only supports a small set of configuration/source patterns. It does not build an AST, perform control-flow analysis, trace data flow, or understand an application's runtime behaviour.

## Why is it useful for software assurance?

It shows a repeatable checking process: define a security property, encode a rule, report evidence, test positive and negative cases, and preserve the result in a reproducible workflow.

## How would an industrial tool differ?

An industrial analyser may parse Java with a real syntax tree, track data/control flow, understand frameworks, analyse much larger rule sets, manage suppressions, reduce duplicates, and integrate with CI/CD and development environments. Those capabilities are intentionally outside this educational project.

## Why Java?

Java matches the target ecosystem and the author's backend experience. It also makes the rule engine, typed finding model, testing and Maven build straightforward.

## Why no database?

The scanner has no persistent state requirement. A local filesystem input produces a report, so adding a database would increase operational complexity without improving the core security-analysis idea.

## Why no LLM inside the product?

The project is intended to demonstrate deterministic, reproducible security checks. An LLM-powered detector could make the core decision process harder to reproduce and defend. LLMs may be used while developing or studying the repository, but they are not part of the product runtime.

## What is the most important limitation to admit?

This is a lightweight rule-based analyser, not a comprehensive vulnerability scanner or formal verification system.
