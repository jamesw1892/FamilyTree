package cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.zip.DataFormatException;

import core.Person;
import core.PersonStore;
import core.Util;

import validation.Validator;
import validation.Spec;
import validation.SpecIntRange;
import validation.SpecStr;

class FamilyTreeCLI {
    private Scanner scanner;
    private Validator validator;
    private TextTable table;
    private static final String[] MENU_OPTIONS = {"View all data stored about everyone",
                                                  "View useful details stored about everyone",
                                                  "Add a person",
                                                  "Edit, delete or link a person",
                                                  "Exit"};
    private static final String[] EDIT_OPTIONS = {"Edit names",
                                                  "Edit sex",
                                                  "Edit date of birth",
                                                  "Edit date of death",
                                                  "Edit notes",
                                                  "Link",
                                                  "Delete",
                                                  "Cancel"};
    private static final SpecIntRange SPEC_YEAR = new SpecIntRange(1000, 9999, new String[]{""});
    private static final SpecIntRange SPEC_ID = new SpecIntRange(1, null, new String[0]);
    private static final SpecIntRange SPEC_PARENT_ID = new SpecIntRange(1, null, new String[]{""});

    private FamilyTreeCLI() {
        this.scanner = new Scanner(System.in);
        this.validator = new Validator(this.scanner);
        this.table = new TextTable();
        this.table.setMaxWidth(170);
        this.table.setDeco(false, true, true, true);
    }

    private void close() {
        this.scanner.close();
        this.validator.close();
    }

    private void printTable(String[][] data) {
        System.out.println("\n");
        this.table.reset();
        this.table.addRows(data, true);
        this.table.print();
        System.out.println("\n");
    }

    private void outputDataAboutEveryoneByID(PersonStore personStore) {
        TreeSet<Person> people = personStore.getEveryoneByID();
        String[][] data = new String[people.size() + 1][];

        data[0] = new String[] {"ID", "Full Name", "Sex", "Date of Birth",
        "Date of Death", "Mother ID", "Father ID", "Child IDs", "Notes"};

        int count = 1;
        for (Person person: people) {
            data[count] = new String[] {
                person.formatID(),
                person.formatNameFull(),
                person.formatSex(),
                person.formatDateOfBirthShort(),
                person.formatDateOfDeathShort(),
                person.formatMotherID(),
                person.formatFatherID(),
                person.formatChildrenIDs(),
                person.formatNotes()
            };
            count++;
        }

        printTable(data);
    }

    private void outputDetailsAboutEveryoneByID(PersonStore personStore) {
        TreeSet<Person> people = personStore.getEveryoneByID();
        String[][] data = new String[people.size() + 1][];

        data[0] = new String[] {"Full Name", "Sex", "Date of Birth",
        "Date of Death", "Age", "Mother", "Father", "Children", "Notes"};

        int count = 1;
        for (Person person: people) {
            data[count] = new String[] {
                person.formatNameFull(),
                person.formatSex(),
                person.formatDateOfBirthLong(),
                person.formatDateOfDeathLong(),
                person.formatAgeAtDeath(),
                personStore.formatMotherName(person),
                personStore.formatFatherName(person),
                personStore.formatChildrenNames(person),
                person.formatNotes()
            };
            count++;
        }

        printTable(data);
    }

    private void editLinkDelete(PersonStore personStore) {

        outputDataAboutEveryoneByID(personStore);
        try {
            int ID = this.inputID();
            personStore.findAssert(ID);

            switch (this.validator.menu("What would you like to edit?", EDIT_OPTIONS)) {
                case 1:
                    personStore.editNames(ID, this.inputNameFirst(), this.inputNameMiddles(), this.inputNameLast());
                    break;
                case 2:
                    personStore.editSex(ID, this.inputIsMale());
                    break;
                case 3:
                    Integer year = this.inputBirthYear();
                    Integer month = this.inputBirthMonth();
                    personStore.editDOB(ID, year, month, this.inputBirthDay(Util.maxValidDay(year, month)));
                    break;
                case 4:
                    Boolean isLiving = this.inputIsLiving();
                    Integer deathYear = null;
                    Integer deathMonth = null;
                    Integer deathDay = null;
                    if (isLiving != null && !isLiving) {
                        deathYear = this.inputDeathYear();
                        deathMonth = this.inputDeathMonth();
                        deathDay = this.inputDeathDay(Util.maxValidDay(deathYear, deathMonth));
                    }
                    personStore.editDOD(ID, isLiving, deathYear, deathMonth, deathDay);
                    break;
                case 5:
                    personStore.editNotes(ID, this.inputNotes());
                    break;
                case 6:
                    personStore.link(ID, this.inputMotherID(), this.inputFatherID());
                    break;
                case 7:
                    personStore.delete(ID);
                    break;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }

    private int inputID() {
        return (int) this.validator.validateInput(SPEC_ID, "ID of person to edit: ");
    }

    private String inputNameFirst() {
        System.out.print("First name: ");
        return this.scanner.nextLine();
    }

    private String inputNameMiddles() {
        System.out.print("Middle name(s): ");
        return this.scanner.nextLine();
    }

    private String inputNameLast() {
        System.out.print("Last name (Maiden): ");
        return this.scanner.nextLine();
    }

    private String inputNotes() {
        System.out.println("Notes:");
        return this.scanner.nextLine();
    }

    private Integer validateIntAndConvert(SpecIntRange spec, String prompt) {
        Object value = this.validator.validateInput(spec, prompt);
        if (value.equals("")) {
            return null;
        }
        return (Integer) value;
    }

    private Boolean inputIsMale() {
        Spec spec = new SpecStr(new String[]{"m", "male", "b", "boy", "f", "female", "g", "girl"}, null, true, new String[]{""});
        Object value = this.validator.validateInput(spec, "Sex: ");
        if (value.equals("")) {
            return null;
        } else if (value.equals("m") || value.equals("male") || value.equals("b") || value.equals("boy")) {
            return true;
        } else {
            return false;
        }
    }

    private Integer inputBirthYear() {
        return this.validateIntAndConvert(SPEC_YEAR, "Birth year: ");
    }

    private Integer inputBirthMonth() {
        return this.validator.validateInputMonth("Birth month: ");
    }

    private Integer inputBirthDay(int max) {
        SpecIntRange spec = new SpecIntRange(1, max, new String[]{""});
        return this.validateIntAndConvert(spec, "Birth day: ");
    }

    private Boolean inputIsLiving() {
        Spec spec = new SpecStr(new String[]{"y", "yes", "t", "true", "n", "no", "f", "false"}, null, true, new String[]{""});
        Object value = this.validator.validateInput(spec, "Are they alive? ");
        if (value.equals("")) {
            return null;
        } else if (value.equals("y") || value.equals("yes") || value.equals("t") || value.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    private Integer inputDeathYear() {
        return this.validateIntAndConvert(SPEC_YEAR, "Death year: ");
    }

    private Integer inputDeathMonth() {
        return this.validator.validateInputMonth("Death month: ");
    }

    private Integer inputDeathDay(int max) {
        SpecIntRange spec = new SpecIntRange(1, max, new String[]{""});
        return this.validateIntAndConvert(spec, "Death day: ");
    }

    private Integer inputMotherID() {
        return this.validateIntAndConvert(SPEC_PARENT_ID, "Mother ID: ");
    }

    private Integer inputFatherID() {
        return this.validateIntAndConvert(SPEC_PARENT_ID, "Father ID: ");
    }

    private void addPerson(PersonStore personStore) {

        System.out.println("Input the following information about the new person, you can leave blank for unknown or non-existent data");

        String nameFirst = inputNameFirst();
        String nameMiddles = inputNameMiddles();
        String nameLast = inputNameLast();
        Boolean isMale = inputIsMale();
        Integer birthYear = this.inputBirthYear();
        Integer birthMonth = this.inputBirthMonth();
        Integer birthDay = this.inputBirthDay(Util.maxValidDay(birthYear, birthMonth));
        Boolean isLiving = this.inputIsLiving();

        Integer deathYear = null;
        Integer deathMonth = null;
        Integer deathDay = null;
        if (isLiving != null && !isLiving) {
            deathYear = this.inputDeathYear();
            deathMonth = this.inputDeathMonth();
            deathDay = this.inputDeathDay(Util.maxValidDay(deathYear, deathMonth));
        }

        String notes = this.inputNotes();

        personStore.add(nameFirst, nameMiddles, nameLast, isMale,
                        birthYear, birthMonth, birthDay, isLiving,
                        deathYear, deathMonth, deathDay, notes);
    }

    private void menu(PersonStore personStore) {
        boolean done = false;
        int choice;

        while (!done) {

            choice = this.validator.menu("MENU:", MENU_OPTIONS);

            switch (choice) {
                case 1:
                    this.outputDataAboutEveryoneByID(personStore);
                    break;
                case 2:
                    this.outputDetailsAboutEveryoneByID(personStore);
                    break;
                case 3:
                    this.addPerson(personStore);
                    break;
                case 4:
                    this.editLinkDelete(personStore);
                    break;
                default:
                    done = true;
            }
        }
    }

    public static void run() {

        FamilyTreeCLI cli = new FamilyTreeCLI();
        try {

            PersonStore personStore = new PersonStore();
            cli.menu(personStore);
            personStore.write();

        } catch (DataFormatException | NumberFormatException e) {
            System.out.println("Data stored incorrectly:\n" + e);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error interacting with the file:");
            e.printStackTrace();
        }
        cli.close();
    }

    public static void main(String[] args) {
        run();
    }
}