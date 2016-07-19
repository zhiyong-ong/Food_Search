package orbital.com.foodsearch.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.Iterator;

import orbital.com.foodsearch.Fragments.RecentsFragment;
import orbital.com.foodsearch.Fragments.SettingFragment;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;

public class MainActivity extends AppCompatActivity {
    static final String MyPREFERENCES = "Preferences";
    static final String IMAGE_KEY = "ImageKey";
    static final String TRANSLATE_KEY = "TranslateKey";
    static final String OCR_KEY = "OCRKey";
    private static final int OCR_CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int OCR_CAMERA_INTENT_REQUEST_CODE = 100;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final int IMAGE_PICK_INTENT_REQUEST_CODE = 200;
    private static final String SAVED_URI = "savedUri";
    private static final String LOG_TAG = "FOODIES";
    private static final String PHOTO_FILE_NAME = "photo.jpg";
    private static final String DEBUG_FILE_NAME = "debug.jpg";
    private SharedPreferences sharedpreferences;
    private Uri sourceFileUri = null;
    private Uri destFileUri = null;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private String user = "foodies@firebase.com";
    private String password = "Orbital123";
    private DatabaseReference database;
    private FrameLayout mFabOverlay;
    private FloatingActionMenu mFabMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        setupFab();
        setBottomNavigationBar();
        generateUri();

        database = FirebaseDatabase.getInstance().getReference();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    final SharedPreferences.Editor editor = sharedpreferences.edit();
                    database.child("APIKEY").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                            while (iter.hasNext()) {
                                DataSnapshot next = iter.next();
                                if (next.getKey().equals("OCP_APIM_KEY")) {
                                    editor.putString(IMAGE_KEY, next.getChildren().iterator().next().getValue(String.class));
                                } else if (next.getKey().equals("OCR_KEY")) {
                                    editor.putString(OCR_KEY, next.getChildren().iterator().next().getValue(String.class));
                                } else if (next.getKey().equals("TRANSLATE_KEY")) {
                                    editor.putString(TRANSLATE_KEY, next.getChildren().iterator().next().getValue(String.class));
                                }
                            }
                            editor.commit();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(LOG_TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });

                    Log.e(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.e(LOG_TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        signInFirebase();
    }

    private void setBottomNavigationBar() {
        if (mFabOverlay == null || mFabMenu == null) {
            mFabOverlay = (FrameLayout) findViewById(R.id.fab_overlay);
            mFabMenu = (FloatingActionMenu) findViewById(R.id.start_fab);
        }
        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationItem recentsItem = new AHBottomNavigationItem(R.string.recents_tab, R.drawable.ic_history,
                R.color.colorPrimary);
        AHBottomNavigationItem settingsItem = new AHBottomNavigationItem(R.string.settings_tab, R.drawable.ic_settings,
                R.color.colorPrimary);
        bottomNavigation.addItem(recentsItem);
        bottomNavigation.addItem(settingsItem);
        bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimary));
        bottomNavigation.setForceTitlesDisplay(true);
        bottomNavigation.setCurrentItem(1);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                mFabOverlay.setClickable(false);
                mFabMenu.close(true);
                AnimUtils.fadeOut(mFabOverlay, AnimUtils.FAB_OVERLAY_DURATION);
                if (wasSelected) {
                    return true;
                }
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                switch (position) {
                    case 0:
                        ft.replace(R.id.nav_frag_container, new RecentsFragment());
                        //noinspection WrongConstant
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.commit();
                        return true;
                    case 1:
                        ft.replace(R.id.nav_frag_container, new SettingFragment());
                        //noinspection WrongConstant
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.commit();
                        return true;
                }
                return false;
            }
        });
        bottomNavigation.setCurrentItem(0);
    }

    private void setupFab() {
        mFabOverlay = (FrameLayout) findViewById(R.id.fab_overlay);
        mFabMenu = (FloatingActionMenu) findViewById(R.id.start_fab);
        mFabMenu.setClosedOnTouchOutside(true);
        AnimUtils.setFabMenuIcon(this, mFabMenu);
        mFabMenu.getIconToggleAnimatorSet();
        mFabOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFab();
            }
        });
        mFabMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFabMenu.isOpened()) {
                    closeFab();
                    startCameraOcr();
                } else {
                    openFab();
                }
            }
        });
        FloatingActionButton fileFab = (FloatingActionButton) findViewById(R.id.start_image_pick_fab);
        fileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFab();
                startImagePick();
            }
        });
    }

    private void signInFirebase() {
        mAuth.signInWithEmailAndPassword(user, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.e(LOG_TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.e(LOG_TAG, "signInWithEmail", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        if (mFabMenu != null && mFabMenu.isOpened()) {
            closeFab();
        } else {
            super.onBackPressed();
        }
    }

    private void closeFab() {
        mFabOverlay.setClickable(false);
        mFabMenu.close(true);
        AnimUtils.fadeOut(mFabOverlay, AnimUtils.FAB_OVERLAY_DURATION);
    }

    private void openFab() {
        mFabOverlay.setClickable(true);
        mFabMenu.open(true);
        AnimUtils.fadeIn(mFabOverlay, AnimUtils.FAB_OVERLAY_DURATION);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (sourceFileUri != null) {
            outState.putString(SAVED_URI, sourceFileUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(SAVED_URI)) {
            sourceFileUri = Uri.parse(savedInstanceState.getString(SAVED_URI));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case OCR_CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchCameraIntent();
                } else {
                    Snackbar.make(findViewById(R.id.coord_main_layout), getString(R.string.permission_ungranted),
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                                            requestCode);
                                }
                            })
                            .show();
                }
                break;
            case READ_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchGalleryIntent();
                } else {
                    Snackbar.make(findViewById(R.id.coord_main_layout), getString(R.string.permission_ungranted),
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            requestCode);
                                }
                            })
                            .show();
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void startCameraOcr() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    OCR_CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            dispatchCameraIntent();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void startImagePick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            dispatchGalleryIntent();
        }
    }

    /**
     * This method starts the camera by checking permissions for api > 23
     * and if api < 23 it just dispatches Camera Intent
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void startDebug() {
        Intent intent = new Intent(this, OcrActivity.class);
        generateDebugUri();
        intent.putExtra(OcrActivity.SOURCE_FILE_PATH, sourceFileUri.getPath());
        intent.putExtra(OcrActivity.DEST_FILE_PATH, sourceFileUri.getPath());
        startActivity(intent);
    }

    public void goSearch(View view) {
        Intent intent = new Intent(this, GoogleSearchActivity.class);
        startActivity(intent);
    }

    /**
     * This method dispatches the camera intent
     */
    private void dispatchCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, sourceFileUri);
        startActivityForResult(cameraIntent, OCR_CAMERA_INTENT_REQUEST_CODE);
    }

    /**
     * This method dispatches the image pick intent
     */
    private void dispatchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_INTENT_REQUEST_CODE);
    }

    /**
     * This method is called after user takes or selected a photo.
     * The image is then cropped and sent directly to ocr activity.
     *
     * @param requestCode requestCode for this request
     * @param resultCode  resultCode returned by camera
     * @param data        data as returned by camera, should be null because EXTRA_MEDIA_OUTPUT
     *                    was defined
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OCR_CAMERA_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    startOcrActivity();
                } else {
                    break;
                }
                return;
            case IMAGE_PICK_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    startCropActivity(data);
                } else {
                    break;
                }
                return;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    startOcrActivity();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e("HELLO", result.getError().getMessage());
                }
                return;
        }
    }

    private void startCropActivity(Intent data) {
        CropImage.activity(data.getData())
                .setScaleType(CropImageView.ScaleType.CENTER_CROP)
                .setFixAspectRatio(true)
                .setAspectRatio(3, 5)
                .setAllowRotation(false)
                .setOutputUri(destFileUri)
                .start(this);
    }

    /**
     * Starts OcrActivity with the file uris
     */
    private void startOcrActivity() {
        Intent intent = new Intent(this, OcrActivity.class);
        intent.putExtra(OcrActivity.SOURCE_FILE_PATH, sourceFileUri.getPath());
        intent.putExtra(OcrActivity.DEST_FILE_PATH, destFileUri.getPath());
        startActivity(intent);
    }

    /**
     * This method generates the Uri and saves it as the member variable
     */
    private void generateUri() {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                , "FoodSearch");
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }
        sourceFileUri = destFileUri = Uri.fromFile(new File(mediaStorageDir.getPath()
                + File.separator + PHOTO_FILE_NAME));
    }

    /**
     * This method generates the Uri and saves it as the member variable
     */
    private void generateDebugUri() {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                , "FoodSearch");
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }
        destFileUri = Uri.fromFile(new File(mediaStorageDir.getPath()
                + File.separator + DEBUG_FILE_NAME));
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
