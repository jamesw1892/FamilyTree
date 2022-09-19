package core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDate;

public class Util {

    public static String addLeadingZeros(int num, int numDigits) {
        String s = String.valueOf(num);
        while (s.length() < numDigits) {
            s = "0" + s;
        }
        return s;
    }

    public static String dateShortEuropean(Integer year, Integer month, Integer day) {
        if (year == null && month == null && day == null) {
            return "Unknown";
        }

        return  (day   == null?   "??": addLeadingZeros(day  , 2))
        + "/" + (month == null?   "??": addLeadingZeros(month, 2))
        + "/" + (year  == null? "????": addLeadingZeros(year , 4));
    }

    public static String dateShortInternational(Integer year, Integer month, Integer day) {
        if (year == null && month == null && day == null) {
            return "Unknown";
        }

        return  (year  == null? "????": addLeadingZeros(year , 4))
        + "-" + (month == null?   "??": addLeadingZeros(month, 2))
        + "-" + (day   == null?   "??": addLeadingZeros(day  , 2));
    }

    public static String dateLong(Integer year, Integer month, Integer day) {
        String out = "";

        if (day != null) {
            out += String.valueOf(day) + numberSuffix(day);
        }
        if (month != null) {
            out += " " + monthWord(month);
        }
        if (year != null) {
            out += " " + String.valueOf(year);
        }

        if (out.equals("")) {
            return "Unknown";
        } else {
            return out.strip();
        }
    }

    public static String monthWord(int month) {
        switch (month) {
            case 1:  return "January";
            case 2:  return "February";
            case 3:  return "March";
            case 4:  return "April";
            case 5:  return "May";
            case 6:  return "June";
            case 7:  return "July";
            case 8:  return "August";
            case 9:  return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
            default: throw new IllegalArgumentException("Invalid month - must be between 1 and 12 inclusive");
        }
    }

    public static String numberSuffix(int num) {
        if (num < 1) {
            throw new IllegalArgumentException("Must be greater than 0");
        }

        int lastDigit = num % 10;
        boolean isTeen = ((num % 100) - lastDigit) == 10;
        if (!isTeen) {
            switch (lastDigit) {
                case 1:
                    return "st";
                case 2:
                    return "nd";
                case 3:
                    return "rd";
            }
        }
        return "th";
    }

    public static String toTitleCase(String s) {

        final String ACTIONABLE_DELIMITERS = " '-/"; // these cause the character following
                                                     // to be capitalized

        StringBuilder sb = new StringBuilder();
        boolean capNext = true;

        for (char c: s.toCharArray()) {
            c = (capNext) ? Character.toUpperCase(c) : Character.toLowerCase(c);
            sb.append(c);
            capNext = (ACTIONABLE_DELIMITERS.indexOf((int) c) >= 0); // explicit cast not needed
        }
        return sb.toString();
    }

    public static int maxValidDay(Integer year, Integer month) {
        if (month == null) {
            return 31;
        }
        switch (month) {
            case 2:
                return (year == null || year % 4 == 0)? 29: 28;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            default:
                return 31;
        }
    }

    /**
     * Only returns null if `birthYear == null || (isLiving != null && !isLiving && deathYear == null)`
     * Otherwise if months or days are null, sets them to 1 so we can estimate their age
     * @param birthYear
     * @param birthMonth
     * @param birthDay
     * @param isLiving
     * @param deathYear
     * @param deathMonth
     * @param deathDay
     * @return
     */
    public static Integer calcAge(Integer birthYear, Integer birthMonth, Integer birthDay, Boolean isLiving, Integer deathYear, Integer deathMonth, Integer deathDay) {

        // if unknown whether they are alive or not, gives the age they are if alive
        if (isLiving == null) {
            isLiving = true;
        }

        // if we don't know the years, we cannot get an accurate age
        if (birthYear == null || (!isLiving && deathYear == null)) {
            return null;
        }

        // if don't know months or days, we can still get an age either
        // correct or 1 year too old so default the values and estimate
        if (birthMonth == null) { birthMonth = 1; }
        if (birthDay   == null) { birthDay   = 1; }
        if (deathMonth == null) { deathMonth = 1; }
        if (deathDay   == null) { deathDay   = 1; }

        // from this point onwards, no values are null

        int year;
        int month;
        int day;

        // if alive, use today's date
        if (isLiving) {
            LocalDate today = LocalDate.now();
            year = today.getYear();
            month = today.getMonth().getValue();
            day = today.getDayOfMonth();

        // if not alive, use the date they died
        } else {
            year = deathYear;
            month = deathMonth;
            day = deathDay;
        }

        int age = year - birthYear;

        // adjust if their birthday is later this year
        if (month < birthMonth || (month == birthMonth && day < birthDay)) {
            age--;
        }

        return age;
    }

    public static Integer daysUntilBirthday(Integer birthMonth, Integer birthDay) {

        if (birthMonth == null || birthDay == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        int todayOfYear = today.getDayOfYear();
        int birthOfYear = LocalDate.of(today.getYear(), birthMonth, birthDay).getDayOfYear();

        if (birthOfYear < todayOfYear) {
            birthOfYear += today.lengthOfYear();
        }
        int days = birthOfYear - todayOfYear;

        return days;
    }

    public static String unknownOrValue(Integer value) {
        return (value == null)? "Unknown": String.valueOf(value);
    }

    public static String normaliseString(String s) {
        return s.strip();
    }

    /**
     * Decode the given string for CSV
     */
    public static String decodeString(String s) {
        return URLDecode(s);
    }

    /**
     * Encode the given string for CSV
     */
    public static String encodeString(String s) {
        return URLEncode(s);
    }

    /**
     * Return the given string encoded for URLs in UTF-8
     */
    public static String URLEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException err) {
            throw new RuntimeException(err);
        }
    }

    /**
     * Return the given string decoded for URLs from UTF-8
     */
    public static String URLDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException err) {
            throw new RuntimeException(err);
        }
    }
}