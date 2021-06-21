package web_server_custom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
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
    private static final String[] pages = {"Home", "People", "Birthdays"};
    public static final String linkToPerson = "/people?id=";
    private static final String linkToCSS = "/styles.css";
    private static final String pathToCSS = "web/Styles.css";
    private static final String linkToJS = "/script.js";
    private static final String pathToJS = "web/Script.js";

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
        handler.returnString(generateHTML("Post", header.getQueryStr().replace(System.lineSeparator(), "<br>")));
    }

    private void handleGet(Handler handler, Header header) throws IOException {

        switch (header.getPath()) {
            case linkToCSS:
                handler.returnFile(pathToCSS);
                break;
            case linkToJS:
                handler.returnFile(pathToJS);
                break;
            case "/favicon.ico":
                handler.returnFile("web/favicon.ico");
                break;
            case "/people":
                this.handleGetPeople(handler, header);
                break;
            case "/birthdays":
                this.handleGetBirthdays(handler, header);
                break;
            // add other pages here
            default:
                this.handleGetHome(handler, header);
        }
    }

    private void handleGetPeople(Handler handler, Header header) throws IOException {
        boolean showPersonPage = false;
        int ID = 0;

        try {
            ID = Integer.parseInt(header.getQuery().get("id"));
            if (ID > 0) {
                showPersonPage = true;
            }
        } catch (NumberFormatException e) {}

        if (showPersonPage) {
            this.handleGetPeopleSingle(handler, header, ID);
        } else {
            this.handleGetPeopleAll(handler, header);
        }
    }

    private void handleGetPeopleSingle(Handler handler, Header header, int ID) throws IOException {

        Person person = this.personStore.find(ID);
        if (person == null) {
            handler.errorNotFound();
        } else {

            // should edit person?
            String editResponse = header.getQuery().get("edit");
            if (editResponse != null && editResponse.equals("true")) {
                this.handleGetPeopleSingleEdit(handler, header, person);
            } else {
                this.handleGetPeopleSingleView(handler, header, person);
            }
        }
    }

    private void handleGetPeopleSingleEdit(Handler handler, Header header, Person person) throws IOException {
        // fields in input tag:
        //  - id is used to link labels to inputs
        //  - name is what it is called when submitted in POST request
        //  - value is what they start with already inputted into them

        String out = "<form action='' method='POST'>";

        // names
        out += "<label for='nameFirst'>First Name: </label>";
        out += "<input type='text' id='nameFirst' name='nameFirst' value=" + person.formatNameFirst() + "><br>";
        out += "<label for='nameMiddles'>Middle Name(s): </label>";
        out += "<input type='text' id='nameMiddles' value=" + person.formatNameMiddles() + "><br>";
        out += "<label for='nameLast'>Last Name: </label>";
        out += "<input type='text' id='nameLast' value=" + person.formatNameLast() + "><br>";

        // sex - TODO: change to select tag like isLiving
        out += "Sex: ";
        out += "<input type='radio' id='sexMale'   name='isMale' value='true' " + (person.getIsMale() != null && person.getIsMale() ? "checked" : "") + ">";
        out += "<label for='sexMale'>Male</label>";
        out += "<input type='radio' id='sexFemale' name='isMale' value='false' " + (person.getIsMale() != null && !person.getIsMale() ? "checked" : "") + ">";
        out += "<label for='sexFemale'>Female</label><br>";

        // Date of birth - TODO: use input type='number' min='1' max='5'

        // Is Living
        out += "<label for='isLiving'>Living?</label><br>";
        out += "<select name='isLiving' id='isLiving'>";
        if (person.getIsLiving()) {
            out += "<option value='true'>Living</option>";
            out += "<option value='false'>Deceased</option>";
        } else {
            out += "<option value='false'>Deceased</option>";
            out += "<option value='true'>Living</option>";
        }
        out += "</select><br>";

        // Date of death - TODO: use input type='number' min='1' max='5'

        // Parents - TODO: use datalist tag

        // Notes
        out += "<label for='notes'>Notes:</label><br>";
        out += "<textarea id='notes' name='notes' rows='5' cols='100'>" + person.formatNotes() + "</textarea><br>";

        out += "<input type='submit' value='Submit'>";
        handler.returnString(generateHTML("Edit " + person.formatNameFirstLast(), out));
    }

    private void handleGetPeopleSingleView(Handler handler, Header header, Person person) throws IOException {
        String out = "<button onclick=\"window.location.href='" + linkToPerson + person.formatID() + "&edit=true';\">Edit</button>";
        out += "<h1>" + person.formatNameFirstLast() + "</h1>";
        out += "<p><b>Full Name:</b> " + person.formatNameFull() + "</p>";
        out += "<p><b>Sex:</b> " + person.formatSex() + "</p>";
        out += "<p><b>Date of Birth:</b> " + person.formatDateOfBirthLong() + "</p>";
        if (person.getIsLiving()) {
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

        out += "<p><b>Mother:</b> " + linkName(mother) + "</p>";
        out += "<p><b>Father:</b> " + linkName(father) + "</p>";

        if (children.isEmpty()) {
            out += "<p>No children</p>";
        } else {
            out += "<h2>Children</h2><ul>";
            for (Person child: children) {
                out += "<li>" + linkName(child) + "</li>";
            }
            out += "</ul>";
        }
        out += "<h2>Notes</h2>";
        out += "<p>" + person.formatNotes() + "</p>";
        handler.returnString(generateHTML("View " + person.formatNameFirstLast(), out));
    }

    private void handleGetPeopleAll(Handler handler, Header header) throws IOException {

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
        return "<a href='" + linkToPerson + person.formatID() + "'>" + person.formatNameFirstLast() + "</a>";
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
        String rest = "<h1>Family Tree</h1>"
                    + "<p>Coming soon</p>";
        handler.returnString(generateHTML("Home", rest));
    }

    // TODO: change to not include IDs and instead only link the names themselves in the string data passed
    private static String tabulate(String[][] data) {
        String content = "<div class='table'><table id='table'><tr>";
        for (int j = 0; j < data[0].length; j++) {
            content += "<th>" + data[0][j] + "</th>";
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
        for (String page: pages) {
            navs += "<a";
            if (page.equals(title)) {
                navs += " class='active'";
            }
            navs += " href='/" + page.toLowerCase() + "'>" + page + "</a>";
        }

        // External CSS not being applied so copy it into the head
        String css = Files.readString(Path.of(pathToCSS));

        return ""
        + "<!DOCTYPE html>"
        + "<html>"
            + "<head>"
                + "<title>" + title + " | Family Tree</title>"
                // + "<link rel='stylesheet' type='text/css' href='" + linkToCSS + "'>"
                + "<style>" + css + "</style>"
                + "<script src='" + linkToJS + "'></script>"
            + "</head>"
            + "<body>"
                + "<div class='topnav'>" + navs + "</div><br>"
                + rest
            + "</body>"
        + "</html>";
    }
}