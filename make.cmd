@echo off
@call setenv.cmd 
echo Started at %TIME%
mvn clean install dependency:copy-dependencies
echo Completed at %TIME%
pause