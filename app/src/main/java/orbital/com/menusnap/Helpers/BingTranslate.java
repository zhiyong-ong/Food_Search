package orbital.com.menusnap.Helpers;

import android.util.Log;

import orbital.com.menusnap.Activities.MainActivity;
import orbital.com.menusnap.Helpers.BingTranslateMemetix.translate.Translate;

/**
 * Created by zhiyong on 1/7/2016.
 */

public class BingTranslate {

    private static final String LOG_TAG = "FOODIES";
    public static String getTranslatedText(String txt) {
        Translate.setClientId("foodies1");
        Translate.setClientSecret(GlobalVar.getTranslateKey());
        String translation = null;
        try {
            //2nd param is translate from, 3rd param is translate to
            translation = Translate.execute(txt, MainActivity.BASE_LANGUAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return translation;
    }
}
