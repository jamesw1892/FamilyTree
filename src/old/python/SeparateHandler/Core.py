"""
TODO:
- Fix having global variables by not using menuLoop so can pass them back and forth
- Change input Person to not input ID so needs to automatically generate them
"""

from texttable import Texttable
from Handler import read, writeEverything, FIELDS
from Validation import validate_input, true_false
from Classes import Person, DISPLAY_FIELDS
from CommandLineTools import menuLoop

def inputPerson():

    fields = []
    for name, varName, spec in FIELDS:
        fields.append(validate_input(spec, name + ": "))

    return Person(*fields)

def addPeople(people):

    more = True
    while more:

        people.append(inputPerson())
        more = true_false("\nAdd another person? ")

def display_table(content, headers=None):
    """
    Display a table with the given content and, if provided, the given headers
    """

    if content:

        print("\n\n")
        table = Texttable()
        if headers is None:
            table.add_rows(content, False)
        else:
            table.add_rows([headers] + content)
        table.set_max_width(0)
        print(table.draw())

    else:
        print("\nNo people yet\n")

def displayPeople(people, fields):
    """Display the given details about the given people"""

    headers = []
    varNames = []
    for name, varName in fields:
        headers.append(name)
        varNames.append(varName)

    content = []
    for person in people:
        details = []
        for varName in varNames:
            details.append(getattr(person, varName))
        content.append(details)

    display_table(content, headers)

def displayPeopleAll(people):
    displayPeople(people, DISPLAY_FIELDS)

def displayPeopleSome(people):
    fields = inputFieldsToInclude()
    displayPeople(people, fields)

def displayPeopleData(people):
    displayPeople(people, [field[:2] for field in FIELDS])

def inputFieldsToInclude():
    fields = []
    for field in DISPLAY_FIELDS:
        if true_false("Would you like to display the person's {}? ".format(field[0])):
            fields.append(field)
    return fields

people = read()

options = {
    "View everything about everyone": displayPeopleAll,
    "View some things about everyone": displayPeopleSome,
    "View data stored on everyone": displayPeopleData,
    # "View birthdays": ,
    "Add people": addPeople,
    # "Link family members": ,
    # "Edit family members": 
}

menuLoop(options, [people], "MENU:")

writeEverything(people)
