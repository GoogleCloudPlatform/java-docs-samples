## Description

Fixes #<ISSUE-NUMBER>

## Checklist

### Testing

- [ ] **I have tested this change on a live environment and verified it works as intended.**
- [ ] **Tests** pass:   `mvn clean verify` **required**
- [ ] **Lint**  passes: `mvn -P lint checkstyle:check` **required**
- [ ] **Static Analysis**:  `mvn -P lint clean compile pmd:cpd-check spotbugs:check` **advisory only**

### Compliance & Style

- [ ] I have followed [Sample Format Guide](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/main/SAMPLE_FORMAT.md)
- [ ] `pom.xml` parent set to latest `shared-configuration`
- [ ] Appropriate changes to README are included in PR
- [ ] This sample adds a new sample directory, and I updated the [CODEOWNERS file](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/main/.github/CODEOWNERS) with the codeowners for this sample
- [ ] This sample adds a new **Product API**, and I updated the [Blunderbuss issue/PR auto-assigner](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/main/.github/blunderbuss.yml) with the codeowners for this sample

### Post-Approval Actions

- [ ] Please **merge** this PR for me once it is approved
