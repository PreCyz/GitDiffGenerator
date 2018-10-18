# GitDiffGenerator
To download the latest stable version go [here](https://github.com/PreCyz/GitDiffGenerator/releases/latest).<br />
Program generates one text file containing diff from given repositories. By default it generates diff from last 7 days. Can be run on Windows and Linux.
It handles Git, Mercurial and SVN version control systems. To produce diffs, program executes following commands:<br />
For Git: `git log --patch --all --author=userName --since yyyy-MM-dd --until yyyy-MM-dd`<br />
For Mercurial: `hg log --patch --user userName --date "yyyy-MM-dd to yyyy-MM-dd"`<br />
For SVN: `svn log --diff --search userName --revision {yyyy-MM-dd}:{yyyy-MM-dd}`<br />
<br />
If code is protected by the owner and no code is not allowed inside diff then following commands are executed:<br />
For Git: `git log --decorate     --author=userName --since yyyy-MM-dd --until yyyy-MM-dd`<br />
For Mercurial: `hg log --style changelog --user userName --date "yyyy-MM-dd to yyyy-MM-dd"`<br />
For SVN: `svn log --verbose --search userName --revision {yyyy-MM-dd}:{yyyy-MM-dd}`<br />
<br />
Generated file is uploaded to SharePoint as copyright item for given user. In case of any error, the popup window with relevant message will be shown.<br />
Application logs everything. Logs can be found inside following folder `${APP_HOME}/logs`.
## How to execute
There are few ways to execute this program:
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
If program is executed with commandline parameters and _application.properties_ file at the same time, then setup in _application.properties_ has higher priority.<br />
Generated file name by default is `"year-monthName-week-weekNumber.txt"`. It can be switched to `"yourPrefix-startDate-endDate.txt"` by setting `yourPrefix` to parameter _itemFileNamePrefix_.<br />
You can pass some parameters commandline way and other _application.properties_ way. For instance if you do not want to set your password in _application.properties_, you can pass it as commandline param.
### Params description
_author_ - the user who committed the code. For git it is user name from git config stored under key '_user.name_'.<br />
_committerEmail_ - email of the user who committed the code. For git user email from git config stored under key '_user.email_'.<br />
_codeProtection_ - possible values are `NONE`, `SIMPLE` and `STATEMENT`. Default value is `NONE`. Further explanation [here](https://github.com/PreCyz/GitDiffGenerator#explanation-of-codeprotection-parameter).
_itemPath_ - path where file with git diff should be saved or if `codeProtection` is set as `STATEMENT` then full path to the file with statement.<br />
_projectPath_ - comma separated project paths containing _.git_ folder.<br />
_periodInDays_ - integer number. Default value is 7. It helps define start date of diff calculations. Start date is `now - periodInDays`, end date is now.<br />
_startDate_ - start date of diff given in format `yyyy-MM-dd`.<br />
_endDate_ - end date of diff given in format `yyyy-MM-dd`. By default it is set as now.<br />
_itemFileNamePrefix_ - if given then this value will be used as prefix of the diff file name.<br />
_versionControlSystem_ - possible values are `GIT`, `MERCURIAL`, `SVN`. Default value is `GIT`.<br /><br />
When _periodInDays_ is used together with _startDate_ then _starDate_ has higher priority.<br /><br />

Below parameters are mandatory for toolkit:<br/>
_toolkitUsername_ - user name used to login into SharePoint. Also this value is taken when user's root folder in toolkit is calculated.<br />
_toolkitPassword_ - user password used to log in into SharePoint.<br />
### Explanation of *codeProtection* parameter
* `NONE` - no protection,
* `SIMPLE` - no code can not be shared in anyway but diff can contain headers of changes, 
* `STATEMENT` - not even headers of changes are allowed in diff. File with statement is uploaded to SharePoint instead.
#####
It may be that owner of the code forbids to share the code in anyway but you are allowed to put headers of the changes in the diff.
In that case user should set parameter *codeProtection* to `SIMPLE`.
For `GIT` the `SIMPLE` protection will contain entries as follows:<br />
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
#####
It also may be that owner of the code forbids to share any kind of information about the code and changes, even headers. 
If so then user should upload to SharePoint file with the statement. Application can do it for user if only *codeProtection* parameter is set as `STATEMENT`.
### Sample of setups
**Example 1**<br />
If you want to create diff for _Smeagol Golum_ and _Project1_ from last 7 days then create following setup in your _application.properties_:<br />
```
author=Smeagol Golum
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1
```
*Remember:* Java + Windows == double backslash in the paths! There is no need for double backslashes in Linux. In linux it could be like this `projectPath=/home/Gollum/Git/Project1`
#####
**Example 2**<br />
If you want to create diff for _Project1_ and _Project2_ from last 12 days and for author _Anakin Skywalker_, create following setup in your _application.properties_:<br />
```
author=Anakin Skywalker
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
periodInDays=12
```
*Remember:* Java + Windows == double backslash in the paths!
#####
**Example 3**<br />
If you want to create diff for _Project1_ and _Project2_ from _1st of June 2018_ to _30th of June 2018_ and you know only email of committer `BB8@death.star`, create following setup in your _application.properties_:<br />
```
committerEmail=BB8@death.star
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
startDate=2018-06-01
endDate=2018-06-30
itemFileNamePrefix=june
```
*Explanation:* set _itemFileNamePrefix=june_ in order to have self explanatory item file name.<br />
*Explanation:* you can also use _author_ and _committerEmail_ together.
#####
**Example 4**<br />
If you want to create **MERCURIAL** diff for _Project1_ and _Project2_ from _1st of June 2018_ to _30th of June 2018_ and you know only email of committer `BB8@death.star`, create following setup in your _application.properties_:<br />
```
committerEmail=BB8@death.star
itemPath=c:\\Path\\to\\git\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Git\\Project2
startDate=2018-06-01
endDate=2018-06-30
itemFileNamePrefix=june
versionControlSystem=MERCURIAL
```
*Explanation:* set _itemFileNamePrefix=june_ (or any other value) in order to have your prefix in the item file name.
#####
**Example 5**<br />
If you want to create SVN diff for author `Kit Fisto` and Project1 which is protected by the owner. But you are allowed to use headers of the changes,
create following setup in your _application.properties_:<br />
```
author=Kit Fisto
itemPath=/home/Vader/Path/to/git/diff/item
projectPath=/home/Vader/Git/Project1
versionControlSystem=SVN
codeProtection=SIMPLE
```
*Explanation:* Java + Linux == simple paths with slashes!<br />
**Example 6**<br />
If you want to create SVN diff for Project1 that is fully protected by the owner, then create following setup in your _application.properties_:<br />
```
itemPath=/home/Vader/Path/to/statement/my-statement.docx
codeProtection=STATEMENT
```
**To all above examples toolkit parameters (_toolkitUsername_ & _toolkitPassword_) has to be set up.** 
### Download
To download the latest stable version go [here](https://github.com/PreCyz/GitDiffGenerator/releases/latest).

##Licence
Free to use.
