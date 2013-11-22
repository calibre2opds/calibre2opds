@echo OFF
REM  batch file for running the Calibre2Opds program in CLI mode.
REM
REM  The checks for Java have been adapted from those used to
REM  start up the IzPack package builder and then extended.
REM  Hopefully this means that calibre2opds Java may run on 
REM  some of those systems that do not have Java on the search 
REM  path but do have the one of the following conditions met:
REM  - JAVA_HOME environment variable set
REM  - have installed to default location.
REM  - have the expected registry keys set


cls
echo Calibre2opds startup
echo ====================

set _C2O=OpdsOutput-3.3-SNAPSHOT.jar
set _CD=%cd%
echo [INFO] Current Directory: %_CD%

set _JAVAPROG=JAVA.EXE

REM  The following is used to determine free RAM
REM  (we want to use this as a basis for giving the JVM 
REM  more RAM on systems with plenty free).

for /f "skip=1" %%p in ('wmic os get freephysicalmemory') do ( 
  set m=%%p
  goto freemem_done
)
:freemem_done
echo '
echo Free RAM: %m%


echo '
echo [INFO]  Trying to locate Java on this system
echo [INFO]  ====================================

REM See if JAVA_HOME specifies location of Java (prefered method)

if "%JAVA_HOME%" == "" goto not_javahome
if not exist "%JAVA_HOME%\bin\%_JAVAPROG%" goto not_javahome
set _JAVACMD=%JAVA_HOME%\bin\%_JAVAPROG%
echo [INFO] Java found via JAVA_HOME evironment variable [%JAVA_HOME%]
goto run_c2o
:not_javahome
echo [INFO] Java location not found via JAVA_HOME environment variable


REM Check default install locations
REM ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

if not exist "%ProgramFiles%\Java\jre6\bin\%_JAVAPROG%" goto not_jre6
set _JAVACMD=%ProgramFiles%\Java\jre6\bin\%_JAVAPROG%
echo [INFO] Java found via default JRE6 location
echo [INFO] Java located at %ProgramFiles%\Java\jre6
goto run_c2o
:not_jre6
echo [INFO] Java not found in default JRE6 location [%ProgramFiles%\Java\jre6]

if not exist "%ProgramFiles%\Java\jre7\bin\%_JAVAPROG%" goto not_jre7
set _JAVACMD=%ProgramFiles%\Java\jre7\bin\%_JAVAPROG%
echo [INFO] Java found at default JRE7 location [%ProgramFiles%\Java\jre7]
goto run_c2o
:not_jre7
echo [INFO] Java not found in default JRE7 location [%ProgramFiles%\Java\jre7]

if not exist "%ProgramFiles% (x86)\Java\jre6\bin\%_JAVAPROG%" goto not_jre6_64
set _JAVACMD=%ProgramFiles% (x86)\Java\jre6\bin\%_JAVAPROG%
echo [INFO] Java found at default 32-bit Java on 64-bit Windows JRE6 location [%ProgramFiles% ^(x86^)\Java\jre6]
goto run_c2o
:not_jre6_64
echo [INFO] Java not found in default 32-bit Java on 64-bit Windows JRE6 location [%ProgramFiles% (x86)\Java\jre6]

if not exist "%ProgramFiles% (x86)\Java\jre7\bin\%_JAVAPROG%" goto not_jre7_64
set _JAVACMD=%ProgramFiles% (x86)\Java\jre7\bin\%_JAVAPROG%
echo [INFO] Java found at default 32-bit Java on 64-bit Windows JRE7 location [%ProgramFiles% ^(x86^)\Java\jre7]
goto run_c2o
:not_jre7_64
echo [INFO] Java not found in default 32-bit Java on 64-bit Windows JRE7 location [%ProgramFiles% (x86)\Java\jre7]


REM This next section is about trying to find Java home via the registry
REM ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

set _MYKEY=HKLM\Software\JavaSoft\Java RunTime Environment
for /F "tokens=3" %%A IN ('REG.EXE QUERY "%_MYKEY%" /s ^| FIND "CurrentVersion"') DO set _MYVAR1=%%A
if "%_MYVAR1%" == "" GOTO not_jrereg
echo [INFO] Java found via registry JRE key %_MYKEY%
goto get_javahome
:not_jrereg
echo [INFO] Java not found at reg key  %_MYKEY%

set _MYKEY=HKLM\Software\JavaSoft\Java Development Kit
for /F "tokens=3" %%A IN ('REG.EXE QUERY "%_MYKEY%" /s ^| FIND "CurrentVersion"') DO set _MYVAR1=%%A
if "%_MYVAR1%" == "" GOTO not_jdkreg
echo [INFO] Java found via registry JDK key %_MYKEY%
goto get_javahome
:not_jdkreg
echo [INFO] Java not found at reg key  %_MYKEY%

set _MYKEY=HKLM\Software\Wow6432Node\JavaSoft\Java RunTime Environment
for /F "tokens=3" %%A IN ('REG.EXE QUERY "%_MYKEY%" /s ^| FIND "CurrentVersion"') DO set _MYVAR1=%%A
if "%_MYVAR1%" == "" goto not_jrewow
echo [INFO] Java found via registry JRE key for 32 bit Java on 64 bit system
goto get_javahome
:not_jrewow
echo [INFO] Java not found at reg key  %_MYKEY%

set _MYKEY=HKLM\Software\Wow6432Node\JavaSoft\Java Development Kit
for /F "tokens=3" %%A IN ('REG.EXE QUERY "%_MYKEY%" /s ^| FIND "CurrentVersion"') DO set _MYVAR1=%%A
if "%_MYVAR1%" == "" GOTO not_jdkwow
echo [INFO] Java found via registry JDK key for 32 bit Java on 64 bit system
GOTO get_javahome
:not_jdkwow
echo [INFO] Java not found at reg key  %_MYKEY%

echo [INFO] Unable to find Java Registry entry
goto java_notfound

REM  We apear to have found a location for Java.  Check that it is valid
REM  -------------------------------------------------------------------
:get_javahome
FOR /F "tokens=3*" %%A IN ('REG.EXE QUERY "%_MYKEY%\%_MYVAR1%" /s ^| FIND "JavaHome"') DO set _MYVAR2=%%A %%B
if not "%_MYVAR2%" == "" goto ok_javajomereg
echo [INFO]  Failed to find JavaHome registry key
goto java_notfound
:ok_javahomereg
echo [INFO]  Found JavaHome registry key


if exist "%_MYVAR2%\bin\%_JAVAPROG%"
echo [INFO]  Failed to find Java at location indicated by registry
goto java_notfound
:ok_javaprog
echo [INFO] Found "%_MYVAR2%\bin\%_JAVAPROG%"

set _JAVACMD=%_MYVAR2%\bin\%_JAVAPROG%
echo [INFO] Java located at %_MYVAR2%
goto run_c2o

:java_notfound
echo '
echo ----------------------------------------------------------------------------
echo                                  WARNING
echo '
echo Unable to determine where the %JAVAPROG% program (which is part of Java) has
echo been installed on your system.  Java does not appear to have been installed
echo in any of the default locations or using the standard registry keys.
echo '
echo In this case, the recommended way to proceed is to specify the location of Java
echo by setting the JAVA_HOME environment variable. Normally the value of JAVA_HOME 
echo would be set to something like:
echo   JAVA_HOME=C:\Program Files\Java\jre6
echo although it could well vary on your system (particularily if you are running
echo Windows 7 or if you have installed the Java Development Kit.  Within that
echo folder there should be a folder called 'bin' and the %_JAVAPROG% program will
echo be located inside that.
echo '
echo If you press ENTER then calibre2opds will hope that the %_JAVAPROG% program can
echo found on the system search path and will continue on that basis.  
echo ----------------------------------------------------------------------------
echo '
pause

set _JAVACMD=%_JAVAPROG%

:run_c2o
if exist uninstaller.jar (
	REM This bit is to work around what seems to be a bug in IzPack which seems to
	REM set up the uninstall shortcut to use run.cmd regardless of te specification!
	cd ..
	"%_JAVACMD%" -jar uninstaller/uninstaller.jar
	goto end
)

REM The next few lines are to help with running in Portable mode with minimal user setup required

if NOT "%CALIBRE2OPDS_CONFIG%" == "" goto start_c2o
if NOT exist Calibre2OpdsConfig goto start_c2o
set CALIBRE2OPDS_CONFIG=%cd%/Calibre2OpdsConfig


:start_c2o
echo '
echo "-----------------------"
echo " Calibre2Opds STARTING "
echo "-----------------------"
echo '
echo cd %TEMP%
echo [INFO] Current directory set to %cd%
pause

REM  We set stack limits explicitly here to get consistency across systems
REM -Xms<value> define starting size
REM -Xmx<value> defines maximum size
REM -Xss<value> defines stack size
REM It is possible that for very large libraries this may not be enough - we will have to see.
echo [INFO]  "%_JAVACMD%" -Xms128m -Xmx512m -cp "%_CD%/*" -jar "%_cd%\%_C2O%" Cli %*
"%_JAVACMD%" -Xms128m -Xmx1024m -cp "%_CD%/*" -jar "%_cd%/%_C2O%" Cli %*
echo '
echo "-----------------------"
echo " Calibre2Opds FINISHED "
echo "-----------------------"
echo '
pause

:end
REM Clear down all the environment variables we used
set _JAVACMD=
set _JAVAPROG=
set _C2O=
set _MYVAR1=
set _MYVAR2=
cd %_CD%
set _CD%=



