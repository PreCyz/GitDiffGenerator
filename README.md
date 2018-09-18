# GitDiffGenerator
Generates git diff file from given repositories. By default it generates from last 7 days till now.

## How to execute
There are 2 ways of execution this program:
- java -jar with params
- java -jar + application.properties at the same location as _*.jar_ file

### java -jar with params

```
java -jar Gipter.jar author="Anakin Skywalker" itemPath="c:\\Path\\to\\git\\diff\\item" 
projectPath="c:\\Path\\to\\git\\project1,c:\\Path\\to\\git\\project" 
gitBashPath="C:\\Program Files\\Git\\bin\\bash.exe"
```
### java -jar + application.properties at at the same location as jar file
If there are _application.properties_ file at the same location as your jar file then program can be run as follows:

```java -jar Gipter.jar```

Sample application.properties:

```
author=Anakin Skywalker
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
gitBashPath=C:\\Path\\to\\Git\\bash.exe
```
#### Tip
If one runs program with parameters and _application.properties_ at the same time, then configuration from file has higher priority. 

### Params description

_author_ - the git user who committed the code.

_itemPath_ - path where file with git diff should be saved.

_projectPath_ - comma separated project paths containing _.git_ folder.

_gitBashPath_ - path to git bash.

_minusDays_ - when to start calculating git diff given in days. Default value is 7.
