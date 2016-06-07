package orbital.com.foodsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;

public class OcrActivity extends AppCompatActivity {
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
                    .newBuilder().addHeader("Ocp-Apim-Subscription-Key", "b2d6262c77174bafbb5bda3e5997dbfe")
                    .addHeader("Content-Type", "multipart/form-data")
                    .build());
        }
    };
    private final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    private final OkHttpClient client = clientBuilder.addInterceptor(interceptor)
            .build();
    private Bitmap bitmap = null;
    private String filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        filePath = getIntent().getStringExtra("filePath");
        File file = new File(filePath);
        TextView textView = (TextView) findViewById(R.id.jsonTextView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        bitmap = BitmapFactory.decodeFile(filePath);
        imgView.setImageBitmap(bitmap);
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
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(OCR_BASE_URL)
                //.addConverterFactory(GsonConverterFactory.create())
                .build();
        BingOCRService service = retrofit.create(BingOCRService.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),
                new File(filePath));
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
                    e.printStackTrace();
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
    private boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException |InterruptedException e)          { e.printStackTrace(); }
        return false;
    }

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
}
