# GitDiffGenerator
To download the latest stable version go [here](https://github.com/PreCyz/GitDiffGenerator/releases/latest).<br />
Program generates one text file containing diff from given repositories. By default it generates diff from last 7 days. Can be run on Windows and Linux.
It handles Git, Mercurial and SVN version control systems. To produce diffs, program executes following commands:<br />
For Git: `git log --remotes=origin --patch --author=userName --since yyyy-MM-dd --until yyyy-MM-dd`<br />
For Mercurial: `hg log --patch --user userName --date "yyyy-MM-dd to yyyy-MM-dd"`<br />
For SVN: `svn log --diff --search userName --revision {yyyy-MM-dd}:{yyyy-MM-dd}`<br />
<br />
If code is protected by the owner and no code is not allowed inside diff then following commands are executed:<br />
For Git: `git log --remotes=origin --oneline --author=userName --since yyyy-MM-dd --until yyyy-MM-dd`<br />
For Mercurial: `hg log --style changelog --user userName --date "yyyy-MM-dd to yyyy-MM-dd"`<br />
For SVN: `svn log --verbose --search userName --revision {yyyy-MM-dd}:{yyyy-MM-dd}`<br />
<br />
Generated file is uploaded to SharePoint as copyright item for given user. In case of any error, the popup window with relevant message will be shown.<br />
Application logs everything. Logs can be found inside following folder `${APP_HOME}/logs`.
## How to execute
There are few ways to execute this program:
- from command line java -jar with params,
- from command line java -jar + application.properties at the same location as _*.jar_ file,
- double click on *.jar file will run the application in UI mode and this is the default mode, description of user interface can be found [here](https://github.com/PreCyz/GitDiffGenerator/tree/master/docs)
### java -jar with params
For *Windows*
```
java -jar Gipter.jar useUI=N author="Anakin Skywalker" itemPath="c:\\Path\\to\\git\\diff\\item" 
projectPath="c:\\Path\\to\\git\\project1,c:\\Path\\to\\git\\project"
```
For *Linux*
```
java -jar Gipter.jar useUI=N author="Anakin Skywalker" itemPath="/home/wall-e/Path/to/git/diff/item"
projectPath="/home/eva/Path/to/git/project1,/home/eva/Path/to/git/project2"
```
### java -jar + application.properties at at the same location as jar file
If there are _application.properties_ file at the same location as your jar file then program can be run as follows: `java -jar Gipter.jar useUI=N`
#### Tips
If program is executed with commandline parameters and _application.properties_ file at the same time, then setup from commandline has higher priority. Unless `preferredArgSource` is set to `FILE`, then _application.properties_ has higher priority.<br />
Generated file name by default is `year-monthName-week-weekNumber.txt`. If diff is generated with end date set in the past then diff file name is `year-monthName-startDate-endDate.txt`.
If parameter _itemFileNamePrefix_ is set then its value is added at the front of the file name. File name (without extension) is used also as the title of the toolkit item, that is created.<br />
You can pass some parameters commandline way and other _application.properties_ way. For instance if you do not want to set your password in _application.properties_, you can pass it as commandline param.<br/>
When application is executed with `uploadType = TOOLKIT_DOCS', then item file is a _zip_ file. It is because there could be more then one document created or changed by the user. 
### Params description
**configurationName** - Each defined configuration has its own unique name. This parameter is the unique name of the configuration.<br/>
**author** - comma separated users who committed the code.<br />
**committerEmail** - email of the user who committed the code. For git user email from git config stored under key '_user.email_'.<br />
**uploadType** - possible values are `SIMPLE`, `PROTECTED` and `STATEMENT`. Default value is `SIMPLE`. Further explanation [here](https://github.com/PreCyz/GitDiffGenerator#explanation-of-codeprotection-parameter).<br />
**itemPath** - path where file with git diff should be saved or if `uploadType` is set as `STATEMENT` then full path to the file with statement.<br />
**projectPath** - comma separated project paths containing _.git_, _.svn_ or _.hg_ folders.<br />
**periodInDays** - integer number. Default value is 7. It helps define start date of diff calculations. Start date is `now - periodInDays`, end date is now.<br />
**startDate** - start date of diff given in format `yyyy-MM-dd`.<br />
**endDate** - end date of diff given in format `yyyy-MM-dd`. By default it is set as now.<br />
**itemFileNamePrefix** - if given then this value will be used as prefix of the diff file name.<br />
**useAsFileName** - when set as `Y` then `itemFileNamePrefix` is used as file name for produced item. Default value is `N`.<br />
**gitAuthor** - author specific for git repository stored at git config under key '_user.name_'. When used together with _author_, this parameter has higher priority.<br />
**mercurialAuthor** - author specific for mercurial repository. When used together with _author_, this parameter has higher priority.<br />
**svnAuthor** - author specific for svn repository. When used together with _author_, this parameter has higher priority.<br />
**confirmationWindow** - if parameter set as `Y` then confirmation window will be displayed after successful upload. Default value is `N`.<br />
**preferredArgSource** - if parameter set as `FILE` then arguments from _application.properties_ will be resolved as first. Default value is `CLI`. This parameter can be changed only from command line.<br />
**skipRemote** - if parameter set as `N` then git diff will be generated only from origin (`--remotes=origin`). If set as `Y` then git diff will be generated only from local git repository. Default value is `Y`.<br />
**useUI** - if parameter set as `N` application will be launched in command line mode. If set as `Y` then UI mode is launched. Default value is `Y`.<br />
**activeTray** - used in UI, tells if tray icon should be active or not. Default value is `Y`.<br/>
**silentMode** - used in UI, tells if application should be executed and located directly in tray icon. It is used by startup functionality. Default value is `N`.<br/>
**enableOnStartup** - used in UI, enables application on system start up, if value on  is `Y` then application will be launched on system start.<br/>
**loggerLevel** - If given then all loggers in the application has that logger level.<br/>

Below parameters are mandatory for toolkit:<br/>
**toolkitUsername** - user name used as a login to SharePoint. Also this value is taken when user's root folder in toolkit is calculated.<br />
**toolkitPassword** - user password used to log in into SharePoint.<br /><br />
**toolkitProjectListNames** - comma separated names of the folders to scan on toolkit, when looking for changes in documents made by user. Default value is `Deliverables`.<br/>
**deleteDownloadedFiles** - if `Y` then all downloaded files from toolkit will be downloaded afterwards. This parameter works together with upload type `TOOLKIT_DOCS`<br/>

_Note:_ When `periodInDays` is used together with `startDate` then **startDate** has higher priority.
### Explanation of *uploadType* parameter
* `SIMPLE` - full git diff is generated and uploaded,
* `PROTECTED` - no code can not be shared in anyway but diff can contain headers of changes, 
* `STATEMENT` - not even headers of changes are allowed in diff. File with statement is uploaded to SharePoint instead.
* `TOOLKIT_DOCS` - scanning given project documentation to find documents that were changed by the user.
### Explanation of *preferredArgSource* parameter
* `CLI` - commandLine arguments will be used as first then missing parameters will be read from _application.properties_,
* `FILE` - arguments from _application.properties_ will be used as first then missing parameters will be read from commandLine arguments.
* `UI` - it is used by the UI. Arguments from _ui-application.properties_ will be used as first then missing parameters will be read from commandLine arguments.
### Explanation of *TOOLKIT_DOCS* upload type
This option should be used by the users that do not create code, but work with project documentation. If this is the case then copyright items are created from documentations produced by the user.<br/> 
How the application handles such a case? Documentation of the project is kept on SharePoint in different folders. In order to extract the changes from these documents made by the user,<br/>
application will scan the documents from given folders within given date range (_startDate and endDate_). Because of the nature of SharePoint, the produced item will consist pair of documents before and after the change made by the user.<br/>
The example will explain what exactly item consists.
#####
**Toolkit docs Case**<br />
The user is Yoda. Documents are located in 2 folders: _Deliverables_ and _DocumentLibrary_. We want to generate item for date range `now - 30 days` and `now`. The history of documents is as follows:<br/>
<br/>Deliverable-doc:<br/>

Modified by | Date | Version
----------- | ---- | -------
Yoda | now - 40 days | 1.0
Kit Fisto | now - 35 days | 2.0
Yoda | now - 25 days | 3.0
Yoda | now - 15 days | 4.0
Obi-Wan Kenobi | now - 13 days | 5.0
Obi-Wan Kenobi | now - 12 days | 6.0
Yoda | now - 2 days | 7.0
Ashoka Tano | now - 1 days | 9.0

<br/>DocumentLibrary-doc:<br/>

Modified by | Date | Version
----------- | ---- | -------
Mace Windu | now - 15 days | 1.0
Kit Fisto | now - 14 days | 2.0
Ashoka Tano | now - 6 days | 3.0
Anakin Skywalker| now - 5 days | 4.0
Shak Ti | now - 4 days | 5.0
Obi-Wan Kenobi | now - 3 days | 6.0
Yoda | now - 2 days | 7.0

The item will contains following documents:
_Deliverable-doc-2.0, Deliverable-doc-4.0, Deliverable-doc-6.0, Deliverable-doc-7.0_ - all document before change and Yoda's last change.<br/>
_DocumentLibrary-doc-6.0, DocumentLibrary-doc-7.0_ - document before Yoda's change and the last change made by Yoda.
All these files will be zipped into one file and uploaded as one item.
#####
**Case 1**<br />
It may be that owner of the code forbids to share the code in anyway but you are allowed to put headers of the changes in the diff.
In that case user should set parameter *uploadType* to `PROTECTED`.
For `GIT` the `PROTECTED` protection will contain entries as follows:<br />
```
a42212890 Cleaning code.
5cfc248ad Fixing that balck hole.
```
No code! Just:
 - who - optional,
 - when - optional,
 - commit hash - mandatory,
 - branch name - optional,
 - commit message - mandatory.
#####
**Case 2**<br />
It also may be that owner of the code forbids to share any kind of information about the code and changes, even headers.
If so then user should upload to SharePoint file with the statement. Application can do it for user if only *uploadType* parameter is set as `STATEMENT`.
### Toolkit
To setup toolkit details only two parameters has to be set: _toolkitUsername_ and _toolkitPassword_. If parameters are not set the application will generate diff file and report an error.
Popup window will be displayed and in the logs there will be relevant entry. If parameters are set then new item in the toolkit will be created. New item will contain attachment, which is generated
file with diffs from repositories. Below are details of that item:<br />
**Title**: _diff file name_<br />
**Submission date**: _endDate + LocalTime.now()_<br />
**Classification**: Changeset (repository change report)<br />
**Body**: _vcsName_ diff file     or     STATEMENT diff<br />
### Sample of setups
**Example 1**<br />
If you want to create diff for _Smeagol Golum_ and _Project1_ from last 7 days then create following setup in your _application.properties_:<br />
```
author=Smeagol Gollum
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
#####
**Example 3**<br />
If you want to create diff for _Project1_, _Project2_ and _Project3_ from _1st of June 2018_ to _30th of June 2018_ for authors _Ezra Bridger_ and _Kanan Jarrus_, create following setup in your _application.properties_:<br />
```
author=Ezra Bridger,Kanan Jarrus
itemPath=c:\\Path\\to\\diff\\item
projectPath=c:\\Git\\Project1,c:\\Svn\\Project2,c:\\hg\\Project3
startDate=2018-06-01
endDate=2018-06-30
itemFileNamePrefix=june
```
*Explanation:* set _itemFileNamePrefix=june_ in order to have file name starts with _june_.<br />
#####
**Example 4**<br />
If you want to create diff for _Git-Project_ with author _Shak Ti_, _Svn-Project_ with author _Starkiller_ and _Hg-Project_ with author _Juno_ from _1st of June 2018_ to _30th of June 2018_ for authors _Ezra Bridger_ and _Kanan Jarrus_, create following setup in your _application.properties_:<br />
```
gitAuthor=Shak Ti
svnAuthor=Starkiller
mercurialAuthor=Juno
itemPath=c:\\Path\\to\\diff\\item
projectPath=c:\\Git-Project,c:\\Svn-Project,c:\\Hg-Project
startDate=2018-06-01
endDate=2018-06-30
itemFileNamePrefix=june
```
#####
**Example 5**<br />
If you want to create SVN diff for author _Kit Fisto_ and _Project1_ which is protected by the owner, but you are allowed to use headers of the changes,
create following setup in your _application.properties_:<br />
```
svnAuthor=Kit Fisto
itemPath=/home/Vader/Path/to/diff/item
projectPath=/home/Vader/SVN/Project1
uploadType=SIMPLE
```
*Explanation:* Java + Linux == simple paths with slashes!<br />
**Example 6**<br />
If you want to create SVN diff for Project1 that is fully protected by the owner, then create following setup in your _application.properties_:<br />
```
itemPath=/home/Vader/Path/to/statement/my-statement.docx
uploadType=STATEMENT
```
**To all above examples toolkit parameters (_toolkitUsername_ & _toolkitPassword_) has to be set up.** 
### Download
To download the latest stable version go [here](https://github.com/PreCyz/GitDiffGenerator/releases/latest).

### License
MIT - please read the file [LICENSE](https://github.com/PreCyz/GitDiffGenerator/blob/master/LICENSE).
