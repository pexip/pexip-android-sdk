# Releasing

1. Pick the next version name (e.g `1.2.3`)
2. Update `gradle.properties` `version` to the next version
3. Replace current version name in `README.md` and `bug_report.yml`
4. Add relevant changes to `CHANGELOG.md` and an entry for the next version
5. Create and merge a pull request with the commit message `release: 1.2.3`
6. Update `gradle.properties` `version` with the next snapshot version (e.g. `1.2.4-SNAPSHOT`)
7. Create and merge a pull request with the commit message
   `snapshot: prepare next development iteration`
