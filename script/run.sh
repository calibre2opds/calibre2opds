#!/bin/sh
# script file for running the Calibre2Opds program in CLI mode on Unix-like systems
# such as Linux and Mac.

c2o_jar=OpdsOutput-3.4-SNAPSHOT.jar

#  We set Java VM stack limits explicitly here to get consistency across systems
# -Xms<value> define starting size
# -Xmx<value> defines maximum size
# -Xss<value> defines stack size
# It is possible that for very large libraries this may not be enough - we will have to see.
c2o_opts="-Xms128m -Xmx1024m"

old=`pwd`
scriptdir=`dirname "$0"`

# Check we know how to run from where binaries are located
if [ ! -f $c2o_jar ]; then
  if [ ! -f $scriptdir/$c2o_jar ]; then
    echo "ERROR: calibre2opds binaries not found"
    exit -1
  fi
  cd $scriptdir
fi

# The next few lines are to help with running in Portable mode with minimal user setup required

if [ "$CALIBRE2OPDS_CONFIG" = "" ]; then
  if [ -d $scriptdir/Calibre2OpdsConfig ]; then
    CALIBRE2OPDS_CONFIG=Calibre2OpdsConfig
    export CALIBRE2OPDS_CONFIG
  fi
fi

echo "Starting calibre2opds"

echo java $c2o_opts $1 -cp $c2o_jar Cli
java $c2o_opts $1 -cp $c2o_jar Cli $*
cd $old
