package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.zip.DataFormatException;

/**
 * Instantiate to read the file.
 * Write to the file at the end or changes will not be saved.
 */
public class PersonStore {

    // Constants
    private static final String FILENAME = "./People.csv";
    private static final String HEADINGS = "ID,First Name,Middle Names,Last Name,Is Male,Birth Day,Birth Month,Birth Year,Is Living,Death Day,Death Month,Death Year,Mother ID,Father ID,Notes";

    // Comparators
    private static final ComparatorDaysUntilBirthday comparatorDaysUntilBirthday = new ComparatorDaysUntilBirthday();

    // Attributes
    private TreeSet<Person> peopleByID;
    private TreeSet<Person> peopleByDaysUntilBirthday;
    private TreeSet<Integer> unusedIDs;
    private int nextID;

    public PersonStore() throws IOException, DataFormatException {
        this.read();
    }

    public TreeSet<Person> getEveryoneByID(){
        return this.peopleByID;
    }

    public TreeSet<Person> getEveryoneByDaysUntilBirthday() {
        return this.peopleByDaysUntilBirthday;
    }

    public Person getMother(Person person) {
        if (person == null || person.getMotherID() == null) {
            return null;
        }
        return this.find(person.getMotherID());
    }

    public String formatMotherName(Person person) {
        Person mother = this.getMother(person);
        return mother == null ? "Unknown" : mother.formatNameFirstLast();
    }

    public Person getFather(Person person) {
        if (person == null || person.getFatherID() == null) {
            return null;
        }
        return this.find(person.getFatherID());
    }

    public String formatFatherName(Person person) {
        Person father = this.getFather(person);
        return father == null ? "Unknown" : father.formatNameFirstLast();
    }

    public HashSet<Person> getChildren(Person person) {
        if (person == null) {
            return null;
        }
        HashSet<Person> children = new HashSet<>();

        for (Integer childID: person.getChildrenIDs()) {
            Person child = this.find(childID);
            if (child != null) {
                children.add(child);
            }
        }

        return children;
    }

    public String formatChildrenNames(Person person) {
        if (person == null) {
            return null;
        }
        String out = "";
        for (Integer childID: person.getChildrenIDs()) {
            Person child = this.find(childID);
            if (child != null) {
                out += child.formatNameFirstLast() + ", ";
            }
        }
        if (out.equals("")) {
            // out = "None";
        } else {
            out = out.substring(0, out.length() - 2);
        }
        return out;
    }

    private int getNextIDToUse() {
        int IDToUse;
        if (this.unusedIDs.isEmpty()) {
            IDToUse = nextID;
            nextID++;
        } else {
            IDToUse = this.unusedIDs.pollFirst();
        }
        return IDToUse;
    }

    /**
     * Return the person with given ID or null if they do not exist
     * @param ID
     * @return
     */
    public Person find(int ID) {
        Person person = this.peopleByID.floor(Person.dummyPerson(ID));
        if (person == null || person.getID() != ID) {
            return null;
        }
        return person;
    }

    public Person findAssert(int ID) {
        Person person = this.find(ID);
        if (person == null) {
            throw new IllegalArgumentException("Person not found");
        }
        return person;
    }

    public void add(String nameFirst, String nameMiddles, String nameLast, Boolean isMale,
                    Integer birthYear, Integer birthMonth, Integer birthDay, Boolean isLiving,
                    Integer deathYear, Integer deathMonth, Integer deathDay, String notes) {
        Person person = new Person(this.getNextIDToUse(), nameFirst, nameMiddles, nameLast,
        isMale, birthYear, birthMonth, birthDay, isLiving,
        deathYear, deathMonth, deathDay, notes, null, null);
        this.peopleByID.add(person);
        this.peopleByDaysUntilBirthday.add(person);
    }

    public void delete(int ID) {
        for (Person person: this.peopleByID) {
            Integer mID = person.getMotherID();
            Integer fID = person.getFatherID();
            if (mID != null && mID.equals(ID)) {
                // person.setMotherID(null);
                throw new IllegalArgumentException("Cannot delete someone who is the mother of someone else");
            }
            if (fID != null && fID.equals(ID)) {
                // person.setFatherID(null);
                throw new IllegalArgumentException("Cannot delete someone who is the father of someone else");
            }
        }
        Person dummy = Person.dummyPerson(ID);
        this.peopleByID.remove(dummy);
        this.peopleByDaysUntilBirthday.remove(dummy);
    }

    public void link(int ID, Integer motherID, Integer fatherID) {
        this.link(this.findAssert(ID), motherID, fatherID);
    }

    public void link(Person person, Integer motherID, Integer fatherID) {
        if (motherID != null) {
            Boolean motherIsMale = this.findAssert(motherID).getIsMale();
            if (motherIsMale == null || motherIsMale) {
                throw new IllegalArgumentException("Mother must be a female");
            }
        }

        if (fatherID != null) {
            Boolean fatherIsMale = this.findAssert(fatherID).getIsMale();
            if (fatherIsMale == null || !fatherIsMale) {
                throw new IllegalArgumentException("Father must be a male");
            }
        }

        person.setMotherID(motherID);
        person.setFatherID(fatherID);
    }

    public void editAll(int ID, String nameFirst, String nameMiddles, String nameLast,
    Boolean isMale,   Integer birthYear, Integer birthMonth, Integer birthDay,
    Boolean isLiving, Integer deathYear, Integer deathMonth, Integer deathDay,
    String notes, Integer motherID, Integer fatherID) {
        Person person = this.findAssert(ID);
        this.editNames(person, nameFirst, nameMiddles, nameLast);
        this.editSex(person, isMale);
        this.editDOB(person, birthYear, birthMonth, birthDay);
        this.editDOD(person, isLiving, deathYear, deathMonth, deathDay);
        this.editNotes(person, notes);
        this.link(person, motherID, fatherID);
    }

    public void editNames(int ID, String nameFirst, String nameMiddles, String nameLast) {
        this.editNames(this.findAssert(ID), nameFirst, nameMiddles, nameLast);
    }

    public void editNames(Person person, String nameFirst, String nameMiddles, String nameLast) {
        person.setNameFirst(nameFirst);
        person.setNameMiddles(nameMiddles);
        person.setNameLast(nameLast);
    }

    public void editSex(int ID, Boolean isMale) {
        this.editSex(this.findAssert(ID), isMale);
    }

    public void editSex(Person person, Boolean isMale) {
        for (Person current: this.peopleByID) {
            Integer mID = current.getMotherID();
            Integer fID = current.getFatherID();
            if (mID != null && mID.equals(person.getID()) && (isMale == null || isMale)) {
                // current.setMotherID(null);
                throw new IllegalArgumentException("Cannot edit the sex of someone who is the mother of someone else so they are no longer female");
            }
            if (fID != null && fID.equals(person.getID()) && (isMale == null || !isMale)) {
                // current.setFatherID(null);
                throw new IllegalArgumentException("Cannot edit the sex of someone who is the father of someone else so they are no longer male");
            }
        }
        person.setIsMale(isMale);
    }

    public void editDOB(int ID, Integer birthYear, Integer birthMonth, Integer birthDay) {
        this.editDOB(this.findAssert(ID), birthYear, birthMonth, birthDay);
        
    }

    public void editDOB(Person person, Integer birthYear, Integer birthMonth, Integer birthDay) {
        person.setBirthYear(birthYear);
        person.setBirthMonth(birthMonth);
        person.setBirthDay(birthDay);
    }

    public void editDOD(int ID, Boolean isLiving, Integer deathYear, Integer deathMonth, Integer deathDay) {
        this.editDOD(this.findAssert(ID), isLiving, deathYear, deathMonth, deathDay);
    }

    public void editDOD(Person person, Boolean isLiving, Integer deathYear, Integer deathMonth, Integer deathDay) {
        person.setIsLiving(isLiving);
        person.setDeathYear(deathYear);
        person.setDeathMonth(deathMonth);
        person.setDeathDay(deathDay);
    }

    public void editNotes(int ID, String notes) {
        this.editNotes(this.findAssert(ID), notes);
    }

    public void editNotes(Person person, String notes) {
        person.setNotes(notes);
    }

    /**
     * Read all people from the file and get the next IDs to use for new people
     * Only called in the constructor
     */
    private void read() throws IOException, DataFormatException {

        this.peopleByID = new TreeSet<>();
        this.peopleByDaysUntilBirthday = new TreeSet<>(comparatorDaysUntilBirthday);
        this.unusedIDs = new TreeSet<>();
        this.nextID = 1;

        MultiMap<Integer, Integer> childrenToAdd = new MultiMap<>();

        BufferedReader br = new BufferedReader(new FileReader(FILENAME));

        String line;

        // skip the header
        br.readLine();

        int currentID = 0;

        while ((line = br.readLine()) != null) {
            if (!("".equals(line))) {
                String[] fields = line.split(",", -1);

                if (fields.length != 15) {
                    br.close();
                    throw new DataFormatException("Must be 15 fields per line");
                }

                currentID = Integer.parseInt(fields[0]);

                Person person = new Person(
                    currentID,
                    fields[1],
                    fields[2],
                    fields[3],
                    "".equals(fields[4]) ? null : Boolean.parseBoolean(fields[4]),
                    "".equals(fields[5]) ? null : Integer.parseInt(fields[5]),
                    "".equals(fields[6]) ? null : Integer.parseInt(fields[6]),
                    "".equals(fields[7]) ? null : Integer.parseInt(fields[7]),
                    "".equals(fields[8]) ? null : Boolean.parseBoolean(fields[8]),
                    "".equals(fields[9]) ? null : Integer.parseInt(fields[9]),
                    "".equals(fields[10]) ? null : Integer.parseInt(fields[10]),
                    "".equals(fields[11]) ? null : Integer.parseInt(fields[11]),
                    fields[14],
                    "".equals(fields[12]) ? null : Integer.parseInt(fields[12]),
                    "".equals(fields[13]) ? null : Integer.parseInt(fields[13])
                );

                // START adding children

                if (person.getMotherID() != null) {
                    Person mother = this.find(person.getMotherID());

                    // if the current person's mother is saved but they haven't
                    // been processed yet, add them to the multimap so the current
                    // person is saved as their child when they are processed
                    if (mother == null) {
                        childrenToAdd.add(person.getMotherID(), currentID);

                    // if the current person's mother has already been processed,
                    // add the current person as their child
                    } else {
                        mother.addChild(currentID);
                    }
                }

                if (person.getFatherID() != null) {
                    Person father = this.find(person.getFatherID());

                    // if the current person's father is saved but they haven't
                    // been processed yet, add them to the multimap so the current
                    // person is saved as their child when they are processed
                    if (father == null) {
                        childrenToAdd.add(person.getFatherID(), currentID);

                    // if the current person's father has already been processed,
                    // add the current person as their child
                    } else {
                        father.addChild(currentID);
                    }
                }

                // any children of the current person that were processed
                // before now are added to the current person
                HashSet<Integer> childrenIDs = childrenToAdd.getAll(currentID);
                if (childrenIDs != null) {
                    for (Integer childID: childrenIDs) {
                        person.addChild(childID);
                    }
                    childrenToAdd.remove(currentID);
                }

                // END adding children

                this.peopleByID.add(person);
                this.peopleByDaysUntilBirthday.add(person);

                if (currentID == this.nextID) {
                    this.nextID++;
                } else if (currentID > this.nextID) {
                    for (int i = this.nextID; i < currentID; i++) {
                        this.unusedIDs.add(i);
                    }
                    this.nextID = currentID + 1;
                } else if (this.unusedIDs.contains(currentID)) {
                    this.unusedIDs.remove(currentID);
                } else {
                    br.close();
                    throw new DataFormatException("Repeated ID");
                }
            }
        }

        br.close();
    }

    /**
     * Write all stored people to the file
     * Must be called in order to save data
     */
    public void write() throws IOException {
        String contents = HEADINGS;

        for (Person person: this.peopleByID) {
            contents += System.lineSeparator() + person.toFile();
        }

        FileWriter fileWriter = new FileWriter(FILENAME);
        fileWriter.write(contents);
        fileWriter.close();
    }
}