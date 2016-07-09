package orbital.com.foodsearch.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import orbital.com.foodsearch.R;

/**
 * Created by Abel on 6/28/2016.
 */

public class AnimUtils {
    public static final int PROGRESS_BAR_DURATION = 150;
    public static final int SEARCH_BAR_SHOW = 270;
    public static final int RESULTS_UP_DURATION = 550;
    public static final int RESULTS_DOWN_DURATION = 400;
    public static final int OVERLAY_DURATION = 400;
    public static final int SEARCH_BAR_RAISE = 450;
    public static final int SEARCH_BAR_DROP = 400;
    public static final int TRANSLATE_SHOW = 400;

    public static void brightenOverlay(final View overlay) {
        int currentColor = ((ColorDrawable) overlay.getBackground()).getColor();
        ValueAnimator darkenAnim = ValueAnimator.ofObject(new ArgbEvaluator(),
                currentColor,
                Color.TRANSPARENT);
        darkenAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                overlay.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        darkenAnim.setDuration(OVERLAY_DURATION);
        darkenAnim.start();
    }

    public static void darkenOverlay(final View overlay) {
        int currentColor = ((ColorDrawable) overlay.getBackground()).getColor();
        ValueAnimator darkenAnim = ValueAnimator.ofObject(new ArgbEvaluator(),
                currentColor,
                Color.parseColor("#9F000000"));
        darkenAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                overlay.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        darkenAnim.setDuration(OVERLAY_DURATION);
        darkenAnim.start();
    }

    public static void containerSlideDown(View rootView, Animator.AnimatorListener animListener,
                                    int containerTransY) {
        FrameLayout resultsContainer = (FrameLayout)rootView.findViewById(R.id.search_frag_container);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultsContainer,
                View.TRANSLATION_Y, containerTransY);
        anim.addListener(animListener);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.setDuration(RESULTS_DOWN_DURATION);
        if (resultsContainer.getTranslationY() != containerTransY){
            brightenOverlay(rootView.findViewById(R.id.drawable_overlay));
        }
        anim.start();
    }

    public static void containerSlideUp(final Context context, final View rootView) {
        FrameLayout resultsContainer = (FrameLayout) rootView.findViewById(R.id.search_frag_container);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultsContainer,
                View.TRANSLATION_Y, 0);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.setDuration(RESULTS_UP_DURATION);
        anim.start();
    }

    public static void showSearchBar(Context context, View searchBar, String searchParam) {
        searchBar.animate().translationY(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(SEARCH_BAR_SHOW)
                .start();
        EditText editText = (EditText)searchBar.findViewById(R.id.edit_text);
        editText.setText(searchParam);
        editText.clearFocus();
        ImageButton cancelBtn = (ImageButton) searchBar.findViewById(R.id.cancel_search);
        cancelBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cancel_search));
    }

    public static void hideSearchBar(View searchBar, int searchbarTrans){
        searchBar.animate().translationY(searchbarTrans)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(SEARCH_BAR_SHOW)
                .start();
    }

    public static void raiseSearchBar(Context context, View searchBar, int marginTopPx) {
        searchBar.animate().y(marginTopPx)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(SEARCH_BAR_RAISE)
                .start();
        EditText editText = (EditText) searchBar.findViewById(R.id.edit_text);
        editText.clearFocus();
        ImageButton cancelBtn = (ImageButton) searchBar.findViewById(R.id.cancel_search);
        cancelBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_drop_search));
    }

    public static void dropSearchBar(Context context, View searchBar) {
        if (searchBar.getTranslationY() > 0) {
            return;
        }
        searchBar.animate().translationY(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(SEARCH_BAR_DROP)
                .start();
        EditText editText = (EditText) searchBar.findViewById(R.id.edit_text);
        editText.clearFocus();
        ImageButton cancelBtn = (ImageButton) searchBar.findViewById(R.id.cancel_search);
        cancelBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cancel_search));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void exitReveal(final View view) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        // get the center for the clipping circle
        int cx = view.getMeasuredWidth() / 2;
        int cy = view.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = view.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });

        // start the animation
        anim.start();
    }

    public static void fadeIn(View view, int duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        animator.setDuration(duration);
        animator.start();
    }

    public static void fadeOut(final View view, int duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), 0);
        animator.setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }
}

