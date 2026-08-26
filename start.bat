@echo off
title CareRoute - Hospital Operations Console
cd /d "%~dp0"

if not exist build\app mkdir build\app

echo Building...
for /f "delims=" %%F in ('dir /s /b shared\src\main\java\*.java') do echo %%F >> build\shared.txt
javac -d build\app -cp "lib\sqlite-jdbc.jar" @build\shared.txt

for /f "delims=" %%F in ('dir /s /b pod2_lists_search_sort\src\main\java\*.java') do echo %%F >> build\pod2.txt
javac -d build\app -cp "lib\sqlite-jdbc.jar;build\app" @build\pod2.txt

for /f "delims=" %%F in ('dir /s /b pod3_queues_priority\src\main\java\*.java') do echo %%F >> build\pod3.txt
javac -d build\app -cp "lib\sqlite-jdbc.jar;build\app" @build\pod3.txt

for /f "delims=" %%F in ('dir /s /b pod4_trees_lookup\src\main\java\*.java') do echo %%F >> build\pod4.txt
javac -d build\app -cp "lib\sqlite-jdbc.jar;build\app" @build\pod4.txt

for /f "delims=" %%F in ('dir /s /b pod5_hashing_grouping_maps\src\main\java\*.java') do echo %%F >> build\pod5.txt
javac -d build\app -cp "lib\sqlite-jdbc.jar;build\app" @build\pod5.txt

for /f "delims=" %%F in ('dir /s /b pod1_data_database\src\main\java\*.java') do echo %%F >> build\pod1.txt
javac -d build\app -cp "lib\sqlite-jdbc.jar;build\app" @build\pod1.txt

del build\*.txt 2>nul
echo Build complete.
echo.
echo Starting CareRoute on http://127.0.0.1:8091
echo Press Ctrl+C to stop.
echo.
java -cp "lib\sqlite-jdbc.jar;build\app" com.hospitaloptimizer.pod1.WebServer
pause
