package orbital.com.foodsearch.Helpers;

import android.util.Log;

import orbital.com.foodsearch.Activities.OcrActivity;
import orbital.com.foodsearch.Helpers.BingTranslateMemetix.translate.Translate;

/**
 * Created by zhiyong on 1/7/2016.
 */

public class BingTranslate {

    private static final String LOG_TAG = "FOODIES";
    public static String getTranslatedText(String txt) {
        Translate.setClientId("foodies1");
        Translate.setClientSecret(OcrActivity.TRANSLATE_KEY);
        String translation = null;
        try {
            //2nd param is translate from, 3rd param is translate to
            translation = Translate.execute(txt, OcrActivity.BASE_LANGUAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "Translated text from: " + txt + "  to: " + translation);
        return translation;
    }
}
