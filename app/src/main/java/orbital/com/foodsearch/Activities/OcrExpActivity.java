package orbital.com.foodsearch.Activities;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import orbital.com.foodsearch.Helpers.ImageUtils;
import orbital.com.foodsearch.Models.BingResponse;
import orbital.com.foodsearch.Models.Line;
import orbital.com.foodsearch.R;
import orbital.com.foodsearch.Views.DrawableView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;

public class OcrExpActivity extends AppCompatActivity {
    private static final String LOG_TAG = "FOODIES";
    private final String OCR_BASE_URL = "https://api.projectoxford.ai/vision/v1.0/ocr/";
    private final String API_KEY = "b2d6262c77174bafbb5bda3e5997dbfe";

    private final Interceptor interceptor = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header("Content-Type") != null
                    || originalRequest.header("Ocp-Apim-Subscription-Key")!= null) {
                return chain.proceed(originalRequest);
            }
            return chain.proceed(originalRequest
                    .newBuilder().addHeader("Ocp-Apim-Subscription-Key", API_KEY)
                    .addHeader("Content-Type", "multipart/form-data")
                    .build());
        }
    };
    private final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    private final OkHttpClient client = clientBuilder.addInterceptor(interceptor)
            .build();
    private String filePath = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (filePath != null) {
            outState.putString("savedFilePath", filePath);
        }
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
        filePath = getIntent().getStringExtra("filePath");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_exp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imgView = (ImageView) findViewById(R.id.previewImageView2);
        Picasso.with(this).load("file://" + filePath)
                //.placeholder(R.color.black_overlay)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(64,0)
                .into(imgView);
        startConnect();
    }

    private void startConnect() {
        if (isNetworkAvailable() && isOnline()) {
            bingConnect();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_ocr_exp),
                    R.string.internet_error_text, Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startConnect();
                }
            });
            snackbar.show();
        }
    }

    /**
     * Method that uses RetroFit library to perform a url connection query
     * to the bing API server
     * */
    private void bingConnect() {
        View root = findViewById(R.id.activity_ocr_exp);
        CompressAsyncTask compressTask = new CompressAsyncTask(this, root){
            @Override
            protected void onPostExecute(byte[] result) {
                // When compressTask is done, invoke dispatchCall for POST call
                dispatchCall(result);
                // After call is dispatched, load compress image into preview
                ImageView previewImageView2 = (ImageView) findViewById(R.id.previewImageView2);
                Picasso.with(mContext).load("file://" + filePath)
                        .placeholder(previewImageView2.getDrawable())
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resize(previewImageView2.getWidth(), previewImageView2.getHeight())
                        .into(previewImageView2);
            }
        };
        compressTask.execute(filePath);
    }

    /**
     * This method sets up the POST call query and enqueues it for async up/download.
     * @param rawImage raw image binary data to be uploaded via POST
     */
    private void dispatchCall(byte[] rawImage) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(OCR_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BingOCRService service = retrofit.create(BingOCRService.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),
                rawImage);
        // For params:
        // Map<String, String> params = new HashMap<String, String>();
        Call<BingResponse> call = service.processImage(requestBody);
        // Enqueue the method to the call and wait for callback (Asynchronous call)
        call.enqueue(new OcrCallback(findViewById(R.id.activity_ocr_exp), filePath));
    }

    // To check for network conditions
    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    // To check for network conditions
    private boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException |InterruptedException e)          { e.printStackTrace(); }
        return false;
    }

    /**
     * This interface works with retrofit to abstract the API calls into
     * a java interface
     */

    private interface BingOCRService {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter
        @Multipart
        @POST("./")
        Call<BingResponse> processImage(@Part("image") RequestBody image,
                                        @QueryMap Map<String, String> params);
        @Multipart
        @POST("./")
        Call<BingResponse> processImage(@Part("image") RequestBody image);
    }

    /**
     * Callback class that implements callback method to be used for when a
     * response is received from server
     */
    private static class OcrCallback implements Callback<BingResponse> {
        private View mRootView = null;
        private String mFilePath = null;
        private DrawableView mDrawableView = null;

        OcrCallback(View rootView, String filePath){
            mRootView = rootView;
            mFilePath = filePath;
        }

        @Override
        public void onResponse(Call<BingResponse> call, Response<BingResponse> response) {
            BingResponse bingResponse = response.body();
            List<Line> lines = bingResponse.getAllLines();
            mDrawableView = (DrawableView) mRootView
                    .findViewById(R.id.drawable_view);
            mDrawableView.setOnTouchListener(mDrawableView );
            mDrawableView.drawBoxes(mRootView, mFilePath, lines,
                    new Float(bingResponse.getTextAngle()));
            mDrawableView.setBackgroundColor(Color.TRANSPARENT);
            ProgressBar progressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar2);
            progressBar.setVisibility(View.GONE);
        }
        @Override
        public void onFailure(Call<BingResponse> call, Throwable t) {
            mDrawableView.setBackgroundColor(Color.TRANSPARENT);
            ProgressBar progressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar2);
            progressBar.setVisibility(View.GONE);
            Snackbar.make(mRootView.findViewById(R.id.activity_ocr_exp), R.string.post_fail_text,
                    Snackbar.LENGTH_SHORT)
                    .show();
            Log.e(LOG_TAG, "POST Call Failed!" + t.getMessage());
        }
    }

    /**
     * This async task is used to compress the image to be sent in the
     * background thread using ImageUtils.compressImage
     */
    private static class CompressAsyncTask extends AsyncTask<String, Integer, byte[]> {
        Context mContext = null;
        View mRootView = null;

        CompressAsyncTask(Context context, View rootView){
            mContext = context;
            mRootView = rootView;
        }

        @Override
        protected byte[] doInBackground(String... params) {
            return ImageUtils.compressImage(params[0]);
        }
    }
}
