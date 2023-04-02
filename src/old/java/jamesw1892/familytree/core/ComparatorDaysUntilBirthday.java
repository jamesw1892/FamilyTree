package core;

import java.util.Comparator;

public class ComparatorDaysUntilBirthday implements Comparator<Person> {
	public int compare(Person person1, Person person2) {
        if (person1 == null || person2 == null) {
            throw new NullPointerException();
        }
        int p1days = person1.getDaysUntilBirthday();
        int p2days = person2.getDaysUntilBirthday();
        int comp = Integer.compare(p1days, p2days);

        /*
        I store people in a tree set which does not allow duplicates.
        Things are considered the same by the tree set if this returns 0.
        So if two people have the same birthday, only one would appear.
        Therefore I add this code to not treat different people with the same
        birthday as the same. This only affects the comparison between 2 people
        with the same birthday so they still both appear in the same place in
        relation to all other people.
        */
        if (comp == 0 && person1.getID() != person2.getID()) {
            return 1;
        }

        return comp;
	}
}