package orbital.com.foodsearch.Activities;

import android.os.Bundle;
import android.os.Handler;

import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import orbital.com.foodsearch.R;

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {
    private final Handler waitHandler = new Handler();
    private final Runnable waitCallback = new Runnable() {
        @Override
        public void run() {
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        waitHandler.postDelayed(waitCallback, 2000);

        setButtonBackVisible(false);
        setButtonNextVisible(false);
        setButtonCtaVisible(false);

//        setPagerIndicatorVisible(false);

        setPageScrollDuration(500);

        /**
         * Standard slide (like Google's intros)
         */
        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide_title)
                .description(R.string.intro_slide_desc)
                .image(R.drawable.large_launcher_icon)
                .background(R.color.white)
                .backgroundDark(R.color.onboardingGrey)
                .layout(R.layout.slide_onboarding)
                .build());

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
//                .buttonCtaLabel(R.string.label_button_cta)
//                .buttonCtaClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        nextSlide();
//                    }
//                })
                .layout(R.layout.slide_onboarding)
                .build());
        autoplay(2500, 1);
    }

    @Override
    protected void onDestroy() {
        waitHandler.removeCallbacks(waitCallback);
        super.onDestroy();
    }

}
