"""
Handles CSV files, automatically converting them to the correct datatypes - only built in datatypes

TODO:
- Add functionality to take in data as strings and automatically convert it to the correct datatypes using the specifications
"""

from Validation import assert_valid, Spec, SpecNumRange, SpecStr

class CSVHandler:
    """
    @param filename     Filename with extension
    """
    def __init__(self, filename, fieldSpecs, fieldNames=None, recordDelimiter="\n", fieldDelimiter=","):

        assert isinstance(filename, str), "filename must be a string"
        assert all([isinstance(fieldSpec, Spec) for fieldSpec in fieldSpecs]), "all fieldSpecs must be 'Spec's"
        assert fieldNames is None or all([isinstance(fieldName, str) for fieldName in fieldNames]), "if provided, all fieldNames must be strings"
        assert isinstance(recordDelimiter, str), "recordDelimiter must be a string"
        assert isinstance(fieldDelimiter, str), "fieldDelimiter must be a string"
        assert fieldNames is None or len(fieldNames) == len(fieldSpecs), "must be the same number of fields"

        self.filename = filename
        self.fieldNames = fieldNames
        self.fieldSpecs = fieldSpecs
        self.recordDelimiter = recordDelimiter
        self.fieldDelimiter = fieldDelimiter

    def read(self):
        try:
            with open(self.filename) as f:
                contents = f.read()
        except FileNotFoundError:
            return []

        records = [record.split(self.fieldDelimiter) for record in contents.split(self.recordDelimiter)]

        headingRecord, *otherRecords = contents.split(self.recordDelimiter)
        headingFields = headingRecord.split(self.fieldDelimiter)

        if self.fieldNames is None:
            self.fieldNames = headingFields
        else:
            assert list(self.fieldNames) == headingFields, "headings don't match"

        records = []
        for record in otherRecords: # slice ignores first row which is the heading row
            if record == "":
                continue
            fields = record.split(self.fieldDelimiter)
            assert len(fields) == len(self.fieldSpecs), "number of fields doesn't match"
            newRecord = []
            for count, field in enumerate(fields):
                newRecord.append(assert_valid(field, self.fieldSpecs[count], self.fieldNames[count]))
            records.append(newRecord)

        return records

    def write(self, records):

        assert self.fieldNames is not None, "if haven't provided field names, must read first"

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
    SpecStr(["True", "False"], extra_values=["?"]),
    SpecNumRange(1, 31, restrict_to_int=True, extra_values=["?"]),
    SpecNumRange(1, 12, restrict_to_int=True, extra_values=["?"]),
    SpecNumRange(restrict_to_int=True, extra_values=["?"]),
    SpecStr(["True", "False"], extra_values=["?"]),
    SpecNumRange(1, 31, restrict_to_int=True, extra_values=["?", ""]),
    SpecNumRange(1, 12, restrict_to_int=True, extra_values=["?", ""]),
    SpecNumRange(restrict_to_int=True, extra_values=["?", ""]),
    SpecNumRange(1, restrict_to_int=True, extra_values=["?"]),
    SpecNumRange(1, restrict_to_int=True, extra_values=["?"])
)

i = CSVHandler("test.csv", fieldSpecifications, fieldNames)
for _ in i.read():
    print(_)
# i.write((
#     (1, "F", "M", "L", True, 1, 1, 1970, True, None, None, None, Unknown(), "?", "?")
# ))