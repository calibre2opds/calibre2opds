#!/bin/sh
#  Command File for building the Calibre2opds install packages

ZIPFILE=`pwd`/Calibre2opds.zip

echo -------------------------------
echo Building ZIP Install
echo -------------------------------

rm $ZIPFILE
zip  "$ZIPFILE" licence.txt   readme.txt   release.txt
cd script
zip  "$ZIPFILE" *
cd ..
cd brand
zip "$ZIPFILE" calibre2opds.ico                   calibre2opds-icon.png           calibre2opds-icon_64.png   
zip "$ZIPFILE" calibre2opds-icon_192.png      calibre2opds-icon-small_32.ico  calibre2opds-text-icon.ico
zip "$ZIPFILE" calibre2opds-text-icon_64.png
cd ..
cd Install/target
zip "$ZIPFILE"  OpdsOutput-3.0-SNAPSHOT.jar
cd Dependency
zip "$ZIPFILE"  DataModel-3.0-SNAPSHOT.jar    Tools-3.0-SNAPSHOT.jar  CalibreQueryLanguage-3.0-SNAPSHOT.jar
zip "$ZIPFILE"  jdom-1.1.jar   junit-4.7.jar  log4j-1.2.12.jar    sqlite-jdbc-3.6.17.1.jar
cd ../../..

echo -------------------------------
echo Building Unix/Linux Install.jar
echo -------------------------------

/Developer/Applications/IzPack/bin/compile install.xml

echo ----------------------------
echo Building Windows Install.exe
echo ----------------------------

python /Developer/Applications/IzPack/utils/wrappers/izpack2exe/izpack2exe.py --file=install.jar --output=install.exe --no-upx

echo ----------------------------------------
echo Completed building Calibre2Opds Installs
echo ----------------------------------------


