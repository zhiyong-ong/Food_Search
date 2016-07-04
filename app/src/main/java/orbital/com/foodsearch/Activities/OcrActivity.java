package orbital.com.foodsearch.Activities;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import me.zhanghai.android.materialprogressbar.IndeterminateHorizontalProgressDrawable;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import orbital.com.foodsearch.Fragments.SearchResultsFragment;
import orbital.com.foodsearch.Helpers.BingOcr;
import orbital.com.foodsearch.Helpers.BingSearch;
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

public class OcrActivity extends AppCompatActivity implements SearchResultsFragment.OnFragmentInteractionListener {
    private static final String LOG_TAG = "FOODIES";
    private static final String SAVED_FILE_PATH = "SAVEDFILEPATH";
    private static final String SEARCH_FRAGMENT_TAG = "SEARCHFRAGMENT";
    private static final int IMAGE_COUNT = 1;
    private final FragmentManager FRAGMENT_MANAGER = getSupportFragmentManager();

    private boolean animating = false;
    private int containerTransY;

    private String filePath = null;
    private List<ImageValue> mImageValues = null;
    private int searchBarTrans;

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
        setContentView(R.layout.activity_ocr_exp);
        if (filePath == null) {
            filePath = getIntent().getStringExtra("filePath");
        }

        ImageView imgView = (ImageView) findViewById(R.id.previewImageView2);
        Picasso.with(this).load("file://" + filePath)
                //.placeholder(R.color.black_overlay)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(30, 44)
                .into(imgView);
        startOcrService();
        initializeDrawView();
        setupSearchContainer();
        setupSearchBar();
    }

    private void setupSearchBar() {
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.search_progress);
        progressBar.setIndeterminateDrawable(new IndeterminateHorizontalProgressDrawable(this));
        final CardView searchBar = (CardView)findViewById(R.id.search_bar);
        final EditText editText = (EditText) searchBar.findViewById(R.id.edit_text);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editText.setCursorVisible(true);
                } else {
                    editText.setCursorVisible(false);
                }
            }
        });
        final Button searchButton = (Button) searchBar.findViewById(R.id.start_search);
        final ImageButton cancelButton = (ImageButton) searchBar.findViewById(R.id.cancel_search);
        View.OnClickListener listener= new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.cancel_search:
                        AnimUtils.hideSearchBar(searchBar, searchBarTrans);
                        break;
                    case R.id.edit_text:
                        break;
                    case R.id.start_search:
                        searchButton.setEnabled(false);
                        enqueueSearch(editText.getText().toString());
                        break;
                }
            }
        };
        editText.setOnClickListener(listener);
        searchButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(listener);
    }

    private void setupSearchContainer() {
        final View rootView = findViewById(R.id.activity_ocr_exp);
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
                View searchBar = findViewById(R.id.search_bar);
                searchBarTrans = 2 * searchBar.getHeight();
                searchBar.setTranslationY(searchBarTrans);
                return true;
            }
        });
    }

    private void initializeDrawView() {
        final DrawableView drawableView = (DrawableView) findViewById(R.id.drawable_view);
        drawableView.setOnTouchListener(new DrawableTouchListener(this, findViewById(R.id.activity_ocr_exp)));
    }

    private void startOcrService() {
        if (NetworkUtils.isNetworkAvailable(this) && NetworkUtils.isOnline()) {
            bingOcrConnect();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_ocr_exp),
                    R.string.internet_error_text, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startOcrService();
                }
            });
            snackbar.show();
        }
    }

    /**
     * Method that compresses the image before sending it to BingOcrService
     */
    private void bingOcrConnect() {
        View root = findViewById(R.id.activity_ocr_exp);
        CompressAsyncTask compressTask = new CompressAsyncTask(this, root) {
            @Override
            protected void onPostExecute(byte[] compressedImage) {
                // When compressTask is done, invoke dispatchCall for POST call
                Call<BingOcrResponse> call = BingOcr.buildCall(compressedImage);
                // Enqueue the method to the call and wait for callback (Asynchronous call)
                call.enqueue(new OcrCallback(findViewById(R.id.activity_ocr_exp), filePath));
                // After call is dispatched, load compress image into preview
                ImageView previewImageView2 = (ImageView) findViewById(R.id.previewImageView2);
                Picasso.with(mContext).load("file://" + filePath)
                        .noPlaceholder()
                        .fit()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(previewImageView2);
            }
        };
        compressTask.execute(filePath);
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.search_frag_container).getTranslationY() != 0) {
            super.onBackPressed();
        } else {
            closeSearch(findViewById(R.id.activity_ocr_exp));
        }
    }

    public void closeSearch(View view) {
        View rootView = findViewById(R.id.activity_ocr_exp);
        Button searchButton = (Button)rootView.findViewById(R.id.start_search);
        searchButton.setEnabled(true);
        AnimUtils.containerSlideDown(rootView,
                new AnimListener(rootView),
                containerTransY);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * This method creates a search call based on the input param and enqueues it
     *
     * @param searchParam parameter string to be searched for
     */
    private void enqueueSearch(String searchParam) {
        AnimUtils.darkenOverlay((FrameLayout)findViewById(R.id.drawable_overlay));
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.search_progress);
        progressBar.setVisibility(View.VISIBLE);
        Log.d(LOG_TAG, "Search String: " + searchParam);
        BingSearch bingImg = new BingSearch(searchParam);
        Call<ImageSearchResponse> call = bingImg.buildCall();
        call.enqueue(new ImageSearchCallback(findViewById(R.id.activity_ocr_exp), FRAGMENT_MANAGER));
    }

    /**
     * This method creates a imageinsight search call based on the input imageValues list and enqueues it
     *
     * @param imageValues image values list to be used as param for insightsToken call
     *                    Multiple calls.
     */
    private void enqueueImageInsightSearch(List<ImageValue> imageValues) {
        for (ImageValue imageValue : imageValues) {
            ImageInsights imageInsights = new ImageInsights(imageValue.getImageInsightsToken(), "");
            Call<ImageInsightsResponse> call = imageInsights.buildCall();
            call.enqueue(new ImageInsightCallback(findViewById(R.id.activity_ocr_exp),
                    FRAGMENT_MANAGER,
                    imageValue));
        }
    }

    /**
     * Callback class for ImageInsightCallback.
     */
    private static class ImageInsightCallback implements Callback<ImageInsightsResponse> {
        private static volatile int count = 0;
        private View rootView = null;
        private FragmentManager fm = null;
        private ImageValue imageValue = null;

        public ImageInsightCallback(View rootView, FragmentManager fm, ImageValue imageValue) {
            this.rootView = rootView;
            this.fm = fm;
            this.imageValue = imageValue;
        }

        @Override
        public void onResponse(Call<ImageInsightsResponse> call, Response<ImageInsightsResponse> response) {
            ImageInsightsResponse insightsResponse = response.body();
            imageValue.setInsightsResponse(insightsResponse);
            count++;
            if (count < IMAGE_COUNT) {
                return;
            }
            count = 0;
            SearchResultsFragment searchFragment = (SearchResultsFragment) fm.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            searchFragment.finalizeRecycler();
            // TODO: improve loading progress animations
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress);
            progressBar.setVisibility(View.GONE);
            AnimUtils.containerSlideUp(rootView);
        }

        @Override
        public void onFailure(Call<ImageInsightsResponse> call, Throwable t) {
            if (count < IMAGE_COUNT) {
                count++;
                return;
            }
            Log.e(LOG_TAG, t.getMessage());
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress);
            progressBar.setVisibility(View.GONE);
            AnimUtils.brightenOverlay((FrameLayout) rootView.findViewById(R.id.drawable_overlay));
            Snackbar.make(rootView.findViewById(R.id.activity_ocr_exp), R.string.insights_search_fail,
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private class DrawableTouchListener implements View.OnTouchListener {
        private Context context;
        private View rootView;
        private DrawableView mDrawableView = null;

        public DrawableTouchListener(Context context, View rootView) {
            this.context = context;
            this.rootView = rootView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!animating) {
                closeSearch(v);
            }
            mDrawableView = (DrawableView) v;
            List<Rect> rects = mDrawableView.getmRects();
            int x = (int) event.getX();
            int y = (int) event.getY();
            for (int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                if (rect.contains(x, y)) {
                    selectRect(i);
                    break;
                }
            }
            return true;
        }

        private void selectRect(int i) {
            mDrawableView.updateSelection(i);
            // Display the string in a snackbar and allow for search
            final String searchParam = mDrawableView.getmLineTexts().get(i).toLowerCase();
            View searchBar = findViewById(R.id.search_bar);
            AnimUtils.showSearchBar(searchBar, searchParam);
        }
    }

    /**
     * Callback class for ImageSearchResponse, handling of POST responses is done here
     */
    private class ImageSearchCallback implements Callback<ImageSearchResponse> {
        private View rootView = null;
        private FragmentManager fm = null;

        public ImageSearchCallback(View rootView, FragmentManager fm) {
            this.rootView = rootView;
            this.fm = fm;
        }

        @Override
        public void onResponse(Call<ImageSearchResponse> call, Response<ImageSearchResponse> response) {
            ImageSearchResponse searchResponse = response.body();
            if (searchResponse == null) {
                try {
                    Log.e(LOG_TAG, response.errorBody().string());
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            List<ImageValue> imageValues = searchResponse.getImageValues();
            // image search results received, now enqueueImageInsightSearch with received value
            SearchResultsFragment searchFragment = (SearchResultsFragment) fm.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            if (!imageValues.isEmpty()) {
                searchFragment.updateRecyclerList(imageValues);
                enqueueImageInsightSearch(imageValues);
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
            Snackbar.make(rootView.findViewById(R.id.activity_ocr_exp), R.string.image_search_fail,
                    Snackbar.LENGTH_LONG)
                    .show();
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.search_progress);
            progressBar.setVisibility(View.GONE);
            FrameLayout drawableOverlay = (FrameLayout) rootView.findViewById(R.id.drawable_overlay);
            AnimUtils.brightenOverlay(drawableOverlay);
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
            List<Line> lines = bingOcrResponse.getAllLines();
            mDrawableView.drawBoxes(rootView, mFilePath, lines,
                    bingOcrResponse.getTextAngle().floatValue(),
                    bingOcrResponse.getLanguage());
            mDrawableView.setBackgroundColor(Color.TRANSPARENT);
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onFailure(Call<BingOcrResponse> call, Throwable t) {
            mDrawableView.setBackgroundColor(Color.TRANSPARENT);
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);
            Snackbar.make(rootView.findViewById(R.id.activity_ocr_exp), R.string.ocr_call_fail,
                    Snackbar.LENGTH_LONG)
                    .show();
            Log.e(LOG_TAG, "POST Call Failed!" + t.getMessage());
        }
    }

    /**
     * This async task is used to compress the image to be sent in the
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


    /**
     * Listener to set boolean value for animating so that we can track it
     */
    private class AnimListener implements Animator.AnimatorListener {
        View rootView = null;

        AnimListener(View rootView) {
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
