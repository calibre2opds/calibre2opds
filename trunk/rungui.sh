#!/bin/sh

# Script file to start a Calibre2Opds that has just been built in GUI mode
# (avoids having to build installer or deploy calibre2odps)

PWD_SAVE=`pwd`
cd Install/target/dependency
# If you want to use a particular config folder set the
# CALIBRE2OPDS_CONFIG environment variable appropriately.
# If not set the default for this user is used.
# set CALIBRE2OPDS_CONFIG=\data\Dropbox\Calibre2opds\
echo Starting Calibre2Opds [GUI mode with assertions enabled]
../../../script/rungui.sh -enableassertions
cd ../../../
