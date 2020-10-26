package core;

import java.util.Comparator;

public class ComparatorDaysUntilBirthday implements Comparator<Person> {
	public int compare(Person person1, Person person2) {
        if (person1 == null || person2 == null) {
            throw new NullPointerException();
        }
        Integer p1days = person1.getDaysUntilBirthday();
        Integer p2days = person2.getDaysUntilBirthday();
        if (p1days == null) {
            return 1;
        }
        if (p2days == null) {
            return -1;
        }
        return p1days.compareTo(p2days);
	}
}