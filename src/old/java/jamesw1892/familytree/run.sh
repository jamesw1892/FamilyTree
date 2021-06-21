#!/bin/bash

function help {
    echo ""
    echo "USAGE:"
    echo "./run.sh -h                 Display this help"
    echo "./run.sh -r                 Remove all compiled files"
    echo "./run.sh -b [-a]            Compile all files"
    echo "./run.sh -m file [-a]       Run main function of specified file - file must be absolute path or relative path from this batch file and filename without .java extension"
    echo "./run.sh -c [-a]            Run cli"
    echo "./run.sh -g [port] [-a]     Run custom web server - optional port to run the webserver on, default: 8080"
    echo "./run.sh -t [-a]            Run tests"
    echo ""
    echo "Provide the optional -a argument if already compiled. Otherwise will first remove all compiled files and compile the relevant file(s)"
    echo ""
}

if test $# = 0
then
    echo "Must provide an argument"
    help
else
    for p in $*
    do
        case $p in
            "-a")
                echo "a"
                ;;
            "-h")
                help
                ;;
            *)
                echo "Unknown argument"
                help
                ;;
        esac
    done
fi