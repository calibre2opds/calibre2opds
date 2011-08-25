
@echo OFF
REM  batch file for running the Calibre2Opds program in CLI mode.
REM
REM  The checks for Java have been adapted from those used to
REM  start up the IzPack package builder.   Hopefully this means
REM  that Java may run on some of those systems that do not
REM  have Java on the search path but do have the JAVA_HOME
REM  environment variable set or have installed to default location.

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto run_c2o

:noJavaHome
REM Check standard default 32-bit install locations
if not exist "%ProgramFiles%\Java\jre6\bin\javaw.exe" goto check64bit
set _JAVACMD=%ProgramFiles%\Java\jre6\bin\javaw.exe
goto run_c2o

:check64bit
REM Check standard default 64-bit install locations
if not exist "%ProgramFiles% (x86)\Java\jre6\bin\javaw.exe" goto no64bit
set _JAVACMD=%ProgramFiles% (x86)\Java\jre6\bin\javaw.exe
goto run_c2o

:no64bit
REM not found so hope on search path
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:run_c2o

"%_JAVACMD%" -cp OpdsOutput-2.5-SNAPSHOT.jar Cli %*
goto end

:end
set _JAVACMD=

pause

