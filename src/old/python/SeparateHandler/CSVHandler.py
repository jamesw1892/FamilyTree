"""
Handles CSV files, automatically converting them to the correct datatypes

TODO:
- Add funcitonality to take in data as strings and automatically convert it to the correct datatypes using the specifications
"""

from Validation import SpecStr, SpecNumRange
from Classes import Unknown

class CSVHandler:
    """
    @param filename     Filename with extension
    """
    def __init__(self, filename, fieldNames, fieldSpecifications, recordDelimiter="\n", fieldDelimiter=","):
        self.filename = filename
        self.fieldNames = fieldNames
        self.fieldSpecifications = fieldSpecifications
        self.recordDelimiter = recordDelimiter
        self.fieldDelimiter = fieldDelimiter

    def read(self):
        # ignore first row when reading
        try:
            with open(self.filename) as f:
                contents = f.read()
        except FileNotFoundError:
            return []

        records = [field.split(self.fieldDelimiter) for field in contents.split(self.recordDelimiter)]

        # TODO: convert to correct datatypes

        return records

    def write(self, records):
        with open(self.filename, "w") as f:
            f.write(self.fieldDelimiter.join(fieldNames) + self.recordDelimiter)
            for record in records:
                f.write(self.fieldDelimiter.join([str(field) for field in record]) + self.recordDelimiter)

fieldNames = ("ID", "First Name", "Middle Names", "Last Name", "Is Male", "Birth Day", "Birth Month", "Birth Year", "Is Living", "Death Day", "Death Month", "Death Year", "Mother ID", "Father ID")
fieldSpecifications = (
    SpecNumRange(1, restrict_to_int=True),
    SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?"]),
    SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?", ""]),
    SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?"]),
    SpecStr(["t", "f"], extra_values=["?"]),
    SpecNumRange(1, 31, restrict_to_int=True, extra_values=["?"]),
    SpecNumRange(1, 12, restrict_to_int=True, extra_values=["?"]),
    SpecNumRange(restrict_to_int=True, extra_values=["?"]),
    SpecStr(["t", "f"], extra_values=["?"]),
    SpecNumRange(1, 31, restrict_to_int=True, extra_values=["?", ""]),
    SpecNumRange(1, 12, restrict_to_int=True, extra_values=["?", ""]),
    SpecNumRange(restrict_to_int=True, extra_values=["?", ""]),
    SpecNumRange(1, restrict_to_int=True, extra_values=["?"]),
    SpecNumRange(1, restrict_to_int=True, extra_values=["?"])
)

i = CSVHandler("test.csv", fieldNames, fieldSpecifications)
i.write((
    (1, "F", "M", "L", True, 1, 1, 1970, True, None, None, None, Unknown(), "?", "?")
))
for _ in i.read():
    print(_)
