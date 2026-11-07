@echo off
setlocal
if not exist out mkdir out
javac -encoding UTF-8 -d out src\roadtogold\*.java
if errorlevel 1 pause & exit /b 1
java -cp out roadtogold.RoadToGold
