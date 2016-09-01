package orbital.com.menusnap.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.transition.Transition;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewPropertyAnimator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.github.clans.fab.FloatingActionMenu;

import orbital.com.menusnap.R;

/**
 * Created by Abel on 6/28/2016.
 */

public class AnimUtils {
    public static final int PROGRESS_BAR_DURATION = 150;
    public static final int SEARCH_BAR_SHOW = 200;
    public static final int SEARCH_BAR_HIDE = 150;
    public static final int RESULTS_UP_DURATION = 550;
    public static final int RESULTS_DOWN_DURATION = 400;
    public static final int OVERLAY_DURATION = 400;
    public static final int OVERLAY_EXIT_DURATION = 300;
    public static final int FAB_OVERLAY_DURATION = 150;
    public static final int TRANSLATE_REVEAL_DURATION = 600;
    public static final int SEARCH_BAR_RAISE = 450;
    public static final int SEARCH_BAR_DROP = 400;
    public static final int TEXT_UPDATE = 500;
    private static final int REVEAL_DURATION = 250;
    private static final int EXIT_DURATION = 250;
    public static final int FAST_FADE = 200;
    public static final int FASTER_FADE = 100;

    public static void brightenOverlay(final View overlay) {
        int currentColor = ((ColorDrawable) overlay.getBackground()).getColor();
        ValueAnimator brightenAnim = ValueAnimator.ofObject(new ArgbEvaluator(),
                currentColor,
                Color.TRANSPARENT);
        brightenAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                overlay.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        brightenAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                overlay.setVisibility(View.GONE);
            }
        });

        brightenAnim.setDuration(OVERLAY_DURATION);
        brightenAnim.start();
    }

    public static void darkenOverlay(final View overlay) {
        overlay.setVisibility(View.VISIBLE);
        int currentColor = ((ColorDrawable) overlay.getBackground()).getColor();
        ValueAnimator darkenAnim = ValueAnimator.ofObject(new ArgbEvaluator(),
                currentColor,
                Color.parseColor("#BF000000"));
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
        View searchFragContainer = rootView.findViewById(R.id.search_frag_container);
        ObjectAnimator anim = ObjectAnimator.ofFloat(searchFragContainer,
                View.TRANSLATION_Y, containerTransY);
        anim.addListener(animListener);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.setDuration(RESULTS_DOWN_DURATION);
        anim.start();
    }

    public static void containerSlideUp(final View rootView, Animator.AnimatorListener listener) {
        FrameLayout resultsContainer = (FrameLayout) rootView.findViewById(R.id.search_frag_container);
        ObjectAnimator anim = ObjectAnimator.ofFloat(resultsContainer,
                View.TRANSLATION_Y, 0);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.setDuration(RESULTS_UP_DURATION);
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    public static void showSearchBar(View searchBar, Animator.AnimatorListener listener) {
        searchBar.animate().translationY(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(SEARCH_BAR_SHOW)
                .setListener(listener)
                .start();
    }

    public static void hideSearchBar(View searchBar, int searchbarTrans) {
        if (searchBar != null) {
            searchBar.animate().translationY(searchbarTrans)
                    .setDuration(SEARCH_BAR_HIDE)
                    .start();
        }
    }

    public static void scaleUp(View view, float scale, AnimatorListenerAdapter listener){
        int duration = REVEAL_DURATION;
        ViewPropertyAnimator anim = view.animate()
                .scaleX(scale).scaleY(scale)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(duration);
        if(listener!=null){
            anim.setListener(listener);
        }
        anim.start();
    }

    public static void scaleDown(View view, AnimatorListenerAdapter listener) {
        ViewPropertyAnimator anim = view.animate()
                .scaleX(0).scaleY(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(EXIT_DURATION);
        anim.setListener(listener);
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void circularReveal(View startView, final View revealView, View filledView, Animator.AnimatorListener listener) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            AnimUtils.fadeIn(revealView, OVERLAY_DURATION);
            return;
        }
        // get the center for the clipping circle
        int cx = startView.getLeft() + startView.getWidth() / 2;
        int cy = startView.getTop() + startView.getHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = (int) Math.hypot(filledView.getWidth(), filledView.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(revealView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        anim.setDuration(TRANSLATE_REVEAL_DURATION);
        revealView.setVisibility(View.VISIBLE);
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void enterReveal(View enterView, Animator.AnimatorListener listener) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            AnimUtils.fadeIn(enterView, OVERLAY_DURATION);
            return;
        }
        // get the center for the clipping circle
        int cx = (int) enterView.getPivotX();
        int cy = (int) enterView.getPivotY();

        // get the final radius for the clipping circle
        int finalRadius = (int) Math.hypot(enterView.getWidth(), enterView.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(enterView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        anim.setDuration(TRANSLATE_REVEAL_DURATION);
        enterView.setVisibility(View.VISIBLE);
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void exitReveal(final View exitView, View endView, Animator.AnimatorListener listener) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            AnimUtils.fadeOut(exitView, OVERLAY_DURATION);
            return;
        }
        // get the center for the clipping circle
        int cx = endView.getLeft() + endView.getMeasuredWidth() / 2;
        int cy = endView.getTop() + endView.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = exitView.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim =
                null;
        anim = ViewAnimationUtils.createCircularReveal(exitView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                exitView.setVisibility(View.GONE);
            }
        });
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.setDuration(OVERLAY_EXIT_DURATION);

        // start the animation
        anim.start();
    }

    public static void fadeIn(View view, int duration) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        animator.setDuration(duration);
        animator.start();
    }

    public static void setFabMenuIcon(Context context, final FloatingActionMenu fam) {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(fam.getMenuIconView(), "rotation", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(fam.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(fam.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(fam.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                fam.getMenuIconView().setImageResource(fam.isOpened()
                        ? R.drawable.ic_camera_iris_white : R.drawable.ic_camera_white);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        fam.setIconToggleAnimatorSet(set);
    }

    public static void fadeOut(final View view, int duration) {
        final Float originalAlpha = view.getAlpha();
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, originalAlpha, 0);
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                view.setAlpha(originalAlpha);
            }
        });
        animator.start();
    }

    public static class TransitionListenerAdapter implements Transition.TransitionListener {

        @Override
        public void onTransitionStart(Transition transition) {

        }

        @Override
        public void onTransitionEnd(Transition transition) {

        }

        @Override
        public void onTransitionCancel(Transition transition) {

        }

        @Override
        public void onTransitionPause(Transition transition) {

        }

        @Override
        public void onTransitionResume(Transition transition) {

        }
    }
}

