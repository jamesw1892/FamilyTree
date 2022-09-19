package web_server_custom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.zip.DataFormatException;

import core.Person;
import core.PersonStore;

/**
 * Constructed once by the web server to handle all requests but
 * started many times for each request
 */
class GUI {
    private PersonStore personStore;
    private static final String[] PAGES = {"Home", "People", "Birthdays"};
    private static final String LINK_TO_PERSON = "/person/";
    private static final String LINK_TO_CSS = "/styles.css";
    private static final String PATH_TO_CSS = "web/Styles.css";
    private static final String LINK_TO_JS = "/script.js";
    private static final String PATH_TO_JS = "web/Script.js";
    private static final String LINK_TO_FAVICON = "/favicon.ico";
    private static final String PATH_TO_FAVICON = "web/favicon.ico";

    public GUI() throws IOException, DataFormatException {
        this.personStore = new PersonStore();
    }

    /**
     * Close the GUI which writes the data in PersonStore
     * @throws IOException
     */
    public void close() throws IOException {
        this.personStore.write();
    }

    public void handleRequest(Handler handler, Header header) throws IOException {

        System.out.println((new Timestamp(System.currentTimeMillis())).toString().split("\\.")[0]
            + ": Handling " + header.getMethod() + " request for " + header.getHalfURL());

        switch (header.getMethod()) {
            case "GET":
                this.handleGet(handler, header);
                break;
            case "POST":
                this.handlePost(handler, header);
                break;
            // add other methods here
            default:
                handler.errorNotImplemented();
        }
    }

    private void handlePost(Handler handler, Header header) throws IOException {

        try {
            int ID = Integer.parseInt(header.getFilename());
            if (ID > 0) {
                Person person = this.personStore.find(ID);
                if (person != null) {
                    this.handlePostSaveData(handler, header, person);
                    return;
                }
            }
        } catch (NumberFormatException e) {}

        handler.errorNotFound();
    }

    private static Boolean parseBool(String b) {
        if (b.equals("true")) {
            return true;
        } else if (b.equals("false")) {
            return false;
        }
        return null;
    }

    private static Integer parseInt(String i) {
        if (i == null || i.isEmpty() || i.equals("null") || i.equals("Unknown")) {
            return null;
        }

        return Integer.parseInt(i);
    }

    private void handlePostSaveData(Handler handler, Header header, Person person) throws IOException {

        HashMap<String, String> data = header.getData();
        String nameFirst = data.get("nameFirst");
        String nameMiddles = data.get("nameMiddles");
        String nameLast = data.get("nameLast");
        Boolean isMale = parseBool(data.get("isMale"));
        Integer birthYear = parseInt(data.get("birthYear"));
        Integer birthMonth = parseInt(data.get("birthMonth"));
        Integer birthDay = parseInt(data.get("birthDay"));
        Boolean isLiving = parseBool(data.get("isLiving"));
        Integer deathYear = parseInt(data.get("deathYear"));
        Integer deathMonth = parseInt(data.get("deathMonth"));
        Integer deathDay = parseInt(data.get("deathDay"));
        Integer motherID = parseInt(data.get("mother"));
        Integer fatherID = parseInt(data.get("father"));
        String notes = data.get("notes");

        String html;

        try {
            this.personStore.editAll(person, nameFirst, nameMiddles, nameLast,
                isMale, birthYear, birthMonth, birthDay, isLiving, deathYear,
                deathMonth, deathDay, notes, motherID, fatherID);
            html = "<h1>Success</h1><p>Saved</p>";
        } catch (IllegalArgumentException e) {
            html = "<h1>Failure</h1><p>Invalid data, not edited, try again</p><p>" + e.getMessage() + "</p>";
        }
        html += "<button onclick=\"window.location.href='" + LINK_TO_PERSON + person.formatID() + "';\">Back</button>";

        handler.returnString(generateHTML("Post Result", html));
    }

    private void handleGet(Handler handler, Header header) throws IOException {

        switch (header.getPath()) {
            case LINK_TO_CSS:
                handler.returnFile(PATH_TO_CSS);
                break;
            case LINK_TO_JS:
                handler.returnFile(PATH_TO_JS);
                break;
            case LINK_TO_FAVICON:
                handler.returnFile(PATH_TO_FAVICON);
                break;
            case "/people":
                this.handleGetPeople(handler, header);
                break;
            case "/birthdays":
                this.handleGetBirthdays(handler, header);
                break;
            // add other pages here
            default: {
                if (header.getDirname().equals("/person")) {
                    this.handleGetPerson(handler, header);
                } else {
                    this.handleGetHome(handler, header);
                }
            }
        }
    }

    private void handleGetPerson(Handler handler, Header header) throws IOException {

        try {
            int ID = Integer.parseInt(header.getFilename());
            if (ID > 0) {
                Person person = this.personStore.find(ID);
                if (person != null) {

                    // should edit person?
                    String editResponse = header.getQuery().get("edit");
                    if (editResponse != null && editResponse.equals("true")) {
                        this.handleGetPersonEdit(handler, header, person);
                    } else {
                        this.handleGetPersonView(handler, header, person);
                    }
                    return;
                }
            }
        } catch (NumberFormatException e) {}

        handler.errorNotFound();
    }

    private void handleGetPersonEdit(Handler handler, Header header, Person person) throws IOException {
        // fields in input tag:
        //  - id is used to link labels to inputs
        //  - name is what it is called when submitted in POST request
        //  - value is what they start with already inputted into them
        String out = "<button onclick=\"window.location.href='" + LINK_TO_PERSON + person.formatID() + "';\">Cancel</button>";
        out += "<form action='' method='POST' enctype='application/x-www-form-urlencoded' accept-charset='UTF-8'>";

        // names
        out += "<label for='nameFirst'>First Name: </label>";
        out += "<input type='text' id='nameFirst' name='nameFirst' value=" + person.formatNameFirst() + "><br>";
        out += "<label for='nameMiddles'>Middle Name(s): </label>";
        out += "<input type='text' id='nameMiddles' name='nameMiddles' value=" + person.formatNameMiddles() + "><br>";
        out += "<label for='nameLast'>Last Name: </label>";
        out += "<input type='text' id='nameLast' name='nameLast' value=" + person.formatNameLast() + "><br>";

        // sex
        out += "<label for='isMale'>Sex: </label>";
        out += "<select name='isMale' id='isMale'>";
        if (person.getIsMale() == null) {
            out += "<option value='null'>Unknown</option>";
            out += "<option value='true'>Male</option>";
            out += "<option value='false'>Female</option>";
        } else if (person.getIsMale()) {
            out += "<option value='true'>Male</option>";
            out += "<option value='false'>Female</option>";
            out += "<option value='null'>Unknown</option>";
        } else {
            out += "<option value='false'>Female</option>";
            out += "<option value='true'>Male</option>";
            out += "<option value='null'>Unknown</option>";
        }
        out += "</select><br>";

        // Date of birth
        out += "<label for='birthYear'>Birth Year: </label>";
        out += "<input type='number' id='birthYear' name='birthYear' min='1000' max='9999' value='" + person.getBirthYear() + "'><br>";
        out += "<label for='birthMonth'>Birth Month: </label>";
        out += "<input type='number' id='birthMonth' name='birthMonth' min='1' max='12' value='" + person.getBirthMonth() + "'><br>";
        out += "<label for='birthDay'>Birth Day: </label>";
        out += "<input type='number' id='birthDay' name='birthDay' min='1' max='31' value='" + person.getBirthDay() + "'><br>";

        // Is Living
        out += "<label for='isLiving'>Living? </label>";
        out += "<select name='isLiving' id='isLiving'>";
        if (person.getIsLiving() == null) {
            out += "<option value='null'>Unknown</option>";
            out += "<option value='true'>Living</option>";
            out += "<option value='false'>Deceased</option>";
        } else if (person.getIsLiving()) {
            out += "<option value='true'>Living</option>";
            out += "<option value='false'>Deceased</option>";
            out += "<option value='null'>Unknown</option>";
        } else {
            out += "<option value='false'>Deceased</option>";
            out += "<option value='true'>Living</option>";
            out += "<option value='null'>Unknown</option>";
        }
        out += "</select><br>";

        // Date of death
        out += "<label for='deathYear'>Death Year: </label>";
        out += "<input type='number' id='deathYear' name='deathYear' min='1000' max='9999' value='" + person.getDeathYear() + "'><br>";
        out += "<label for='deathMonth'>Death Month: </label>";
        out += "<input type='number' id='deathMonth' name='deathMonth' min='1' max='12' value='" + person.getDeathMonth() + "'><br>";
        out += "<label for='deathDay'>Death Day: </label>";
        out += "<input type='number' id='deathDay' name='deathDay' min='1' max='31' value='" + person.getDeathDay() + "'><br>";

        // Mother
        out += "<label for='mother'>Mother: </label>";
        out += "<input list='motherlist' name='mother' id='mother' value='" + person.formatMotherID() + "'>"; // use currently set mother as default if there is one
        out += "<datalist name='motherlist' id='motherlist'>";
        out += "<option value='Unknown'>"; // unknown option
        for (Person potentialMother: this.personStore.getEveryoneByID()) { // TODO: potentially only include females
            out += "<option value='" + potentialMother.formatID() + "'>" + potentialMother.formatNameFirstLast() + "</option>";
        }
        out += "</datalist><br>";

        // Father
        out += "<label for='father'>Father: </label>";
        out += "<input list='fatherlist' name='father' id='father' value='" + person.formatFatherID() + "'>"; // use currently set father as default if there is one
        out += "<datalist name='fatherlist' id='fatherlist'>";
        out += "<option value='Unknown'>"; // unknown option
        for (Person potentialFather: this.personStore.getEveryoneByID()) { // TODO: potentially only include males
            out += "<option value='" + potentialFather.formatID() + "'>" + potentialFather.formatNameFirstLast() + "</option>";
        }
        out += "</datalist><br>";

        // Notes
        out += "<label for='notes'>Notes:</label><br>";
        out += "<textarea id='notes' name='notes' rows='5' cols='100'>" + person.formatNotes() + "</textarea><br>";

        // Submit button
        out += "<input type='submit' value='Submit'>";
        out += "</form>";
        handler.returnString(generateHTML("Edit " + person.formatNameFirstLast(), out));
    }

    private void handleGetPersonView(Handler handler, Header header, Person person) throws IOException {
        String out = "<button onclick=\"window.location.href='" + LINK_TO_PERSON + person.formatID() + "?edit=true';\">Edit</button>";
        out += "<h1>" + person.formatNameFirstLast() + "</h1>";
        out += "<p><b>Full Name:</b> " + person.formatNameFull() + "</p>";
        out += "<p><b>Sex:</b> " + person.formatSex() + "</p>";
        out += "<p><b>Date of Birth:</b> " + person.formatDateOfBirthLong() + "</p>";
        if (person.getIsLiving() != null && person.getIsLiving()) {
            out += "<p><b>Age:</b> " + person.formatAgeAtDeath();
        } else {
            out += "<p><b>Date of Death:</b> " + person.formatDateOfDeathLong() + "</p>";
            out += "<p><b>Age when deceased:</b> " + person.formatAgeAtDeath() + "</p>";
            out += "<p><b>Age if were still living:</b> " + person.formatAgeIfWereAlive() + "</p>";
        }
        out += "<p><b>Days until birthday:</b> " + person.formatDaysUntilBirthday() + "</p>";

        Person mother = personStore.getMother(person);
        Person father = personStore.getFather(person);
        HashSet<Person> children = personStore.getChildren(person);
        HashSet<Person> siblings = personStore.getSiblings(person);
        HashSet<Person> partners = personStore.getPartners(person);

        out += "<p><b>Mother:</b> " + linkName(mother) + "</p>";
        out += "<p><b>Father:</b> " + linkName(father) + "</p>";

        out += "<div class='row'>";

        out += "<div class='column'><h2>Children</h2>";
        if (children.isEmpty()) {
            out += "<p>No children</p>";
        } else {
            for (Person child: children) {
                out += "<p>" + linkName(child) + "</p>";
            }
        }

        out += "</div><div class='column'><h2>Siblings</h2>";
        if (siblings.isEmpty()) {
            out += "<p>No siblings</p>";
        } else {
            for (Person sibling: siblings) {
                out += "<p>" + linkName(sibling) + "</p>";
            }
        }

        out += "</div><div class='column'><h2>Partners</h2>";
        if (partners.isEmpty()) {
            out += "<p>No partners</p>";
        } else {
            for (Person partner: partners) {
                out += "<p>" + linkName(partner) + "</p>";
            }
        }

        out += "</div></div>";

        out += "<h2>Notes</h2>";
        String notes = person.formatNotes();
        if (notes.isEmpty()) {
            out += "<p>No notes yet</p>";
        } else {
            out += "<p>" + notes + "</p>";
        }

        // family tree
        out += "<div class='row'>";
        out += "<div class='column'><h2>Descendants Tree</h2>" + treeHTMLDown(person) + "</div>";
        out += "<div class='column'><h2>Ancestors Tree</h2>" + treeHTMLUp(person) + "</div>";
        out += "</div>";

        handler.returnString(generateHTML("View " + person.formatNameFirstLast(), out));
    }

    private String treeHTMLUp(Person person) {
        return "<div class='tree'><ul><li>" + treeHTMLUpRec(person) + "</li></ul></div>";
    }

    private String treeHTMLDown(Person person) {
        return "<div class='tree'><ul><li>" + treeHTMLDownRec(person) + "</li></ul></div>";
    }

    /**
     * The same as treeHTMLDownRec but instead of going down it goes up.
     * It still creates a tree going down though so it's the wrong way up.
     * It creates a sub-list containing their parents and recurse.
     * So it's a binary tree until we don't know their parents anymore.
     */
    private String treeHTMLUpRec(Person person) {
        String out = linkName(person);
        HashSet<Person> parents = new HashSet<>();
        Person mother = this.personStore.getMother(person);
        if (mother != null) {
            parents.add(mother);
        }
        Person father = this.personStore.getFather(person);
        if (father != null) {
            parents.add(father);
        }
        if (!parents.isEmpty()) {
            out += "<ul>";
            for (Person parent: parents) {
                out += "<li>" + treeHTMLUpRec(parent) + "</li>";
            }
            out += "</ul>";
        }

        return out;
    }

    /**
     * Recursively create tree in HTML unordered lists of children of the current person
     * https://thecodeplayer.com/index.php/walkthrough/css3-family-tree
     */
    private String treeHTMLDownRec(Person person) {
        String out = linkName(person);
        HashSet<Person> children = personStore.getChildren(person);
        if (!children.isEmpty()) {
            out += "<ul>";
            for (Person child: children) {
                out += "<li>" + treeHTMLDownRec(child) + "</li>";
            }
            out += "</ul>";
        }

        return out;
    }

    private void handleGetPeople(Handler handler, Header header) throws IOException {

        // TODO: sorting and choosing data buttons

        // String info = header.getQuery().get("info");
        // if (info == null) {
        //     info = "data-formatted";
        // }

        // String sort = header.getQuery().get("sort");
        // if (sort == null) {
        //     sort = "id";
        // }

        // boolean reverse = false;
        // String reverseResponse = header.getQuery().get("reverse");
        // if (reverseResponse != null && reverseResponse.equals("true")) {
        //     reverse = true;
        // }

        // switch (info) {
        //     case "details":
        //         switch (sort) {
        //             case "name":
        //                 rest += "";
        //                 break;
        //             // other sorts
        //             default: // id
        //                 rest += this.tabulate(this.personStore.getDetailsAboutEveryoneByID());
        //         }
        //         break;
        //     // other views
        //     default: // everything
        //         switch (sort) {
        //             case "name-first":
        //                 rest += "";
        //                 break;
        //             case "name-middles":
        //                 rest += "";
        //                 break;
        //             case "name-last":
        //                 rest += "";
        //                 break;
        //             case "sex":
        //                 rest += "";
        //                 break;
        //             case "dob":
        //                 rest += "";
        //                 break;
        //             case "dod":
        //                 rest += "";
        //                 break;
        //             // other sorts
        //             default: // id
        //                 rest += this.tabulate(this.personStore.getEverythingButParentIDsAndNotesAboutEveryoneByID());
        //         }
        // }

        TreeSet<Person> people = this.personStore.getEveryoneByID();
        String[][] data = new String[people.size() + 1][];
        String[] IDs = new String[people.size()];

        data[0] = new String[] {"Name", "Sex", "Date of Birth",
        "Date of Death", "Age (at death)", "Mother", "Father", "Children"};

        int count = 1;
        for (Person person: people) {
            IDs[count - 1] = person.formatID();
            data[count] = new String[] {
                linkName(person),
                person.formatSex(),
                person.formatDateOfBirthLong(),
                person.formatDateOfDeathLong(),
                person.formatAgeAtDeath(),
                linkName(this.personStore.getMother(person)),
                linkName(this.personStore.getFather(person)),
                this.linkChildNames(person)
            };
            count++;
        }

        handler.returnString(generateHTML("People", tabulate(data)));
    }

    private static String linkName(Person person) {
        if (person == null) {
            return "Unknown";
        }
        return "<a href='" + LINK_TO_PERSON + person.formatID() + "'>" + person.formatNameFirstLast() + "</a>";
    }

    private String linkChildNames(Person person) {
        HashSet<Person> children = this.personStore.getChildren(person);
        String out = "";
        for (Person child: children) {
            out += linkName(child) + ", ";
        }
        if (!out.equals("")) {
            out = out.substring(0, out.length() - 2);
        }
        return out;
    }

    private void handleGetBirthdays(Handler handler, Header header) throws IOException {
        TreeSet<Person> people = this.personStore.getEveryoneByDaysUntilBirthday();
        String[][] data = new String[people.size() + 1][];

        data[0] = new String[] {"Days Until Birthday", "Name", "Birthday", "Age on Birthday", "Living?"};
        int count = 1;
        for (Person person: people) {
            data[count] = new String[] {
                person.formatDaysUntilBirthday(),
                linkName(person),
                person.formatBirthday(),
                person.formatAgeAtBirthday(),
                person.formatIsLiving()
            };
            count++;
        }

        String filters = "<input type='checkbox' id='onlyShowLivingInput' onclick='onlyShowLivingFunc(this)'>"
                       + "<label for='onlyShowLivingInput'>Only show living people?</label><br><br>";

        handler.returnString(generateHTML("Birthdays", filters + tabulate(data)));
    }

    private void handleGetHome(Handler handler, Header header) throws IOException {
        String rest = "<h1>Family Tree</h1>" + treeHTMLDown(personStore.find(15));
        handler.returnString(generateHTML("Home", rest));
    }

    // TODO: change to not include IDs and instead only link the names themselves in the string data passed
    private static String tabulate(String[][] data) {
        String content = "<div class='table'><table id='table'><tr style='position:sticky;top:50px;'>";
        for (int j = 0; j < data[0].length; j++) {
            content += String.format("<th onclick='sortTable(%d)'>%s</th>", j, data[0][j]);
        }
        content += "</tr>";
        for (int i = 1; i < data.length; i++) {
            String[] fields = data[i];
            content += "<tr>";
            for (int j = 0; j < fields.length; j++) {
                content += "<td>" + fields[j] + "</td>";
            }
            content += "</tr>";
        }
        return content + "</table></div>";
    }

    private static String generateHTML(String title, String rest) throws IOException {
        String navs = "";
        for (String page: PAGES) {
            navs += "<a";
            if (page.equals(title)) {
                navs += " class='active'";
            }
            navs += " href='/" + page.toLowerCase() + "'>" + page + "</a>";
        }

        // External CSS not being applied so copy it into the head
        String css = Files.readString(Path.of(PATH_TO_CSS));
        String js  = Files.readString(Path.of(PATH_TO_JS));

        return ""
        + "<!DOCTYPE html>"
        + "<html>"
            + "<head>"
                + "<title>" + title + " | Family Tree</title>"
                // + "<link rel='stylesheet' type='text/css' href='" + LINK_TO_CSS + "'>"
                + "<style>" + css + "</style>"
                // + "<script src='" + LINK_TO_JS + "'></script>"
                + "<script>" + js + "</script>"
            + "</head>"
            + "<body>"
                + "<div class='topnav'>" + navs + "</div><br>"
                + rest
            + "</body>"
        + "</html>";
    }
}