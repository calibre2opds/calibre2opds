@echo OFF
REM  batch file for running the Calibre2Opds program in GUI mode.
REM
REM  The checks for Java have been adapted from those used to
REM  start up the IzPack package builder.   Hopefully this means
REM  that Java may run on some of those systems that do not
REM  have Java on the search path but do have the JAVA_HOME
REM  environment variable set or have installed to default location.

:checkJava
set _JAVACMD=%JAVACMD%
if "%1"=="-enableassertions" (
  set JAVAPROG=java.exe
)  else (
  set JAVAPROG=javaw.exe
)

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\%JAVAPROG%" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\%JAVAPROG%
goto run_c2o

:noJavaHome
REM Check standard default 32-bit install locations
if not exist "%ProgramFiles%\Java\jre6\bin\%JAVAPROG%" goto check64bit
set _JAVACMD=%ProgramFiles%\Java\jre6\bin\%JAVAPROG%
goto run_c2o

:check64bit
REM Check standard default 64-bit install locations
if not exist "%ProgramFiles% (x86)\Java\jre6\bin\%JAVAPROG%" goto no64bit
set _JAVACMD=%ProgramFiles% (x86)\Java\jre6\bin\%JAVAPROG%
goto run_c2o

:no64bit
REM not found so hope on search path
if "%_JAVACMD%" == "" set _JAVACMD=%JAVAPROG

:run_c2o
REM  We set stack limits explicitly here to get consistency across systems
REM -Xms<value> define starting size
REM -Xmx<value> defines maximum size
REM -Xss<value> defines stack size
REM It is possible that for very large libraries this may not be enough - we will have to see.
REM If these options are omitted then defaults are chosen depending on system configuration
if "%1"=="-enableassertions" (
  cls
  echo "----------------------"
  echo " Calibre2Opds STARTED "
  echo "----------------------"
  echo '
  "%_JAVACMD%" -Xms128m -Xmx512m  -enableassertions -cp OpdsOutput-3.0-SNAPSHOT.jar Gui
  echo '
  echo "-----------------------"
  echo " Calibre2Opds FINISHED "
  echo "-----------------------"
)  else (
  START "Calibre2Opds" "%_JAVACMD%" -Xms128m -Xmx512m -cp OpdsOutput-3.0-SNAPSHOT.jar Gui
)
goto end

:end
set _JAVACMD=
