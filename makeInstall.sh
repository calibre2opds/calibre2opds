#!/bin/sh
#  Command File for building the Calibre2opds install packages
#
# If IzPack not installed then stops after ZIP package

BASEVERSION=3.6
SVNVERSION=`svnversion`
VERSION=calibre2opds-$BASEVERSION-$SVNVERSION

ZIPFILE=`pwd`/calibre2opds-${BASEVERSION}-$SVNVERSION.zip

echo ------------------------------------------
echo Building ZIP Install to $ZIPFILE
echo ------------------------------------------

if [ -f $ZIPFILE ]
then 
	rm $ZIPFILE
	fi
	
zip  "$ZIPFILE" licence.txt   readme.txt   release.txt
cd script
zip  "$ZIPFILE" *
cd ..
cd brand
zip "$ZIPFILE" calibre2opds.ico                   calibre2opds-icon.png           calibre2opds-icon_64.png   
zip "$ZIPFILE" calibre2opds-icon_192.png      calibre2opds-icon-small_32.ico  calibre2opds-text-icon.ico
zip "$ZIPFILE" calibre2opds-text-icon_64.png
cd ..
cd Install/target/dependency
zip "$ZIPFILE"  OpdsOutput-%BASEVERSION%-SNAPSHOT.jar  DataModel-%BASEVERSION%-SNAPSHOT.jar    Tools-%BASEVERSION%-SNAPSHOT.jar   CalibreQueryLanguage-%BASEVERSION%-SNAPSHOT.jar
zip "$ZIPFILE"  antlr-2.7.7.jar antlr-runtime-3.1.3.jar commons-io-2.1.jar hamcrest-core-1.3.jar jdom2-2.0.6.jar jtidy-r938.jar junit-4.12.jar log4j-api-2.5.jar log4j-core-2.5.jar sqlite-jdbc-3.8.7.jar stringtemplate-3.2.jar

cd ../../..

if [ ! -f ${IZPACK_HOME}/bin/IzPack/bin/compile ]
then
  # Try a default location (edit if needed)
  IZPACK_HOME=/opt/IzPack
  export IZPACK_HOME
  if [ ! -f ${IZPACK_HOME}/bin/IzPack/bin/compile ]
  then
    echo
    echo ERROR:  Unable to locate IzPack package
    echo 
    echo Only ZIP file built
    echo
    exit -1
  fi
fi

echo -------------------------------
echo Building Unix/Linux Install.jar
echo -------------------------------

${IZPACK_HOME}/bin/compile install.xml
if [! -f install.jar ]
then
  echo
  echo ERROR:  Failed to build .jar file
  echo
  exit -1
fi

cp install.jar $VERSION.jar

echo ----------------------------
echo Building Windows $VERSION.exe
echo ----------------------------

python ${IZPACK_HOME}/utils/wrappers/izpack2exe/izpack2exe.py --file=$VERSION.jar --output=$VERSION.exe --no-upx

echo ----------------------------
echo Building Mac $VERSION.app
echo ----------------------------

python ${IZPACK_HOME}/utils/wrappers/izpack2exe/izpack2app.py  $VERSION.app

echo ----------------------------------------
echo Completed building $VERSION Installs
echo ----------------------------------------
