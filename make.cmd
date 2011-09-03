@echo OFF
rem		calibre2opds make.cmd
rem		~~~~~~~~~~~~~~~~~~~~~
rem	Batch file to build the calibre2opds program
rem	when running under Windows.. 
rem
rem	It checks if the environment variables that are required
rem	to get calibre2opds to compile correctly have been
rem	set up correctly.
rem
rem	You can either set up the environment variables
rem	explicitly at the start of this file or do it by
rem	setting them at the System Properties level
rem
rem	The folloing might be typical settings:
rem
rem	set M2_HOME=C:\Program Files\Maven
rem	set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_27
rem	set SVN_HOME=C:\Program Files\SubVersion


set C2O_ENV=
cls
echo Build calibre2opds
echo ~~~~~~~~~~~~~~~~~~
:CHECK_M2_HOME
if "%M2_HOME%" == "" (
	echo ERROR: The M2_HOME environe=ment variable is not set.
	echo        This needs to point to where Maven has been installed.
	echo        A typical value might be something like:
	echo        set M2_HOME=C:\Program Files\Maven
	set C2O_ENV=BAD
	goto CHECK_JAVA_HOME
)
if NOT EXIST "%M2_HOME%" (
	echo M2_HOME=%M2_HOME%
	echo ERROR: Maven not found at location specified by M2_HOME
	set C2O_ENV=BAD
	goto CHECK_JAVA_HOME
)
PATH=%PATH%;%M2_HOME%\bin

:CHECK_JAVA_HOME
if "%JAVA_HOME" == "" (
	echo ERROR: The JAVA_HOME environment variable is not set.
	echo        This needs to point to where the Java SDK has been installed.
	echo        A typical value might be something like:
	echo        set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_27
	set C2O_ENV=BAD
	goto CHECK_SVN_HOME
)
if NOT EXIST "%JAVA_HOME%" (
	echo JAVA_HOME=%JAVA_HOME%
	echo ERROR: Java SDK not found at location specified by JAVA_HOME
	set C2O_ENV=BAD
	goto CHECK_SVN_HOME
)
PATH=%PATH%;%JAVA_HOME%\bin

:CHECK_SVN_HOME
if "%SVN_HOME%" == "" (
	echo ERROR: The SVN_HOME environment variable is not set.
	echo        This needs to point to where Subversion has been installed.
	echo        A typical value might be something like:
	echo        set SVN_HOME=C:\Program Files\SubVersion
	set C2O_ENV=BAD
	goto CHECK_OK
)
if NOT EXIST "%SVN_HOME%" (
	echo SVN_HOME=%SVN_HOME%
	echo ERROR: Subversion not found at location specified by SVN_HOME
	set C2O_ENV=BAD
	goto CHECK_OK
)
if NOT EXIST "%SVN_HOME%\bin\svnversion.exe" (
	echo SVN_HOME=%SVN_HOME%
	echo ERROR: Subversion 'svnversion' command not found.
	echo        You need to install an implementation that provides
	echo        this as a command line utility.  In particular it
	echo        it is not included with TortoiseSVN.
	set C2O_ENV=BAD
	goto CHECK_OK
)
PATH=%PATH%;%SVN_HOME%\bin

:CHECK_OKset

if NOT "%C2O_ENV%" == "" (
	echo ------ ERRORS IN ENVIRONMENT SETUP -------
	echo Please correct errors listed and try again
	pause
	exit /b 1
)

echo Dependency Checks OK

echo Started compile at %TIME%
mvn clean install dependency:copy-dependencies
echo Completed at %TIME%
pause
exit 0
