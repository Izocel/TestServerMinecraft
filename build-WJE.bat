@echo off
cls
set CURRENT=%cd%
set BUILDER=C:/TestServerMinecraft/
set CONFFILE=pom.xml
cd %BUILDER%
call java --version
call mvn --version
call mvn -U clean install package -f %BUILDER%

echo 
echo Job's done look if BUILD SUCCES...
set /p "id=</>Press enter to continue:"