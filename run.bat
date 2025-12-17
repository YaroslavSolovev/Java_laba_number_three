@echo off
chcp 65001 > nul
echo ========================================
echo   Запуск системы такси
echo ========================================
echo.

if not exist "bin" (
    echo ОШИБКА: Скомпилированные файлы не найдены!
    echo Пожалуйста, сначала запустите compile.bat
    pause
    exit /b 1
)

echo Запускаем систему...
echo.
java -Dfile.encoding=UTF-8 -cp bin TaxiSystem

echo.
echo ========================================
echo   Система завершила работу
echo ========================================
pause
