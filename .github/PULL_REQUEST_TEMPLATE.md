## Description

Fixes #<ISSUE-NUMBER>

Note: Before submitting a pull request, please open an issue for discussion if you are not associated with Google.

## Checklist

- [ ] I have followed [Sample Format Guide](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/main/SAMPLE_FORMAT.md)
- [ ] `pom.xml` parent set to latest `shared-configuration`
- [ ] Appropriate changes to README are included in PR
- [ ] These samples need a new **API enabled** in testing projects to pass (let us know which ones)
- [ ] These samples need a new/updated **env vars** in testing projects set to pass (let us know which ones)
- [ ] **Tests** pass:   `mvn clean verify` **required**
- [ ] **Lint**  passes: `mvn -P lint checkstyle:check` **required**
- [ ] **Static Analysis**:  `mvn -P lint clean compile pmd:cpd-check spotbugs:check` **advisory only**
- [ ] This sample adds a new sample directory, and I updated the [CODEOWNERS file](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/main/.github/CODEOWNERS) with the codeowners for this sample
- [ ] This sample adds a new **Product API**, and I updated the [Blunderbuss issue/PR auto-assigner](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/main/.github/blunderbuss.yml) with the codeowners for this sample 
- [ ] Please **merge** this PR for me once it is approved
