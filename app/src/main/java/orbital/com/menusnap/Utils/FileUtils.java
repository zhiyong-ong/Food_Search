package orbital.com.menusnap.Utils;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Abel on 7/24/2016.
 */

public class FileUtils {
    public static String getTimeStamp(Calendar cal) {
        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        return df.format(cal.getTime()) + ".jpg";
    }

    public static String getFormattedDate(Calendar cal) {
        DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.FULL);
        return df.format(cal.getTime());
    }

    public static String getFormattedTime(Calendar cal) {
        DateFormat tf = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        return tf.format(cal.getTime());
    }

    public static void sortFileByTime(File[] files) {
        final DateFormat df = SimpleDateFormat.getDateTimeInstance();
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
