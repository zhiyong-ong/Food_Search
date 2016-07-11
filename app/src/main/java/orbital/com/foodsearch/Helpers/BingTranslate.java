package orbital.com.foodsearch.Helpers;

import android.util.Log;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

/**
 * Created by zhiyong on 1/7/2016.
 */

public class BingTranslate {

    private static final String LOG_TAG = "FOODIES";

    public static String getTranslatedText(String txt) {
        Translate.setClientId("foodies1");
        Translate.setClientSecret("visGDAEgH0FXxw/Qskj+vHWNhfWphVu+0RRK/5cVYAw=");
        String translation = txt;
        try {
            //2nd param is translate from, 3rd param is translate to
            translation = Translate.execute(txt, Language.CHINESE_SIMPLIFIED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "Translated text from: " + txt + "  to: " + translation);
        return translation;
    }
}
