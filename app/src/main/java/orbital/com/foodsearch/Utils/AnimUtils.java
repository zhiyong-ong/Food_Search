package orbital.com.foodsearch.Utils;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;

import orbital.com.foodsearch.R;

/**
 * Created by Abel on 6/28/2016.
 */

public class AnimUtils {
    public static int DURATION_VERY_FAST = 150;
    public static int DURATION_FAST = 250;
    public static int DURATION_NORMAL = 350;
    public static int DURATION_SLOW = 400;
    public static int DURATION_SLOWER = 500;
    public static int DURATION_VERY_SLOW = 600;

    public static void brightenOverlay(final View overlay, int duration) {
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

        darkenAnim.setDuration(duration);
        darkenAnim.start();
    }

    public static void darkenOverlay(final View overlay, int duration) {
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

        darkenAnim.setDuration(duration);
        darkenAnim.start();
    }

    public static void containerSlideDown(View rootView, Animator.AnimatorListener animListener,
                                    int containerTransY) {
        FrameLayout resultsContainer = (FrameLayout)rootView.findViewById(R.id.search_frag_container);
        ObjectAnimator containerAnimation = ObjectAnimator.ofFloat(resultsContainer,
                View.TRANSLATION_Y, containerTransY);
        containerAnimation.addListener(animListener);
        containerAnimation.setDuration(DURATION_NORMAL);
        if (resultsContainer.getTranslationY() != containerTransY){
            brightenOverlay(rootView.findViewById(R.id.drawable_overlay), DURATION_NORMAL);
        }
        containerAnimation.start();
    }

    public static void containerSlideUp(View rootView) {
        FrameLayout resultsContainer = (FrameLayout) rootView.findViewById(R.id.search_frag_container);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultsContainer,
                View.TRANSLATION_Y, 0);
        anim.setDuration(DURATION_VERY_SLOW);
        anim.start();
    }

    public static void showSearchBar(View searchBar, String searchParam){
        searchBar.animate().translationY(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(DURATION_FAST)
                .start();
        EditText editText = (EditText)searchBar.findViewById(R.id.edit_text);
        editText.setText(searchParam);
        editText.clearFocus();
    }

    public static void hideSearchBar(View searchBar, int searchbarTrans){
        searchBar.animate().translationY(searchbarTrans)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(DURATION_FAST)
                .start();
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

