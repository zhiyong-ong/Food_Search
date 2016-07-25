package orbital.com.foodsearch.DAO;

import android.provider.BaseColumns;

/**
 * Created by zhiyong on 19/7/2016.
 */

public class PhotosContract {
    // put definitions that are global to your whole database in the root level of the class.

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PhotosContract() {}

    /* Inner class that defines the table contents */
    public static abstract class PhotosEntry implements BaseColumns {
        public static final String TABLE_NAME = "photos";
        public static final String COLUMN_NAME_ENTRY_TIME = "entry_time";
        public static final String COLUMN_NAME_TITLE = "photo_data";
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_FORMATTED_DATE = "formatted_date";
        public static final String COLUMN_FORMATTED_TIME = "formatted_time";
    }
}

