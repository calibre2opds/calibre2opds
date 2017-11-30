@echo OFF
SETLOCAL

REM  Command File for building the Calibre2opds install packages
REM
REM The following specifies the ZIP program to use
REM (it is recommended that the GNU ZIP for Windows is used)
REM If not on the search path, then the full path must be used.

ZIPPROG=zip
call setenv.cmd

for /f "delims=" %%a in ('svnversion') do @set SVNVERSION=%%a

SET BASEVERSION=3.6
SET VERSION=calibre2opds-%BASEVERSION%-%SVNVERSION%

SET ZIPFILE=%cd%\%VERSION%.zip

del /q %cd%\calibre2opds-%BASEVERSION%-*

echo '
echo -------------------------------
echo Preparing %VERSION%
echo -------------------------------
echo '

echo '
echo ------------------------------------------
echo Building generic Install package (.zip)
echo '

:ZIP_OK
REM delete and ZIP temp files lying around
del /q zia*

echo Creating %ZIPFILE%

"%ZIPPROG%"  "%ZIPFILE%" licence.txt   readme.txt   release.txt
cd script
REM short delays are to stop spurious zia* style files being left behind
timeout /t 1
"%ZIPPROG%"  "%ZIPFILE%" *
cd ..
cd brand
timeout /t 1
"%ZIPPROG%" "%ZIPFILE%" calibre2opds.ico                   calibre2opds-icon.png           calibre2opds-icon_64.png   
timeout /t 1
"%ZIPPROG%" "%ZIPFILE%" calibre2opds-icon_192.png      calibre2opds-icon-small_32.ico  calibre2opds-text-icon.ico
timeout /t 1
"%ZIPPROG%" "%ZIPFILE%" calibre2opds-text-icon_64.png
cd ..
cd Install\target\Dependency
timeout /t 1
"%ZIPPROG%" "%ZIPFILE%"  OpdsOutput-%BASEVERSION%-SNAPSHOT.jar  DataModel-%BASEVERSION%-SNAPSHOT.jar    Tools-%BASEVERSION%-SNAPSHOT.jar   CalibreQueryLanguage-%BASEVERSION%-SNAPSHOT.jar
timeout /t 1
"%ZIPPROG%" "%ZIPFILE%"  antlr-2.7.7.jar antlr-runtime-3.1.3.jar commons-io-2.1.jar hamcrest-core-1.3.jar jdom2-2.0.6.jar jtidy-r938.jar junit-4.12.jar log4j-api-2.5.jar log4j-core-2.5.jar sqlite-jdbc-3.16.1.jar stringtemplate-3.2.jar
cd ..\..\..\

timeout /t 1
if exist "%ZIPFILE%" (
	echo %ZIPFILE% created
	goto LINUX
)

echo ERROR: %ZIPFILE% not created successfully
goto FINISHED

:LINUX
echo '
echo ------------------------------------------
echo Building Unix/Linux Install package (.jar)
echo '
if exist "%VERSION%.jar" (
	echo Deleting %VERSION%.jar
	del %VERSION%.jar
)
if exist install.jar (
	echo Deleting install.jar
	del install.jar
)

if exist "%ProgramFiles%\IzPack" goto CHECK_COMPILE
echo ERROR:  Unable to locate IzPack
goto FINISHED

:CHECK_COMPILE
if exist "%ProgramFiles%\IzPack\bin\compile.bat" goto BUILD_LINUX
echo ERROR:  Unable to locate "%ProgramFiles%\IzPack\bin\compile.bat"
goto FINISHED

:BUILD_LINUX
echo cmd /c "%ProgramFiles%\IzPack\bin\compile.bat" install.xml
cmd /c "%ProgramFiles%\IzPack\bin\compile.bat" install.xml
if exist install.jar goto RENAME_JAR
echo ERROR: Building LINUX install package failed
goto FINISHED

:RENAME_JAR
echo Renaming install.jar to %VERSION%.jar
ren install.jar %VERSION%.jar

:WINDOWS
echo '
echo ----------------------------------------
echo Building Windows package
echo '
if not exist %VERSION%.jar (
	echo ERROR: %VERSION%.jar missing
	echo ERROR: Build of Windows version failed
	goto MAC
)
if exist "%VERSION%.exe" 	del %VERSION%.exe
if exist "%ProgramFiles%\IzPack\utils\wrappers\izpack2exe" goto BUILD_WINDOWS
echo ERROR: Unable to locate izpack2exe
goto MAC
:BUILD_WINDOWS
echo "%ProgramFiles%\izPack\utils\wrappers\izpack2exe\izpack2exe.py" --name="calibre2opds" --file="%VERSION%.jar" --output="%VERSION%.exe" --with-7z="%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\7za.exe" --with-upx="%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\upx.exe"
"%ProgramFiles%\izPack\utils\wrappers\izpack2exe\izpack2exe.py" --name="calibre2opds" --file="%VERSION%.jar" --output="%VERSION%.exe" --with-7z="%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\7za.exe" --with-upx="%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\upx.exe"


:MAC
echo '
echo ----------------------------------------
echo Building Mac package
echo '
if not exist %VERSION%.jar (
	echo ERROR: %VERSION%.jar missing
	echo ERROR: Build of Mac version failed
	goto FINISHED
)
if exist "%VERSION%.app" (
	echo Deleting %VERSION%.app
	del /Q %VERSION%.app
)
if exist "%ProgramFiles%\IzPack\utils\wrappers\izpack2app" goto BUILD_MAC
echo ERROR: Unable to locate izpack2app
goto FINSIHED

:BUILD_MAC
echo "%ProgramFiles%\IzPack\utils\wrappers\izpack2app\izpack2app.py" "%VERSION%.jar" "%VERSION%.app"
"%ProgramFiles%\IzPack\utils\wrappers\izpack2app\izpack2app.py" "%VERSION%.jar"	"%VERSION%.app"

echo '
echo ------------------------------------------------
echo Completed building Calibre2opds Install packages

:FINISHED
echo ------------------------------------------------
echo '
dir /b %VERSION%.*
echo '
ENDLOCAL
