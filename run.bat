@echo off
setlocal
cd /d "%~dp0"
if not exist src\out mkdir src\out
javac -encoding UTF-8 -d src\out src\*.java
if errorlevel 1 (
    pause
    exit /b 1
)
java -cp src\out RoadToGold
