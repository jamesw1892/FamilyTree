package core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
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
    private static final String HEADINGS = "ID,First Name,Middle Names,Last Name,Is Male,Birth Year,Birth Month,Birth Day,Is Living,Death Year,Death Month,Death Day,Mother ID,Father ID,Notes";

    // Comparators
    private static final ComparatorDaysUntilBirthday comparatorDaysUntilBirthday = new ComparatorDaysUntilBirthday();

    // Attributes
    private TreeSet<Person> peopleByID;
    private TreeSet<Person> peopleByDaysUntilBirthday;
    private TreeSet<Integer> unusedIDs;
    private int nextID;
    private LocalDate dateLastUpdatedPeopleByDaysUntilBirthday;

    public PersonStore() throws IOException, DataFormatException {
        this.read();
    }

    public TreeSet<Person> getEveryoneByID(){
        return this.peopleByID;
    }

    public TreeSet<Person> getEveryoneByDaysUntilBirthday() {

        // If the program is running for multiple days, the tree set ordering
        // will become wrong. So recalculate it if it hasn't been updated today.
        LocalDate today = LocalDate.now();
        if (!today.equals(this.dateLastUpdatedPeopleByDaysUntilBirthday)) {
            this.peopleByDaysUntilBirthday.clear();
            this.peopleByDaysUntilBirthday.addAll(this.peopleByID);
            this.dateLastUpdatedPeopleByDaysUntilBirthday = today;
        }

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

    /**
     * Return all people who share children with this person, not including
     * this person themself.
     * Return null if and only if this person is null.
     */
    public HashSet<Person> getPartners(Person person) {
        if (person == null) {
            return null;
        }
        HashSet<Person> partners = new HashSet<>();

        for (Person child: this.getChildren(person)) {
            Person[] parents = {this.getMother(child), this.getFather(child)};
            for (Person parent: parents) {
                if (parent != null && !parent.equals(person)) {
                    partners.add(parent);
                }
            }
        }
        return partners;
    }

    /**
     * Return all people who share parents with this person, not including this
     * person themself.
     * Return null if and only if this person is null.
     */
    public HashSet<Person> getSiblings(Person person) {
        if (person == null) {
            return null;
        }
        HashSet<Person> siblings = new HashSet<>();
        Person[] parents = {this.getMother(person), this.getFather(person)};

        for (Person parent: parents) {
            if (parent != null) {
                for (Person child: this.getChildren(parent)) {
                    if (!child.equals(person)) {
                        siblings.add(child);
                    }
                }
            }
        }
        return siblings;
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
     * Return the number of descendants of the given person using the given
     * cache of already calculated number of descendants.
     */
    private int numDescendants(Person person, HashMap<Integer, Integer> cache) {

        // If we have already calculated the number of descendants of this person
        // then use it
        Integer cached = cache.get(person.getID());
        if (cached != null) {
            return cached;
        }

        // Otherwise, sum the number of descendants of their children +1 per child
        int total = 0;
        for (int childID: person.getChildrenIDs()) {
            total += 1 + numDescendants(this.find(childID), cache);
        }

        // Cache this result to use in future
        cache.put(person.getID(), total);

        return total;
    }

    /**
     * Return the person with the most descendants or null if there are no
     * people stored.
     */
    public Person personWithMostDescendants() {
        // TODO: Potentially calculate this once when read and again when
        // certain changes are made and store so don't have to recalculate
        // each time someone views the homepage

        // Create an empty cache
        HashMap<Integer, Integer> cache = new HashMap<>();

        // Initialise the default - return null if and only if there are no
        // people stored. Initialise numDescendants to -1 so even if no-one
        // stored has any children, the number of descendants is 0 which is
        // > -1 so they are returned rather than null.
        Person currentWinner = null;
        int currentWinnerNumDescendants = -1;

        // Go through each person in order of ID and only replace if a strictly
        // greater number of descendants so people with lower IDs are favoured
        for (Person person: this.peopleByID) {
            int descendants = numDescendants(person, cache);
            if (descendants > currentWinnerNumDescendants) {
                currentWinner = person;
                currentWinnerNumDescendants = descendants;
            }
        }
        return currentWinner;
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

    public int add(String nameFirst, String nameMiddles, String nameLast, Boolean isMale,
                    Integer birthYear, Integer birthMonth, Integer birthDay, Boolean isLiving,
                    Integer deathYear, Integer deathMonth, Integer deathDay, String notes) {
        int id = this.getNextIDToUse();
        Person person = new Person(id, nameFirst, nameMiddles, nameLast,
        isMale, birthYear, birthMonth, birthDay, isLiving,
        deathYear, deathMonth, deathDay, notes, null, null);
        this.peopleByID.add(person);
        this.peopleByDaysUntilBirthday.add(person);
        return id;
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

    public void link(Person person, Integer newMotherID, Integer newFatherID) {

        // ensure the mother is female
        if (newMotherID != null) {
            Boolean motherIsMale = this.findAssert(newMotherID).getIsMale();
            if (motherIsMale == null || motherIsMale) {
                throw new IllegalArgumentException("Mother must be a female");
            }
        }

        // ensure the father is male
        if (newFatherID != null) {
            Boolean fatherIsMale = this.findAssert(newFatherID).getIsMale();
            if (fatherIsMale == null || !fatherIsMale) {
                throw new IllegalArgumentException("Father must be a male");
            }
        }

        // remove the person from the old mother's children if it has changed
        Integer oldMotherID = person.getMotherID();
        if (oldMotherID != null && oldMotherID != newMotherID) {
            Person oldMother = this.find(oldMotherID);
            if (oldMother != null) {
                oldMother.removeChild(person.getID());
            }
        }

        // remove the person from the old father's children if it has changed
        Integer oldFatherID = person.getFatherID();
        if (oldFatherID != null && oldFatherID != newFatherID) {
            Person oldFather = this.find(oldFatherID);
            if (oldFather != null) {
                oldFather.removeChild(person.getID());
            }
        }

        // add the person to the new mother's children if it has changed
        if (newMotherID != null && newMotherID != oldMotherID) {
            Person newMother = this.find(newMotherID);
            if (newMother != null) {
                newMother.addChild(person.getID());
            }
        }

        // add the person to the new father's children if it has changed
        if (newFatherID != null && newFatherID != oldFatherID) {
            Person newFather = this.find(newFatherID);
            if (newFather != null) {
                newFather.addChild(person.getID());
            }
        }

        // make the change
        person.setMotherID(newMotherID);
        person.setFatherID(newFatherID);
    }

    public void editAll(int ID, String nameFirst, String nameMiddles, String nameLast,
    Boolean isMale,   Integer birthYear, Integer birthMonth, Integer birthDay,
    Boolean isLiving, Integer deathYear, Integer deathMonth, Integer deathDay,
    String notes, Integer motherID, Integer fatherID) {
        this.editAll(this.findAssert(ID), nameFirst, nameMiddles, nameLast,
        isMale, birthYear, birthMonth, birthDay, isLiving, deathYear,
        deathMonth, deathDay, notes, motherID, fatherID);
    }

    public void editAll(Person person, String nameFirst, String nameMiddles, String nameLast,
    Boolean isMale,   Integer birthYear, Integer birthMonth, Integer birthDay,
    Boolean isLiving, Integer deathYear, Integer deathMonth, Integer deathDay,
    String notes, Integer motherID, Integer fatherID) {
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
        this.dateLastUpdatedPeopleByDaysUntilBirthday = LocalDate.now();

        MultiMap<Integer, Integer> childrenToAdd = new MultiMap<>();

        // If no file, return with no people saved
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(FILENAME));
        } catch (FileNotFoundException e) {
            return;
        }

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
                    Util.URLDecode(fields[1]),
                    Util.URLDecode(fields[2]),
                    Util.URLDecode(fields[3]),
                    "".equals(fields[4]) ? null : Boolean.parseBoolean(fields[4]),
                    "".equals(fields[5]) ? null : Integer.parseInt(fields[5]),
                    "".equals(fields[6]) ? null : Integer.parseInt(fields[6]),
                    "".equals(fields[7]) ? null : Integer.parseInt(fields[7]),
                    "".equals(fields[8]) ? null : Boolean.parseBoolean(fields[8]),
                    "".equals(fields[9]) ? null : Integer.parseInt(fields[9]),
                    "".equals(fields[10]) ? null : Integer.parseInt(fields[10]),
                    "".equals(fields[11]) ? null : Integer.parseInt(fields[11]),
                    Util.URLDecode(fields[14]),
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