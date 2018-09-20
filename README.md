# GitDiffGenerator
Generates git diff file from given repositories. By default it generates from last 7 days till now. Can be run on Windows and Linux (from v2.0).
To download latest version go [here](https://github.com/PreCyz/GitDiffGenerator/releases/latest).

## How to execute
There few ways of execution this program:
- from command line java -jar with params,
- from command line java -jar + application.properties at the same location as _*.jar_ file,
- double click on *.jar file (no window will open, but you will see diff file at given item path),

### java -jar with params
For *Windows*
```
java -jar Gipter.jar author="Anakin Skywalker" itemPath="c:\\Path\\to\\git\\diff\\item" 
projectPath="c:\\Path\\to\\git\\project1,c:\\Path\\to\\git\\project" 
gitBashPath="C:\\Program Files\\Git\\bin\\bash.exe"
```

For *Linux*
```
java -jar Gipter.jar author="Anakin Skywalker" itemPath="/home/wall-e/Path/to/git/diff/item"
projectPath="/home/eva/Path/to/git/project1,/home/eva/Path/to/git/project2"
```

### java -jar + application.properties at at the same location as jar file
If there are _application.properties_ file at the same location as your jar file then program can be run as follows:

```java -jar Gipter.jar```

#### Tip
If one runs program with parameters and _application.properties_ at the same time, then setup in _application.properties_ has higher priority.

Generated file name by default is
```String.format("%s-week-%d.txt", now.getMonth().name(), weekNumber).toLowerCase()```.
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

### Sample setup

#### Windows
**Example 1**

If you want to create diff for Smeagol Golum and Project1 from last 7 days then create following setup in your _application.properties_:

```
author=Smeagol Golum
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1
gitBashPath=C:\\Path\\to\\Git\\bash.exe
```
#####
**Example 2**

If you want to create diff for Project1 and Project2 for last 12 days for _Anakin Skywalker_, create following setup in your _application.properties_:

```
author=Anakin Skywalker
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
gitBashPath=C:\\Path\\to\\Git\\bash.exe
minusDays=12
```
*Remember:* Java + Windows == double backslash in the paths!
#####
**Example 3**

If you want to create diff for Project1 and Project2 from 1st of June 2018 to 30th of June 2018 and you know only email of committer ```BB8@death.star```, create following setup in your _application.properties_:

```
committerEmail=BB8@death.star
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
gitBashPath=C:\\Path\\to\\Git\\bash.exe
startDate=2018/06/01
endDate=2018/06/30
itemFileName=diff
```
*Explanation:* set _itemFileName=diff_ in order to have self explanatory item file name.
*Explanation:* you can also use _author_ and _committerEmail_ together.

#### Linux
The setup for linux is similarly.

**Example 1**

If you want to create diff for Project1 and Project2 for last 26 days for jedi master _Ki Adi Mundi_, create following setup in your _application.properties_:

```
author=Ki Adi Mundi
itemPath=/home/Vader/Path/to/git/diff/item
projectPath=/home/Vader/Mustafar/Project1,/home/Vader/Mustafar/Project2
minusDays=26
```
*Explanation 1* Java + Linux == simple paths with slashes!
*Explanation 2* For Linux there is no need to set _gitBashPath_. Program uses built-in bash.
#####
**Example 2**

If you want to create diff for Project1 and Project2 from 1st of June 2018 to 30th of June 2018 and you know only email of committer ```BB8@death.star```,
create following setup in your _application.properties_:

```
committerEmail=BB8@death.star
itemPath=/home/Vader/Path/to/git/diff/item
projectPath=/home/Vader/Git/Project1,/home/Vader/Git/Project2
startDate=2018/06/01
endDate=2018/06/30
itemFileName=diff
```
*Explanation:* set _itemFileName=diff_ (or any other value) in order to have self explanatory item file name.

### Download
To download latest version go [here](https://github.com/PreCyz/GitDiffGenerator/releases/latest).
