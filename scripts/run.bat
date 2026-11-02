@echo off
rem Compile and run the game (Windows).   Usage: scripts\run.bat
cd /d "%~dp0\.."
if exist out rmdir /s /q out
mkdir out
dir /s /b src\main\java\*.java > out\sources.txt
javac -encoding UTF-8 -d out @out\sources.txt
if errorlevel 1 exit /b 1
xcopy /e /i /y /q src\main\resources out >nul
java -cp out legacyknight.Main
