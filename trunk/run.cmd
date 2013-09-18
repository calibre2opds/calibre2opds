echo OFF

REM  Batch file to start a Calibre2Opds that has just been built
REM  (avoids having to build installer or deploy calibre2odps)

pushd Install\target\dependency
set CALIBRE2OPDS_CONFIG=c:\data\dropbox\Calibre2opds\

for /f "skip=1" %%p in ('wmic os get freephysicalmemory') do ( 
  set m=%%p
  goto :done
)
:done
echo free: %m%

echo Starting Calibre2Opds
call ..\..\..\script\rungui.cmd -enableassertions
popd
