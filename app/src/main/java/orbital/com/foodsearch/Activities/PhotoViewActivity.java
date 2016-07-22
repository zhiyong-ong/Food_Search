package orbital.com.foodsearch.Activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import orbital.com.foodsearch.R;
import uk.co.senab.photoview.PhotoView;

public class PhotoViewActivity extends AppCompatActivity {
    public static final String URL = "url";
    public static final String POSITION = "position";
    public static final String THUMBURL = "thumburl";
    public static final String PATH = "path";
    public static final String RESPONSE = "response";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(null);
        }
        setContentView(R.layout.activity_photo_view);
        initialize();

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initialize() {
        final PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        final Context context = this;
        final String url = getIntent().getStringExtra(URL);
        Picasso.with(this)
                .load(getIntent().getStringExtra(THUMBURL))
                .noPlaceholder()
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Picasso.with(context)
                                .load(url) // IMAGE_KEY url goes here
                                .noPlaceholder()
                                .into(photoView);
                    }

                    @Override
                    public void onError() {
                    }
                });
    }

}
