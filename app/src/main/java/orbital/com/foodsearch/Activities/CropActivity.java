package orbital.com.foodsearch.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImageView;

import orbital.com.foodsearch.R;

public class CropActivity extends AppCompatActivity implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnSaveCroppedImageCompleteListener, CropImageView.OnGetCroppedImageCompleteListener {
    public static final String SOURCE_URI = "SourceUri";
    public static final String OUTPUT_URI = "OutputUri";
    private CropImageView mCropImageView;
    private Uri mOutputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        setSupportActionBar((Toolbar) findViewById(R.id.crop_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_crop);
        mCropImageView = (CropImageView) findViewById(R.id.crop_image_view);
        mCropImageView.setFixedAspectRatio(true);
        mCropImageView.setAspectRatio(3, 5);
        mCropImageView.setShowProgressBar(false);
        mCropImageView.setImageUriAsync((Uri) getIntent().getParcelableExtra(SOURCE_URI));
        mOutputUri = getIntent().getParcelableExtra(OUTPUT_URI);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.crop_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCropImageView.getCroppedImageAsync();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crop_view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crop_rotate:
                mCropImageView.rotateImage(90);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnGetCroppedImageCompleteListener(this);
        mCropImageView.setOnSaveCroppedImageCompleteListener(this);
    }

    @Override
    protected void onStop() {
        mCropImageView.setOnSetImageUriCompleteListener(null);
        mCropImageView.setOnGetCroppedImageCompleteListener(null);
        mCropImageView.setOnSaveCroppedImageCompleteListener(null);
        super.onStop();
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
    }

    @Override
    public void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error) {
        view.saveCroppedImageAsync(mOutputUri);
    }

    @Override
    public void onSaveCroppedImageComplete(CropImageView view, Uri uri, Exception error) {
        //create a new intent...
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
