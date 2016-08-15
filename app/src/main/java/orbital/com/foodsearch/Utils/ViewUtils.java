package orbital.com.foodsearch.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import orbital.com.foodsearch.R;

/**
 * Created by Abel on 7/20/2016.
 */

public class ViewUtils {
    public static final int GRID_VIEW_ID = 0;
    public static final int LIST_VIEW_ID = 1;
    public static ArrayList<String> multStringQuery = new ArrayList<>();

    public static void startSearchProgress(View rootView) {
        AnimUtils.fadeIn(rootView.findViewById(R.id.drawable_overlay), AnimUtils.OVERLAY_DURATION);
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.VISIBLE);
    }

    public static void terminateSearchProgress(View rootView) {
        AnimUtils.fadeOut(rootView.findViewById(R.id.drawable_overlay), AnimUtils.OVERLAY_DURATION);
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.GONE);
    }

    public static void showSearchBar(View rootView, String searchParam, Animator.AnimatorListener listener, Boolean mult) {
        ImageButton translateBtn = (ImageButton) rootView.findViewById(R.id.searchbar_translate_btn);
        AnimUtils.fadeOut(translateBtn, AnimUtils.FAST_FADE);
        AnimUtils.showSearchBar(rootView.findViewById(R.id.search_bar_container), listener);
        for(int i = 0; i < multStringQuery.size(); i++) {
            Log.e("FOODIES", "create search bar: " + multStringQuery.get(i));
        }
        EditText editText = (EditText) rootView.findViewById(R.id.searchbar_edit_text);
        editText.setText(searchParam.toLowerCase());
        editText.clearFocus();
        if(mult) {
            multStringQuery.add(searchParam.toLowerCase());
        }
    }

    public static void appendSearchBar(View rootView, String appendParam) {
        multStringQuery.add(appendParam);
        EditText editText = (EditText) rootView.findViewById(R.id.searchbar_edit_text);
        editText.append(" " + appendParam);
        editText.clearFocus();
        for(int i = 0; i < multStringQuery.size(); i++) {
            Log.e("FOODIES", "append search bar: " + multStringQuery.get(i));
        }
    }

    public static void deleteSearchQuery(View rootView, String deleteParam) {
        multStringQuery.remove(deleteParam);
        for(int i = 0; i < multStringQuery.size(); i++) {
            Log.e("FOODIES", "arraylist: " + multStringQuery.get(i));
        }
        EditText editText = (EditText) rootView.findViewById(R.id.searchbar_edit_text);
        editText.clearComposingText();
        editText.setText(multStringQuery.get(0));
        for(int i = 1; i < multStringQuery.size(); i++) {
            editText.append(" " + multStringQuery.get(i));
        }
        editText.clearFocus();
    }
    public static void clearSearch() {
        multStringQuery.clear();
    }

    public static void searchImmediately(View rootView, String searchParam, Animator.AnimatorListener listener) {
        Button searchBtn = (Button) rootView.findViewById(R.id.searchbar_start_search);
        ImageButton translateBtn = (ImageButton) rootView.findViewById(R.id.searchbar_translate_btn);
        AnimUtils.fadeOut(translateBtn, AnimUtils.FAST_FADE);
        AnimUtils.showSearchBar(rootView.findViewById(R.id.search_bar_container), listener);
        EditText editText = (EditText) rootView.findViewById(R.id.searchbar_edit_text);
        editText.setText(searchParam);
        editText.clearFocus();
        searchBtn.performClick();
    }

    public static void showSearchResults(final View rootView, String translatedText) {
        TextView textView = (TextView) rootView.findViewById(R.id.searchbar_translate_text);
        textView.setText(translatedText);
        AnimUtils.darkenOverlay(rootView.findViewById(R.id.drawable_overlay));
        AnimUtils.containerSlideUp(rootView, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                rootView.findViewById(R.id.drawable_overlay).setClickable(false);
                rootView.findViewById(R.id.searchbar_translate_btn).performClick();
            }
        });
        AnimUtils.fadeIn(rootView.findViewById(R.id.searchbar_translate_btn), AnimUtils.OVERLAY_DURATION);
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.GONE);
    }

    public static void closeSearchResults(View rootView, Animator.AnimatorListener listener, int containerTransY) {
        View containerView = rootView.findViewById(R.id.search_frag_container);
        // If container is at the center position, slide it down t0 containerTransY.
        if (containerView.getTranslationY() == 0) {
            AnimUtils.containerSlideDown(rootView, listener, containerTransY);
        }
        Button searchButton = (Button) rootView.findViewById(R.id.searchbar_start_search);
        searchButton.setEnabled(true);
        ImageButton translateCloseBtn = (ImageButton) rootView.findViewById(R.id.searchbar_translate_close);
        translateCloseBtn.performClick();
        View overlay = rootView.findViewById(R.id.drawable_overlay);
        AnimUtils.fadeOut(overlay, AnimUtils.OVERLAY_DURATION);
        overlay.setClickable(true);
    }

    public static void startOcrProgress(View rootView) {
        AnimUtils.fadeIn(rootView.findViewById(R.id.drawable_overlay), AnimUtils.OVERLAY_DURATION);
        AnimUtils.fadeIn(rootView.findViewById(R.id.ocr_progress_bar), AnimUtils.PROGRESS_BAR_DURATION);
    }

    public static void finishOcrProgress(View rootView) {
        AnimUtils.fadeOut(rootView.findViewById(R.id.drawable_overlay), AnimUtils.OVERLAY_DURATION);
        AnimUtils.fadeOut(rootView.findViewById(R.id.ocr_progress_bar), AnimUtils.PROGRESS_BAR_DURATION);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
