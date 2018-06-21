@echo off
cd %cd%
call mvn clean eclipse:clean -e
echo. & pause

