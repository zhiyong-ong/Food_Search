package orbital.com.foodsearch.Activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Adapters.BingImageAdapter;
import orbital.com.foodsearch.Helpers.BingOcr;
import orbital.com.foodsearch.Helpers.BingSearch;
import orbital.com.foodsearch.Helpers.ImageUtils;
import orbital.com.foodsearch.Helpers.NetworkUtils;
import orbital.com.foodsearch.Models.BingImageSearch;
import orbital.com.foodsearch.Models.BingOcrResponse;
import orbital.com.foodsearch.Models.ImageValue;
import orbital.com.foodsearch.Models.Line;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Views.DrawableView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OcrActivity extends AppCompatActivity{
    private static final String LOG_TAG = "FOODIES";

    private String filePath = null;
    private List<ImageValue> mImageValues = null;

    private static String Lang = "";
    //query params for bing search
    public final String count = "5";
    public final String offset = "0";
    public final String markets = "";
    public final String safeSearch = "Moderate";
    public Context context = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (filePath != null) {
            outState.putString("savedFilePath", filePath);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("savedFilePath")) {
            filePath = savedInstanceState.getString("savedFilePath");
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
    }

    private void initializeDrawView() {
        final DrawableView drawableView = (DrawableView)findViewById(R.id.drawable_view);
        drawableView.setOnTouchListener(new DrawableTouchListener(this, findViewById(R.id.activity_ocr_exp)));
    }

    public void initializeRecycler(ArrayList<String[]> content) {
        final View parentView = findViewById(R.id.preview_container);
        final RecyclerView rvImages = (RecyclerView) findViewById(R.id.recycler_view);
        ObjectAnimator anim = ObjectAnimator.ofFloat(rvImages,
                View.TRANSLATION_Y, 0);
        anim.setDuration(650);
        anim.start();
        ViewTreeObserver vto = rvImages.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
            @Override
            public boolean onPreDraw() {
                rvImages.getViewTreeObserver().removeOnPreDrawListener(this);
                rvImages.setTranslationY(parentView.getHeight());
                return true;
            }
        });
        mImageValues = new ArrayList<>();
        for(int i = 0; i < content.size(); i++) {
            ImageValue img = new ImageValue();
            img.setThumbnailUrl(content.get(i)[0]);
            img.setContentUrl(content.get(i)[1]);
            img.setHostPageUrl(content.get(i)[2]);
            img.setName(content.get(i)[3]);
            mImageValues.add(img);
        }

        ImageView cardImageView = (ImageView)findViewById(R.id.card_image);
        BingImageAdapter adapter = new BingImageAdapter(this, mImageValues);
        rvImages.setAdapter(adapter);
        LinearLayoutManager layoutMgr = new LinearLayoutManager(this);
        layoutMgr.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvImages.setLayoutManager(layoutMgr);
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
                    selectRect(rect, i);
                    break;
                }
            }
            return true;
        }

        private void selectRect(Rect rect, int i){
            final String searchParam = mDrawableView.getmLineTexts().get(i);
            Snackbar.make(mDrawableView, searchParam, Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.CYAN)
                    .setAction(R.string.search, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.e(LOG_TAG, "Search String: " + searchParam);
                            BingSearch bingImg = new BingSearch(searchParam, count, offset, markets, safeSearch);
                            Call<BingImageSearch> call = bingImg.getImage();
                            //get arraylist to store the results. first elem is thumbnail url
                            // second elem is content url. third elem is hostpageurl. fourth elem is name
                            final ArrayList<String[]> results = new ArrayList<>();
                            call.enqueue(new Callback<BingImageSearch>() {
                                @Override
                                public void onResponse(Call<BingImageSearch> call, Response<BingImageSearch> response) {
                                    Log.e(LOG_TAG, response.body().toString());
                                    for (int i = 0; i < response.body().getImageValues().size(); i++) {
                                        String[] name = {response.body().getImageValues().get(i).getThumbnailUrl(),
                                                response.body().getImageValues().get(i).getContentUrl(),
                                                response.body().getImageValues().get(i).getHostPageUrl(),
                                                response.body().getImageValues().get(i).getName()};
                                        results.add(name);
                                    }
                                    initializeRecycler(results);
                                }

                                @Override
                                public void onFailure(Call<BingImageSearch> call, Throwable t) {
                                    Log.e(LOG_TAG, call.toString());
                                    results.add(new String[]{t.toString(), null});
                                    initializeRecycler(results);
                                }
                            });
                        }
                    }).show();

//            mDrawableView.selectIndex(i);
//            mDrawableView.invalidate();
            // TODO: Move this to OcrActivity and do loading progress animations and do adapter.notifyDataSetChanged
            // TODO: scrollToPosition(0)
        }
    }

    /**
     * Callback class that implements callback method to be used for when a
     * response is received from server
     * */
    private static class OcrCallback implements Callback<BingOcrResponse> {
        private View mRootView = null;
        private String mFilePath = null;
        private DrawableView mDrawableView = null;

        private OcrCallback(View rootView, String filePath){
            mRootView = rootView;
            mFilePath = filePath;
            mDrawableView = (DrawableView) rootView.findViewById(R.id.drawable_view);
        }

        @Override
        public void onResponse(Call<BingOcrResponse> call, Response<BingOcrResponse> response) {
            BingOcrResponse bingOcrResponse = response.body();
            List<Line> lines = bingOcrResponse.getAllLines();
            Lang = bingOcrResponse.getLanguage();
            Log.e(LOG_TAG, "Language is: " + Lang);
            mDrawableView.drawBoxes(mRootView, mFilePath, lines,
                    bingOcrResponse.getTextAngle().floatValue(),
                    bingOcrResponse.getLanguage());
            mDrawableView.setBackgroundColor(Color.TRANSPARENT);
            ProgressBar progressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar2);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onFailure(Call<BingOcrResponse> call, Throwable t) {
            mDrawableView.setBackgroundColor(Color.TRANSPARENT);
            ProgressBar progressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar2);
            progressBar.setVisibility(View.GONE);
            Snackbar.make(mRootView.findViewById(R.id.activity_ocr_exp), R.string.post_fail_text,
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
}
