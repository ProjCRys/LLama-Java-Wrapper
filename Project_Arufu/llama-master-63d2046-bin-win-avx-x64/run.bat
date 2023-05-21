@echo off
cd /d "%~dp0"

title llama.cpp

set "projectFolderPath=%~dp0..\..\"
set "mainExePath=%projectFolderPath%\Pygmalion\llama-master-63d2046-bin-win-avx-x64\main.exe"

start "" /B /WAIT "%mainExePath%" -i --interactive-first -r "### Human:" --temp 0 -c 2048 -n -1 --ignore-eos --repeat_penalty 1.2 --instruct -m ggml-model-q4_0.bin

pause >nul
taskkill /F /IM main.exe >nul
