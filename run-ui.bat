@echo off
chcp 65001 > nul
echo ========================================
echo   Запуск системы такси с GUI
echo ========================================
echo.

if not exist "bin" (
    echo ОШИБКА: Скомпилированные файлы не найдены!
    echo Пожалуйста, сначала запустите compile.bat
    pause
    exit /b 1
)

echo Запускаем систему с графическим интерфейсом...
echo.
java -Dfile.encoding=UTF-8 -cp bin ui.TaxiSystemUI

pause
