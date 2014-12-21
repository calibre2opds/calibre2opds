echo OFF

REM  Batch file to start a Calibre2Opds that has just been built
REM  (avoids having to build installer or deploy calibre2odps)

SETLOCAL
pushd Install\target\dependency

for /f "skip=1" %%p in ('wmic os get freephysicalmemory') do ( 
  set m=%%p
  goto :done
)
:done
echo free: %m%

echo Starting Calibre2Opds
call ..\..\..\script\rungui.cmd -enableassertions
popd
ENDLOCAL