package orbital.com.foodsearch.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

import java.io.File;
import java.util.Locale;

import orbital.com.foodsearch.DAO.PhotosContract;
import orbital.com.foodsearch.DAO.PhotosDBHelper;
import orbital.com.foodsearch.Fragments.RecentsFragment;
import orbital.com.foodsearch.Fragments.SettingFragment;
import orbital.com.foodsearch.Misc.GlobalVar;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int OCR_CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int OCR_CAMERA_INTENT_REQUEST_CODE = 100;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final int IMAGE_PICK_INTENT_REQUEST_CODE = 200;
    private static final int CAMERA_CROP_INTENT_REQUEST_CODE = 300;
    private static final int GALLERY_CROP_INTENT_REQUEST_CODE = 400;
    private static final int OCR_IMAGE_INTENT_CODE = 500;
    private static final String RECENTS_FRAG_TAG = "recentsFrag";
    private static final String SETTINGS_FRAG_TAG = "settingsFrag";
    private static final String SAVED_URI = "savedUri";
    private static final String LOG_TAG = "FOODIES";
    private static final String PHOTO_FILE_NAME = "photo.jpg";
    public static String BASE_LANGUAGE;
    private static SharedPreferences sharedPreferencesSettings;
    private final String foodSearch = "FoodSearch";
    private final String user = "foodies@firebase.com";
    private final String password = "Orbital123";
    public boolean savedNewImage = false;
    private Uri fileUri = null;
    private FrameLayout mFabOverlay;
    private FloatingActionMenu mFabMenu;
    private RecentsFragment mRecentsFrag;
    private SettingFragment mSettingFrag;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseAsync mFirebaseTask;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        setupFab();
        setBottomNavigationBar();
        generateUri();

        sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.settings_preference, false);
        getBaseLanguage();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (sharedPreferencesSettings != null) {
            sharedPreferencesSettings.registerOnSharedPreferenceChangeListener(this);
        }
        mFirebaseTask = new FirebaseAsync();
        mFirebaseTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setupFirebase() {
        database = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    database.child("APIKEY").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot next : dataSnapshot.getChildren()) {
                                if (next.getKey().equals("OCP_APIM_KEY")) {
                                    GlobalVar.setImageKey(next.getChildren().iterator().next().getValue(String.class));
                                } else if (next.getKey().equals("OCR_KEY")) {
                                    GlobalVar.setOcrKey(next.getChildren().iterator().next().getValue(String.class));
                                } else if (next.getKey().equals("TRANSLATE_KEY")) {
                                    GlobalVar.setTranslateKey(next.getChildren().iterator().next().getValue(String.class));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(LOG_TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });
                    Log.e(LOG_TAG, "onAuthStateChanged:signed_in");
                } else {
                    // User is signed out
                    Log.e(LOG_TAG, "onAuthStateChanged:signed_out");
                }
<<<<<<< HEAD:app/src/main/java/orbital/com/foodsearch/activities/MainActivity.java
            }

        };
        mAuth.addAuthStateListener(mAuthListener);
=======
                //refreshDB();
            }

        };
        if(GlobalVar.getImageKey() == null) {
            signInFirebase();
        }
>>>>>>> origin/master:app/src/main/java/orbital/com/foodsearch/Activities/MainActivity.java
    }

    //feeder code... add it into the signing in code if you wanna refresh db.
    private void refreshDB() {
        PhotosDBHelper mDbHelper = new PhotosDBHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + PhotosContract.PhotosEntry.TABLE_NAME);
        mDbHelper.onCreate(db);
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        String langKey = getString(R.string.select_lang_key);
        String numRecentsKey = getString(R.string.num_recents_key);
        if (langKey.equals(s)) {
            MainActivity.BASE_LANGUAGE = sharedPreferences.getString(langKey, MainActivity.BASE_LANGUAGE);
        }
    }

    private void setBottomNavigationBar() {
        if (mFabOverlay == null || mFabMenu == null) {
            mFabOverlay = (FrameLayout) findViewById(R.id.fab_overlay);
            mFabMenu = (FloatingActionMenu) findViewById(R.id.start_fab);
        }
        final AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationItem recentsItem = new AHBottomNavigationItem(R.string.recents_tab, R.drawable.ic_history,
                R.color.colorPrimary);
        AHBottomNavigationItem settingsItem = new AHBottomNavigationItem(R.string.settings_tab, R.drawable.ic_settings,
                R.color.colorPrimary);
        bottomNavigation.addItem(recentsItem);
        bottomNavigation.addItem(settingsItem);
        bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimary));
        bottomNavigation.setForceTitlesDisplay(true);
        bottomNavigation.setBehaviorTranslationEnabled(false);
        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset < 0) {
                    bottomNavigation.hideBottomNavigation(true);
                } else {
                    bottomNavigation.restoreBottomNavigation(true);
                }
            }
        });

        bottomNavigation.setCurrentItem(1);
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                closeFab();
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                if (wasSelected) {
                    switch (position) {
                        case 0:
                            if (mRecentsFrag == null) {
                                mRecentsFrag = new RecentsFragment();
                                ft.replace(R.id.nav_frag_container, mRecentsFrag);
                                ft.commit();
                            } else {
                                mRecentsFrag.smoothScrollToTop();
                            }
                            return true;
                    }
                    return false;
                }
                switch (position) {
                    case 0:
                        mRecentsFrag = new RecentsFragment();
                        ft.replace(R.id.nav_frag_container, mRecentsFrag);
                        //noinspection WrongConstant
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.commit();
                        return true;
                    case 1:
                        mSettingFrag = new SettingFragment();
                        ft.replace(R.id.nav_frag_container, mSettingFrag);
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
        mFabMenu.setClosedOnTouchOutside(false);
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

    @Override
    public void onBackPressed() {
        if (mFabMenu != null && mFabMenu.isOpened()) {
            closeFab();
        } else {
            super.onBackPressed();
        }
    }

    private void closeFab() {
        if (mRecentsFrag != null) {
            mRecentsFrag.finishActionMode();
        }
        mFabOverlay.setClickable(false);
        mFabMenu.close(true);
        AnimUtils.fadeOut(mFabOverlay, AnimUtils.FAB_OVERLAY_DURATION);
    }

    private void openFab() {
        if (mRecentsFrag != null) {
            mRecentsFrag.finishActionMode();
        }
        mFabOverlay.setClickable(true);
        mFabMenu.open(true);
        AnimUtils.fadeIn(mFabOverlay, AnimUtils.FAB_OVERLAY_DURATION);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (fileUri != null) {
            outState.putString(SAVED_URI, fileUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(SAVED_URI)) {
            fileUri = Uri.parse(savedInstanceState.getString(SAVED_URI));
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
            case OCR_IMAGE_INTENT_CODE:
                if (resultCode == RESULT_OK) {
                    expandAppBar();
                    savedNewImage = true;
                }
                break;
            case OCR_CAMERA_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    ExifInterface exif;
                    try {
                        exif = new ExifInterface(fileUri.getPath());
                        int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 1);
                        int length = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                        if (width > length) {
                            startCropActivity(fileUri, true);
                        } else {
                            startOcrActivity();
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                        Snackbar.make(findViewById(R.id.coord_main_layout), R.string.no_photo_text, Snackbar.LENGTH_LONG);
                    }
                }
                break;
            case IMAGE_PICK_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    startCropActivity(data.getData(), false);
                }
                break;
            case CAMERA_CROP_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    startOcrActivity();
                } else if (resultCode == RESULT_CANCELED) {
                    dispatchCameraIntent();
                }
                break;
            case GALLERY_CROP_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    startOcrActivity();
                } else if (resultCode == RESULT_CANCELED) {
                    dispatchGalleryIntent();
                }
                break;
        }
    }

    /**
     * Starts OcrActivity with the file uris
     */
    private void startOcrActivity() {
        Intent intent = new Intent(this, OcrActivity.class);
        intent.putExtra(OcrActivity.FILE_PATH, fileUri.getPath());
        startActivityForResult(intent, OCR_IMAGE_INTENT_CODE);

    }

    public void openRecentPhoto(View itemView, String path, String data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(null);
            getWindow().setEnterTransition(null);
        }
        Intent intent = new Intent(this, OcrActivity.class);
        intent.putExtra(OcrActivity.FILE_PATH, path);
        intent.putExtra(OcrActivity.RESPONSE, data);
        ImageView recentsImage = (ImageView) itemView.findViewById(R.id.recent_image_view);
        String transName = getString(R.string.recents_transition_name);
        ViewCompat.setTransitionName(recentsImage, transName);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, recentsImage, transName);
        startActivity(intent, options.toBundle());
    }

    private void startCropActivity(Uri uri, final boolean isCamera) {
        Intent intent = new Intent(this, CropActivity.class);
        intent.putExtra(CropActivity.SOURCE_URI, uri);
        intent.putExtra(CropActivity.OUTPUT_URI, fileUri);
        if (isCamera) {
            startActivityForResult(intent, CAMERA_CROP_INTENT_REQUEST_CODE);
        } else {
            startActivityForResult(intent, GALLERY_CROP_INTENT_REQUEST_CODE);
        }
    }

    /**
     * This method checks if we already registered a default language. If not,
     * we will check through system language until we find one that tallies and
     * set it as default lang. BASE_LANGUAGE is then set to this default.
     * If already registered default then we simply load for shared preference settings.
     */
    private void getBaseLanguage() {
        String defaultLang = sharedPreferencesSettings.getString(getString(R.string.select_lang_key), null);
        if (defaultLang == null) {
            String[] langValues = getResources().getStringArray(R.array.listLanguagesValues);
            String sysLang = Locale.getDefault().getLanguage();
            // Run through language values, if matching found for locale then set it to preferences
            for (String langValue : langValues) {
                if ((langValue.equals(sysLang))) {
                    defaultLang = langValue;
                    break;
                } else if (sysLang.equals("zh")) {
                    // TODO: Fix locales with country
                    defaultLang = "zh-CHS";
                    break;
                }
            }
            if (defaultLang == null) {
                defaultLang = "en";
            }
            sharedPreferencesSettings.edit().putString(getString(R.string.select_lang_key), defaultLang).apply();
            BASE_LANGUAGE = defaultLang;
        } else {
            BASE_LANGUAGE = sharedPreferencesSettings.getString(
                    getResources().getString(R.string.select_lang_key), defaultLang);
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

    public void goSearch(View view) {
        Intent intent = new Intent(this, GoogleSearchActivity.class);
        startActivity(intent);
    }

    /**
     * This method dispatches the camera intent
     */
    private void dispatchCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
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

    @Override
    public void onStop() {
        if (mAuthListener != null && mAuth != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (sharedPreferencesSettings != null) {
            sharedPreferencesSettings.registerOnSharedPreferenceChangeListener(null);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        expandAppBar();
    }

    public void enableScroll() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams)
                toolbar.getLayoutParams();
        params.setScrollFlags(
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        toolbar.setLayoutParams(params);
    }

    public void disableScroll() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams)
                toolbar.getLayoutParams();
        params.setScrollFlags(0);
        toolbar.setLayoutParams(params);
    }

    private void expandAppBar() {
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);
    }

    /**
     * This method generates the Uri and saves it as the member variable
     */
    private void generateUri() {
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                , foodSearch);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }
        fileUri = Uri.fromFile(new File(mediaStorageDir.getPath()
                + File.separator + PHOTO_FILE_NAME));
    }

    private class FirebaseAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            if (GlobalVar.hasKeyValues()) {
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            setupFirebase();
            signInFirebase();
            return null;
        }

        @Override
        protected void onCancelled() {
            if (mAuthListener != null && mAuth != null) {
                mAuth.addAuthStateListener(mAuthListener);
            }
        }
    }
}
