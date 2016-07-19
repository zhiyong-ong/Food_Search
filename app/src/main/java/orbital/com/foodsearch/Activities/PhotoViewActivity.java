package orbital.com.foodsearch.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import orbital.com.foodsearch.R;
import uk.co.senab.photoview.PhotoView;

public class PhotoViewActivity extends AppCompatActivity {
    public static final String URL = "url";
    public static final String POSITION = "position";
    public static final String THUMBURL = "thumbUrl";
    public static final String FORMATTED_URL = "formattedUrl";
    public static final String TITLE = "title";
    private boolean backPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        setSupportActionBar((Toolbar) findViewById(R.id.photo_view_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }
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

    @Override
    public void onEnterAnimationComplete() {
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.getLayoutParams().width = PercentRelativeLayout.LayoutParams.MATCH_PARENT;
        photoView.getLayoutParams().height = PercentRelativeLayout.LayoutParams.MATCH_PARENT;
        super.onEnterAnimationComplete();
    }

    private void initialize() {
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
                        }
                        Picasso.with(PhotoViewActivity.this)
                                .load(url)
                                .noPlaceholder()
                                .into(photoView);
                    }
                    @Override
                    public void onError() {

                    }
                });
    }
}
