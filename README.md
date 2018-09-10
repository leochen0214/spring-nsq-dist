### deploy release version

```jshelllanguage
mvn release:prepare -Darguments="-DskipTests"

mvn release:perform -DuseReleaseProfile=false
```