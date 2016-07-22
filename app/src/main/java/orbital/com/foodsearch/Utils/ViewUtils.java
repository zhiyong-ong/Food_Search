package orbital.com.foodsearch.Utils;

import android.animation.Animator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import orbital.com.foodsearch.R;

/**
 * Created by Abel on 7/20/2016.
 */

public class ViewUtils {
    public static void startSearchProgress(View rootView) {
        AnimUtils.fadeIn(rootView.findViewById(R.id.drawable_overlay), AnimUtils.OVERLAY_DURATION);
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.VISIBLE);
    }

    public static void terminateSearchProgress(View rootView) {
        AnimUtils.fadeOut(rootView.findViewById(R.id.drawable_overlay), AnimUtils.OVERLAY_DURATION);
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.GONE);
    }

    public static void showSearchResults(final View rootView, String translatedText) {
        TextView textView = (TextView) rootView.findViewById(R.id.searchbar_translate_text);
        textView.setText(translatedText);
        AnimUtils.containerSlideUp(rootView, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                rootView.findViewById(R.id.drawable_overlay).setClickable(false);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.GONE);
        AnimUtils.fadeIn(rootView.findViewById(R.id.searchbar_translate_btn), AnimUtils.OVERLAY_DURATION);
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
        AnimUtils.fadeIn(rootView.findViewById(R.id.progress_bar), AnimUtils.PROGRESS_BAR_DURATION);
    }

    public static void finishOcrProgress(View rootView) {
        AnimUtils.fadeOut(rootView.findViewById(R.id.drawable_overlay), AnimUtils.OVERLAY_DURATION);
        AnimUtils.fadeOut(rootView.findViewById(R.id.progress_bar), AnimUtils.PROGRESS_BAR_DURATION);
    }
}
