package orbital.com.foodsearch.Helpers;

import android.util.Log;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import orbital.com.foodsearch.Activities.OcrActivity;

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
            //2nd param is TRANSLATE_KEY from, 3rd param is TRANSLATE_KEY to
            translation = Translate.execute(txt, Language.CHINESE_SIMPLIFIED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(LOG_TAG, "Translated text from: " + txt + "  to: " + translation);
        return translation;
    }
}
