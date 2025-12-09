@ECHO off
SETLOCAL ENABLEDELAYEDEXPANSION

REM ===============================
REM 1) Ensure Docker Desktop is running
REM ===============================
ECHO Checking if Docker Desktop is running...

docker info >nul 2>&1
IF %ERRORLEVEL% EQU 0 (
    ECHO Docker is already running.
) ELSE (
    tasklist /FI "IMAGENAME eq Docker Desktop.exe" 2>NUL | find /I "Docker Desktop.exe" >NUL

    IF ERRORLEVEL 1 (
        ECHO Docker Desktop is not running. Starting it...
        START "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    ) ELSE (
        ECHO Docker Desktop process found, waiting for Docker engine to be ready...
    )

    ECHO Waiting for Docker engine to be ready...
    :WAIT_DOCKER
    docker info >nul 2>&1
    IF NOT %ERRORLEVEL%==0 (
        ECHO  - Docker not ready yet, retrying in 3 seconds...
        timeout /t 3 /nobreak >nul
        GOTO WAIT_DOCKER
    )
    ECHO Docker engine is ready.
)

REM ===============================
REM 2) Run docker-compose (Detached Mode)
REM ===============================
ECHO.
ECHO Changing directory to project docker folder...
cd /d backend/docker

ECHO Stopping existing containers...
docker-compose -f docker_compose.yml down -v

ECHO Building and starting containers in detached mode...
docker-compose -f docker_compose.yml up --build -d
SET COMPOSE_EXIT=%ERRORLEVEL%

IF NOT %COMPOSE_EXIT%==0 (
 ECHO.
    ECHO ======================================
    ECHO Docker compose FAILED (exit %COMPOSE_EXIT%)
    ECHO ======================================
    ENDLOCAL
    PAUSE
    EXIT /B %COMPOSE_EXIT%
)

ECHO.
ECHO ======================================
ECHO Containers are running in the background.
ECHO Use 'docker-compose logs -f' to view logs.
ECHO ======================================

ENDLOCAL
PAUSE