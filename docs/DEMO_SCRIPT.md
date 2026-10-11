# Five-Minute Demonstration Script

## 1. Introduce the problem

“SecureSpring Inspector is a small static-analysis tool for checking selected Spring Boot configuration and Java source files for common security misconfigurations.”

## 2. Show the architecture

Point to: file discovery -> lightweight parsing -> rule engine -> findings -> console/JSON report.

## 3. Scan the vulnerable fixture

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="examples/sample-vulnerable-app"
```

Or use the packaged JAR command from the main README.

Explain one high-severity result in detail, such as the hard-coded secret rule.

## 4. Explain why the result is trustworthy

“The rule is deterministic. It only fires for a secret-like configuration key containing a literal value, and the tests include a placeholder negative case.”

## 5. Scan the secure fixture

Run the same command against `examples/sample-secure-app` and show zero findings.

## 6. Mention assurance

“The important part is not the number of rules. It is the traceability from a stated security property to a deterministic rule, evidence line, recommendation, and test.”

## 7. State limitations honestly

“It is not a commercial scanner. It does not execute target applications, perform formal verification, build a Java AST, or prove that an application is secure.”
