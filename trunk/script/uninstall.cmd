@echo OFF
REM  batch file for runinstalling the Calibre2Opds program.
REM
REM  The checks for Java have been adapted from those used to
REM  start up the IzPack package builder and then extended.
REM  Hopefully this means that calibre2opds Java may run on 
REM  some of those systems that do not have Java on the search 
REM  path but do have the JAVA_HOME environment variable set;
REM  or have the expected registry keys set; or have installed 
REM  to default location.

:checkJava
set _JAVAPROG=JAVAW.EXE

REM See if JAVA_HOME specifies location of Java (prefered method)
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
if exist "%ProgramFiles%\Java\jre7\bin\%JAVAPROG%" (
  set _JAVACMD=%ProgramFiles%\Java\jre7\bin\%JAVAPROG%
  goto run_c2o
)

REM Check standard default 64-bit install locations
if exist "%ProgramFiles% (x86)\Java\jre6\bin\%_JAVAPROG%" (
  set _JAVACMD="%ProgramFiles% (x86)\Java\jre6\bin\%_JAVAPROG%"
  goto run_c2o
)
if exist "%ProgramFiles% (x86)\Java\jre7\bin\%_JAVAPROG%" (
  set _JAVACMD="%ProgramFiles% (x86)\Java\jre7\bin\%_JAVAPROG%"
  goto run_c2o
)

REM Now lets try and get a value for JAVA_HOME from the registry!

REM Start by finding the version of the RTE
FOR /F "tokens=3" %%A IN ('REG.EXE QUERY "HKLM\Software\JavaSoft\Java RunTime Environment" /v "CurrentVersion"') DO set _MYVAR1=%%A

REM then where this version is installed
FOR /F "tokens=3*" %%A IN ('REG.EXE QUERY "HKLM\Software\JavaSoft\Java RunTime Environment\%_MYVAR%" /v "JavaHome"') DO set _MYVAR2=%%A %%B

if exist %_MYVAR2%\bin\%_JAVAPROG% {
  set _JAVACMD=%_MYVAR2%\bin\%_JAVAPROG%
  echo _JAVACMD=%_JAVACMD%
  goto run_c2o
)  

REM Now try finding the version of the JDK
FOR /F "tokens=3" %%A IN ('REG.EXE QUERY "HKLM\Software\JavaSoft\Java Development Kit" /v "CurrentVersion"') DO set _MYVAR1=%%A

REM then where this version is installed
FOR /F "tokens=3*" %%A IN ('REG.EXE QUERY "HKLM\Software\JavaSoft\Java Development Kit\%_MYVAR%" /v "JavaHome"') DO set _MYVAR2=%%A %%B

if exist %_MYVAR2%\bin\%_JAVAPROG% {
  set _JAVACMD=%_MYVAR2%\bin\%_JAVAPROG%
  echo _JAVACMD=%_JAVACMD%
  goto run_c2o
)

echo '
echo ----------------------------------------------------------------------------
echo                                  WARNING
echo '
echo Unable to determine where the %JAVAPROG% program (which is part of Java) has
echo been installed on your system.  Java does not appear to have been installed
echo in any of the default locations or using the standard registry keys.
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
echo found on the system search path and will continue on that basis.  If you do not
echo want to do this then use CTRL-C to abandon trying to start calibre2opds.
echo ----------------------------------------------------------------------------
echo '
pause

set _JAVACMD=%_JAVAPROG%

:run_c2o

REM  We set stack limits explicitly here to get consistency across systems
REM -Xms<value> define starting size
REM -Xmx<value> defines maximum size
REM -Xss<value> defines stack size
REM It is possible that for very large libraries this may not be enough - we will have to see.
REM If these options are omitted then defaults are chosen depending on system configuration
START "Calibre2Opds" "%_JAVACMD%" -jar uninstaller.jar


:end
REM Clear down all the environment variables we used
set _JAVACMD=
set _JAVAPROG=
set _C2O=
set _MYVAR1=
set _MYVAR2=
