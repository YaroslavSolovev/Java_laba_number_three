@echo off
cls
echo ================================================
echo   TAXI MANAGEMENT SYSTEM - GUI
echo ================================================
echo.

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found!
    echo Install Java 8 or higher
    echo.
    pause
    exit /b 1
)

echo [OK] Java found
echo.

REM Create bin directory
if not exist "bin" mkdir bin

REM Compile
echo Compiling project...
javac -encoding UTF-8 -d bin -sourcepath src src\TaxiSystem.java src\ui\TaxiSystemUI.java 2>compile_errors.txt

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    echo.
    type compile_errors.txt
    echo.
    pause
    exit /b 1
)

echo [OK] Compilation successful
echo.

REM Remove error file
if exist "compile_errors.txt" del compile_errors.txt

REM Launch GUI
cls
echo ================================================
echo   LAUNCHING GUI
echo ================================================
echo.
echo FEATURES:
echo  - Auto-start system
echo  - New order every 2 seconds
echo  - 6 taxis processing orders
echo  - Trip takes 5-10 seconds
echo  - UI updates every 2 seconds
echo  - Optimized for performance
echo.
echo TAXI COLORS:
echo  - Green circles = taxi available
echo  - Yellow circles = going to client
echo  - Red circles = transporting passenger
echo.
echo CONTROLS:
echo  - Pause button - stop visualization
echo  - Stop button - exit system
echo  - Or just close window with X
echo.
echo Window will open in 2 seconds...
timeout /t 2 >nul

start javaw -Dfile.encoding=UTF-8 -cp bin ui.TaxiSystemUI

echo.
echo GUI launched in background.
echo You can close this console window now.
echo.
pause
