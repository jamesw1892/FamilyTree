@echo off

IF "%~1"=="" (
    ECHO.
    ECHO    No argument provided
    ECHO.
    GOTO HELP
)
IF "%~1"=="-h" (
    GOTO HELP
)
IF "%~1"=="-r" (
    GOTO REMOVE
)
IF "%~1"=="-b" (
    GOTO BUILD
)
IF "%~1"=="-m" (
    GOTO MAIN_CLASS
)
IF "%~1"=="-c" (
    GOTO CLI
)
IF "%~1"=="-g" (
    GOTO GUI
)
IF "%~1"=="-t" (
    GOTO TEST
)

ECHO.
ECHO    Invalid argument
ECHO.
GOTO HELP

:HELP
    ECHO.
    ECHO    USAGE:
    ECHO    %~n0 -h                 Display this help
    ECHO    %~n0 -r                 Remove all compiled files
    ECHO    %~n0 -b [-a]            Compile all files
    ECHO    %~n0 -m file [-a]       Run main function of specified file - file must be absolute path or relative path from this batch file and filename without .java extension
    ECHO    %~n0 -c [-a]            Run cli
    ECHO    %~n0 -g [port] [-a]     Run custom web server - optional port to run the webserver on, default: 8080
    ECHO    %~n0 -t [-a]            Run tests
    ECHO.
    ECHO    Provide the optional -a argument if already compiled. Otherwise will first remove all compiled files and compile the relevant file(s)
    ECHO.
GOTO EOF

:REMOVE
    ECHO.
    ECHO    Removing compiled java files
    ECHO.

    DEL /S *.class

    ECHO.
    ECHO    Successfully removed compiled java files
    ECHO.
GOTO EOF

:BUILD
    IF NOT "%~2"=="-a" (
        CALL :REMOVE
    )

    ECHO.
    ECHO    Compiling all java files
    ECHO.

    SET FOLDER=test
    CALL :COMPILEFOLDER
    SET FOLDER=cli
    CALL :COMPILEFOLDER
    SET FOLDER=web_server_custom
    CALL :COMPILEFOLDER
    SET FOLDER=core
    CALL :COMPILEFOLDER
    SET FOLDER=validation
    CALL :COMPILEFOLDER

    ECHO.
    ECHO    Successfully compiled all java files
    ECHO.
GOTO EOF

:COMPILEFOLDER
    FOR %%i IN ("./%FOLDER%/*.java") DO (
        SET FILE=%FOLDER%/%%~ni
        CALL :COMPILE
    )
GOTO EOF

:COMPILE
    IF EXIST "%FILE%.class" (
        ECHO        Already compiled    %FILE%
    )
    IF NOT EXIST "%FILE%.class" (
        ECHO        Compiling           %FILE%
        javac %FILE%.java
    )
GOTO EOF

:MAIN_CLASS
    IF "%~2"=="" (
        ECHO.
        ECHO    Must provide file to run
        ECHO.
        GOTO HELP
    )
    IF "%~2"=="-a" (

        IF "%~3"=="" (
            ECHO.
            ECHO    Must provide file to run
            ECHO.
            GOTO HELP
        )

        SET MAIN_CLASS=%3

    )
    IF NOT "%~2"=="-a" (
        IF NOT "%~3"=="-a" (
            CALL :REMOVE
            SET FILE=%2
            CALL :COMPILE
        )

        SET MAIN_CLASS=%2
    )

    ECHO.
    ECHO    Running main method of %MAIN_CLASS% ...
    ECHO.
    ECHO ============================================================
    java %MAIN_CLASS%

GOTO EOF

:CLI
    IF NOT "%~2"=="-a" (
        CALL :REMOVE
        SET FILE=cli/FamilyTreeCLI
        CALL :COMPILE
    )

    ECHO.
    ECHO    Running command line interface...
    ECHO.
    ECHO ============================================================
    java cli/FamilyTreeCLI

GOTO EOF

:GUI
    SET PORT="8080"
    IF NOT "%~2"=="-a" (
        IF NOT "%~3"=="-a" (
            CALL :REMOVE
            SET FILE=web_server_custom/WebServer
            CALL :COMPILE
        )
        IF NOT "%~2"=="" (
            SET PORT = %2
        )
    )
    IF NOT "%~3"=="" (
        SET PORT = %3
    )

    ECHO.
    ECHO    Running graphical user interface...
    ECHO.
    ECHO ============================================================
    java web_server_custom/WebServer %PORT%

GOTO EOF

:TEST
    IF NOT "%~2"=="-a" (
        CALL :REMOVE
        SET FILE=test/Controller
        CALL :COMPILE
    )

    ECHO.
    ECHO    Running tests...
    ECHO.
    ECHO ============================================================
    java test/Controller

GOTO EOF

:EOF