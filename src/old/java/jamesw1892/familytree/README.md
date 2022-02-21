# Old Family Tree - Java

Files:

- `cli`: Command-Line Interface files:
    - `FamilyTreeCLI.java`: The main CLI - a way to interact with the family tree program on the command line
    - `TextTable.java`: A port of the Python library `texttable` that formats data as a table for the command line
- `core`: The core of the program that reads, writes and formats the data:
    - `Person.java`: Class representing a person storing all data about them and providing methods to get and format this data
    - `PersonStore.java`: Stores the people and provides methods for interacting with them such as searching for a particular person. It also handles reading and writing the data from the backing store
    - `Util.java`: Provides methods for utility functions such as handling dates
    - `MultiMap.java`: Data structure used to efficiently store all the children of each person
    - `ComparatorDaysUntilBirthday.java`: Describes how to compare `Person` objects to order them by the number of days until their birthday
- `test`: Unit tests
- `validation`: A port of my Python Validation library - a helper library for the CLI that validates user input
- `web`: Web objects such as CSS styles, JS scripts, HTML files, favicon
- `web_server_custom`: Custom web server that dynamically generates HTML for each request and sends it:
    - `GUI.java`: The main GUI - a way to interact with the family tree program via the web
    - `WebServer.java`: Entry point, runs the server and passes requests to `Handler.java`. Initialises GUI and passes the instance to the handler each time.
    - `Handler.java`: Handles a single request to the web server
    - `Header.java`: Parse the headers of web requests
- `web_server_file`: PHP to do the same as the custom web server but on a standard web server that just serves files