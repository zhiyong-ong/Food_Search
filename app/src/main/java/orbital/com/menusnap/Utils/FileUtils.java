package orbital.com.menusnap.Utils;

import android.content.SharedPreferences;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Abel on 7/24/2016.
 */

public class FileUtils {
    private static final String SPECIAL_LOCALE = "speciallocale";

    public static String getTimeStamp(SharedPreferences prefs, Calendar cal) {
        final DateFormat df;
        boolean specialLocale = prefs.getBoolean(SPECIAL_LOCALE, false);
        if (!specialLocale) {
            df = SimpleDateFormat.getDateTimeInstance();
        } else {
            df = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                    DateFormat.MEDIUM, Locale.ENGLISH);
        }
        String timeStamp = df.format(cal.getTime()) + ".jpg";
        if (timeStamp.contains("/")){
            prefs.edit().putBoolean(SPECIAL_LOCALE, true).apply();
            return getTimeStamp(prefs, cal);
        }
        return timeStamp;
    }

    public static String getFormattedDate(Calendar cal) {
        DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.FULL);
        return df.format(cal.getTime());
    }

    public static String getFormattedTime(Calendar cal) {
        DateFormat tf = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        return tf.format(cal.getTime());
    }

    public static void sortFileByTime(SharedPreferences prefs, File[] files) {
        boolean specialLocale = prefs.getBoolean(SPECIAL_LOCALE, false);
        final DateFormat df;
        if (!specialLocale) {
            df = SimpleDateFormat.getDateTimeInstance();
        } else {
            df = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                    DateFormat.MEDIUM, Locale.ENGLISH);
        }
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = df.parse(file1.getName().replace(".jpg", ""));
                    date2 = df.parse(file2.getName().replace(".jpg", ""));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return date1 != null ? -date1.compareTo(date2) : 0;
            }
        });
    }
}
