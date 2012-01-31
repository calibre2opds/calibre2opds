@echo off
set USER=%1
set PASSWORD=%2
FOR %%G IN (calibre2opds-3.0-alpha-*.zip) DO (
	echo uploading %%G
	googlecode_upload.py -u %USER% -w %PASSWORD% -p calibre2opds -s "An archive built from the bleeding edge sources. Use at you own risks, it's not even a beta..." -l "Type-Archive,OpSys-All,Alpha,HEAD" %%G
)
FOR %%G IN (calibre2opds-3.0-alpha-*.exe) DO (
	echo uploading %%G
	googlecode_upload.py -u %USER% -w %PASSWORD% -p calibre2opds -s "An installer built from the bleeding edge sources. Use at you own risks, it's not even a beta..." -l "Type-Installer,OpSys-Windows,Alpha,HEAD" %%G
)
FOR %%G IN (calibre2opds-3.0-alpha-*.jar) DO (
	echo uploading %%G
	googlecode_upload.py -u %USER% -w %PASSWORD% -p calibre2opds -s "An installer built from the bleeding edge sources. Use at you own risks, it's not even a beta..." -l "Type-Installer,OpSys-All,Alpha,HEAD" %%G
)
