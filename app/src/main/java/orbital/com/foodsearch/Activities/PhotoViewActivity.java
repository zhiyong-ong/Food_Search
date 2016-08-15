package orbital.com.foodsearch.Activities;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.transition.Fade;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.squareup.picasso.Callback;

import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;
import orbital.com.foodsearch.Utils.ImageUtils;
import uk.co.senab.photoview.PhotoView;

public class PhotoViewActivity extends AppCompatActivity {
    public static final String URL = "url";
    public static final String POSITION = "position";
    public static final String THUMBURL = "thumbUrl";
    public static final String FORMATTED_URL = "formattedUrl";
    public static final String TITLE = "title";
    private boolean finishedEnter = false;
    private boolean leavingActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        setSupportActionBar((Toolbar) findViewById(R.id.photo_view_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        initialize();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.getLayoutParams().width = PercentRelativeLayout.LayoutParams.MATCH_PARENT;
        photoView.getLayoutParams().height = PercentRelativeLayout.LayoutParams.WRAP_CONTENT;
        leavingActivity = true;
        photoView.requestLayout();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initializeLollipop();
        }

        TextView titleTextView = (TextView) findViewById(R.id.photo_title);
        titleTextView.setText(getIntent().getStringExtra(TITLE));
        TextView urlTextView = (TextView) findViewById(R.id.photo_url);
        urlTextView.setMovementMethod(LinkMovementMethod.getInstance());
        urlTextView.setText(Html.fromHtml(getIntent().getStringExtra(FORMATTED_URL)));
        final PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        final View infoView = findViewById(R.id.photo_view_info);
        photoView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (leavingActivity) {
                    supportFinishAfterTransition();
                } else if (finishedEnter) {
                    AnimUtils.fadeIn(infoView, AnimUtils.FAST_FADE);
                    ImageUtils.getPicassoInstance(PhotoViewActivity.this)
                            .load(getIntent().getStringExtra(URL))
                            .noFade()
                            .noPlaceholder()
                            .into(photoView);
                }
            }
        });
        final String url = getIntent().getStringExtra(URL);
        // Use picasso to load thumbnail into image view for shared element transition first
        // On success, load full res into photo view and start transition animation.
        // On success of full res, set placeholder imageview to INVISIBLE.
        ImageUtils.getPicassoInstance(this)
                .load(getIntent().getStringExtra(THUMBURL))
                .noPlaceholder()
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            AnimUtils.fadeIn(infoView, AnimUtils.FAST_FADE);
                            ImageUtils.getPicassoInstance((PhotoViewActivity.this))
                                    .load(url)
                                    .noFade()
                                    .noPlaceholder()
                                    .into(photoView);
                        }
                    }
                    @Override
                    public void onError() {

                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initializeLollipop() {
        postponeEnterTransition();
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.BLACK);
        Transition fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);
        final Transition sharedElement = getWindow().getSharedElementEnterTransition();
        sharedElement.addListener(new AnimUtils.TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                sharedElement.removeListener(this);
                finishedEnter = true;
                PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
                photoView.getLayoutParams().width = PercentRelativeLayout.LayoutParams.MATCH_PARENT;
                photoView.getLayoutParams().height = PercentRelativeLayout.LayoutParams.MATCH_PARENT;
                photoView.requestLayout();
                super.onTransitionEnd(transition);
            }
        });
    }
}
