#!/bin/sh
old=$(pwd)
f=`dirname "$0"`
cd $f
#  We set stack limits explicitly here to get consistency across systems
# -Xms<value> define starting size
# -Xmx<value> defines maximum size
# -Xss<value> defines stack size
# It is possible that for very large libraries this may not be enough - we will have to see.
java -Xms128m -Xmx512m -cp ./OpdsOutput-3.1-SNAPSHOT.jar Gui %*
cd $old
