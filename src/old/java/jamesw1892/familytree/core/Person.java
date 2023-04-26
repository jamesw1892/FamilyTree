package core;

import java.time.LocalDate;
import java.util.HashSet;

/**
 * Methods starting with 'get' get the data in its original datatype
 * whereas methods starting with 'format' format it as a string
 */
public class Person implements Comparable<Person> {

    //                                      Can be null?        Other restrictions
    private final int ID;               //  N                   > 0
    private String nameFirst;           //  N (empty instead)
    private String nameMiddles;         //  N (empty instead)
    private String nameLast;            //  N (empty instead)
    private Boolean isMale;             //  Y
    private Integer birthYear;          //  Y                   [1000, 9999]
    private Integer birthMonth;         //  Y                   [1, 12]
    private Integer birthDay;           //  Y                   [1, 31] but depends on month and year
    private Boolean isLiving;           //  Y
    private Integer deathYear;          //  Y                   [1000, 9999]
    private Integer deathMonth;         //  Y                   [1, 12]
    private Integer deathDay;           //  Y                   [1, 31] but depends on month and year
    private String notes;               //  N (empty instead)
    private Integer motherID;           //  Y                   > 0
    private Integer fatherID;           //  Y                   > 0
    private HashSet<Integer> children;  //  N (empty instead)

    public Person(int ID, String nameFirst, String nameMiddles, String nameLast,
                  Boolean isMale,   Integer birthYear, Integer birthMonth, Integer birthDay,
                  Boolean isLiving, Integer deathYear, Integer deathMonth, Integer deathDay,
                  String notes, Integer motherID, Integer fatherID) {

        if (ID < 1) {
            throw new IllegalArgumentException("ID must be greater than 0");
        }
        this.ID = ID;

        this.setNameFirst(nameFirst);
        this.setNameMiddles(nameMiddles);
        this.setNameLast(nameLast);
        this.setIsMale(isMale);
        this.setBirthYear(birthYear);
        this.setBirthMonth(birthMonth);
        this.setBirthDay(birthDay);
        this.setIsLiving(isLiving);
        this.setDeathYear(deathYear);
        this.setDeathMonth(deathMonth);
        this.setDeathDay(deathDay);
        this.setNotes(notes);
        this.setMotherID(motherID);
        this.setFatherID(fatherID);
        this.children = new HashSet<>();
    }

    public static Person dummyPerson(int ID) {
        return new Person(ID, "", "", "", null, null, null, null, null, null, null, null, "", null, null);
    }

    // setters
    protected void setNameFirst(String nameFirst) {
        if (nameFirst == null) {
            throw new IllegalArgumentException("nameFirst must not be null");
        }
        this.nameFirst = Util.toTitleCase(Util.normaliseString(nameFirst));
    }

    protected void setNameMiddles(String nameMiddles) {
        if (nameMiddles == null) {
            throw new IllegalArgumentException("nameMiddles must not be null");
        }
        this.nameMiddles = Util.toTitleCase(Util.normaliseString(nameMiddles));
    }

    protected void setNameLast(String nameLast) {
        if (nameLast == null) {
            throw new IllegalArgumentException("nameLast must not be null");
        }
        this.nameLast = Util.toTitleCase(Util.normaliseString(nameLast));
    }

    protected void setIsMale(Boolean isMale) {
        this.isMale = isMale;
    }

    protected void setBirthYear(Integer birthYear) {
        if (birthYear != null && (birthYear < 1000 || birthYear > 9999)) {
            throw new IllegalArgumentException("birthYear must be between 1000 and 9999 inclusive");
        }
        this.birthYear = birthYear;
    }

    protected void setBirthMonth(Integer birthMonth) {
        if (birthMonth != null && (birthMonth < 1 || birthMonth > 12)) {
            throw new IllegalArgumentException("birthMonth must be between 1 and 12 inclusive");
        }
        this.birthMonth = birthMonth;
    }

    protected void setBirthDay(Integer birthDay) {
        if (birthDay != null) {
            int maxDay = Util.maxValidDay(this.birthYear, this.birthMonth);
            if (birthDay < 1 || birthDay > maxDay) {
                throw new IllegalArgumentException("birthDay must be between 1 and " + maxDay + " inclusive");
            }
        }
        this.birthDay = birthDay;
    }

    protected void setIsLiving(Boolean isLiving) {
        this.isLiving = isLiving;
    }

    protected void setDeathYear(Integer deathYear) {
        if (deathYear != null && (deathYear < 1000 || deathYear > 9999)) {
            throw new IllegalArgumentException("deathYear must be between 1000 and 9999 inclusive");
        }
        this.deathYear = deathYear;
    }

    protected void setDeathMonth(Integer deathMonth) {
        if (deathMonth != null && (deathMonth < 1 || deathMonth > 12)) {
            throw new IllegalArgumentException("deathMonth must be between 1 and 12 inclusive");
        }
        this.deathMonth = deathMonth;
    }

    protected void setDeathDay(Integer deathDay) {
        if (deathDay != null) {
            int maxDay = Util.maxValidDay(this.deathYear, this.deathMonth);
            if (deathDay < 1 || deathDay > maxDay) {
                throw new IllegalArgumentException("deathDay must be between 1 and " + maxDay + " inclusive");
            }
        }
        this.deathDay = deathDay;
    }

    protected void setNotes(String notes) {
        if (notes == null) {
            throw new IllegalArgumentException("Notes must not be null");
        }
        this.notes = Util.normaliseString(notes);
    }

    protected void setMotherID(Integer motherID) {
        if (motherID != null && motherID < 1) {
            throw new IllegalArgumentException("motherID must be greater than 0");
        }
        this.motherID = motherID;
    }

    protected void setFatherID(Integer fatherID) {
        if (fatherID != null && fatherID < 1) {
            throw new IllegalArgumentException("motherID must be greater than 0");
        }
        this.fatherID = fatherID;
    }

    protected void addChild(int childID) {
        this.children.add(childID);
    }

    protected void removeChild(int childID) {
        this.children.remove(childID);
    }

    // getters (data as original type)
    public int getID()                          { return this.ID; }
    public String getNameFirst()                { return this.nameFirst; }
    public String getNameMiddles()              { return this.nameMiddles; }
    public String getNameLast()                 { return this.nameLast; }
    public Boolean getIsMale()                  { return this.isMale; }
    public Integer getBirthYear()               { return this.birthYear; }
    public Integer getBirthMonth()              { return this.birthMonth; }
    public Integer getBirthDay()                { return this.birthDay; }
    public Boolean getIsLiving()                { return this.isLiving; }
    public Integer getDeathYear()               { return this.deathYear; }
    public Integer getDeathMonth()              { return this.deathMonth; }
    public Integer getDeathDay()                { return this.deathDay; }
    public String getNotes()                    { return this.notes; }
    public Integer getMotherID()                { return this.motherID; }
    public Integer getFatherID()                { return this.fatherID; }
    public HashSet<Integer> getChildrenIDs()    { return this.children; }

    // format-ers (data formatted as String)
    public String formatID() {
        return String.valueOf(this.getID());
    }

    public String formatNameFirst() {
        return this.getNameFirst();
    }

    public String formatNameMiddles() {
        return this.getNameMiddles();
    }

    public String formatNameLast() {
        return this.getNameLast();
    }

    public String formatNameFull() {
        String out = this.nameFirst + " ";
        if (!this.nameMiddles.equals("")) {
            out += this.nameMiddles + " ";
        }
        out += this.nameLast;

        return out.strip();
    }

    public String formatNameFirstLast() {
        return (this.nameFirst + " " + this.nameLast).strip();
    }

    public String formatSex() {
        if (this.isMale == null) {
            return "Unknown";
        }
        return this.isMale? "Male": "Female";
    }

    public String formatDateOfBirthShortEuropean() {
        return Util.dateShortEuropean(this.birthYear, this.birthMonth, this.birthDay);
    }

    public String formatDateOfBirthShortInternational() {
        return Util.dateShortInternational(this.birthYear, this.birthMonth, this.birthDay);
    }

    public String formatDateOfBirthLong() {
        return Util.dateLong(this.birthYear, this.birthMonth, this.birthDay);
    }

    public String formatBirthday() {
        return Util.dateLong(null, this.birthMonth, this.birthDay);
    }

    public String formatIsLiving() {
        if (this.isLiving == null) {
            return "Unknown";
        }
        return this.isLiving? "Living": "Deceased";
    }

    public String formatDateOfDeathShortEuropean() {
        if (this.isLiving == null) {
            return "Unknown";
        }
        return this.isLiving? "Living": Util.dateShortEuropean(this.deathYear, this.deathMonth, this.deathDay);
    }

    public String formatDateOfDeathShortInternational() {
        if (this.isLiving == null) {
            return "Unknown";
        }
        return this.isLiving? "Living": Util.dateShortInternational(this.deathYear, this.deathMonth, this.deathDay);
    }

    public String formatDateOfDeathLong() {
        if (this.isLiving == null) {
            return "Unknown";
        }
        return this.isLiving? "Living": Util.dateLong(this.deathYear, this.deathMonth, this.deathDay);
    }

    public Integer getAgeAtDeath() {
        return Util.calcAge(this.birthYear, this.birthMonth, this.birthDay, this.isLiving, this.deathYear, this.deathMonth, this.deathDay);
    }

    public String formatAgeAtDeath() {
        return Util.unknownOrValue(this.getAgeAtDeath());
    }

    public Integer getAgeIfWereAlive() {
        return Util.calcAge(this.birthYear, this.birthMonth, this.birthDay, true, this.deathYear, this.deathMonth, this.deathDay);
    }

    public String formatAgeIfWereAlive() {
        return Util.unknownOrValue(this.getAgeIfWereAlive());
    }

    /**
     * Ranges from 0 if birthday is today to 365 if leap year and birthday was yesterday
     * @return
     */
    public int getDaysUntilBirthday() {
        return Util.daysUntilBirthday(this.birthMonth, this.birthDay);
    }

    public String formatDaysUntilBirthday() {
        return String.valueOf(this.getDaysUntilBirthday());
    }

    /**
     * If were alive
     * Their age on their birthday is always 1 more than their age unless their
     * birthday is today. If it's unknown whether today is their birthday,
     * assume it's not.
     * @return
     */
    public Integer getAgeAtBirthday() {
        Integer age = this.getAgeIfWereAlive();
        if (age == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        int month = today.getMonth().getValue();
        if (this.birthMonth != null && this.birthMonth == month && this.birthDay != null && this.birthDay == day) {
            return age;
        } else {
            return age + 1;
        }
    }

    /**
     * If were alive
     * @return
     */
    public String formatAgeAtBirthday() {
        return Util.unknownOrValue(this.getAgeAtBirthday());
    }

    public String formatMotherID() {
        Integer id = this.getMotherID();
        if (id == null) {
            return "Unknown";
        }
        return String.valueOf(id);
    }

    public String formatFatherID() {
        Integer id = this.getFatherID();
        if (id == null) {
            return "Unknown";
        }
        return String.valueOf(id);
    }

    public String formatChildrenIDs() {
        String s = this.getChildrenIDs().toString();
        return s.substring(1, s.length() - 1);
    }

    public String formatNotes() {
        return this.getNotes();
    }

    // comparisons
    public int compareTo(Person other) {
        return Integer.compare(this.getID(), other.getID());
    }

    public int hashCode() {
        return this.ID;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Person other = (Person) obj;
        return this.ID == other.getID();
    }

    // format to write
    private static String optionalObjectToFile(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    public String toString() {
        return this.formatNameFirstLast();
    }

    public String toFile() {
        return  this.formatID()
        + "," + Util.URLEncode(this.nameFirst)
        + "," + Util.URLEncode(this.nameMiddles)
        + "," + Util.URLEncode(this.nameLast)
        + "," + optionalObjectToFile(this.isMale)
        + "," + optionalObjectToFile(this.birthYear)
        + "," + optionalObjectToFile(this.birthMonth)
        + "," + optionalObjectToFile(this.birthDay)
        + "," + optionalObjectToFile(this.isLiving)
        + "," + optionalObjectToFile(this.deathYear)
        + "," + optionalObjectToFile(this.deathMonth)
        + "," + optionalObjectToFile(this.deathDay)
        + "," + optionalObjectToFile(this.motherID)
        + "," + optionalObjectToFile(this.fatherID)
        + "," + Util.URLEncode(this.notes);
    }
}