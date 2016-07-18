package orbital.com.foodsearch.Activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import orbital.com.foodsearch.DAO.BingImageDAO;
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
import orbital.com.foodsearch.Utils.ImageUtils;
import orbital.com.foodsearch.Utils.NetworkUtils;
import orbital.com.foodsearch.Views.DrawableView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OcrActivity extends AppCompatActivity {

    private static final String LOG_TAG = "FOODIES";
    private static final String SAVED_FILE_PATH = "SAVEDFILEPATH";
    private static final String SEARCH_FRAGMENT_TAG = "SEARCHFRAGMENT";
    private static final String SEARCH_BAR_TAG = "SEARCHBARTAG";
    public static String IMAGE_KEY;
    public static String OCR_KEY;
    public static String TRANSLATE_KEY;
    public static String BASE_LANGUAGE;
    public static int IMAGES_COUNT;
    private static volatile int insightsCount = 0;
    private static String mTranslatedText = null;
    private static AsyncTask<Void, Void, String> translateTask;
    private static int barMarginTop = 0;
    private static BingImageDAO imgDAO = null;
    private static ImageSearchResponse searchResponse;
    private static ImageSearchResponse searchResponseDB;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences sharedPreferencesSettings;
    private static int IMAGES_COUNT_MAX;
    private final FragmentManager FRAGMENT_MANAGER = getSupportFragmentManager();
    private DatabaseReference database;
    private boolean animating = false;
    private int containerTransY;
    private int searchBarTrans;
    private String filePath = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (filePath != null) {
            outState.putString(SAVED_FILE_PATH, filePath);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(SAVED_FILE_PATH)) {
            filePath = savedInstanceState.getString(SAVED_FILE_PATH);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        if (filePath == null) {
            filePath = getIntent().getStringExtra("filePath");
        }
        database = FirebaseDatabase.getInstance().getReference();
        imgDAO = new BingImageDAO();
        sharedPreferences = getSharedPreferences(MainActivity.MyPREFERENCES, MODE_PRIVATE);
        IMAGE_KEY = sharedPreferences.getString(MainActivity.IMAGE_KEY, null);
        OCR_KEY = sharedPreferences.getString(MainActivity.OCR_KEY, null);
        TRANSLATE_KEY = sharedPreferences.getString(MainActivity.TRANSLATE_KEY, null);

        sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(this);
        BASE_LANGUAGE = sharedPreferencesSettings.getString(getResources().getString(R.string.select_lang_key), "test");
        IMAGES_COUNT_MAX = getResources().getIntArray(R.array.listNumber)[getResources().getIntArray(R.array.listNumber).length - 1];
        IMAGES_COUNT = Integer.parseInt(sharedPreferencesSettings.getString(getResources().getString(R.string.num_images_key), "1"));
        Log.e(LOG_TAG, "MAX IMAGES: " + IMAGES_COUNT_MAX);

        // Load a placeholder low res image first, resized to 30x48
        ImageView previewImageView = (ImageView) findViewById(R.id.preview_image_view);
        Picasso.with(this).load("file://" + filePath)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(30, 48)
                .into(previewImageView);
        // Start and enqeueue ocr service while setting up all the other views/fragments.
        startOcrService();
        initializeDrawView();
        setupSearchContainer();
        setupSearchBar();
    }

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
                barMarginTop = getResources().getDimensionPixelSize(R.dimen.activity_half_margin);
                searchBarContainer.setTranslationY(searchBarTrans);
                android.support.v4.app.FragmentTransaction ft = FRAGMENT_MANAGER.beginTransaction();
                ft.replace(R.id.search_bar_container, SearchBarFragment.newInstance(searchBarTrans), SEARCH_BAR_TAG);
                ft.commit();
                return true;
            }
        });
    }

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
                containerTransY = rootView.getHeight();
                recyclerContainer.setTranslationY(containerTransY);
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
                    Picasso.with(mContext).load("file://" + filePath)
                            .noPlaceholder()
                            .fit()
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(previewImageView);
                    onHoldBingOcrConnect(compressedImage);
                }
            };
            compressTask.execute(filePath);
        }
    }

    /**
     * This method puts bing ocr connect on hold with a snackbar for user to retry connection.
     *
     * @param compressedImage compressed image to be sent for ocr service
     */
    private void onHoldBingOcrConnect(final byte[] compressedImage) {
        // When compressTask is done, load preview into preview imageview
        // and remove progressbars and overlay.
        // then allow user to retry
        AnimUtils.brightenOverlay(findViewById(R.id.drawable_overlay));
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        AnimUtils.fadeOut(progressBar, AnimUtils.PROGRESS_BAR_DURATION);
        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_ocr),
                R.string.internet_error_text, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimUtils.darkenOverlay(findViewById(R.id.drawable_overlay));
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                AnimUtils.fadeIn(progressBar, AnimUtils.PROGRESS_BAR_DURATION);
                if (NetworkUtils.isNetworkAvailable(OcrActivity.this) && NetworkUtils.isOnline()) {
                    Call<BingOcrResponse> call = BingOcr.buildCall(compressedImage);
                    // Enqueue the method to the call and wait for callback (Asynchronous call)
                    call.enqueue(new OcrCallback(findViewById(R.id.activity_ocr), filePath));
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
                call.enqueue(new OcrCallback(findViewById(R.id.activity_ocr), filePath));
                // After call is dispatched, load full res IMAGE_KEY into preview
                ImageView previewImageView = (ImageView) findViewById(R.id.preview_image_view);
                Picasso.with(mContext).load("file://" + filePath)
                        .noPlaceholder()
                        .fit()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(previewImageView);
            }
        };
        compressTask.execute(filePath);
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.search_frag_container).getTranslationY() != 0) {
            super.onBackPressed();
        } else {
            closeSearchResults();
        }
    }

    /**
     * Method called by cancel button on search bar.
     * This method closes both search bar and search results.
     */
    public void cancelSearch() {
        View searchBarContainer = findViewById(R.id.search_bar_container);
        closeSearchResults();
        AnimUtils.hideSearchBar(searchBarContainer,
                searchBarTrans);
    }

    /**
     * Method called by clicking on close button in cards item.
     * It only closes the search results fragment.
     */
    public void closeSearchResults() {
        View rootView = findViewById(R.id.activity_ocr);
        Button searchButton = (Button) rootView.findViewById(R.id.start_search);
        searchButton.setEnabled(true);
        AnimUtils.containerSlideDown(rootView,
                new AnimatingListener(rootView),
                containerTransY);
    }


    /**
     * Opens photo view activity to view the high resolution photo
     *
     * @param view     View in which button was clicked on, gives us the exact card position
     * @param imageUrl String of the high res IMAGE_KEY url
     * @param thumbUrl String of the thumbnail IMAGE_KEY url for placeholder
     * @param position position of the card so that we can assign the correct transition name
     */
    public void openPhotoView(View view, String imageUrl, String thumbUrl, int position) {
        Intent intent = new Intent(this, PhotoViewActivity.class);
        intent.putExtra(PhotoViewActivity.POSITION, position);
        intent.putExtra(PhotoViewActivity.URL, imageUrl);
        intent.putExtra(PhotoViewActivity.THUMBURL, thumbUrl);
        // TODO: Set ZOOM anims here by Abel
        startActivity(intent);
    }

    /**
     * Called by click listeners and other methods for when a search is to be
     * scheduled
     *
     * @param searchParam String to be searched for
     */
    private void searchImageResponse(final Context context, final String searchParam) {
        AnimUtils.containerSlideDown(findViewById(R.id.activity_ocr),
                new AnimatingListener(findViewById(R.id.activity_ocr)),
                containerTransY);
        AnimUtils.darkenOverlay(findViewById(R.id.drawable_overlay));
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.search_progress);
        progressBar.setVisibility(View.VISIBLE);

        database.child("images").child(searchParam).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set search response DB to value from cloud
                searchResponseDB = dataSnapshot.getValue(ImageSearchResponse.class);
                // initialize the imagesearch again
                if (searchResponseDB == null) {
                    enqueueSearch(searchParam);
                } else {
                    SearchResultsFragment searchFragment = (SearchResultsFragment) FRAGMENT_MANAGER.findFragmentByTag(SEARCH_FRAGMENT_TAG);
                    searchFragment.clearRecycler();
                    for (int i = 0; i < IMAGES_COUNT; i++) {
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
                    if (insightsCount < IMAGES_COUNT) {
                        return;
                    }
                    new CompleteTask(context, rootView, FRAGMENT_MANAGER)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                insightsCount = 0;
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
        Log.d(LOG_TAG, "Search String: " + searchParam);
        BingSearch bingImg = new BingSearch(searchParam, String.valueOf(IMAGES_COUNT_MAX));
        Call<ImageSearchResponse> call = bingImg.buildCall();
        call.enqueue(new ImageSearchCallback(this, findViewById(R.id.activity_ocr), FRAGMENT_MANAGER, searchParam));
    }

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
            while (!translateTask.getStatus().equals(Status.FINISHED)) {
                if (count == 10) {
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
            SearchResultsFragment searchFragment = (SearchResultsFragment) fm.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            if (searchResponseDB == null) {
                //persist search result to DB
                imgDAO.persistImage(searchResponse);
                //mTranslatedText is the translated text here.
            }
            insightsCount = 0;
            //in the case where the database has the image but doesn't have the imageinsights.
            searchFragment.finalizeRecycler();//searchResponseDB.getTranslatedQuery());
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress);
            progressBar.setVisibility(View.GONE);
            Log.e(LOG_TAG, "TRANSLATED TEXT: " + mTranslatedText);
            AnimUtils.containerSlideUp(context, rootView,
                    new AnimUtils.displaySearchListener(rootView.findViewById(R.id.translation_card),
                            mTranslatedText));
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
            if (insightsCount < IMAGES_COUNT) {
                return;
            }
            // Once we have collated X imageinsights count, start CompleteTask to synchronize all tasks
            new CompleteTask(context, rootView, fm)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        @Override
        public void onFailure(Call<ImageInsightsResponse> call, Throwable t) {
            if (insightsCount < IMAGES_COUNT) {
                insightsCount++;
                return;
            }
            insightsCount = 0;
            // Failed to get the IMAGE_KEY insights so display snackbar error dialog
            Log.e(LOG_TAG, t.getMessage());
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress);
            progressBar.setVisibility(View.GONE);
            AnimUtils.brightenOverlay(rootView.findViewById(R.id.drawable_overlay));
            Snackbar.make(rootView.findViewById(R.id.activity_ocr), R.string.insights_search_fail,
                    Snackbar.LENGTH_LONG)
                    .show();
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
                searchFragment.clearRecycler();
                for (int i = 0; i < IMAGES_COUNT; i++) {
                    ImageValue imgVal = imageValues.get(i);
                    searchFragment.updateRecyclerList(imgVal);
                    searchImageInsights(context, imgVal);
                }
            } else {
                ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress);
                progressBar.setVisibility(View.GONE);
                FrameLayout drawableOverlay = (FrameLayout) rootView.findViewById(R.id.drawable_overlay);
                AnimUtils.brightenOverlay(drawableOverlay);
                Snackbar.make(rootView, R.string.no_image_found, Snackbar.LENGTH_LONG).show();
            }

        }

        @Override
        public void onFailure(Call<ImageSearchResponse> call, Throwable t) {
            Log.e(LOG_TAG, t.getMessage());
            Snackbar.make(rootView.findViewById(R.id.activity_ocr), R.string.image_search_fail,
                    Snackbar.LENGTH_LONG)
                    .show();
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress);
            progressBar.setVisibility(View.GONE);
            FrameLayout drawableOverlay = (FrameLayout) rootView.findViewById(R.id.drawable_overlay);
            AnimUtils.brightenOverlay(drawableOverlay);
            Snackbar.make(rootView, R.string.no_image_found, Snackbar.LENGTH_LONG).show();
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
            List<Line> lines = bingOcrResponse.getAllLines();
            mDrawableView.drawBoxes(rootView, mFilePath, lines,
                    bingOcrResponse.getTextAngle().floatValue(),
                    bingOcrResponse.getLanguage());
            AnimUtils.brightenOverlay(findViewById(R.id.drawable_overlay));
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
            AnimUtils.fadeOut(progressBar, AnimUtils.PROGRESS_BAR_DURATION);
        }

        @Override
        public void onFailure(Call<BingOcrResponse> call, Throwable t) {
            AnimUtils.brightenOverlay(findViewById(R.id.drawable_overlay));
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
            AnimUtils.fadeOut(progressBar, AnimUtils.PROGRESS_BAR_DURATION);
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
            return ImageUtils.compressImage(params[0]);
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
                closeSearchResults();
            }
            mDrawableView = (DrawableView) v;
            List<Rect> rects = mDrawableView.getmRects();
            int x = (int) event.getX();
            int y = (int) event.getY();
            for (int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                if (rect.contains(x, y)) {
                    selectRect(i);
                    return true;
                }
            }
            return v.performClick();
        }

        private void selectRect(int i) {
            mDrawableView.updateSelection(i);
            // Display the string in a snackbar and allow for search
            String searchParam = mDrawableView.getmLineTexts().get(i).toLowerCase();
            searchParam = searchParam.substring(0, 1).toUpperCase() + searchParam.substring(1);
            View searchBar = findViewById(R.id.search_bar_container);
            AnimUtils.showSearchBar(context, searchBar, searchParam);
        }
    }

    /**
     * Listener to set boolean value for animating so that we can track it
     */
    private class AnimatingListener implements Animator.AnimatorListener {
        View rootView = null;

        AnimatingListener(View rootView) {
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

