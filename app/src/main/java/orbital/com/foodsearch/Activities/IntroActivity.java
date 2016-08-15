package orbital.com.foodsearch.Activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import orbital.com.foodsearch.R;

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonBackVisible(false);
        setButtonNextVisible(false);
        setButtonCtaVisible(false);
//        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_BACKGROUND);
//        TypefaceSpan labelSpan = new TypefaceSpan(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "sans-serif-medium" : "sans serif");
//        SpannableString label = SpannableString.valueOf(getString(R.string.label_button_cta));
//        label.setSpan(labelSpan, 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        setPageScrollDuration(500);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPageScrollInterpolator(android.R.interpolator.fast_out_slow_in);
        }
        /**
         * Standard slide (like Google's intros)
         */
        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide_title)
                .description(R.string.intro_slide_desc)
                .image(R.drawable.ic_history_grey_128dp)
                .background(R.color.white)
                .backgroundDark(R.color.onboardingBgColor)
                .layout(R.layout.slide_onboarding)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.fab_slide_title)
                .description(R.string.fab_slide_desc)
                .image(R.drawable.ic_history_grey_128dp)
                .background(R.color.white)
                .backgroundDark(R.color.onboardingBgColor)
                .layout(R.layout.slide_onboarding)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.ocr_slide_title)
                .description(R.string.ocr_slide_desc)
                .image(R.drawable.ic_history_grey_128dp)
                .background(R.color.white)
                .backgroundDark(R.color.onboardingBgColor)
                .layout(R.layout.slide_onboarding)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.recents_slide_title)
                .description(R.string.images_slide_desc)
                .image(R.drawable.ic_history_grey_128dp)
                .background(R.color.white)
                .backgroundDark(R.color.onboardingBgColor)
                .layout(R.layout.slide_onboarding)
                .buttonCtaLabel(R.string.label_button_cta)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextSlide();
                    }
                })
                .build());
        autoplay(3500, 0);
    }
}
