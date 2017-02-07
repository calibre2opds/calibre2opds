#!/bin/sh
# Script to start calibre2opds in GUI mnode on Unix-like systems such
# as Linux and Mac
#
# Normally we expect the calibre2opds binaries to be in the same location
# as this script, but we need to allow for the case of testing where they
# are actually in the location this script was invoked from!

c2o_jar=OpdsOutput-3.6-SNAPSHOT.jar

#  We set Java VM stack limits explicitly here to get consistency across systems
#
# -Xms<value> define starting size
# -Xmx<value> defines maximum size
# -Xss<value> defines stack size
#
# It is possible that for very large libraries this may not be enough - we will have to see.
c2o_opts="-Xms128m -Xmx512m"

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

#  We run in foreground if assertions active, background if not
if [ "$1" == "-enableassertions" -o "$old" != "$scriptdir" ]; then
  echo java $c2o_opts $1 -cp $c2o_jar Gui
  java $c2o_opts $1 -cp $c2o_jar Gui $*
else
  echo java $c2o_opts -cp $c2o_jar Gui
  java $c2o_opts -cp $c2o_jar Gui >/dev/null &
fi
cd $old
