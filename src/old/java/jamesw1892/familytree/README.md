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

# How it works

The batch file `run.bat` can be used to run the program, use `run -h` for the commands.

People are stored in a .csv file. They were stored in a database in the old Python version but this is not very easy to human read and I only need a single table anyway so don't get the benefits of a database.

Fundamentally, this progam stores and displays people. For each person, the following real data is stored about them and in addition, we store an ID, their mother's ID and their father's ID. This is deliberately the only way we link people together to keep it simple. We do not store children, although this can be worked out from searching through all people for who's mother ID or father ID matches the desired person. We also do not store partners of any kind or anything else. We store biological sex only to verify mother/father links. Any other information such as gender, marriages, etc can be recorded as text in the notes section.

## Person Data

For each person, we store the following real data:

- **First name:** string
- **Middle name(s):** string, space separated, can be empty
- **Last name:** string
- **Is Male:** boolean (biological), can be null meaning unknown
- **Birth year:** integer between 1000 and 9999 inclusive, can be null meaning unknown
- **Birth month:** integer between 1 and 12 inclusive, can be null meaning unknown
- **Birth day:** integer between 1 and 31 inclusive but depends on the month, can be null meaning unknown
- **Is Living:** boolean, can be null meaning unknown
- **Death year:** integer between 1000 and 9999 inclusive, can be null meaning unknown
- **Death month:** integer between 1 and 12 inclusive, can be null meaning unknown
- **Death day:** integer between 1 and 31 inclusive but depends on the month, can be null meaning unknown
- **Notes:** string - a paragraph or two describing things about them, things that don't have a separate field can be included here

And the following meta data:

- **ID:** integer >=1 uniquely identifying that person
- **Mother ID:** integer >=0 identifying their mother's ID or 0 meaning unknown
- **Father ID:** integer >=0 identifying their father's ID or 0 meaning unknown

## Calculating Children

Because of the simple nature of the program where the only link between people are mother and father, it is computationally expensive to calculate children. The only method is to search through every person and see if one of their parents is the desired person. To save time, we do this once at the start of the program - when we read in the data from the database. Then we store the IDs of each person's children so they can be efficiently looked-up. To do this, the `MultiMap` class is used to efficiently but temporarily store children of people that haven't been processed until they are.