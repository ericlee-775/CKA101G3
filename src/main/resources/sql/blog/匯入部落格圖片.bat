@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ============================================
echo   Downloading blog images and importing to DB...
echo ============================================
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "gen_blog_photos_sql.ps1"
echo.
pause
