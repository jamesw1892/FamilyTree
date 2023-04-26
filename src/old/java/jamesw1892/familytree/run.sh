#!/bin/bash

# TODO: Add -q option to not print anything

# Check in correct directory
cd $(dirname "${BASH_SOURCE[0]}")

printUsage() {
    cat << EOF

Usage: $0 COMMAND [OPTIONS]
Run the family tree program.

COMMANDS:
    -r                      Remove all class files
    -b                      Compile all java files
    -m [-a] FILE [ARGS]     Run main function of specified java FILE - must be absolute path or relative path from this script and not include the .java extension. All arguments after FILE are passed onto java so if -a is provided, it must be before the file.
    -g [-a] [PORT]          Run custom web server. Optionally provide the PORT between 0 and 65535. By default choose 8080. If 0 is chosen then a random port will be assigned. In any case, a link to the website including the port will be printed once the server has started.
    -c [-a]                 Run command-line interface
    -t [-a]                 Run tests

Provide the optional -a argument to many of the commands to assume the necessary files are already compiled. If given, it must be directly after the command. Otherwise will first remove all compiled files and then recompile the necessary files.

Note that -g, -c, and -t all internally call -m so all arguments (except -a if provided directly after COMMAND) are passed onto the java file that runs custom web server, command-line interface, and tests respectively. Any arguments that these java files expect are described above, but all error checking and handling of these arguments is done within the java file and not this script - it just blindly passes on all arguments.

EOF
}

removeAll() {
    # Remove all files with the .class extension in all directories and sub-
    # directories of the current directory (location of script).
    echo -n "Removing all class files... "
    find . -name \*.class -type f -delete
    echo "done"
}

buildAll() {
    # Call the removeAll function and then run javac on all files with the .java
    # extension in all directories and sub-directories of the current directory
    # (location of script).
    removeAll
    echo -n "Compiling all java files... "
    find . -name \*.java -type f -exec javac {} +
    echo "done"
}

runMain() {
    # Run the main function of the given Java file. The first argument may be -a
    # to indicate not to rebuild but assume the necessary files have already
    # been compiled. The first argument that is not -a is assumed to be the file
    # to run and all subsequent arguments are blindly passed to it when running.
    shouldRebuild=1
    providedFile=0
    otherArgs=""

    # Go through all arguments except the command which we know is -m
    for arg in $@; do

        # If we have already seen a file then accumulate the arguments to pass
        # to java
        if [ $providedFile -eq 1 ]; then
            otherArgs="$otherArgs $arg"

        # If we haven't already seen a file and the argument is -a then remember
        # not to rebuild
        elif [ "$arg" == "-a" ]; then
            shouldRebuild=0

        # If we haven't already seen a file and the argument is not -a then
        # assume it is the file so check the file exists. If so, remember it.
        elif [ -f "$arg.java" ]; then
            filename="$arg"
            providedFile=1

        # Otherwise, error
        else
            echo "$arg.java does not exist, check file name and path" >&2
            printUsage
            exit 1
        fi
    done

    # Error if no file was provided
    if [ $providedFile -eq 0 ]; then
        echo "Must provide the filename of the main class to run" >&2
        printUsage
        exit 1
    fi

    # If -a is not provided then remove all class files and compile the target
    if [ $shouldRebuild -eq 1 ]; then
        removeAll
        echo -n "Compiling $filename.java... "
        javac $filename.java
        echo "done"
    fi

    # Run the target and pass in the other arguments
    echo -e "Running 'java $filename$otherArgs'...\n============================================================\n"
    java $filename $otherArgs
}

runParticular() {
    # The first argument is a file to run. The second argument may be -a. All
    # subsequent arguments are passed to the java file we are running.
    # This function swaps the -a and file arguments if -a is present before
    # passing all arguments to the runMain function.
    if [ "$2" == "-a" ]; then
        runMain -a $1 ${@:3}
    else
        runMain $1 ${@:2}
    fi
}

case "$1" in
    "-r") removeAll ;;
    "-b") buildAll ;;
    "-m") runMain ${@:2} ;;
    "-g") runParticular web_server_custom/WebServer ${@:2} ;;
    "-c") runParticular cli/FamilyTreeCLI ${@:2} ;;
    "-t") runParticular test/Controller ${@:2} ;;
    *)
        printUsage
        exit 1
        ;;
esac
