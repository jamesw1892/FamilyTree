from Validation import SpecNumRange, SpecStr, assert_valid
from Classes import Unknown, Person

FILENAME = "People.csv"
FIELDS = (
    # Field Name        Attribute Name  Spec
    ("ID",              "ID",           SpecNumRange(1, restrict_to_int=True)),
    ("First Name",      "nameFirst",    SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?"])),
    ("Middle Names",    "nameMiddles",  SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?", ""])),
    ("Last Name",       "nameLast",     SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?"])),
    ("Is Male",         "isMale",       SpecStr(["t", "f"], extra_values=["?"])),
    ("Birth Day",       "birthDay",     SpecNumRange(1, 31, restrict_to_int=True, extra_values=["?"])),
    ("Birth Month",     "birthMonth",   SpecNumRange(1, 12, restrict_to_int=True, extra_values=["?"])),
    ("Birth Year",      "birthYear",    SpecNumRange(restrict_to_int=True, extra_values=["?"])),
    ("Is Living",       "isLiving",     SpecStr(["t", "f"], extra_values=["?"])),
    ("Death Day",       "deathDay",     SpecNumRange(1, 31, restrict_to_int=True, extra_values=["?", ""])),
    ("Death Month",     "deathMonth",   SpecNumRange(1, 12, restrict_to_int=True, extra_values=["?", ""])),
    ("Death Year",      "deathYear",    SpecNumRange(restrict_to_int=True, extra_values=["?", ""])),
    ("Mother ID",       "motherID",     SpecNumRange(1, restrict_to_int=True, extra_values=["?"])),
    ("Father ID",       "fatherID",     SpecNumRange(1, restrict_to_int=True, extra_values=["?"]))
)

def lineToPerson(line):
    """Convert a line from the file into a 'Person' object"""

    if len(line) != len(FIELDS):
        raise Exception("Expected {} fields in line {} but got {}".format(len(FIELDS), line, len(line)))

    fields = []
    for i in range(len(line)):
        value = assert_valid(line[i], FIELDS[i][2])
        if value == "?":
            value = Unknown()
        elif value == "":
            value = None
        elif isinstance(value, str):
            value = value.title()
        fields.append(value)

    return Person(*fields)

def personToLine(person):
    """Convert a 'Person' object into a line to write to the file"""

    fields = []
    for field in [person.ID, person.nameFirst, person.nameMiddles, person.nameLast, person.isMale, person.birthDay, person.birthMonth, person.birthYear, person.isLiving, person.deathDay, person.deathMonth, person.deathYear, person.motherID, person.fatherID]:#
        if field is None:
            fields.append("")
        elif isinstance(field, Unknown):
            fields.append("?")
        elif isinstance(field, bool):
            if field:
                fields.append("t")
            else:
                fields.append("f")
        elif isinstance(field, int):
            fields.append(str(field))
        else:
            fields.append(field)

    return ",".join(fields)

def read():
    """Return a list of 'Person' objects from the file"""
    try:
        with open(FILENAME) as f:
            content = f.readlines()[1:]
    except FileNotFoundError:
        content = []

    people = []
    for line in content:
        line = [field.strip() for field in line.split(",")]
        people.append(lineToPerson(line))

    return people

def writeEverything(people):
    """Overwrite the current file with the given people"""
    
    with open(FILENAME, "w") as f:
        f.write(",".join([field[1] for field in FIELDS]) + "\n")
        for person in people:
            f.write(personToLine(person) + "\n")

def writeChangesOnly(changes):
    """Update the current file to have the given changes"""
    # TODO: make it so always stored in order of ID (but may be some missing) so can
    # binary search on the lines (if possible to select lines like this using I/O)
    pass

print(",".join([field[1] for field in FIELDS]))
