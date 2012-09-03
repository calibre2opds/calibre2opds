@echo OFF
REM  batch file for running the Calibre2Opds program in GUI mode.
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

set _C2O=OpdsOutput-3.2-SNAPSHOT.jar

set _JAVAPROG=JAVAW.EXE
if "%1"=="-enableassertions" set _JAVAPROG=JAVA.EXE

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
if not "%_MYVAR1%" == "" (
  echo [INFO] Java found via registry JRE key
  goto get_javahome
) 

set _MYKEY=HKLM\Software\JavaSoft\Java Development Kit
for /F "tokens=3" %%A IN ('REG.EXE QUERY "%_MYKEY%" /s ^| FIND "CurrentVersion"') DO set _MYVAR1=%%A
if not "%_MYVAR1%" == "" (
  echo [INFO] Java found via registry JDK key
  goto get_javahome
)
set _MYKEY=HKLM\Software\Wow6432Node\JavaSoft\Java RunTime Environment
for /F "tokens=3" %%A IN ('REG.EXE QUERY "%_MYKEY%" /s ^| FIND "CurrentVersion"') DO set _MYVAR1=%%A
if not "%_MYVAR1%" == "" (
  echo [INFO] Java found via registry JRE key for 32 bit Java on 64 bit system
  goto get_javahome
)
set _MYKEY=HKLM\Software\Wow6432Node\JavaSoft\Java Development Kit  
for /F "tokens=3" %%A IN ('REG.EXE QUERY "%_MYKEY%" /s ^| FIND "CurrentVersion"') DO set _MYVAR1=%%A
if not "%_MYVAR1%" == "" (
  echo [INFO] Java found via registry JDK key for 32 bit Java on 64 bit system
  goto get_javahome
)
echo [INFO] Unable to find Java Registry entry
goto java_notfound

:get_javahome
FOR /F "tokens=3*" %%A IN ('REG.EXE QUERY "%_MYKEY%\%_MYVAR1%" /s ^| FIND "JavaHome"') DO set _MYVAR2=%%A %%B
if "%_MYVAR2%" == "" (
  echo [INFO]  Failed to find JavaHome registry key
  goto java_notfound
)
if not exist "%_MYVAR2%\bin\%_JAVAPROG%" (
  echo [INFO]  Failed to find Java at location indicated by registry
  goto java_notfound
)
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
REM  We set stack limits explicitly here to get consistency across systems
REM -Xms<value> define starting size
REM -Xmx<value> defines maximum size
REM -Xss<value> defines stack size
REM It is possible that for very large libraries this may not be enough - we will have to see.
REM If these options are omitted then defaults are chosen depending on system configuration
echo '
echo "-----------------------"
echo " Calibre2Opds STARTING "
echo "-----------------------"
echo '
if not "%1"=="-enableassertions" goto no_assertions
REM Start the GUI leaving this batch file running for progress/debug messages
echo [INFO]  "%_JAVACMD%" -Xms128m -Xmx512m  -enableassertions -cp %_C2O% Gui
echo '
"%_JAVACMD%" -Xms128m -Xmx512m  -enableassertions -cp %_C2O% Gui
echo '
echo "-----------------------"
echo " Calibre2Opds FINISHED "
echo "-----------------------"
echo '
goto end

:no_assertions
REM Start the GUI in normal mode as a separate process and close this batch file
echo [INFO]  START "Calibre2Opds" "%_JAVACMD%" -Xms128m -Xmx512m -cp %_C2O% Gui  
echo '
START "Calibre2Opds" "%_JAVACMD%" -Xms128m -Xmx512m -cp %_C2O% Gui

:end
REM Clear down all the environment variables we (might have) used
REM ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
set _JAVACMD=
set _JAVAPROG=
set _C2O=
set _MYVAR1=
set _MYVAR2=
set _MYKEY=
