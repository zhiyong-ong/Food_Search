package orbital.com.foodsearch.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import orbital.com.foodsearch.R;

/**
 * Created by Abel on 7/20/2016.
 */

public class ViewUtils {
    public static Snackbar makeTranslateBar(Context context, final View rootView, final String translatedText) {
        Resources resources = context.getResources();
        int margin = (int) resources.getDimension(R.dimen.activity_half_margin);
        final Snackbar snackbar = Snackbar.make(rootView.findViewById(R.id.activity_ocr),
                "Translated text: " + translatedText, Snackbar.LENGTH_INDEFINITE);
        final View snackbarView = snackbar.getView();
        TextView snackbarText = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        final EditText editText = (EditText) rootView.findViewById(R.id.searchbar_edit_text);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) snackbarView.getLayoutParams();
        layoutParams.setMargins(margin, 0, margin, margin);
        snackbarView.setLayoutParams(layoutParams);
        snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        snackbarText.setTextColor(Color.WHITE);
        snackbarText.setMaxLines(1);
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setAction(R.string.copy, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText(translatedText);
            }
        });
        return snackbar;
    }
}
