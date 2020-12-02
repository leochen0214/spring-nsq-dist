### deploy release version

```jshelllanguage
mvn release:prepare -Darguments="-DskipTests"

mvn release:perform -DuseReleaseProfile=false
```

### 使用文档
https://47.101.153.224/docs/spring-nsq/1.0.7/reference/htmlsingle/
