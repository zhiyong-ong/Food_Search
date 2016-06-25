package orbital.com.foodsearch.Activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import orbital.com.foodsearch.Fragments.SearchResultsFragment;
import orbital.com.foodsearch.Helpers.BingOcr;
import orbital.com.foodsearch.Helpers.BingSearch;
import orbital.com.foodsearch.Helpers.ImageUtils;
import orbital.com.foodsearch.Helpers.NetworkUtils;
import orbital.com.foodsearch.Models.BingOcrResponse;
import orbital.com.foodsearch.Models.BingSearchResponse;
import orbital.com.foodsearch.Models.ImageValue;
import orbital.com.foodsearch.Models.Line;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Views.DrawableView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OcrActivity extends AppCompatActivity implements SearchResultsFragment.OnFragmentInteractionListener{
    private static final String SAVED_FILE_PATH = "savedFilePath";
    private static final String LOG_TAG = "FOODIES";
    private static final String SEARCH_FRAGMENT_TAG = "SEARCHFRAGMENT";

    private final FragmentManager FRAGMENT_MANAGER = getSupportFragmentManager();

    private String filePath = null;
    private List<ImageValue> mImageValues = null;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_exp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imgView = (ImageView) findViewById(R.id.previewImageView2);
        Picasso.with(this).load("file://" + filePath)
                //.placeholder(R.color.black_overlay)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(30, 44)
                .into(imgView);
        startOcrService();
        initializeDrawView();
        setupRecContainer();
    }

    private void setupRecContainer() {
        final DrawableView drawableView = (DrawableView) findViewById(R.id.drawable_view);
        final FrameLayout recyclerContainer = (FrameLayout) findViewById(R.id.rec_frag_container);
        ViewTreeObserver vto = recyclerContainer.getViewTreeObserver();
        // To move recyclerContainer out of the screen
        // Done using onPreDrawListener so as to get the correct measured height
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
            @Override
            public boolean onPreDraw() {
                recyclerContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                recyclerContainer.setTranslationY(drawableView.getHeight());
                android.support.v4.app.FragmentTransaction ft = FRAGMENT_MANAGER.beginTransaction();
                ft.replace(R.id.rec_frag_container, new SearchResultsFragment(), SEARCH_FRAGMENT_TAG);
                ft.commit();
                return true;
            }
        });
    }

    private void initializeDrawView() {
        final DrawableView drawableView = (DrawableView)findViewById(R.id.drawable_view);
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
     * */
    private void bingOcrConnect() {
        View root = findViewById(R.id.activity_ocr_exp);
        CompressAsyncTask compressTask = new CompressAsyncTask(this, root){
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
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resize(previewImageView2.getWidth(), previewImageView2.getHeight())
                        .into(previewImageView2);
            }
        };
        compressTask.execute(filePath);
    }

    private void enqueueSearch(String searchParam) {
        Log.e(LOG_TAG, "Search String: " + searchParam);
        BingSearch bingImg = new BingSearch(searchParam);
        Call<BingSearchResponse> call = bingImg.buildCall();
        call.enqueue(new ImageSearchCallback(findViewById(R.id.activity_ocr_exp), FRAGMENT_MANAGER));
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private static class ImageSearchCallback implements Callback<BingSearchResponse>{
        private FragmentManager fm = null;
        private View rootView = null;

        public ImageSearchCallback(View rootView, FragmentManager fm){
            this.fm = fm;
            this.rootView = rootView;
        }

        @Override
        public void onResponse(Call<BingSearchResponse> call, Response<BingSearchResponse> response) {
            Log.e(LOG_TAG, response.body().toString());

            FrameLayout fragmentContainer = (FrameLayout) rootView.findViewById(R.id.rec_frag_container);
            // TODO: do loading progress animations
            // TODO: scrollToPosition(0)
            ObjectAnimator anim = ObjectAnimator.ofFloat(fragmentContainer,
                    View.TRANSLATION_Y, 0);
            anim.setDuration(650);
            anim.start();

            BingSearchResponse searchResponse = response.body();
            SearchResultsFragment searchFragment = (SearchResultsFragment)fm.findFragmentByTag(SEARCH_FRAGMENT_TAG);
            searchFragment.updateRecycler(searchResponse.getImageValues());
        }

        @Override
        public void onFailure(Call<BingSearchResponse> call, Throwable t) {
            Log.e(LOG_TAG, call.toString());
            Snackbar.make(rootView.findViewById(R.id.activity_ocr_exp), R.string.image_search_fail,
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Callback class that implements callback method to be used for when a
     * response is received from server
     * */
    private static class OcrCallback implements Callback<BingOcrResponse> {
        private View rootView = null;
        private String mFilePath = null;
        private DrawableView mDrawableView = null;

        private OcrCallback(View rootView, String filePath){
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
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar2);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onFailure(Call<BingOcrResponse> call, Throwable t) {
            mDrawableView.setBackgroundColor(Color.TRANSPARENT);
            ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar2);
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
    private static class CompressAsyncTask extends AsyncTask<String, Integer, byte[]> {
        protected Context mContext = null;
        protected View mRootView = null;

        protected CompressAsyncTask(Context context, View rootView){
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

        public DrawableTouchListener(Context context, View rootView) {
            this.context = context;
            this.rootView = rootView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mDrawableView = (DrawableView) v;
            List<Rect> rects = mDrawableView.getmRects();
            int x = (int) event.getX();
            int y = (int) event.getY();
            for (int i = 0; i < rects.size(); i++) {
                Rect rect = rects.get(i);
                if (rect.contains(x, y)){
                    selectRect(i);
                    break;
                }
            }
            return true;
        }

        private void selectRect(int i){
            mDrawableView.updateBoxes(i);
            final String searchParam = mDrawableView.getmLineTexts().get(i);

            Snackbar.make(mDrawableView, searchParam, Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.CYAN)
                    .setAction(R.string.search, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    enqueueSearch(searchParam);
                                }
                            })
                    .show();
        }
    }

}
