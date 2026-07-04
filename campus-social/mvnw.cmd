@REM Maven Wrapper startup batch script for Windows
@REM This script downloads Maven if not present and runs the build

@echo off
setlocal

set MAVEN_VERSION=3.9.6
set MAVEN_URL=https://archive.apache.org/dist/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip
set MAVEN_HOME=%~dp0.mvn\wrapper\maven

if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

echo Maven not found. Downloading Maven %MAVEN_VERSION%...
mkdir "%MAVEN_HOME%" 2>nul

powershell -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%TEMP%\maven.zip'; Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%MAVEN_HOME%' -Force; Move-Item '%MAVEN_HOME%\apache-maven-%MAVEN_VERSION%\*' '%MAVEN_HOME%\' -Force; Remove-Item '%MAVEN_HOME%\apache-maven-%MAVEN_VERSION%' -Recurse -Force; Remove-Item '%TEMP%\maven.zip'"

:runMaven
"%MAVEN_HOME%\bin\mvn.cmd" %*
