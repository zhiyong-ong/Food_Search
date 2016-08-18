package orbital.com.menusnap.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.heinrichreimersoftware.materialintro.slide.Slide;
import com.heinrichreimersoftware.materialintro.view.FadeableViewPager;

import orbital.com.menusnap.R;
import orbital.com.menusnap.Utils.AnimUtils;

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonBackVisible(false);
        setButtonNextVisible(false);
        setButtonCtaVisible(false);
        setButtonCtaLabel(R.string.label_button_cta);

//        setPagerIndicatorVisible(false);

        setPageScrollDuration(500);

        /**
         * Standard slide (like Google's intros)
         */
        Slide slide = new SimpleSlide.Builder()
                .title(R.string.intro_slide_title)
                .description(R.string.intro_slide_desc)
                .image(R.drawable.large_launcher_icon)
                .background(R.color.white)
                .backgroundDark(R.color.onboardingGrey)
                .layout(R.layout.slide_onboarding)
                .build();
        addSlide(slide);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.snap_slide_title)
                .description(R.string.snap_slide_desc)
                .image(R.drawable.scan_slide_image)
                .background(R.color.white)
                .backgroundDark(R.color.onboardingGrey)
                .layout(R.layout.slide_onboarding)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.search_slide_title)
                .description(R.string.search_slide_desc)
                .image(R.drawable.search_slide_image)
                .background(R.color.searchSlideBg)
                .backgroundDark(R.color.searchSlideDarkBg)
                .layout(R.layout.slide_onboarding)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.save_slide_title)
                .description(R.string.save_slide_desc)
                .image(R.drawable.save_slide_image)
                .background(R.color.saveSlideBg)
                .backgroundDark(R.color.saveSlideDarkBg)
                .layout(R.layout.slide_onboarding)
                .build());
        addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                if (position == 3){
                    cancelAutoplay();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setButtonCtaVisible(true);
                        }
                    }, 2000);
                }
            }
        });
        final FadeableViewPager pager = (FadeableViewPager) findViewById(R.id.mi_pager);
        pager.setVisibility(View.INVISIBLE);
        AnimUtils.fadeIn(pager, 1500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                autoplay(3000, 1);
            }
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
