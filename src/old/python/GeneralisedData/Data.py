from Validation import Spec

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

##############################################################################

# from Classes import Unknown

# def typeUnknownFileToProgram(f):
#     if f == "?":
#         return Unknown()
#     else:
#         raise ValueError("Should be '?'")

# def typeUnknownProgramToFile(p):
#     if isinstance(p, Unknown):
#         return "?"
#     else:
#         raise ValueError("Should be type 'Unknown'")

# typeUnknown = Conversion(typeUnknownFileToProgram, typeUnknownProgramToFile)

##########################################################################

# from itertools import islice
# import ast

# def getFieldnames(csvFile):
#     """
#     Read the first row and store values in a tuple
#     """
#     with open(csvFile) as csvfile:
#         firstRow = csvfile.readlines(1)
#         fieldnames = tuple(firstRow[0].strip('\n').split("\t"))
#     return fieldnames

# def writeCursor(csvFile, fieldnames):
#     """
#     Convert csv rows into an array of dictionaries
#     All data types are automatically checked and converted
#     """
#     cursor = []  # Placeholder for the dictionaries/documents
#     with open(csvFile) as csvFile:
#         for row in islice(csvFile, 1, None):
#             values = list(row.strip('\n').split("\t"))
#             for i, value in enumerate(values):
#                 nValue = ast.literal_eval(value)
#                 values[i] = nValue
#             cursor.append(dict(zip(fieldnames, values)))
#     return cursor
