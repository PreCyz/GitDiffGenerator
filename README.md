# GitDiffGenerator
Generates git diff file from given repositories by default from last 7 days.

## How to execute
There are 2 ways of execution this program
- java -jar with params
- java -jar + application.properties at the same location as jar file

### java -jar with params

```
java -jar Gipter.jar author="Anakin Skywalker" itemPath="c:\\Path\\to\\git\\diff\\item" projectPath="c:\\Work\\workspace\\spotlanes-backend,c:\\Work\\workspace\\eseago-frontend" gitBashPath="C:\\Program Files\\Git\\bin\\bash.exe"
```

### java -jar + application.properties at at the same location as jar file

If put _application.properties_ file at the same location as your jar file then:

```java -jar Gipter.jar```

Sample application.properties:

```
author=Anakin Skywalker
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
gitBashPath=C:\\Path\\to\\Git\\bash.exe
```

### Params description

_author_ - the git user who committed the code.

_itemPath_ - path where file with git diff should be saved.

_projectPath_ - comma separated project paths containing _.git_ folder.

_gitBashPath_ - path to git bash.

_minusDays_ - when to start calculating git diff given in days.
