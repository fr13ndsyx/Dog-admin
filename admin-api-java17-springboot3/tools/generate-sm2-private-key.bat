@echo off
chcp 65001 >nul
node "%~dp0generate-sm2-private-key.cjs"
pause
