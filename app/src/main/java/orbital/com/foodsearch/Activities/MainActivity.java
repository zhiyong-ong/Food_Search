package orbital.com.foodsearch.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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

import orbital.com.foodsearch.R;

public class MainActivity extends AppCompatActivity {
    private static final int OCR_CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int EXP_CAMERA_PERMISSION_REQUEST_CODE = 2;
    private static final int OCR_CAMERA_INTENT_REQUEST_CODE = 100;
    private static final int EXP_CAMERA_INTENT_REQUEST_CODE = 200;

    private final String LOG_TAG = "FOODIES";
    private final String photoFileName = "photo.jpg";

    private Uri photoFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (photoFileUri != null) {
            outState.putString("savedUri", photoFileUri.toString());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("savedUri")) {
            photoFileUri = Uri.parse(savedInstanceState.getString("savedUri"));
        }
    }

    /**
     * This method starts the camera by checking permissions for api > 23
     * and if api < 23 it just dispatches Camera Intent
     * @param view
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void startOcr(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    OCR_CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            dispatchCameraIntent(OCR_CAMERA_INTENT_REQUEST_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void startExp(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    EXP_CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            dispatchCameraIntent(EXP_CAMERA_INTENT_REQUEST_CODE);
        }
    }

    public void goSearch(View view) {
        Intent intent = new Intent(this, GoogleSearchActivity.class);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case OCR_CAMERA_PERMISSION_REQUEST_CODE: case EXP_CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchCameraIntent(requestCode * 100);
                } else {
                    Snackbar.make(findViewById(R.id.container), getString(R.string.permission_ungranted),
                            Snackbar.LENGTH_SHORT)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                                            requestCode);
                                }
                            })
                            .show();
                }
            }
        }
    }

    /**
     * This method dispatches the camera intent by generating uri and attaching
     * it to the camera intent.
     */
    private void dispatchCameraIntent(int requestCode){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        generateUri();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
        startActivityForResult(cameraIntent, requestCode);
    }

    /**
     * This method is called after user takes a photo. If result is OK
     * then send the image file path to the ocr activity intent.
     * @param requestCode requestCode for this request
     * @param resultCode resultCode returned by camera
     * @param data data as returned by camera, should be null because EXTRA_MEDIA_OUTPUT
     *             was defined
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case OCR_CAMERA_INTENT_REQUEST_CODE:{
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, OcrDebugActivity.class);
                    intent.putExtra("filePath", photoFileUri.getPath());
                    startActivity(intent);
                }
            }
            case EXP_CAMERA_INTENT_REQUEST_CODE: {
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, OcrExpActivity.class);
                    intent.putExtra("filePath", photoFileUri.getPath());
                    startActivity(intent);
                }
            } default: {
                // requestCode fits none of the case so make snackbar to show that no
                // photo was taken
                Snackbar.make(findViewById(R.id.container), R.string.no_photo_text, Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * This method generates the Uri and saves it as the member variable
     */
    private void generateUri() {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                , "FoodSearch");
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }
        photoFileUri = Uri.fromFile(new File(mediaStorageDir.getPath()
                + File.separator + photoFileName));
    }
}