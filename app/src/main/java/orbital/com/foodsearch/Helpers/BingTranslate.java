package orbital.com.foodsearch.helpers;

import android.util.Log;

import orbital.com.foodsearch.activities.MainActivity;
import orbital.com.foodsearch.helpers.BingTranslateMemetix.translate.Translate;
import orbital.com.foodsearch.misc.GlobalVar;

/**
 * Created by zhiyong on 1/7/2016.
 */

public class BingTranslate {

    private static final String LOG_TAG = "FOODIES";
    public static String getTranslatedText(String txt) {
        Log.e(LOG_TAG, "MAIN activity base lang: " + MainActivity.BASE_LANGUAGE);
        Translate.setClientId("foodies1");
        Translate.setClientSecret(GlobalVar.getTranslateKey());
        String translation = null;
        try {
            //2nd param is translate from, 3rd param is translate to
            translation = Translate.execute(txt, MainActivity.BASE_LANGUAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "Translated text from: " + txt + "  to: " + translation);
        return translation;
    }

    public static String getTranslatedText(String txt, String language) {
        Translate.setClientId("foodies1");
        Translate.setClientSecret(GlobalVar.getTranslateKey());
        String translation = null;
        try {
            //2nd param is translate from, 3rd param is translate to
            translation = Translate.execute(txt, language);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "Translated text from: " + txt + "  to: " + translation);
        return translation;
    }
}
