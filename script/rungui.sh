#!/bin/sh
old=$(pwd)
f=`dirname "$0"`
cd $f
java -cp ./OpdsOutput-3.0-SNAPSHOT.jar Gui
cd $old
