@echo off
echo ========================================
echo   Компиляция системы такси
echo ========================================

if not exist "bin" mkdir bin

echo Компилируем исходники...
javac -encoding UTF-8 -d bin -sourcepath src src\TaxiSystem.java

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Компиляция успешно завершена!
    echo ========================================
    echo.
    echo Для запуска используйте: run.bat
) else (
    echo.
    echo ========================================
    echo   ОШИБКА КОМПИЛЯЦИИ!
    echo ========================================
)

pause
