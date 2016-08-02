package orbital.com.foodsearch.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import orbital.com.foodsearch.R;

import static orbital.com.foodsearch.Activities.MainActivity.BASE_LANGUAGE;
import static orbital.com.foodsearch.Activities.MainActivity.MARKET_CODE;

/**
 * Created by Abel on 7/29/2016.
 */

public class LocaleUtils {
    /**
     * This method checks if we already registered a default language. If not,
     * we will check through system language until we find one that tallies and
     * set it as default lang. BASE_LANGUAGE is then set to this default.
     * If already registered default then we simply load for shared preference settings.
     */
    public static void getBaseLanguage(Context context, SharedPreferences preferences) {
        String languageSetting = preferences.getString(context.getString(R.string.select_lang_key), null);
        if (languageSetting == null) {
            String[] langValues = context.getResources().getStringArray(R.array.listLanguagesValues);
            Locale locale = Locale.getDefault();
            String sysLang = locale.getLanguage();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String script = locale.getScript();
                if (!script.isEmpty()) {
                    sysLang += "-" + script;
                }
            }
            // Run through language values, if matching found for locale then set it to preferences
            for (String langValue : langValues) {
                if (sysLang.equals("zh")) {
                    String country = locale.getCountry();
                    if (country.equals("CN")) {
                        languageSetting = "zh-CHS";
                    } else if (country.equals("TW")) {
                        languageSetting = "zh-CHT";
                    }
                    break;
                } else if ((langValue.equals(sysLang))) {
                    languageSetting = langValue;
                    break;
                }
            }
            if (languageSetting == null) {
                languageSetting = "en";
            }
            preferences.edit().putString(context.getString(R.string.select_lang_key), languageSetting).apply();
            BASE_LANGUAGE = languageSetting;
        } else {
            BASE_LANGUAGE = preferences.getString(
                    context.getResources().getString(R.string.select_lang_key), languageSetting);
        }
    }

    public static void getMarketCode() {
        MARKET_CODE = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
    }
}
