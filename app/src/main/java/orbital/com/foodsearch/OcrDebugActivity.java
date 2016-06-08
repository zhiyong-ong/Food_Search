package orbital.com.foodsearch;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import orbital.com.foodsearch.Helpers.ImageUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;

public class OcrDebugActivity extends AppCompatActivity {
    private final String LOG_TAG = "FOODIES";
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
        if (filePath == null) {
            outState.putString("savedFilePath", filePath);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        if (savedInstanceState.containsKey("savedFilePath")) {
            filePath = savedInstanceState.getString("savedFilePath");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        filePath = getIntent().getStringExtra("filePath");

        TextView textView = (TextView) findViewById(R.id.jsonTextView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        ImageView imgView = (ImageView) findViewById(R.id.previewImageView);
        Picasso.with(this).load("file://" + filePath)
                .placeholder(R.color.black_overlay)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(imgView.getMaxWidth(),0)
                .into(imgView);

        startConnect();
    }

    private void startConnect() {
        if (isNetworkAvailable() && isOnline()) {
            bingConnect();
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_ocr),
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
        View root = findViewById(R.id.activity_ocr);
        CompressAsyncTask compressTask = new CompressAsyncTask(this, root){
            @Override
            protected void onPostExecute(byte[] result) {
                // When compressTask is done, invoke dispatchCall for POST call
                dispatchCall(result);
                // After call is dispatched, load compress image into preview
                ImageView previewImageView = (ImageView) findViewById(R.id.previewImageView);
                Picasso.with(mContext).load("file://" + filePath)
                        .resize(previewImageView.getWidth(), 0)
                        .placeholder(previewImageView.getDrawable())
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into((ImageView)findViewById(R.id.previewImageView));
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
                //.addConverterFactory(GsonConverterFactory.create()) //THIS IS PLACEHOLDER FOR SERIALIZATION
                .build();
        BingOCRService service = retrofit.create(BingOCRService.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),
                rawImage);
        // For params:
        // Map<String, String> params = new HashMap<String, String>();
        Call<ResponseBody> call = service.postImage(requestBody);
        // Enqueue the method to the call and wait for callback (Asynchronous call)
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String result = response.body().string();
                    TextView textView = (TextView) findViewById(R.id.jsonTextView);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(result);
                    Log.e(LOG_TAG, result);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Exception!" + e.getMessage());
                }

            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(LOG_TAG, "FAILED! Error MSG:" + t.getMessage());
            }
        });
    }

    // To check for network conditions
    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    // To check for network conditions
    public boolean isOnline() {
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
        Call<ResponseBody> postImage(@Part("image") RequestBody image,
                                     @QueryMap Map<String, String> params);
        @Multipart
        @POST("./")
        Call<ResponseBody> postImage(@Part("image") RequestBody image);
    }

    /**
     * This async task is used to compress the image to be sent in the
     * background thread using ImageUtils.compressImage
     */
    private static class CompressAsyncTask extends AsyncTask<String, Void, byte[]>{
        protected Context mContext = null;
        protected View mRootView = null;

        public CompressAsyncTask(Context context, View rootView){
            mContext = context;
            mRootView = rootView;
        }

        @Override
        protected byte[] doInBackground(String... params) {
            return ImageUtils.compressImage(params[0]);
        }
    }
}
