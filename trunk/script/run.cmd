@echo OFF
REM  batch file for running the Calibre2Opds program in CLI mode.
REM
REM  The checks for Java have been adapted from those used to
REM  start up the IzPack package builder.   Hopefully this means
REM  that Java may run on some of those systems that do not
REM  have Java on the search path but do have the JAVA_HOME
REM  environment variable set or have installed to default location.


cls
echo Calibre2opds startup
echo ====================
echo '
echo [INFO]  Trying to locate Java on this system
echo [INFO]  ====================================

set _C2O=OpdsOutput-3.1-SNAPSHOT.jar

set _JAVAPROG=JAVA.EXE


REM See if JAVA_HOME specifies location of Java (prefered method)

if not "%JAVA_HOME%" == "" (
  if exist "%JAVA_HOME%\bin\%_JAVAPROG%"  (
    set _JAVACMD=%JAVA_HOME%\bin\%_JAVAPROG%
    echo [INFO] Java found via JAVA_HOME evironment variable [%JAVA_HOME%]
    goto run_c2o
  )
)
echo [INFO] Java location not found via JAVA_HOME environment variable

REM Check default install locations

if exist "%ProgramFiles%\Java\jre6\bin\%_JAVAPROG%" (
  set _JAVACMD=%ProgramFiles%\Java\jre6\bin\%_JAVAPROG%
  echo [INFO] Java found via default 32-bit JRE6 location
  echo [INFO] Java located at %ProgramFiles%\Java\jre6
  goto run_c2o
)
echo [INFO] Java not found in default 32-bit JRE6 location
if exist "%ProgramFiles%\Java\jre7\bin\%JAVAPROG%" (
  set _JAVACMD=%ProgramFiles%\Java\jre7\bin\%JAVAPROG%
  echo [INFO] Java found at 32-bit JRE7 location [%ProgramFiles%\Java\jre7]
  goto run_c2o
)
echo [INFO] Java not found in default 32-bit JRE7 location
if exist "%ProgramFiles% (x86)\Java\jre6\bin\%_JAVAPROG%" (
  set _JAVACMD="%ProgramFiles% (x86)\Java\jre6\bin\%_JAVAPROG%"
  echo [INFO] Java found at default 64-bit JRE6 location [%ProgramFiles% ^(x86^)\Java\jre6]
  goto run_c2o
)
echo [INFO] Java not found in default 64-bit JRE6 location
if exist "%ProgramFiles% (x86)\Java\jre7\bin\%_JAVAPROG%" (
  set _JAVACMD="%ProgramFiles% (x86)\Java\jre7\bin\%_JAVAPROG%"
  echo [INFO] Java found at default 64-bit JRE7 location [%ProgramFiles% ^(x86^)\Java\jre7]
  goto run_c2o
)
echo [INFO] Java not found in default 64-bit JRE7 location


REM This next section is about trying to find Java home via the registry


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
if exist uninstaller.jar (
	REM This bit is to work around what seems to be a bug in IzPack which seems to
	REM set up the uninstall shortcut to use run.cmd regardless of te specification!
	cd ..
	"%_JAVACMD%" -jar uninstaller/uninstaller.jar
	goto end
)
echo '
echo "-----------------------"
echo " Calibre2Opds STARTING "
echo "-----------------------"
echo '
REM  We set stack limits explicitly here to get consistency across systems
REM -Xms<value> define starting size
REM -Xmx<value> defines maximum size
REM -Xss<value> defines stack size
REM It is possible that for very large libraries this may not be enough - we will have to see.
echo [INFO]  "%_JAVACMD%" -Xms128m -Xmx512m -cp OpdsOutput-3.1-SNAPSHOT.jar Cli %*
"%_JAVACMD%" -Xms128m -Xmx512m -cp OpdsOutput-3.1-SNAPSHOT.jar Cli %*
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


