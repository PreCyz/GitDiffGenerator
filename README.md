# GitDiffGenerator
Generates git diff file from given repositories. By default it generates from last 7 days till now. Can be run on Windows and Linux (from v2.0). 

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
Generated file name by default is ```String.format("%s-week-%d.txt", now.getMonth().name(), weekNumber).toLowerCase()```. 
It can be switched to ```String.format("diff-%s-%s.txt", startDate, endDate)``` by setting any value to parameter _itemFileName_.

### Params description

_author_ - the git user who committed the code, user name from git config stored under key 'user.name'.

_committerEmail_ - email of the user who committed the code, user email from git config stored under key 'user.email'.

_itemPath_ - path where file with git diff should be saved.

_projectPath_ - comma separated project paths containing _.git_ folder.

_gitBashPath_ - path to git bash. Mandatory for Windows platform.

_minusDays_ - when to start calculating git diff given in days. Default value is 7.

_startDate_ - start date of diff given in format yyyy/MM/dd.

_endDate_ - end date of diff given in format yyyy/MM/dd.

_itemFileName_ - if given then different item file name will be produced. By default item file name is ```String.format("%s-week-%d.txt", now.getMonth().name(), weekNumber).toLowerCase()```
