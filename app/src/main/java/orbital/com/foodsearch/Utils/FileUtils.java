package orbital.com.foodsearch.Utils;

import java.util.Calendar;

/**
 * Created by Abel on 7/24/2016.
 */

public class FileUtils {
    public static String getDate(Calendar cal) {
        StringBuilder builder = new StringBuilder();
        builder.append(cal.get(Calendar.DATE));
        builder.append('-');
        builder.append(cal.get(Calendar.MONTH));
        builder.append('-');
        builder.append(cal.get(Calendar.YEAR));
        builder.append('_');
        builder.append(cal.get(Calendar.HOUR_OF_DAY));
        builder.append(':');
        builder.append(cal.get(Calendar.MINUTE));
        builder.append(':');
        builder.append(cal.get(Calendar.SECOND));
        builder.append(".jpg");
        return builder.toString();
    }
}
