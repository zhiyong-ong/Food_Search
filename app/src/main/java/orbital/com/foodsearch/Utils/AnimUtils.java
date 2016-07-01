package orbital.com.foodsearch.Utils;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

import orbital.com.foodsearch.R;

/**
 * Created by Abel on 6/28/2016.
 */

public class AnimUtils {
    public static void brightenOverlay(final FrameLayout drawableOverlay){
        ValueAnimator darkenAnim = ValueAnimator.ofObject(new ArgbEvaluator(),
                Color.parseColor("#9F000000"),
                Color.TRANSPARENT);
        darkenAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                drawableOverlay.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        darkenAnim.setDuration(400);
        darkenAnim.start();
    }

    public static void darkenOverlay(final FrameLayout drawableOverlay){
        ValueAnimator darkenAnim = ValueAnimator.ofObject(new ArgbEvaluator(),
                Color.TRANSPARENT,
                Color.parseColor("#9F000000"));
        darkenAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                drawableOverlay.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        darkenAnim.setDuration(400);
        darkenAnim.start();
    }

    public static void containerSlideDown(View rootView, Animator.AnimatorListener animListener,
                                    int containerTransY) {
        FrameLayout resultsContainer = (FrameLayout)rootView.findViewById(R.id.search_frag_container);
        ObjectAnimator containerAnimation = ObjectAnimator.ofFloat(resultsContainer,
                View.TRANSLATION_Y, containerTransY);
        containerAnimation.addListener(animListener);
        containerAnimation.setDuration(300);
        if (resultsContainer.getTranslationY() != containerTransY){
            brightenOverlay((FrameLayout)rootView.findViewById(R.id.drawable_overlay));
        }
        containerAnimation.start();
    }

    public static void containerSlideUp(View rootView) {
        FrameLayout resultsContainer = (FrameLayout) rootView.findViewById(R.id.search_frag_container);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultsContainer,
                View.TRANSLATION_Y, 0);
        anim.setDuration(600);
        anim.start();
    }
}
