package orbital.com.foodsearch.Activities;

import android.animation.Animator;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.Html;
import android.transition.Transition;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import orbital.com.foodsearch.DAO.BingImageDAO;
import orbital.com.foodsearch.DAO.PhotosContract;
import orbital.com.foodsearch.DAO.PhotosDAO;
import orbital.com.foodsearch.DAO.PhotosDBHelper;
import orbital.com.foodsearch.Fragments.HintDialogFragment;
import orbital.com.foodsearch.Fragments.SearchBarFragment;
import orbital.com.foodsearch.Fragments.SearchResultsFragment;
import orbital.com.foodsearch.Helpers.BingOcr;
import orbital.com.foodsearch.Helpers.BingSearch;
import orbital.com.foodsearch.Helpers.BingTranslate;
import orbital.com.foodsearch.Helpers.ImageInsights;
import orbital.com.foodsearch.Misc.GlobalVar;
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
    public static final String FILE_PATH = "FILEPATH";
    private static final String DATA = "DATA";
    private static final String LOG_TAG = "FOODIES";
    private static final String SEARCH_FRAGMENT_TAG = "SEARCHFRAGMENT";
    private static final String SEARCH_BAR_TAG = "SEARCHBARTAG";
    private static final String HINT_DIALOG_TAG = "DIALOGTAG";
    private static final int INSIGHTS_COUNT_CAP = 5;
    public static int IMAGES_COUNT;
    private static int IMAGES_COUNT_MAX;
    private static int expectedResultCount;
    private static int expectedInsightCount;
    private static volatile int dispatchedInsightCount = 0;
    private static volatile int receivedInsightsCount = 0;
    private static String mTranslatedText = null;
    private static AsyncTask<Void, Void, String> translateTask;
    private static BingImageDAO imgDAO = null;
    private static ImageSearchResponse searchResponse;
    private static ImageSearchResponse searchResponseDB;
    private final FragmentManager FRAGMENT_MANAGER = getSupportFragmentManager();
    private SharedPreferences sharedPreferencesSettings;
    private String mFilePath = null;
    private String mJson = null;
    private boolean animating = false;
    private boolean leavingActivity = false;
    private boolean ocrSaved = false;
    private int searchBarTrans;
    private DatabaseReference database;
    private ConcurrentHashMap<DatabaseReference, ValueEventListener> runningDBListeners;
    private ArrayList<AsyncTask> runningTasks;
    private ArrayList<Call> runningCalls;
    private String currentTime;
    private String formattedDate;
    private String formattedTime;
    private String mQuery;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mFilePath != null) {
            outState.putString(FILE_PATH, mFilePath);
        }
        if (mJson != null) {
            outState.putString(DATA, mJson);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(FILE_PATH)) {
            mFilePath = savedInstanceState.getString(FILE_PATH);
        }
        if (savedInstanceState.containsKey(DATA)) {
            mJson = savedInstanceState.getString(DATA);
        }
        // To restore boxes in the event that ocr activity gets destroyed
        final View previewImageView = findViewById(R.id.ocr_preview_image);
        previewImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                previewImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                drawBoxesRecentImage();
                findViewById(R.id.drawable_view).setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedPreferencesSettings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        sharedPreferencesSettings.registerOnSharedPreferenceChangeListener(null);
        super.onStop();
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
            window.setStatusBarColor(Color.BLACK);
        }
        database = FirebaseDatabase.getInstance().getReference();
        sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(this);
        onCreateBackground();
        initializeDrawView();
        setupSearchContainer();
        setupSearchBar();
        setupPreview();
    }

    private void onCreateBackground() {
        runningDBListeners = new ConcurrentHashMap<>();
        runningTasks = new ArrayList<>();
        runningCalls = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                imgDAO = new BingImageDAO();

                IMAGES_COUNT_MAX = getResources().getIntArray(R.array.listNumber)[getResources().getIntArray(R.array.listNumber).length - 1];
                IMAGES_COUNT = Integer.parseInt(sharedPreferencesSettings.getString(getResources().getString(R.string.num_images_key), "1"));

                ocrSaved = false;

                //current time
                Calendar cal = Calendar.getInstance();
                currentTime = FileUtils.getTimeStamp(cal);
                formattedDate = FileUtils.getFormattedDate(cal);
                formattedTime = FileUtils.getFormattedTime(cal);
                return null;
            }

            @Override
            protected void onPreExecute() {
                runningTasks.add(this);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                runningTasks.remove(this);
            }
        }.execute();

    }

    private void setupPreview() {
        mJson = getIntent().getStringExtra(RESPONSE);
        final ImageView previewImageView = (ImageView) findViewById(R.id.ocr_preview_image);
        if (mJson == null) {
            ImageUtils.getPicassoInstance(this)
                    .load("file://" + mFilePath)
                    //.placeholder(R.color.black_overlay)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resize(30, 48)
                    .into(previewImageView);
            prepareOcrService();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                findViewById(R.id.ocr_progress_bar).setVisibility(View.GONE);
                String transName = getString(R.string.recents_transition_name);
                ViewCompat.setTransitionName(previewImageView, transName);
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
            ImageUtils.getPicassoInstance(this)
                    .load("file://" + mFilePath)
                    .into(previewImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                startPostponedEnterTransition();
                            } else {
                                AnimUtils.fadeIn(findViewById(R.id.drawable_view), AnimUtils.FASTER_FADE);
                            }
                        }

                        @Override
                        public void onError() {
                            FirebaseCrash.report(new Exception("Picasso load failed!"));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                startPostponedEnterTransition();
                            } else {
                                AnimUtils.fadeIn(findViewById(R.id.drawable_view), AnimUtils.FASTER_FADE);
                            }
                        }
                    });
            previewImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    previewImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    drawBoxesRecentImage();
                    return true;
                }
            });
        }
    }

    /**
     * Draw boxes onto the image that have been saved.
     */
    private void drawBoxesRecentImage() {
        DrawableView mDrawableView = (DrawableView) findViewById(R.id.drawable_view);
        if (mJson == null) {
            if (!ocrSaved) {
                return;
            }
            PhotosDBHelper mDBHelper = new PhotosDBHelper(this);
            Cursor cursor = PhotosDAO.readDatabaseAllRowsOrderByTime(mDBHelper);
            cursor.moveToFirst();
            mJson = cursor.getString(cursor.getColumnIndexOrThrow(PhotosContract.PhotosEntry.COLUMN_NAME_DATA));
        }
        Gson gson = new Gson();
        BingOcrResponse responseData = gson.fromJson(mJson, BingOcrResponse.class);
        List<Line> lines = responseData.getAllLines();
        mDrawableView.drawBoxes(findViewById(R.id.activity_ocr), mFilePath, lines,
                responseData.getTextAngle().floatValue(),
                responseData.getLanguage());
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
                searchBarTrans = searchBarContainer.getHeight() + ViewUtils.dpToPx(8);
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
        final View recyclerContainer = findViewById(R.id.search_frag_container);
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
        gestureDetector = new GestureDetectorCompat(this, new GestureListener(drawableView, findViewById(R.id.activity_ocr)));
        drawableView.setOnTouchListener(new DrawableTouchListener(findViewById(R.id.activity_ocr)));
    }

    private void prepareOcrService() {
        CompressAsyncTask compressTask = new CompressAsyncTask(findViewById(R.id.activity_ocr)) {
            @Override
            protected void onPostExecute(byte[] compressedImage) {
                super.onPostExecute(compressedImage);
                ImageView previewImageView = (ImageView) findViewById(R.id.ocr_preview_image);
                ImageUtils.getPicassoInstance(OcrActivity.this)
                        .load("file://" + mFilePath)
                        .noPlaceholder()
                        .fit()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(previewImageView);
                if (NetworkUtils.isNetworkAvailable(OcrActivity.this) && NetworkUtils.isOnline()) {
                    enqueueOcr(compressedImage);
                } else {
                    onHoldEnqueueOcr(compressedImage);
                }
            }
        };
        compressTask.execute(mFilePath, mFilePath);
    }

    /**
     * This method puts bing ocr connect on hold with a snackbar for user to retry connection.
     *
     * @param compressedImage compressed image to be sent for ocr service
     */
    private void onHoldEnqueueOcr(final byte[] compressedImage) {
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
                    enqueueOcr(compressedImage);
                } else {
                    onHoldEnqueueOcr(compressedImage);
                }

            }
        });
        snackbar.show();
    }

    public void startSearch(final String searchParam) {
        if (NetworkUtils.isNetworkAvailable(this) && NetworkUtils.isOnline()) {
            searchImageResponse(searchParam);
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_ocr),
                    R.string.internet_error_text, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSearch(searchParam);
                }
            });
            snackbar.show();
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
    public void onBackPressed() {
        if (findViewById(R.id.search_frag_container).getTranslationY() != 0) {
            sharedPreferencesSettings.unregisterOnSharedPreferenceChangeListener(this);
            leavingActivity = true;
            findViewById(R.id.drawable_overlay).setVisibility(View.INVISIBLE);
            findViewById(R.id.search_bar_container).setVisibility(View.INVISIBLE);
            findViewById(R.id.drawable_view).setVisibility(View.INVISIBLE);
            Intent intent = getIntent();
            if (ocrSaved) {
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED, intent);
            }
            supportFinishAfterTransition();
        } else {
            endAllThreads();
            View rootView = findViewById(R.id.activity_ocr);
            ViewUtils.closeSearchResults(rootView, new IsAnimatingListener(rootView), rootView.getHeight());
        }
    }

    @Override
    protected void onDestroy() {
        endAllThreads();
        super.onDestroy();
    }

    private void endAllThreads() {
        if (runningDBListeners != null) {
            for (Map.Entry<DatabaseReference, ValueEventListener> entry : runningDBListeners.entrySet()) {
                entry.getKey().removeEventListener(entry.getValue());
            }
            runningDBListeners.clear();
        }
        if (runningCalls != null) {
            for (Call call : runningCalls) {
                call.cancel();
            }
            runningCalls.clear();
        }
        if (runningTasks != null) {
            for (AsyncTask task : runningTasks) {
                if (task.getStatus() != AsyncTask.Status.FINISHED) {
                    task.cancel(true);
                }
            }
            runningTasks.clear();
        }
    }

    /**
     * Method called by cancel button on search bar.
     * This method closes both search bar and search results.
     */
    public void cancelSearch() {
        if (actionMode != null) {
            actionMode.finish();
        }
        endAllThreads();
        View rootView = findViewById(R.id.activity_ocr);
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.GONE);
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
    private void searchImageResponse(final String searchParam) {
        if (searchParam.trim().isEmpty()) {
            return;
        }
        if (sharedPreferencesSettings.getBoolean(getString(R.string.google_search_key), false)) {
            googleSearch(searchParam);
            return;
        }
        if (actionMode != null) {
            actionMode.finish();
        }
        mQuery = searchParam;
        final View rootView = findViewById(R.id.activity_ocr);
        AnimUtils.containerSlideDown(rootView, new IsAnimatingListener(rootView), rootView.getHeight());
        ViewUtils.startSearchProgress(rootView);
        dispatchedInsightCount = 0;
        receivedInsightsCount = 0;
        DatabaseReference ref = database.child("images").child(searchParam);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (runningDBListeners.get(dataSnapshot.getRef()) == null) {
                    return;
                }
                runningDBListeners.remove(dataSnapshot.getRef());
                // Set search response DB to value from cloud
                searchResponseDB = dataSnapshot.getValue(ImageSearchResponse.class);
                // initialize the imagesearch again
                if (searchResponseDB == null) {
                    enqueueSearch(searchParam);
                } else {
                    // Expected result size is the minimum of the max count and the size.
                    expectedResultCount = Math.min(searchResponseDB.getImageValues().size(), IMAGES_COUNT);
                    expectedInsightCount = Math.min(expectedResultCount, INSIGHTS_COUNT_CAP);
                    SearchResultsFragment searchFragment = (SearchResultsFragment) FRAGMENT_MANAGER.findFragmentByTag(SEARCH_FRAGMENT_TAG);
                    searchFragment.clearRecycler();
                    for (int i = 0; i < expectedResultCount; i++) {
                        ImageValue imgVal = searchResponseDB.getImageValues().get(i);
                        searchFragment.updateRecyclerList(imgVal);
                        searchImageInsights(imgVal);
                    }
                }
                enqueueTranslate(searchParam);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getUser:onCancelled", databaseError.toException());
                ViewUtils.terminateSearchProgress(rootView);
            }
        };
        ref.addListenerForSingleValueEvent(listener);
        runningDBListeners.put(ref, listener);
    }

    /**
     * There is a possibility that there may exist 2 different images with the same image insight.
     * This method is called after onresponse for imagesearch callback in order to prevent duplicates from being
     * persisted into the database. Flag used is imageExists
     */
    private void searchImageInsights(final ImageValue imgVal) {
        dispatchedInsightCount++;
        if (dispatchedInsightCount > expectedInsightCount) {
            return;
        }
        String insightsToken = imgVal.getImageInsightsToken();
        final View rootView = findViewById(R.id.activity_ocr);
        DatabaseReference ref = database.child("imageinsights").child(insightsToken);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (runningDBListeners.get(dataSnapshot.getRef()) == null) {
                    return;
                }
                runningDBListeners.remove(dataSnapshot.getRef());
                ImageInsightsResponse imgInsightsDB = dataSnapshot.getValue(ImageInsightsResponse.class);
                //initialize the IMAGE_KEY insights search again
                if (imgInsightsDB == null) {
                    enqueueImageInsightCall(imgVal);
                } else {
                    imgVal.setInsightsResponse(imgInsightsDB);
                    receivedInsightsCount++;
                    if (receivedInsightsCount < expectedInsightCount) {
                        return;
                    }
//                    Log.e(LOG_TAG, "Insights DB count: " + receivedInsightsCount);
                    new CompleteTask(rootView, FRAGMENT_MANAGER)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "getUser:onCancelled", databaseError.toException());
            }
        };
        ref.addListenerForSingleValueEvent(listener);
        runningDBListeners.put(ref, listener);
    }

    private void googleSearch(String searchParam) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, searchParam);
        startActivity(intent);
    }

    private void enqueueOcr(final byte[] compressedImage) {
        // Keep api key in sync first before building and enqueueing call
        if (GlobalVar.hasKeyValues()) {
            Call<BingOcrResponse> call = BingOcr.buildCall(compressedImage);
            runningCalls.add(call);
            call.enqueue(new OcrCallback(findViewById(R.id.activity_ocr)));
        } else {
            new ApiKeySyncTask() {
                @Override
                protected void onPostExecute(Boolean isSuccessful) {
                    super.onPostExecute(isSuccessful);
                    if (isSuccessful) {
                        enqueueOcr(compressedImage);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * This method creates a search call based on the input param and enqueues it
     *
     * @param searchParam parameter string to be searched for
     */
    private void enqueueSearch(final String searchParam) {
        // Keep api key in sync first before building and enqueueing call
        if (GlobalVar.hasKeyValues()) {
            BingSearch bingImg = new BingSearch(searchParam, String.valueOf(IMAGES_COUNT_MAX));
            Call<ImageSearchResponse> call = bingImg.buildCall();
            runningCalls.add(call);
            call.enqueue(new ImageSearchCallback(findViewById(R.id.activity_ocr), FRAGMENT_MANAGER, searchParam));
        } else {
            new ApiKeySyncTask() {
                @Override
                protected void onPostExecute(Boolean isSuccessful) {
                    super.onPostExecute(isSuccessful);
                    if (isSuccessful) {
                        enqueueSearch(searchParam);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * This method creates a translte call with search param and executes it on TranslateTask.
     *
     * @param searchParam string parameter to translate
     */
    private void enqueueTranslate(final String searchParam) {
        // Keep api key in sync first before building and enqueueing call
        if (GlobalVar.hasKeyValues()) {
            mTranslatedText = searchParam;
            translateTask = new TranslateTask(searchParam).executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ApiKeySyncTask() {
                @Override
                protected void onPostExecute(Boolean isSuccessful) {
                    super.onPostExecute(isSuccessful);
                    enqueueTranslate(searchParam);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * This method creates a imageinsight search call based on the input imageValues list and enqueues it
     *
     * @param imgVal IMAGE_KEY values list to be used as param for insightsToken call
     *               Multiple calls.
     */
    private void enqueueImageInsightCall(final ImageValue imgVal) {
        // Keep api key in sync first before building and enqueueing call
        if (GlobalVar.hasKeyValues()) {
            ImageInsights imageInsights = new ImageInsights(mQuery, imgVal.getImageInsightsToken(), "");
            Call<ImageInsightsResponse> call = imageInsights.buildCall();
            runningCalls.add(call);
            call.enqueue(new ImageInsightCallback(findViewById(R.id.activity_ocr),
                    FRAGMENT_MANAGER,
                    imgVal));
        } else {
            new ApiKeySyncTask() {
                @Override
                protected void onPostExecute(Boolean isSuccess) {
                    super.onPostExecute(isSuccess);
                    enqueueImageInsightCall(imgVal);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * saves the ocr response into the local db in the form of json, with the key being
     * the current time. Delete least recently saved photo if the number of photos exceed the
     * number of photos allowed to be saved.
     *
     * @param response
     */
    private void saveOcrResponse(BingOcrResponse response) {
        if (!sharedPreferencesSettings.getBoolean(getString(R.string.save_recents_key), true)) {
            return;
        }
        ocrSaved = true;
        PhotosDBHelper mDBHelper = new PhotosDBHelper(this);
        Cursor cursor = PhotosDAO.readDatabaseAllRowsOrderByTime(mDBHelper);
        String fileName = null;
        //Log.e(LOG_TAG, "cursor count: " + cursor.getCount());
        if (cursor.getCount() >= MainActivity.IMAGE_RECENTS_COUNT) {
            cursor.moveToFirst();
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(PhotosContract.PhotosEntry.COLUMN_NAME_ENTRY_TIME));
            PhotosDAO.deleteOnEntryTime(fileName, mDBHelper);
            cursor.close();
        }
        //save the lines to local DB
        Gson gson = new Gson();
        String json = gson.toJson(response);
        PhotosDAO.writeToDatabase(currentTime, json, formattedDate, formattedTime, mDBHelper);
        saveImage(BitmapFactory.decodeFile(mFilePath), fileName);
    }

    /**
     * Saves a duplicate of the image into the recent images folder.
     * Name of the image would be the timestamp.
     *
     * @param finalBitmap
     */
    private void saveImage(Bitmap finalBitmap, String fileToDelete) {
        File root = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Recent_Images");
        if (!root.exists()) {
            if (!root.mkdirs()) {
                Log.e(LOG_TAG, getString(R.string.mkdir_fail_text));
            }
        }

        if (fileToDelete != null) {
            File file = new File(root, fileToDelete);
            file.delete();
            //Log.e(LOG_TAG, "FILE: " + fileToDelete + " DELETED");
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
            FirebaseCrash.report(e);
        }
    }

    private class CompleteTask extends AsyncTask<Void, Void, Boolean> {
        View rootView = null;
        FragmentManager fm = null;

        CompleteTask(View rootView, FragmentManager fm) {
            this.rootView = rootView;
            this.fm = fm;
        }

        @Override
        protected void onPreExecute() {
            runningTasks.add(this);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            int count = 0;
            // Wait 10 seconds for translate task to be done
            while (!translateTask.getStatus().equals(Status.FINISHED)) {
                if (count == 20) {
                    return false;
                }
                try {
                    count++;
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean successful) {
            runningTasks.remove(this);
            if (!successful) {
                FirebaseCrash.report(new Exception(getString(R.string.translate_fail)));
                Snackbar.make(rootView, R.string.translate_fail, Snackbar.LENGTH_SHORT);
                return;
            }
            if (searchResponseDB == null) {
                //persist search result to DB
                imgDAO.persistImage(searchResponse);
            }
            // Log.e(LOG_TAG, "TRANSLATED TEXT: " + mTranslatedText);
            SearchResultsFragment searchFragment = (SearchResultsFragment) fm.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            searchFragment.finalizeRecycler();
            ViewUtils.showSearchResults(rootView, mTranslatedText);
            // Log.e(LOG_TAG, "MAX IMAGES: " + IMAGES_COUNT_MAX);
        }
    }

    /**
     * Callback class for ImageInsightCallback.
     */
    private class ImageInsightCallback implements Callback<ImageInsightsResponse> {
        private View rootView = null;
        private FragmentManager fm = null;
        private ImageValue imageValue = null;

        ImageInsightCallback(View rootView, FragmentManager fm, ImageValue imageValue) {
            this.rootView = rootView;
            this.fm = fm;
            this.imageValue = imageValue;
        }

        @Override
        public void onResponse(Call<ImageInsightsResponse> call, Response<ImageInsightsResponse> response) {
            runningCalls.remove(call);
            // Get response body, persist to DB then complete task
            ImageInsightsResponse insightsResponse = response.body();
            imageValue.setInsightsResponse(insightsResponse);
            imgDAO.persistImageInsight(insightsResponse);
            receivedInsightsCount++;
            // Log.e(LOG_TAG, "Insights callback count: " + receivedInsightsCount);
            if (receivedInsightsCount < expectedInsightCount) {
                return;
            }
            // Once we have collated X imageinsights count, start CompleteTask to synchronize all tasks
            new CompleteTask(rootView, fm)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        public void onFailure(Call<ImageInsightsResponse> call, Throwable t) {
            if (call.isCanceled()) {
                return;
            }
            runningCalls.remove(call);
            receivedInsightsCount++;
            if (receivedInsightsCount < expectedInsightCount) {
                return;
            }
            FirebaseCrash.report(t);
            new CompleteTask(rootView, fm)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /**
     * Callback class for ImageSearchResponse, handling of POST responses is done here
     */
    private class ImageSearchCallback implements Callback<ImageSearchResponse> {
        private View rootView = null;
        private FragmentManager fm = null;
        private String searchParam = null;

        ImageSearchCallback(View rootView, FragmentManager fm, String searchParam) {
            this.rootView = rootView;
            this.fm = fm;
            this.searchParam = searchParam;
        }

        @Override
        public void onResponse(Call<ImageSearchResponse> call, Response<ImageSearchResponse> response) {
            runningCalls.remove(call);
            searchResponse = response.body();
            if (searchResponse == null) {
                try {
                    FirebaseCrash.report(new Exception(response.errorBody().string()));
                    Log.e(LOG_TAG, response.errorBody().string());
                    return;
                } catch (IOException e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }
            }
            searchResponse.setSearchQuery(searchParam);
            List<ImageValue> imageValues = searchResponse.getImageValues();
            // IMAGE_KEY search results received, now enqueueImageInsightCall with received value
            SearchResultsFragment searchFragment = (SearchResultsFragment) fm.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            if (!imageValues.isEmpty()) {
                // Expected result size is the minimum of the max count and the size.
                expectedResultCount = Math.min(imageValues.size(), IMAGES_COUNT);
                expectedInsightCount = Math.min(expectedResultCount, INSIGHTS_COUNT_CAP);
                searchFragment.clearRecycler();
                for (int i = 0; i < expectedResultCount; i++) {
                    ImageValue imgVal = imageValues.get(i);
                    searchFragment.updateRecyclerList(imgVal);
                    searchImageInsights(imgVal);
                }
            } else {
                ViewUtils.terminateSearchProgress(rootView);
                Snackbar.make(rootView, R.string.no_image_found, Snackbar.LENGTH_LONG).show();
            }

        }

        @Override
        public void onFailure(Call<ImageSearchResponse> call, Throwable t) {
            if (!call.isCanceled()) {
                runningCalls.remove(call);
                Snackbar.make(rootView.findViewById(R.id.activity_ocr), R.string.image_search_fail,
                        Snackbar.LENGTH_LONG)
                        .show();
                ViewUtils.terminateSearchProgress(rootView);
                Log.e(LOG_TAG, t.getMessage());
                FirebaseCrash.report(t);
                Snackbar.make(rootView, R.string.no_image_found, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Callback class that implements callback method to be used for when a
     * response is received from server
     */
    private class OcrCallback implements Callback<BingOcrResponse> {
        private View rootView = null;
        private DrawableView mDrawableView = null;

        private OcrCallback(View rootView) {
            this.rootView = rootView;
            mDrawableView = (DrawableView) rootView.findViewById(R.id.drawable_view);
        }

        @Override
        public void onResponse(Call<BingOcrResponse> call, Response<BingOcrResponse> response) {
            runningCalls.remove(call);
            BingOcrResponse bingOcrResponse = response.body();
            List<Line> lines = bingOcrResponse.getAllLines();
            mDrawableView.drawBoxes(rootView, mFilePath, lines,
                    bingOcrResponse.getTextAngle().floatValue(),
                    bingOcrResponse.getLanguage());
            ViewUtils.finishOcrProgress(rootView);
            saveOcrResponse(bingOcrResponse);
        }

        @Override
        public void onFailure(Call<BingOcrResponse> call, Throwable t) {
            if (!call.isCanceled()) {
                runningCalls.remove(call);
                ViewUtils.finishOcrProgress(rootView);
                Snackbar.make(rootView.findViewById(R.id.activity_ocr), R.string.OCRFailed,
                        Snackbar.LENGTH_LONG)
                        .show();
                FirebaseCrash.report(t);
                Log.e(LOG_TAG, "POST Call Failed!" + t.getMessage());
            }
        }
    }

    /**
     * This async task is used to compress the IMAGE_KEY to be sent in the
     * background thread using ImageUtils.compressImage
     */
    private class CompressAsyncTask extends AsyncTask<String, Integer, byte[]> {
        View mRootView = null;

        CompressAsyncTask(View rootView) {
            mRootView = rootView;
        }

        @Override
        protected void onPreExecute() {
            runningTasks.add(this);
        }

        @Override
        protected byte[] doInBackground(String... params) {
            return ImageUtils.compressImage(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            runningTasks.remove(this);
        }
    }


    private class DrawableTouchListener implements View.OnTouchListener {
        private View rootView;

        DrawableTouchListener(View rootView) {
            this.rootView = rootView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private DrawableView mDrawableView = null;
        private View rootView = null;

        public GestureListener(DrawableView view, View rootView) {
            mDrawableView = view;
            this.rootView = rootView;
        }

        @Override
        public boolean onDown(final MotionEvent event) {
            return processSingleTouch(event);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return processSingleTouch(e2);
        }

        @Override
        public void onLongPress(MotionEvent event) {
            List<Rect> rects = mDrawableView.getmRects();
            int x = (int) event.getX();
            int y = (int) event.getY();
            for (int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                if (rect.contains(x, y)) {
                    actionMode = startSupportActionMode(new actionModeCallback());
                    chooseMultRect(i);
                    mDrawableView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    Toast.makeText(OcrActivity.this, "Select multiple lines for search", Toast.LENGTH_SHORT).show();
                }
            }
            super.onLongPress(event);
        }

        private boolean processSingleTouch(MotionEvent e) {
            // Only close search results when results are up and not animating
            if (findViewById(R.id.search_frag_container).getTranslationY() == 0 && !animating) {
                ViewUtils.closeSearchResults(rootView, new IsAnimatingListener(rootView), rootView.getHeight());
                return true;
            }
            List<Rect> rects = mDrawableView.getmRects();
            int x = (int) e.getX();
            int y = (int) e.getY();
            for (int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                if (rect.contains(x, y)) {
                    if (actionMode != null) {
                        chooseMultRect(i);
                    } else {
                        chooseRect(i);
                    }
                    return true;
                }
            }
            return false;
        }

        private void chooseRect(int i) {
            mDrawableView.chooseRect(i, false);
            // Display the string in a snackbar and allow for search
            String searchParam = mDrawableView.getmLineTexts().get(i).trim().toLowerCase();
            //searchParam = searchParam.substring(0, 1).toUpperCase() + searchParam.substring(1);
            View searchBar = findViewById(R.id.search_bar_container);
            // if !Animating or searchbar is already up, show searchbar with new search param.
            if (!animating || searchBar.getTranslationY() == 0) {
                ViewUtils.showSearchBar(rootView, searchParam, new IsAnimatingListener(rootView), false);
            }
        }

        private void chooseMultRect(int i) {
            boolean isSelected = mDrawableView.chooseRect(i, true);
//            Log.e(LOG_TAG, "search param: " + searchParam);
            int numSelected = mDrawableView.getSelectedCount();
            if (numSelected == 0) {
                actionMode.finish();
                cancelSearch();
                return;
            }
            String title = getResources().getQuantityString(R.plurals.selected_line_count, numSelected, numSelected);
            actionMode.setTitle(title);
            String searchParam = mDrawableView.getmLineTexts().get(i).trim().toLowerCase();
            if (!isSelected) {
                ViewUtils.deleteSearchQuery(rootView, searchParam);
            } else if (numSelected > 1) {
                ViewUtils.appendSearchQuery(rootView, searchParam);
            } else if (!animating || findViewById(R.id.search_bar_container).getTranslationY() == 0) {
                ViewUtils.showSearchBar(rootView, searchParam, new IsAnimatingListener(rootView), true);
            }
        }

        private class actionModeCallback implements ActionMode.Callback {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_cab_multi_select, menu);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //set your gray color
                    getWindow().setStatusBarColor(ContextCompat.getColor(OcrActivity.this, R.color.colorPrimaryDark));
                }
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.multiselect_help:
                        HintDialogFragment dialog = HintDialogFragment.newInstance(getString(R.string.help_title),
                                getString(R.string.help_message));
                        dialog.show(FRAGMENT_MANAGER, HINT_DIALOG_TAG);
                        return true;
                }
                return false;
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //set your gray color
                    getWindow().setStatusBarColor(Color.BLACK);
                }
                mDrawableView.clearSelectedRects();
                ViewUtils.clearSearch();
                actionMode = null;
            }
        }
    }

    private class ApiKeySyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            runningTasks.add(this);
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            runningTasks.remove(this);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            int count = 0;
            while (!GlobalVar.hasKeyValues()) {
                // Wait 10 seconds max for API keys, modify the max here
                if (count > 50) {
                    FirebaseCrash.report(new Exception("Global var no key values > 10seconds"));
                    return false;
                }
                try {
                    count++;
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

    private class TranslateTask extends AsyncTask<Void, Void, String> {
        String searchParam;

        TranslateTask(String searchParam) {
            this.searchParam = searchParam;
        }

        @Override
        protected String doInBackground(Void... params) {
            return BingTranslate.getTranslatedText(searchParam);
        }

        @Override
        protected void onPreExecute() {
            runningTasks.add(this);
        }

        @Override
        protected void onPostExecute(String result) {
            runningTasks.remove(this);
            mTranslatedText = result;
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

