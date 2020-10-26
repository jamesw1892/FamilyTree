"""
Handles CSV files, automatically converting them to the correct datatypes - only built in datatypes

TODO:
- Add functionality to take in data as strings and automatically convert it to the correct datatypes using the specifications
"""

from Validation import assert_valid, Spec, SpecNumRange, SpecStr

class Data:
    def __init__(self, fields=None):
        self.fields = []
        if fields is not None:
            self.addFields(fields)

    def addFields(self, fields):
        for field in fields:
            assert len(field) == 7, "Must have correct number of descriptors"
            self.addField(*field)

    def addField(self, fieldName, varName, inputSpec, funcInputToProg, funcProgToOutput, funcProgToFile, funcFileToProg):
        self.fields.append(Field(fieldName, varName, inputSpec, funcInputToProg, funcProgToOutput, funcProgToFile, funcFileToProg))

class Field:
    def __init__(self, fieldName, varName, inputSpec, funcInputToProg, funcProgToOutput, funcProgToFile, funcFileToProg):
        assert isinstance(fieldName, str), "fieldName must be a string"
        assert isinstance(varName, str), "varName must be a string"
        assert isinstance(inputSpec, Spec), "inputSpec must be a Spec from Validation"
        self.fieldName = fieldName
        self.varName = varName
        self.inputSpec = inputSpec
        self.__funcInputToProg = funcFileToProg
        self.__funcProgToOutput = funcProgToOutput
        self.__funcProgToFile = funcProgToFile
        self.__funcFileToProg = funcFileToProg
    def inputToProg(self, input_):
        assert self.__funcInputToProg is not None, "Should not call this function"
        return self.__funcInputToProg(input_)
    def progToOutput(self, prog):
        assert self.__funcProgToOutput is not None, "Should not call this function"
        return self.__funcProgToOutput(prog)
    def progToFile(self, prog):
        assert self.__funcProgToFile is not None, "Should not call this function"
        return self.__funcProgToFile(prog)
    def fileToProg(self, file_):
        assert self.__funcFileToProg is not None, "Should not call this function"
        return self.__funcFileToProg(file_)

class Unknown:
    def __repr__(self):
        return "?"

class CSVHandler:
    """
    @param filename     Filename with extension
    """
    def __init__(self, filename, data, recordDelimiter="\n", fieldDelimiter=","):

        assert isinstance(filename, str), "filename must be a string"
        assert isinstance(data, Data), "data must be an instance of Data"
        assert isinstance(recordDelimiter, str), "recordDelimiter must be a string"
        assert isinstance(fieldDelimiter, str), "fieldDelimiter must be a string"

        self.filename = filename
        self.data = data
        self.recordDelimiter = recordDelimiter
        self.fieldDelimiter = fieldDelimiter

    def read(self):
        try:
            with open(self.filename) as f:
                contents = f.read()
        except FileNotFoundError:
            return []

        # records = [record.split(self.fieldDelimiter) for record in contents.split(self.recordDelimiter)]

        headingRecord, *otherRecords = contents.split(self.recordDelimiter)
        headingFields = headingRecord.split(self.fieldDelimiter)
        for count, heading in enumerate(headingFields):
            assert heading == self.data.fields[count].fieldName, "headings don't match"

        records = []
        for record in otherRecords: # slice ignores first row which is the heading row
            if record == "":
                continue
            fields = record.split(self.fieldDelimiter)
            assert len(fields) == len(self.data.fields), "number of fields doesn't match"
            newRecord = []
            for count, field in enumerate(fields):
                if field == "":
                    newRecord.append(None)
                elif field == "?":
                    newRecord.append(Unknown())
                else:
                    fieldData = self.data.fields[count]
                    newRecord.append(fieldData.fileToProg(assert_valid(field, fieldData.inputSpec, fieldData.fieldName)))
            records.append(newRecord)

        return records

    # def write(self, records):

    #     assert self.fieldNames is not None, "if haven't provided field names, must read first"

    #     with open(self.filename, "w") as f:
    #         f.write(self.fieldDelimiter.join(fieldNames) + self.recordDelimiter)
    #         for record in records:
    #             f.write(self.fieldDelimiter.join([str(field) for field in record]) + self.recordDelimiter)
