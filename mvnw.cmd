@echo off
setlocal EnableExtensions EnableDelayedExpansion

for /f "tokens=2 delims==" %%A in ('findstr /b "mavenVersion=" "%BASE_DIR%.mvn\wrapper\maven-wrapper.properties"') do set "VERSION=%%A"
set "CACHE_ROOT=%USERPROFILE%\.m2\wrapper\dists"
set "MAVEN_HOME=%CACHE_ROOT%\apache-maven-%VERSION%"
set "MAVEN_ZIP=%CACHE_ROOT%\apache-maven-%VERSION%-bin.zip"
set "URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%VERSION%/apache-maven-%VERSION%-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Downloading Maven %VERSION%...
  if not exist "%CACHE_ROOT%" mkdir "%CACHE_ROOT%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile '%MAVEN_ZIP%'; Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%CACHE_ROOT%' -Force; Remove-Item '%MAVEN_ZIP%' -Force"
  if errorlevel 1 exit /b 2
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
