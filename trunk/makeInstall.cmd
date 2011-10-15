@echo OFF

REM  Command File for building the Calibre2opds install packages
REM
REM The following specifies the ZIP program to use
REM (it is recommended that the GNU ZIP for Windows is used)
REM If not on the search path, then the full path must be used.
call setenv.cmd

SET VERSION=calibre2opds-3.0-beta1
SET ZIPFILE=%cd%\%VERSION%.zip

del /q %cd%\%VERSION%.*
echo '
echo -------------------------------
echo Building ZIP Install package
echo -------------------------------
echo '

:ZIP_OK
REM delete and ZIP temp files lying around
del /q zia*

echo Creating %ZIPFILE%

"%ZIPPROG%"  "%ZIPFILE%" licence.txt   readme.txt   release.txt
cd script
"%ZIPPROG%"  "%ZIPFILE%" *
cd ..
cd brand
"%ZIPPROG%" "%ZIPFILE%" calibre2opds.ico                   calibre2opds-icon.png           calibre2opds-icon_64.png   
"%ZIPPROG%" "%ZIPFILE%" calibre2opds-icon_192.png      calibre2opds-icon-small_32.ico  calibre2opds-text-icon.ico
"%ZIPPROG%" "%ZIPFILE%" calibre2opds-text-icon_64.png
cd ..
cd Install\target\Dependency
"%ZIPPROG%" "%ZIPFILE%"  OpdsOutput-3.0-SNAPSHOT.jar  DataModel-3.0-SNAPSHOT.jar    Tools-3.0-SNAPSHOT.jar
"%ZIPPROG%" "%ZIPFILE%"  jdom-1.1.jar   junit-4.7.jar  log4j-1.2.12.jar    sqlite-jdbc-3.6.17.1.jar    jtidy-4aug2000r7-dev.jar
cd ..\..\..\

goto LINUX

if exist "%ZIPFILE%" (
	echo %ZIPFILE% created
	goto LINUX
)

echo -----------------------------------------
echo ERROR: %ZIPFILE% not created successfully
echo -----------------------------------------
goto FINISHED

:LINUX
echo '
echo -----------------------------------
echo Building Unix/Linux Install package
echo -----------------------------------
echo '
if exist "%VERSION%.jar" (
	echo Deleting %VERSION%.jar
	del %VERSION%.jar
)
if exist install.jar (
	echo Deleting install.jar
	del install.jar
)

if exist "%ProgramFiles%\IzPack\bin\compile.bat" (
	REM echo cmd /c "%ProgramFiles%\IzPack\bin\compile.bat" install.xml
	cmd /c "%ProgramFiles%\IzPack\bin\compile.bat" install.xml
	goto CHECK_LINUX
)
if exist C:\java\IzPack\bin\compile.bat (
	REM echo cmd /c c:\java\IzPack\bin\compile.bat install.xml
	cmd /c c:\java\IzPack\bin\compile.bat install.xml
	goto CHECK_LINUX
)
echo -----------------------------------------
echo ERROR:  Unable to locate IzPack
echo -----------------------------------------
goto FINISHED

:CHECK_LINUX
if not exist install.jar (
	echo -----------------------------------------
	echo ERROR: Building LINUX install package failed
	echo -----------------------------------------
	goto FINISHED
)
echo Renaming install.jar to %VERSION%.jar
ren install.jar %VERSION%.jar

:WINDOWS
echo '
echo ----------------------------
echo Building Windows package
echo ----------------------------
echo '
if not exist %VERSION%.jar (
	echo -----------------------------------------
	echo ERROR: %VERSION%.jar missing
	echo ERROR: Build of Windows version failed
	echo --------------------------------`---------
	goto FINISHED
)
if exist "%VERSION%.exe" (
	echo Deleting %VERSION%.exe
	del %VERSION%.exe
)
)
if exist "%ProgramFiles%\IzPack\utils\wrappers\izpack2exe" (
	REM echo "%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\izpack2exe" --file="%VERSION%.jar" --output="%VERSION%.exe" --with-7z="%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\7za.exe" --with-upx="%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\upx.exe"
	"%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\izpack2exe" --file="%VERSION%.jar" --output="%VERSION%.exe" --with-7z="%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\7za.exe" --with-upx="%ProgramFiles%\IzPack\utils\wrappers\izpack2exe\upx.exe"
	goto CHECK_WINDOWS
) 
if exist C:\java\IzPack\utils\wrappers\izpack2exe (
	REM echo "C:\Java\IzPack\utils\wrappers\izpack2exe\izpack2exe" --file="%VERSION%.jar" --output="%VERSION%.exe" --with-7z="C:\Java\IzPack\utils\wrappers\izpack2exe\7za.exe" --with-upx="C:\Java\IzPack\utils\wrappers\izpack2exe\upx.exe"
	"C:\Java\IzPack\utils\wrappers\izpack2exe\izpack2exe" --file="%VERSION%.jar" --output="%VERSION%.exe" --with-7z="C:\Java\IzPack\utils\wrappers\izpack2exe\7za.exe" --with-upx="C:\Java\IzPack\utils\wrappers\izpack2exe\upx.exe"
	goto CHECK_WINDOWS
)
echo ----------------------------------------
echo ERROR: Unable to locate izpack2exe
echo ----------------------------------------
goto FINSIHED

:CHECK_WINDOWS

echo '
echo ------------------------------------------------
echo Completed building Calibre2Opds Install packages
echo ------------------------------------------------

:FINISHED
echo '
dir /b %VERSION%.*
echo '
