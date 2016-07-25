package orbital.com.foodsearch.Activities;

import android.animation.Animator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.transition.Transition;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import orbital.com.foodsearch.DAO.BingImageDAO;
import orbital.com.foodsearch.DAO.PhotosContract.PhotosEntry;
import orbital.com.foodsearch.DAO.PhotosDBHelper;
import orbital.com.foodsearch.Fragments.SearchBarFragment;
import orbital.com.foodsearch.Fragments.SearchResultsFragment;
import orbital.com.foodsearch.Helpers.BingOcr;
import orbital.com.foodsearch.Helpers.BingSearch;
import orbital.com.foodsearch.Helpers.BingTranslate;
import orbital.com.foodsearch.Helpers.ImageInsights;
import orbital.com.foodsearch.Models.ImageInsightsPOJO.ImageInsightsResponse;
import orbital.com.foodsearch.Models.ImageSearchPOJO.ImageSearchResponse;
import orbital.com.foodsearch.Models.ImageSearchPOJO.ImageValue;
import orbital.com.foodsearch.Models.OcrPOJO.BingOcrResponse;
import orbital.com.foodsearch.Models.OcrPOJO.Line;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Utils.AnimUtils;
import orbital.com.foodsearch.Utils.FileUtils;
import orbital.com.foodsearch.Utils.ImageUtils;
import orbital.com.foodsearch.Utils.NetworkUtils;
import orbital.com.foodsearch.Utils.ViewUtils;
import orbital.com.foodsearch.Views.DrawableView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OcrActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String RESPONSE = "dataResponse";
    static final String FILE_PATH = "FILEPATH";
    private static final String LOG_TAG = "FOODIES";
    private static final String SEARCH_FRAGMENT_TAG = "SEARCHFRAGMENT";
    private static final String SEARCH_BAR_TAG = "SEARCHBARTAG";
    public static String IMAGE_KEY;
    public static String OCR_KEY;
    public static String TRANSLATE_KEY;
    public static int IMAGES_COUNT;
    public static int imageResultSize;
    private static volatile int insightsCount = 0;
    private static String mTranslatedText = null;
    private static AsyncTask<Void, Void, String> translateTask;
    private static BingImageDAO imgDAO = null;
    private static ImageSearchResponse searchResponse;
    private static ImageSearchResponse searchResponseDB;
    private static SharedPreferences sharedPreferencesSettings;
    private static int IMAGES_COUNT_MAX;
    private final FragmentManager FRAGMENT_MANAGER = getSupportFragmentManager();
    private DatabaseReference database;
    private boolean animating = false;
    private boolean leavingActivity = false;
    private int searchBarTrans;
    private String mFilePath = null;
    private String currentTime;
    private Context context;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private String user = "foodies@firebase.com";
    private String password = "Orbital123";

    private ArrayList<Long> IDArrayList;
    private String formattedDate;
    private String formattedTime;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mFilePath != null) {
            outState.putString(FILE_PATH, mFilePath);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(FILE_PATH)) {
            mFilePath = savedInstanceState.getString(FILE_PATH);
        }
    }

    /**
     * This is attached to sharedpreferencessettings. It checks if changed key is
     * equal to language key. If it is then we update BASE_LANGUAGE accordingly and
     * notify recycler accordingly so as to maintain same translation language
     * with search results card.
     * update
     *
     * @param sharedPreferences sharedpreferencessettings
     * @param s                 Key for the changed value
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        String langKey = getString(R.string.select_lang_key);
        if (langKey.equals(s)) {
            MainActivity.BASE_LANGUAGE = sharedPreferences.getString(langKey, MainActivity.BASE_LANGUAGE);
            SearchResultsFragment frag = (SearchResultsFragment) getSupportFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
            if (frag != null) {
                frag.notifyRecycler();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        if (mFilePath == null) {
            mFilePath = getIntent().getStringExtra(FILE_PATH);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.BLACK);
        }
        sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(this);
        onCreateBackground();
        setupSearchContainer();
        setupSearchBar();
        setupPreview();
    }

    private void onCreateBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                database = FirebaseDatabase.getInstance().getReference();
                imgDAO = new BingImageDAO();
                sharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES, MODE_PRIVATE);
                IMAGE_KEY = sharedPreferences.getString(MainActivity.IMAGE_KEY, null);
                OCR_KEY = sharedPreferences.getString(MainActivity.OCR_KEY, null);
                TRANSLATE_KEY = sharedPreferences.getString(MainActivity.TRANSLATE_KEY, null);

                IMAGES_COUNT_MAX = getResources().getIntArray(R.array.listNumber)[getResources().getIntArray(R.array.listNumber).length - 1];
                IMAGES_COUNT = Integer.parseInt(sharedPreferencesSettings.getString(getResources().getString(R.string.num_images_key), "1"));
                Log.e(LOG_TAG, "MAX IMAGES: " + IMAGES_COUNT_MAX);

                IDArrayList = new ArrayList<>();
                //current time
                Calendar cal = Calendar.getInstance();
                currentTime = FileUtils.getTimeStamp(cal);
                return null;
            }
        }.execute();

    }

    private void setupPreview() {
        final String data = getIntent().getStringExtra(RESPONSE);
        final ImageView previewImageView = (ImageView) findViewById(R.id.preview_image_view);
        if(data == null) {
            Picasso.with(this).load("file://" + mFilePath)
                    //.placeholder(R.color.black_overlay)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resize(30, 48)
                    .into(previewImageView);
            startOcrService();
            Log.e(LOG_TAG, "COMPRESS!  " + data);
        } else {
            String transName = getString(R.string.recents_transition_name);
            ViewCompat.setTransitionName(previewImageView, transName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setExitTransition(null);
                getWindow().setEnterTransition(null);
                findViewById(R.id.drawable_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.drawable_overlay).setVisibility(View.GONE);
                final Transition sharedElement = getWindow().getSharedElementEnterTransition();
                sharedElement.addListener(new AnimUtils.TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(Transition transition) {
                        sharedElement.removeListener(this);
                        if (!leavingActivity) {
                            AnimUtils.fadeIn(findViewById(R.id.drawable_view), AnimUtils.FASTER_FADE);
                        }
                        super.onTransitionEnd(transition);
                    }
                });
                postponeEnterTransition();
            }
            Picasso.with(this).load("file://" + mFilePath)
                    .into(previewImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                startPostponedEnterTransition();
                            }
                        }

                        @Override
                        public void onError() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                startPostponedEnterTransition();
                            }
                        }
                    });
            previewImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    previewImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    drawBoxesRecentImage(data, mFilePath, findViewById(R.id.activity_ocr));
                    return true;
                }
            });
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        sharedPreferencesSettings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        sharedPreferencesSettings.registerOnSharedPreferenceChangeListener(null);
    }

    /**
     * This method attaches a viewtreeobserver to get the appropriate translation y for
     * searchbar container.
     */
    private void setupSearchBar() {
        final FrameLayout searchBarContainer = (FrameLayout) findViewById(R.id.search_bar_container);
        ViewTreeObserver vto = searchBarContainer.getViewTreeObserver();
        // To move searchBarContainer out of the screen
        // Done using onPreDrawListener so as to get the correct measured height
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                searchBarContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                searchBarTrans = 2 * searchBarContainer.getHeight();
                searchBarContainer.setTranslationY(searchBarTrans);
                android.support.v4.app.FragmentTransaction ft = FRAGMENT_MANAGER.beginTransaction();
                ft.replace(R.id.search_bar_container, new SearchBarFragment(), SEARCH_BAR_TAG);
                ft.commit();
                return true;
            }
        });
    }

    /**
     * This method attaches a viewtreeobserver to get the appropriate translation y for
     * search results container.
     */
    private void setupSearchContainer() {
        final View rootView = findViewById(R.id.activity_ocr);
        final FrameLayout recyclerContainer = (FrameLayout) findViewById(R.id.search_frag_container);
        ViewTreeObserver vto = recyclerContainer.getViewTreeObserver();
        // To move recyclerContainer out of the screen
        // Done using onPreDrawListener so as to get the correct measured height
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                recyclerContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                recyclerContainer.setTranslationY(rootView.getHeight());
                android.support.v4.app.FragmentTransaction ft = FRAGMENT_MANAGER.beginTransaction();
                ft.replace(R.id.search_frag_container, new SearchResultsFragment(), SEARCH_FRAGMENT_TAG);
                ft.commit();
                return true;
            }
        });
    }

    private void initializeDrawView() {
        final DrawableView drawableView = (DrawableView) findViewById(R.id.drawable_view);
        drawableView.setOnTouchListener(new DrawableTouchListener(this, findViewById(R.id.activity_ocr)));
    }

    // Checks for internet connection, if available then just start service. Otherwise,
    // create snackbar fall user to retry. Compressed full res image also loaded into preview.
    private void startOcrService() {
        if (NetworkUtils.isNetworkAvailable(this) && NetworkUtils.isOnline()) {
            bingOcrConnect();
        } else {
            // Compress firsty then put bing connect on hold.
            CompressAsyncTask compressTask = new CompressAsyncTask(this, findViewById(R.id.activity_ocr)) {
                @Override
                protected void onPostExecute(byte[] compressedImage) {
                    ImageView previewImageView = (ImageView) findViewById(R.id.preview_image_view);
                    Picasso.with(mContext).load("file://" + mFilePath)
                            .noPlaceholder()
                            .fit()
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(previewImageView);
                    onHoldBingOcrConnect(compressedImage);
                }
            };
            compressTask.execute(mFilePath, mFilePath);
        }
    }

    /**
     * Method that compresses the IMAGE_KEY before sending it to BingOcrService
     */
    private void bingOcrConnect() {
        View root = findViewById(R.id.activity_ocr);
        CompressAsyncTask compressTask = new CompressAsyncTask(this, root) {
            @Override
            protected void onPostExecute(byte[] compressedImage) {
                // When compressTask is done, invoke dispatchCall for POST call
                Call<BingOcrResponse> call = BingOcr.buildCall(compressedImage);
                // Enqueue the method to the call and wait for callback (Asynchronous call)
                call.enqueue(new OcrCallback(findViewById(R.id.activity_ocr), mFilePath));
                // After call is dispatched, load full res IMAGE_KEY into preview
                ImageView previewImageView = (ImageView) findViewById(R.id.preview_image_view);
                Picasso.with(mContext).load("file://" + mFilePath)
                        .noPlaceholder()
                        .fit()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(previewImageView);
            }
        };
        compressTask.execute(mFilePath, mFilePath);
    }

    /**
     * This method puts bing ocr connect on hold with a snackbar for user to retry connection.
     * @param compressedImage compressed image to be sent for ocr service
     */
    private void onHoldBingOcrConnect(final byte[] compressedImage) {
        // When compressTask is done, load preview into preview imageview
        // and remove progressbars and overlay.
        // then allow user to retry
        final View rootView = findViewById(R.id.activity_ocr);
        ViewUtils.finishOcrProgress(rootView);
        Snackbar snackbar = Snackbar.make(rootView, R.string.internet_error_text, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.startOcrProgress(rootView);
                if (NetworkUtils.isNetworkAvailable(OcrActivity.this) && NetworkUtils.isOnline()) {
                    Call<BingOcrResponse> call = BingOcr.buildCall(compressedImage);
                    // Enqueue the method to the call and wait for callback (Asynchronous call)
                    call.enqueue(new OcrCallback(findViewById(R.id.activity_ocr), mFilePath));
                } else {
                    onHoldBingOcrConnect(compressedImage);
                }

            }
        });
        snackbar.show();
    }

    public void startBingImageSearch(final Context context, final String searchParam) {
        if (NetworkUtils.isNetworkAvailable(this) && NetworkUtils.isOnline()) {
            searchImageResponse(context, searchParam);
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_ocr),
                    R.string.internet_error_text, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBingImageSearch(context, searchParam);
                }
            });
            snackbar.show();
        }
    }


    @Override
    public void onBackPressed() {
        if (findViewById(R.id.search_frag_container).getTranslationY() != 0) {
            sharedPreferencesSettings.unregisterOnSharedPreferenceChangeListener(this);
            leavingActivity = true;
            findViewById(R.id.drawable_overlay).setVisibility(View.INVISIBLE);
            findViewById(R.id.search_bar_container).setVisibility(View.INVISIBLE);
            findViewById(R.id.drawable_view).setVisibility(View.INVISIBLE);
            supportFinishAfterTransition();
        } else {
            View rootView = findViewById(R.id.activity_ocr);
            ViewUtils.closeSearchResults(rootView, new IsAnimatingListener(rootView), rootView.getHeight());
        }
    }

    /**
     * Method called by cancel button on search bar.
     * This method closes both search bar and search results.
     */
    public void cancelSearch() {
        View rootView = findViewById(R.id.activity_ocr);
        ViewUtils.closeSearchResults(rootView, new IsAnimatingListener(rootView), rootView.getHeight());
        AnimUtils.hideSearchBar(findViewById(R.id.search_bar_container),
                searchBarTrans);
    }

    public void openPhotoView(View itemView, String contentUrl, String thumbUrl, int position) {
        final Intent intent = new Intent(this, PhotoViewActivity.class);
        intent.putExtra(PhotoViewActivity.URL, contentUrl);
        intent.putExtra(PhotoViewActivity.THUMBURL, thumbUrl);
        intent.putExtra(PhotoViewActivity.POSITION, position);
        intent.putExtra(PhotoViewActivity.TITLE,
                ((TextView) itemView.findViewById(R.id.card_title)).getText());
        String htmlText = Html.toHtml(((TextView) findViewById(R.id.card_hostpage)).getEditableText());
        intent.putExtra(PhotoViewActivity.FORMATTED_URL, htmlText);
        ImageView cardImage = (ImageView) itemView.findViewById(R.id.card_image);
        String transName = getString(R.string.fullscreen_transition_name);
        ViewCompat.setTransitionName(cardImage, transName);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, cardImage, transName);
        startActivity(intent, options.toBundle());
    }

    /**
     * Called by click listeners and other methods for when a search is to be
     * scheduled
     *
     * @param searchParam String to be searched for
     */
    private void searchImageResponse(final Context context, final String searchParam) {
        final View rootView = findViewById(R.id.activity_ocr);
        AnimUtils.containerSlideDown(rootView, new IsAnimatingListener(rootView), rootView.getHeight());
        ViewUtils.startSearchProgress(rootView);
        insightsCount = 0;

        database.child("images").child(searchParam).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set search response DB to value from cloud
                searchResponseDB = dataSnapshot.getValue(ImageSearchResponse.class);
                // initialize the imagesearch again
                if (searchResponseDB == null) {
                    enqueueSearch(searchParam);
                } else {
                    // Expected result size is the minimum of the max count and the size.
                    imageResultSize = Math.min(searchResponseDB.getImageValues().size(), IMAGES_COUNT);
                    SearchResultsFragment searchFragment = (SearchResultsFragment) FRAGMENT_MANAGER.findFragmentByTag(SEARCH_FRAGMENT_TAG);
                    searchFragment.clearRecycler();
                    for (int i = 0; i < imageResultSize; i++) {
                        ImageValue imgVal = searchResponseDB.getImageValues().get(i);
                        searchFragment.updateRecyclerList(imgVal);
                        searchImageInsights(context, imgVal);
                    }
                }
                enqueueTranslate(searchParam);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getUser:onCancelled", databaseError.toException());
                ViewUtils.terminateSearchProgress(rootView);
//                enqueueSearch(searchParam);
//                enqueueTranslate(searchParam);
        }
        });
    }

    /**
     * There is a possibility that there may exist 2 different images with the same image insight.
     * This method is called after onresponse for imagesearch callback in order to prevent duplicates from being
     * persisted into the database. Flag used is imageExists
     */
    private void searchImageInsights(final Context context, final ImageValue imgVal) {
        String insightsToken = imgVal.getImageInsightsToken();
        final View rootView = findViewById(R.id.activity_ocr);
        database.child("imageinsights").child(insightsToken).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ImageInsightsResponse imgInsightsDB = dataSnapshot.getValue(ImageInsightsResponse.class);
                //initialize the IMAGE_KEY insights search again
                if (imgInsightsDB == null) {
                    enqueueImageInsightCall(imgVal);
                } else {
                    imgVal.setInsightsResponse(imgInsightsDB);
                    insightsCount++;
                    Log.e(LOG_TAG, "Insights DB count: " + insightsCount);
                    if (insightsCount < imageResultSize) {
                        return;
                    }
                    new CompleteTask(context, rootView, FRAGMENT_MANAGER)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getUser:onCancelled", databaseError.toException());
                enqueueImageInsightCall(imgVal);
            }
        });
    }

    /**
     * This method creates a search call based on the input param and enqueues it
     *
     * @param searchParam parameter string to be searched for
     */
    private void enqueueSearch(String searchParam) {
        BingSearch bingImg = new BingSearch(searchParam, String.valueOf(IMAGES_COUNT_MAX));
        Call<ImageSearchResponse> call = bingImg.buildCall();
        call.enqueue(new ImageSearchCallback(this, findViewById(R.id.activity_ocr), FRAGMENT_MANAGER, searchParam));
    }

    /**
     * This method creates a translte call with search param and executes it on translateTask.
     * @param searchParam string parameter to translate
     */
    private void enqueueTranslate(final String searchParam) {
        mTranslatedText = searchParam;
        translateTask = new translateTask(searchParam).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * This method creates a imageinsight search call based on the input imageValues list and enqueues it
     *
     * @param imgVal IMAGE_KEY values list to be used as param for insightsToken call
     *               Multiple calls.
     */
    private void enqueueImageInsightCall(ImageValue imgVal) {
        ImageInsights imageInsights = new ImageInsights(imgVal.getImageInsightsToken(), "");
        Call<ImageInsightsResponse> call = imageInsights.buildCall();
        call.enqueue(new ImageInsightCallback(this, findViewById(R.id.activity_ocr),
                FRAGMENT_MANAGER,
                imgVal));
    }

    /**
     * saves the ocr response into the local db in the form of json, with the key being
     * the current time
     *
     * @param response
     */
    private void saveOcrResponse(BingOcrResponse response) {
        Log.e(LOG_TAG, "TIME STAMP: " + currentTime);
        //save the lines to local DB
        Gson gson = new Gson();
        String json = gson.toJson(response);
        PhotosDBHelper mDBHelper = new PhotosDBHelper(this);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Create a new map of values, where column names are the keys
        values.put(PhotosEntry.COLUMN_NAME_ENTRY_TIME, currentTime);
        values.put(PhotosEntry.COLUMN_NAME_TITLE, "Photo_Data");
        values.put(PhotosEntry.COLUMN_NAME_DATA, json);
        // Insert the new row, returning the primary key value of the new row
        long newRowID = db.insert(PhotosEntry.TABLE_NAME, null, values);
        Log.e(LOG_TAG, "INSERTED ROW ID: " + newRowID);
    }

    /**
     * Saves a duplicate of the image into the recent images folder.
     * Name of the image would be the timestamp.
     *
     * @param finalBitmap
     */
    private void saveImage(Bitmap finalBitmap) {
        File root = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Recent_Images");
        if (!root.exists()) {
            if (!root.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }
        String fname = currentTime;
        File file = new File(root, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Draw boxes onto the image that have been saved.
     *
     * @param response  JSON data for the boxes
     * @param mFilePath file path of the image
     * @param rootView  view in which the layout exists
     */
    private void drawBoxesRecentImage(String response, String mFilePath, View rootView) {
        DrawableView mDrawableView = (DrawableView) findViewById(R.id.drawable_view);
        Gson gson = new Gson();
        BingOcrResponse responseData = gson.fromJson(response, BingOcrResponse.class);
        List<Line> lines = responseData.getAllLines();
        mDrawableView.drawBoxes(rootView, mFilePath, lines,
                responseData.getTextAngle().floatValue(),
                responseData.getLanguage());
        ViewUtils.finishOcrProgress(rootView);
    }

    private static class CompleteTask extends AsyncTask<Void, Void, Void> {
        Context context = null;
        View rootView = null;
        FragmentManager fm = null;

        CompleteTask(Context context, View rootView, FragmentManager fm) {
            this.context = context;
            this.rootView = rootView;
            this.fm = fm;
        }

        @Override
        protected Void doInBackground(Void... params) {
            int count = 0;
            // Wait 10 seconds for translate task to be done
            while (!translateTask.getStatus().equals(Status.FINISHED)) {
                if (count == 20) {
                    Snackbar.make(rootView, R.string.translate_fail, Snackbar.LENGTH_SHORT);
                    return null;
                }
                try {
                    count++;
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            if (searchResponseDB == null) {
                //persist search result to DB
                imgDAO.persistImage(searchResponse);
            }
            Log.e(LOG_TAG, "TRANSLATED TEXT: " + mTranslatedText);
            SearchResultsFragment searchFragment = (SearchResultsFragment) fm.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            searchFragment.finalizeRecycler();
            ViewUtils.showSearchResults(rootView, mTranslatedText);
        }
    }

    /**
     * Callback class for ImageInsightCallback.
     */
    private static class ImageInsightCallback implements Callback<ImageInsightsResponse> {
        private Context context = null;
        private View rootView = null;
        private FragmentManager fm = null;
        private ImageValue imageValue = null;

        ImageInsightCallback(Context context, View rootView, FragmentManager fm, ImageValue imageValue) {
            this.context = context;
            this.rootView = rootView;
            this.fm = fm;
            this.imageValue = imageValue;
        }

        @Override
        public void onResponse(Call<ImageInsightsResponse> call, Response<ImageInsightsResponse> response) {
            // Get response body, persist to DB then complete task
            ImageInsightsResponse insightsResponse = response.body();
            imageValue.setInsightsResponse(insightsResponse);
            imgDAO.persistImageInsight(insightsResponse);
            insightsCount++;
            Log.e(LOG_TAG, "Insights callback count: " + insightsCount);
            if (insightsCount < imageResultSize) {
                return;
            }
            // Once we have collated X imageinsights count, start CompleteTask to synchronize all tasks
            new CompleteTask(context, rootView, fm)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        public void onFailure(Call<ImageInsightsResponse> call, Throwable t) {
            insightsCount++;
            if (insightsCount < imageResultSize) {
                return;
            }
            // Failed to get the IMAGE_KEY insights so display snackbar error dialog
            new CompleteTask(context, rootView, fm)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Callback class for ImageSearchResponse, handling of POST responses is done here
     */
    private class ImageSearchCallback implements Callback<ImageSearchResponse> {
        private View rootView = null;
        private Context context = null;
        private FragmentManager fm = null;
        private String searchParam = null;

        ImageSearchCallback(Context context, View rootView, FragmentManager fm, String searchParam) {
            this.context = context;
            this.rootView = rootView;
            this.fm = fm;
            this.searchParam = searchParam;
        }

        @Override
        public void onResponse(Call<ImageSearchResponse> call, Response<ImageSearchResponse> response) {
            searchResponse = response.body();
            if (searchResponse == null) {
                try {
                    Log.e(LOG_TAG, response.errorBody().string());
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            searchResponse.setSearchQuery(searchParam);
            List<ImageValue> imageValues = searchResponse.getImageValues();
            // IMAGE_KEY search results received, now enqueueImageInsightCall with received value
            SearchResultsFragment searchFragment = (SearchResultsFragment) fm.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            if (!imageValues.isEmpty()) {
                // Expected result size is the minimum of the max count and the size.
                imageResultSize = Math.min(imageValues.size(), IMAGES_COUNT);
                searchFragment.clearRecycler();
                for (int i = 0; i < imageResultSize; i++) {
                    ImageValue imgVal = imageValues.get(i);
                    searchFragment.updateRecyclerList(imgVal);
                    searchImageInsights(context, imgVal);
                }
            } else {
                ViewUtils.terminateSearchProgress(rootView);
                Snackbar.make(rootView, R.string.no_image_found, Snackbar.LENGTH_LONG).show();
            }

        }

        @Override
        public void onFailure(Call<ImageSearchResponse> call, Throwable t) {
            Log.e(LOG_TAG, t.getMessage());
            Snackbar.make(rootView.findViewById(R.id.activity_ocr), R.string.image_search_fail,
                    Snackbar.LENGTH_LONG)
                    .show();
            ViewUtils.terminateSearchProgress(rootView);
            Snackbar.make(rootView, R.string.no_image_found, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Callback class that implements callback method to be used for when a
     * response is received from server
     */
    private class OcrCallback implements Callback<BingOcrResponse> {
        private View rootView = null;
        private String mFilePath = null;
        private DrawableView mDrawableView = null;

        private OcrCallback(View rootView, String filePath) {
            this.rootView = rootView;
            mFilePath = filePath;
            mDrawableView = (DrawableView) rootView.findViewById(R.id.drawable_view);
        }

        @Override
        public void onResponse(Call<BingOcrResponse> call, Response<BingOcrResponse> response) {
            BingOcrResponse bingOcrResponse = response.body();
            Gson gson = new Gson();
            String json = gson.toJson(bingOcrResponse);
            List<Line> lines = bingOcrResponse.getAllLines();
            mDrawableView.drawBoxes(rootView, mFilePath, lines,
                    bingOcrResponse.getTextAngle().floatValue(),
                    bingOcrResponse.getLanguage());
            ViewUtils.finishOcrProgress(rootView);
            saveOcrResponse(bingOcrResponse);
            saveImage(BitmapFactory.decodeFile(mFilePath));
        }

        @Override
        public void onFailure(Call<BingOcrResponse> call, Throwable t) {
            ViewUtils.finishOcrProgress(rootView);
            Snackbar.make(rootView.findViewById(R.id.activity_ocr), R.string.OCRFailed,
                    Snackbar.LENGTH_LONG)
                    .show();
            Log.e(LOG_TAG, "POST Call Failed!" + t.getMessage());
        }
    }

    /**
     * This async task is used to compress the IMAGE_KEY to be sent in the
     * background thread using ImageUtils.compressImage
     */
    private class CompressAsyncTask extends AsyncTask<String, Integer, byte[]> {
        Context mContext = null;
        View mRootView = null;

        CompressAsyncTask(Context context, View rootView) {
            mContext = context;
            mRootView = rootView;
        }

        @Override
        protected byte[] doInBackground(String... params) {
            return ImageUtils.compressImage(params[0], params[1]);
        }
    }


    private class DrawableTouchListener implements View.OnTouchListener {
        private Context context;
        private View rootView;
        private DrawableView mDrawableView = null;

        DrawableTouchListener(Context context, View rootView) {
            this.context = context;
            this.rootView = rootView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!animating) {
                ViewUtils.closeSearchResults(rootView, new IsAnimatingListener(rootView), rootView.getHeight());
            }
            mDrawableView = (DrawableView) v;
            List<Rect> rects = mDrawableView.getmRects();
            int x = (int) event.getX();
            int y = (int) event.getY();
            for (int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                if (rect.contains(x, y)) {
                    chooseRect(i);
                    return true;
                }
            }
            return v.performClick();
        }

        private void chooseRect(int i) {
            mDrawableView.chooseRect(i);
            // Display the string in a snackbar and allow for search
            String searchParam = mDrawableView.getmLineTexts().get(i).toLowerCase();
            searchParam = searchParam.substring(0, 1).toUpperCase() + searchParam.substring(1);
            View searchBar = findViewById(R.id.search_bar_container);
            // if !Animating or searchbar is already up, show searchbar with new search param.
            if (!animating || searchBar.getTranslationY() == 0) {
                AnimUtils.showSearchBar(rootView, searchBar, searchParam, new IsAnimatingListener(rootView));
            }
        }
    }

    private class translateTask extends AsyncTask<Void, Void, String> {
        String searchParam;

        translateTask(String searchParam) {
            this.searchParam = searchParam;
        }

        @Override
        protected String doInBackground(Void... params) {
            return BingTranslate.getTranslatedText(searchParam);
        }

        @Override
        protected void onPostExecute(String result) {
            mTranslatedText = result;
            super.onPostExecute(result);
        }
    }


    /**
     * Listener to set boolean value for animating so that we can track it
     */
    private class IsAnimatingListener implements Animator.AnimatorListener {
        View rootView = null;

        IsAnimatingListener(View rootView) {
            this.rootView = rootView;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            animating = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            animating = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}

