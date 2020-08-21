Fixes #issue

> It's a good idea to open an issue first for discussion.

- [ ] I have followed [Sample Format Guide](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/SAMPLE_FORMAT.md)
- [ ] `pom.xml` parent set to latest `shared-configuration`
- [ ] Appropriate changes to README are included in PR
- [ ] API's need to be enabled to test (tell us)
- [ ] Environment Variables need to be set (ask us to set them)
- [ ] **Tests** pass:   `mvn clean verify` **required**
- [ ] **Lint**  passes: `mvn -P lint checkstyle:check` **required**
- [ ] **Static Analysis**:  `mvn -P lint clean compile pmd:cpd-check spotbugs:check` **advisory only**
- [ ] Please **merge** this PR for me once it is approved.
