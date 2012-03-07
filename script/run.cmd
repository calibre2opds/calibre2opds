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
set _JAVAPROG=JAVA.EXE

if not "%JAVA_HOME%" == "" (
  if exist "%JAVA_HOME%\bin\%_JAVAPROG%" (
    set _JAVACMD=%JAVA_HOME%\bin\%_JAVAPROG%
    goto run_c2o
  )
)

REM Check standard default 32-bit install locations
if exist "%ProgramFiles%\Java\jre6\bin\%_JAVAPROG%" (
  set _JAVACMD=%ProgramFiles%\Java\jre6\bin\%_JAVAPROG%
  goto run_c2o
)
if exist "%ProgramFiles%\Java\jre7\bin\%_JAVAPROG%" (
  set _JAVACMD=%ProgramFiles%\Java\jre7\bin\%_JAVAPROG%
  goto run_c2o
)

REM Check standard default 64-bit install locations
if exist "%ProgramFiles% (x86)\Java\jre6\bin\%_JAVAPROG%" (
  set _JAVACMD=%ProgramFiles% (x86)\Java\jre6\bin\%_JAVAPROG%
  goto run_c2o
)
if exist "%ProgramFiles% (x86)\Java\jre7\bin\%_JAVAPROG%" (
  set _JAVACMD=%ProgramFiles% (x86)\Java\jre7\bin\%_JAVAPROG%
  goto run_c2o
)


echo                                  WARNING
echo
echo Unable to determine where the %JAVAPROG% program (which is part of Java) has
echo been installed on your system.  Java does not appear to have been installed
echo in any of the default locations.
echo '
echo The recommended way to specify the location of Java is to set the JAVA_HOME
echo environment variable. Normally the value of JAVA_HOME would be set to something
echo like:
echo   JAVA_HOME=C:\Program Files\Java\jre6
echo although it could well vary on your system (particularily if you are running
echo Windows 7 or if you have installed the Java Development Kit.  Within that
echo folder there should be a folder called 'bin' and the %_JAVAPROG% program will
echo be located inside that.
echo '
echo If you press ENTER then calibre2opds will hope that the %_JAVAPROG% program can
echo found on the system search path and will continue on that basis.   If you do not
echo want to do this then use CTRL-C to abandon trying to start calibre2opds.

pause

set _JAVACMD=JAVA.EXE
%JAVAPROG%

:no64bit
REM not found so hope on search path
set _JAVACMD=%_JAVAPROG%

:run_c2o
REM  We set stack limits explicitly here to get consistency across systems
REM -Xms<value> define starting size
REM -Xmx<value> defines maximum size
REM -Xss<value> defines stack size
REM It is possible that for very large libraries this may not be enough - we will have to see.
"%_JAVACMD%" -Xms128m -Xmx512m -cp OpdsOutput-3.0-SNAPSHOT.jar Cli %*

set _JAVACMD=
set _JAVAPROG=

pause

