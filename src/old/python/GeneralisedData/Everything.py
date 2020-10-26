from Validation import SpecStr, SpecNumRange, specTrueFalse
from CSVHandler import Data, CSVHandler

FILENAME = "People.csv"

funcId = lambda x: x
funcTitle = lambda s: s.title()
funcBool = lambda b: b in ["t", "true", "y", "yes", "1", "on", "enabled", "enable"]

data = Data(Person, (
    # Field Name        Attribute Name  Input Spec                                                                          funcInputToProg     funcProgToOutput        funcProgToFile      funcFileToProg
    ("ID",              "ID",           SpecNumRange(1, restrict_to_int=True),                                              funcId,             str,                    str,                funcId),
    ("First Name",      "nameFirst",    SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?"]),      funcTitle,          funcId,                 funcId,             funcTitle),
    ("Middle Names",    "nameMiddles",  SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?", ""]),  funcTitle,          funcId,                 funcId,             funcTitle),
    ("Last Name",       "nameLast",     SpecStr(allowed_chars=list("abcdefghijklmnopqrstuvwxyz"), extra_values=["?"]),      funcTitle,          funcId,                 funcId,             funcTitle),
    ("Is Male",         "isMale",       specTrueFalse,                                                                      funcBool,           str,                    str,                funcBool),
    ("Birth Day",       "birthDay",     SpecNumRange(1, 31, restrict_to_int=True, extra_values=["?"]),                      funcId,             str,                    str,                funcId),
    ("Birth Month",     "birthMonth",   SpecNumRange(1, 12, restrict_to_int=True, extra_values=["?"]),                      funcId,             str,                    str,                funcId),
    ("Birth Year",      "birthYear",    SpecNumRange(restrict_to_int=True, extra_values=["?"]),                             funcId,             str,                    str,                funcId),
    ("Is Living",       "isLiving",     specTrueFalse,                                                                      funcBool,           str,                    str,                funcBool),
    ("Death Day",       "deathDay",     SpecNumRange(1, 31, restrict_to_int=True, extra_values=["?", ""]),                  funcId,             str,                    str,                funcId),
    ("Death Month",     "deathMonth",   SpecNumRange(1, 12, restrict_to_int=True, extra_values=["?", ""]),                  funcId,             str,                    str,                funcId),
    ("Death Year",      "deathYear",    SpecNumRange(restrict_to_int=True, extra_values=["?", ""]),                         funcId,             str,                    str,                funcId),
    ("Mother ID",       "motherID",     SpecNumRange(1, restrict_to_int=True, extra_values=["?"]),                          funcId,             str,                    str,                funcId),
    ("Father ID",       "fatherID",     SpecNumRange(1, restrict_to_int=True, extra_values=["?"]),                          funcId,             str,                    str,                funcId)
))

handler = CSVHandler(FILENAME, data)
handler.read()