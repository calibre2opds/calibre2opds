echo OFF

REM  Batch file to start a Calibre2Opds that has just been built
REM  (avoids having to build installer or deploy calibre2odps)

pushd Install\target\dependency
set CALIBRE2OPDS_CONFIG=c:\data\dropbox\Calibre2opds\
echo Starting Calibre2Opds
call ..\..\..\script\rungui.cmd -enableassertions
popd
