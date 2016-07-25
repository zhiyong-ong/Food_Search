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
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;
import uk.co.senab.photoview.PhotoView;

public class PhotoViewActivity extends AppCompatActivity {
    public static final String URL = "url";
    public static final String POSITION = "position";
    public static final String THUMBURL = "thumbUrl";
    public static final String FORMATTED_URL = "formattedUrl";
    public static final String TITLE = "title";
    private boolean finishedEnter = false;
    private boolean backPressed = false;

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
        backPressed = true;
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
        photoView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (backPressed) {
                    supportFinishAfterTransition();
                } else if (finishedEnter) {
                    Picasso.with(PhotoViewActivity.this)
                            .load(getIntent().getStringExtra(URL))
                            .noPlaceholder()
                            .into(photoView);
                }
            }
        });
        final String url = getIntent().getStringExtra(URL);
        // Use picasso to load thumbnail into image view for shared element transition first
        // On success, load full res into photo view and start transition animation.
        // On success of full res, set placeholder imageview to INVISIBLE.
        Picasso.with(this)
                .load(getIntent().getStringExtra(THUMBURL))
                .noPlaceholder()
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            Picasso.with(PhotoViewActivity.this)
                                    .load(url)
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
        final Transition sharedElement = getWindow().getSharedElementEnterTransition();
        sharedElement.addListener(new AnimUtils.TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                sharedElement.removeListener(this);
                PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
                finishedEnter = true;
                photoView.getLayoutParams().width = PercentRelativeLayout.LayoutParams.MATCH_PARENT;
                photoView.getLayoutParams().height = PercentRelativeLayout.LayoutParams.MATCH_PARENT;
                photoView.requestLayout();
                super.onTransitionEnd(transition);
            }
        });
    }
}
