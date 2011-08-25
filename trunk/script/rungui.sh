#!/bin/sh
old=$(pwd)
f=`dirname "$0"`
cd $f
java -cp ./OpdsOutput-2.5-SNAPSHOT.jar Gui
cd $old
