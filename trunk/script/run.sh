#!/bin/sh
# script file for running the Calibre2Opds program in CLI mode on Unix-like systems.

old=$(pwd)
f=`dirname "$0"`
cd $f

# The next few lines are to help with running in Portable mode with minimal user setup required

if [ "$CALIBRE2OPDS_CONFIG" == "" ]; then
  if [ -d $f/Calibre2OpdsConfig ]; then
    CALIBRE2OPDS_CONFIG=Calibre2OpdsConfig
    export CALIBRE2OPDS_CONFIG
  fi
fi

#  We set stack limits explicitly here to get consistency across systems
# -Xms<value> define starting size
# -Xmx<value> defines maximum size
# -Xss<value> defines stack size
# It is possible that for very large libraries this may not be enough - we will have to see.
java -Xms128m -Xmx1024m -cp ./OpdsOutput-3.4-SNAPSHOT.jar Cli $*
cd $old
