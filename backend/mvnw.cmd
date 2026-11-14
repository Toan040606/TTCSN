@echo off
setlocal

set "MVN_VERSION=3.9.11"
set "BASE_DIR=%~dp0"
set "MVN_HOME=%BASE_DIR%.mvn\apache-maven-%MVN_VERSION%"
set "MVN_CMD=%MVN_HOME%\bin\mvn.cmd"
set "MVN_ARCHIVE=%BASE_DIR%.mvn\apache-maven-%MVN_VERSION%-bin.zip"

if not exist "%MVN_CMD%" (
  echo Maven %MVN_VERSION% is not installed locally. Downloading...
  if not exist "%BASE_DIR%.mvn" mkdir "%BASE_DIR%.mvn"

  powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference = 'Stop'; $ProgressPreference = 'SilentlyContinue';" ^
    "Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip' -OutFile '%MVN_ARCHIVE%';" ^
    "Expand-Archive -Path '%MVN_ARCHIVE%' -DestinationPath '%BASE_DIR%.mvn' -Force;" ^
    "Remove-Item '%MVN_ARCHIVE%' -Force"

  if errorlevel 1 (
    echo Unable to download or extract Maven.
    exit /b 1
  )
)

call "%MVN_CMD%" %*
exit /b %errorlevel%
