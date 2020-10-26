"""
Facilitates interaction with a database to read and write family members to form a family tree

TODO:
Split everything up into functions which do certain things, e.g.: formatDOB, formatDOD (which can call DOB if not alive), etc.
Separate interfaces and backend so can have command line interface and gui with same backend.
Separate file reading
Use validation to check exact form of inputs
"""

from Validation import validate_input, SpecStr, SpecNumRange, date, true_false
from sqlite3 import OperationalError
from Database import Database
import datetime
from texttable import Texttable
from CommandLineTools import menuLoop

def formatDay(day):

    suffix = "st" if day % 10 == 1 else "nd" if day % 10 == 2 else "rd" if day % 10 == 3 else "th"

    return str(day) + suffix

def formatMonth(monthNum):

    MONTHS = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]

    return MONTHS[monthNum - 1]

def display_table(content, headers=None):

    if headers is None:
        headers = []

    if content:

        print("\n\n")
        table = Texttable()
        table.set_deco(Texttable.BORDER | Texttable.HEADER | Texttable.VLINES)
        table.add_rows([headers] + content)
        table.set_max_width(0)
        print(table.draw())

    else:
        print("\nNo family members yet\n")

class Family:

    count = 1   # ID 0 is reserved for unknown parent references
    members = {}

    def __init__(self, name):
        self.name = name

    def add_member(self, name_firsts, name_last, is_male, DOB, DOD, mother_ID=0, father_ID=0, ID=None):

        if ID is None:
            while self.count in self.members:
                self.count += 1
            self.members[self.count] = Person(self.count, name_firsts, name_last, is_male, DOB, DOD, mother_ID, father_ID)
            self.count += 1
        else:
            self.members[ID] = Person(self.count, name_firsts, name_last, is_male, DOB, DOD, mother_ID, father_ID)

    def display_members(self):

        rows = []
        for ID in self.members:
            m = self.members[ID]
            dob = m.DOB.split("-")
            if m.DOD != "N/A":
                dod = m.DOD.split("-")
            age = m.get_age()
            rows.append([ID, m.name_full, "Male" if m.is_male else "Female", "{}/{}/{}".format(dob[2], dob[1], dob[0]), "{}/{}/{}".format(dod[2], dod[1], dod[0]) if m.DOD != "N/A" else "Living", "Unknown" if age is None else age, m.mother_ID if m.mother_ID != 0 else "Undefined", m.father_ID if m.father_ID != 0 else "Undefined"])

        display_table(rows, ["ID", "Name", "Sex", "Date of Birth", "Date of Death", "Age", "Mother ID", "Father ID"])

        return len(self.members)

    def display_members_birthdays(self):

        today = datetime.date.today()
        rows = []
        for ID in self.members:

            m = self.members[ID]
            year, month, day = [comp for comp in m.DOB.split("-")]

            if m.DOD == "N/A" and month != "?" and day != "?":

                    month = int(month)
                    day = int(day)

                    diff = (datetime.date(today.year, month, day) - today).days

                    if diff < 0:
                        diff = (datetime.date(today.year + 1, month, day) - today).days

                    age = m.get_age()
                    if age is None:
                        age = "Unknown"
                    elif diff != 0:
                        age += 1

                    details = [diff, m.name_full, "{} {}".format(formatDay(day), formatMonth(month)), age]

                    for row in rows.copy():
                        if row[0] == "?" or row[0] > diff:
                            rows.insert(0, details)
                            break
                    else:
                        rows.append(details)

        rows = sorted(rows, key=lambda row: row[0] if row[0] != "?" else 365)

        display_table(rows, ["Days until birthday", "Name", "Birthday", "Age on birthday"])

    def add_parent_ID(self, child_ID, mother_ID=None, father_ID=None):

        if not(mother_ID is None and father_ID is None):
            if child_ID in self.members:
                if mother_ID is None:
                    if father_ID == 0 or father_ID in self.members:
                        self.members[child_ID].father_ID = father_ID
                    else:
                        raise ValueError("Father does not exist yet")
                elif father_ID is None:
                    if mother_ID == 0 or mother_ID in self.members:
                        self.members[child_ID].mother_ID = mother_ID
                    else:
                        raise ValueError("Mother does not exist yet")
                else:
                    if (mother_ID == 0 or mother_ID in self.members) and (father_ID == 0 or father_ID in self.members):
                        self.members[child_ID].mother_ID = mother_ID
                        self.members[child_ID].father_ID = father_ID
                    else:
                        raise ValueError("Mother or father does not exist yet")
            else:
                raise ValueError("Child does not exist yet")

    def __repr__(self):

        s = self.name + " family:"
        for ID in self.members:
            s += "\n- " + self.members[ID].name

        return s

    def write_db(self):

        family = FamilyTree(self.name)
        try:
            family.delete_family()
        except OperationalError:
            pass

        family.create_family()
        for ID in self.members:
            member = self.members[ID]
            family.add_member(ID, member.name_firsts, member.name_last, member.is_male, member.DOB, member.DOD, member.mother_ID, member.father_ID)

class Person:

    def __init__(self, ID, name_firsts, name_last, is_male, DOB, DOD, mother_ID=0, father_ID=0):

        #passed attributes
        self.ID = ID
        self.name_firsts = name_firsts
        self.name_last = name_last
        self.is_male = is_male
        self.DOB = DOB
        self.DOD = DOD
        self.mother_ID = mother_ID
        self.father_ID = father_ID

        #calculated attributes
        self.name_full = self.name_firsts + " " + self.name_last
        self.is_alive = DOD.upper() == "N/A"

    def get_age(self):

        if any([i == "?" for i in self.DOB.split("-")]):
            return None

        if self.is_alive:
            current_date = datetime.date.today()
            current_year = current_date.year
            current_month = current_date.month
            current_day = current_date.day

        else:
            if any([i == "?" for i in self.DOD.split("-")]):
                return None
            current_date = [int(i) for i in self.DOD.split("-")]
            current_year = current_date[0]
            current_month = current_date[1]
            current_day = current_date[2]

        date = [int(i) for i in self.DOB.split("-")]
        year = date[0]
        month = date[1]
        day = date[2]

        diff = current_year - year

        if current_month < month or (month == current_month and current_day < day):
            diff -= 1

        return diff

class FamilyTree:

    def __init__(self, name):
        self.db = Database("Families.db")
        self.name = name

    def create_family(self):
        self.db.execute("""
            CREATE TABLE {}(
                ID integer,
                name_firsts text,
                name_last text,
                is_male int,
                DOB text,
                DOD text,
                mother_ID integer,
                father_ID integer,
                Primary Key(ID));""".format(self.name))

    def add_member(self, ID, name_firsts, name_last, is_male, DOB, DOD, mother_ID, father_ID):
        self.db.execute("""
            INSERT INTO {} VALUES(
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
        """.format(self.name), (ID, name_firsts, name_last, is_male, DOB, DOD, mother_ID, father_ID))

    def delete_family(self):
        self.db.execute("DROP TABLE {}".format(self.name))

    def read_family(self):
        try:
            return self.db.execute("SELECT * FROM {}".format(self.name))
        except OperationalError:
            return []

def read_family(name):

    family = Family(name)

    for member in FamilyTree(name).read_family():
        family.add_member(*member[1:], member[0])

    return family

def view_family(name):
    read_family(name).display_members()

def view_birthdays(name):
    read_family(name).display_members_birthdays()

def input_member(family):
    family.add_member(input("First Name(s): "), input("Last Name (Maiden Name): "), validate_input(SpecStr(["m", "f"], True), "Sex (M/F): ").upper() == "M", date("Date of birth (DOB):", False), "N/A" if true_false("Are they alive? ") else date("Date of death (DOD):", False))

def add_family(name):

    family = read_family(name)

    more = True
    while more:

        input_member(family)
        family.display_members()
        more = true_false("\nAdd another family member? ")

    family.write_db()

def link_family(name):

    family = read_family(name)

    more = True
    while more:
        family.display_members()
        print("\n")
        try:
            family.add_parent_ID(validate_input(SpecNumRange(1, restrict_to_int=True), "Child ID (person to add the parent to): "), validate_input(SpecNumRange(0, restrict_to_int=True), "Mother ID (0 if unsure): "), validate_input(SpecNumRange(0, restrict_to_int=True), "Father ID (0 if unsure): "))
        except ValueError as e:
            print("Error: {}".format(e))
        else:
            more = true_false("\nLink another family member? ")

    family.write_db()

def edit_family(name):

    family = read_family(name)

    more = True
    while more:
        max_ = family.display_members()
        print("\n")
        if max_ == 0:
            print("No members added yet!")
            more = False
        else:
            IDSpec = SpecNumRange(1, max_, restrict_to_int=True)
            ID = validate_input(IDSpec, "ID of member to edit: ")
            while ID not in family.members:
                print("Member does not exist, please try again")
                ID = validate_input(IDSpec, "ID of member to edit: ")

            att = validate_input(SpecStr(["first names", "last name", "sex", "dob", "dod"], to_lower=True), "Which attribute would you like to edit (first names/last name/sex/DOB/DOD)? ")
            if att == "first names":
                family.members[ID].name_firsts = input("First name: ")
            elif att == "last name":
                family.members[ID].name_last = input("Last Name (Maiden Name): ")
            elif att == "sex":
                family.members[ID].is_male = validate_input(SpecStr(["m", "f"], to_lower=True), "Sex (M/F): ").upper() == "M"
            elif att == "dob":
                family.members[ID].DOB = date("DOB (D/M/Y): ", False)
            elif att == "dod":
                family.members[ID].DOD = "N/A" if true_false("Are they alive? ") else date("DOD (D/M/Y): ", False)

            family.write_db()
            family = read_family(name)
            more = true_false("\nEdit another family member? ")

def menu():

    name = input("Family name: ")

    options = {
        "View family members": view_family,
        "View birthdays": view_birthdays,
        "Add family members": add_family,
        "Link family members": link_family,
        "Edit family members": edit_family
    }

    menuLoop(options, [name], "MENU:")

if __name__ == "__main__":
    menu()
