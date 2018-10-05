# GitDiffGenerator
To download latest version go [here](https://github.com/PreCyz/GitDiffGenerator/releases/latest).<br />
Program generates one text file containing diff from given repositories. By default it generates diff from last 7 days. Can be run on Windows and Linux (from v2.0).
It handles Git and Mercurial version control systems. In order to produce diff program executes following commands:<br />
For Git: `git log -p --all --author=userName --since yyyy/MM/dd --until yyyy/MM/dd`<br />
For Mercurial: `hg log -p --user userName --date "yyyy-MM-dd to yyyy-MM-dd"`
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
```
For *Linux*
```
java -jar Gipter.jar author="Anakin Skywalker" itemPath="/home/wall-e/Path/to/git/diff/item"
projectPath="/home/eva/Path/to/git/project1,/home/eva/Path/to/git/project2"
```
### java -jar + application.properties at at the same location as jar file
If there are _application.properties_ file at the same location as your jar file then program can be run as follows: `java -jar Gipter.jar`
#### Tip
If one runs program with parameters and _application.properties_ at the same time, then setup in _application.properties_ has higher priority.<br />
Generated file name by default is ```"monthName-week-weekNumber.txt"```. It can be switched to ```"diff-startDate-endDate.txt"``` by setting any value to parameter _itemFileName_.
### Params description
_author_ - the git user who committed the code, user name from git config stored under key 'user.name'.<br />
_committerEmail_ - email of the user who committed the code, user email from git config stored under key 'user.email'.<br />
_itemPath_ - path where file with git diff should be saved.<br />
_projectPath_ - comma separated project paths containing _.git_ folder.<br />
_gitBashPath_ - path to git bash. Mandatory for Windows platform. **THIS PARAMETER IS NOT NEED IN VERSIONS 2.2+**<br />
_minusDays_ - when to start calculating git diff given in days. Default value is 7.<br />
_startDate_ - start date of diff given in format yyyy/MM/dd.<br />
_endDate_ - end date of diff given in format yyyy/MM/dd.<br />
_itemFileName_ - if given then different item file name will be produced. By default item file name is `String.format("%s-week-%d.txt", now.getMonth().name(), weekNumber).toLowerCase()`<br />
_versionControlSystem_ - default value is `GIT`. If user wants to generate diff from Mercurial then has to set this parameter as `MERCURIAL`.<br />
_codeProtected_ - default value is `false`. If code is protected and can not be shared in anyway, then this parameter should be set as `true`.
### Sample setup
#### Windows
**Example 1**<br />
If you want to create diff for Smeagol Golum and Project1 from last 7 days then create following setup in your _application.properties_:<br />
```
author=Smeagol Golum
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1
gitBashPath=C:\\Path\\to\\Git\\bash.exe
```
#####
**Example 1** *for version 2.2+*<br />
If you want to create diff for Smeagol Golum and Project1 from last 7 days then create following setup in your _application.properties_:<br />
```
author=Smeagol Golum
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1
```
#####
**Example 2**<br />
If you want to create diff for Project1 and Project2 for last 12 days for _Anakin Skywalker_, create following setup in your _application.properties_:<br />
```
author=Anakin Skywalker
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
gitBashPath=C:\\Path\\to\\Git\\bash.exe
minusDays=12
```
*Remember:* Java + Windows == double backslash in the paths!
#####
**Example 2** *for version 2.2+*<br />
If you want to create diff for Project1 and Project2 for last 12 days for _Anakin Skywalker_, create following setup in your _application.properties_:<br />
```
author=Anakin Skywalker
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
minusDays=12
```
#####
**Example 3**<br />
If you want to create diff for Project1 and Project2 from 1st of June 2018 to 30th of June 2018 and you know only email of committer ```BB8@death.star```, create following setup in your _application.properties_:<br />
```
committerEmail=BB8@death.star
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
gitBashPath=C:\\Path\\to\\Git\\bash.exe
startDate=2018/06/01
endDate=2018/06/30
itemFileName=diff
```
*Explanation:* set _itemFileName=diff_ in order to have self explanatory item file name.<br />
*Explanation:* you can also use _author_ and _committerEmail_ together.
#####
**Example 3** *for version 2.2+*<br />
If you want to create diff for Project1 and Project2 from 1st of June 2018 to 30th of June 2018 and you know only email of committer ```BB8@death.star```, create following setup in your _application.properties_:<br />
```
committerEmail=BB8@death.star
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
gitBashPath=C:\\Path\\to\\Git\\bash.exe
startDate=2018/06/01
endDate=2018/06/30
itemFileName=diff
```
#####
**Example 4**<br />
If you want to create **MERCURIAL** diff for Project1 and Project2 from 1st of June 2018 to 30th of June 2018 and you know only email of committer ```BB8@death.star```, create following setup in your _application.properties_:<br />
```
committerEmail=BB8@death.star
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
gitBashPath=C:\\Path\\to\\Git\\bash.exe
startDate=2018/06/01
endDate=2018/06/30
itemFileName=diff
versionControlSystem=MERCURIAL
```
#### Linux
The setup for linux is similarly.<br />

**Example 1**<br />
If you want to create diff for Project1 and Project2 for last 26 days for jedi master _Ki Adi Mundi_, create following setup in your _application.properties_:<br />
```
author=Ki Adi Mundi
itemPath=/home/Vader/Path/to/git/diff/item
projectPath=/home/Vader/Mustafar/Project1,/home/Vader/Mustafar/Project2
minusDays=26
```
*Explanation:* Java + Linux == simple paths with slashes!<br />
*Explanation:* For Linux there is no need to set _gitBashPath_. Program uses built-in bash.
#####
**Example 2**<br />
If you want to create diff for Project1 and Project2 from 1st of June 2018 to 30th of June 2018 and you know only email of committer ```BB8@death.star```,
create following setup in your _application.properties_:<br />
```
committerEmail=BB8@death.star
itemPath=/home/Vader/Path/to/git/diff/item
projectPath=/home/Vader/Git/Project1,/home/Vader/Git/Project2
startDate=2018/06/01
endDate=2018/06/30
itemFileName=diff
```
*Explanation:* set _itemFileName=diff_ (or any other value) in order to have self explanatory item file name.
#####
**Example 3**<br />
If you want to create MERCURIAL diff for Project1 from last 7 days for user ```Kit Fisto```,
create following setup in your _application.properties_:<br />
```
author=Kit Fisto
itemPath=/home/Vader/Path/to/git/diff/item
projectPath=/home/Vader/Git/Project1
versionControlSystem=MERCURIAL
```
### Explanation of *codeProtected* parameter
It may be that owner of the code forbids to share the code in anyway. If so, then it can not be attached to regular diff.
In that case user should set parameter *codeProtected* to `true`. When *true* is set then no code is attached to diff.
For `GIT` the protected diff will contain entries as follows:<br />
```
commit 32d111bf4483264e6a6bd89422b5b7b60e39bee7 (HEAD -> master)
Author: Anton Gorodecki <dniewnoj@dozor.fantasy>
Date:   Fri Oct 5 20:54:44 2018 +0200

    Introducing StringUtils class.
```
No code! Just information about the change:
 - who,
 - when,
 - commit hash,
 - branch name,
 - commit message.

### Download
To download latest version go [here](https://github.com/PreCyz/GitDiffGenerator/releases/latest).
