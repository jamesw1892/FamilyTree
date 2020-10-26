from CSVHandler import Unknown

def dateToString(day, month, year):
    # NOTE: uses UK date format
    day = str(day)
    if len(day) != 2:
        day = "0" + day

    month = str(month)
    if len(month) != 2:
        month = "0" + month

    return "{}/{}/{}".format(day, month, year)

class Person:
    def __init__(self, ID, nameFirst, nameMiddles, nameLast, isMale, birthDay, birthMonth, birthYear, isLiving, deathDay, deathMonth, deathYear, motherID, fatherID):

        # saved attributes
        self.ID = ID
        self.nameFirst = nameFirst
        self.nameMiddles = nameMiddles
        self.nameLast = nameLast
        self.isMale = isMale
        self.birthDay = birthDay
        self.birthMonth = birthMonth
        self.birthYear = birthYear
        self.isLiving = isLiving
        self.deathDay = deathDay
        self.deathMonth = deathMonth
        self.deathYear = deathYear
        self.motherID = motherID
        self.fatherID = fatherID

        # calculated attributes
        self.nameFull = self.getNameFull().title()
        self.sex = "Male" if isMale else "Female"
        self.DOB = dateToString(birthDay, birthMonth, birthYear)
        self.DOD = "Living" if isLiving else dateToString(deathDay, deathMonth, deathYear)
        self.age = self.getAge()

    def getNameFull(self):
        if self.nameMiddles is None:
            return self.nameFirst + " " + self.nameLast
        else:
            return self.nameFirst + " " + self.nameMiddles + " " + self.nameLast

    def getAge(self):

        if any([isinstance(part, Unknown) for part in self.DOB]):
            return None
