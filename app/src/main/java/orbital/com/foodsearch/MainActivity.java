package orbital.com.foodsearch;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public final String LOG_TAG = "FOODIES";
    private String photoFileName = "photo.jpg";
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goSearch(View view) {
        Intent intent = new Intent(this, GoogleSearchActivity.class);
        startActivity(intent);
    }

    @TargetApi(23)
    public void startCamera(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            getPhotoFileUri(photoFileName);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    getPhotoFileUri(photoFileName);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            }
            return;
        }
    }

    public void startOcr(View view) {
        Intent intent = new Intent(this, OcrDebugActivity.class);
        startActivity(intent);
    }

    public Uri getUri() {
        return uri;
    }

    // Use this method to get ready the output file for the camera in the cache dir
    private void getPhotoFileUri(String fileName) {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                , "FoodSearch");
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(LOG_TAG, "failed to create directory");
            }
        }
        uri = Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator +
                fileName));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, OcrDebugActivity.class);
                intent.putExtra("filePath", uri.getPath());
                startActivity(intent);
            } else { // Result was a failure, creates snackbar to show
                Snackbar.make(findViewById(R.id.container), R.string.snackbar_text, Snackbar.LENGTH_SHORT).
                        show();
            }
        }
    }
}