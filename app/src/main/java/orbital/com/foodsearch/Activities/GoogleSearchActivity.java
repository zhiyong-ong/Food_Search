package orbital.com.foodsearch.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import orbital.com.foodsearch.Models.BingImageSearch;
import orbital.com.foodsearch.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public class GoogleSearchActivity extends AppCompatActivity {

    private static final String OCP_APIM_KEY = "e801fac4192d4741976e816b93bdcb48";
    private static final String BING_IMAGE_URL = "https://bingapis.azure-api.net/api/v5/images/";
    private static final String LOG_TAG = "FOODIES";

    //sample data
    private final String queryTxt = "chicken";
    private final String count = "10";
    private final String offset = "0";
    private final String markets = "en-us";
    private final String safeSearch = "Moderate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_search);

//        Button btn = (Button) findViewById(R.id.searchButton);
//        final EditText searchText = (EditText) findViewById(R.id.inputSearch);
//        if(btn != null) {
//            btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
//                    assert searchText != null;
//                    String keyword= searchText.getText().toString();
//                    intent.putExtra(SearchManager.QUERY, keyword);
//                    startActivity(intent);
//                }
//            });
//        }

        final EditText edt = (EditText) findViewById(R.id.editText);
        if(edt != null) {
            edt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
                    httpClient.networkInterceptors().add(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder().addHeader("Ocp-Apim-Subscription-Key", OCP_APIM_KEY).build();
                            return chain.proceed(request);
                        }
                    });
                    httpClient.addInterceptor(logging);

                    // For params:
                    Map<String, String> data = new HashMap<>();
                    data.put("safeSearch", safeSearch);
                    data.put("mkt", markets);
                    data.put("offset", offset);
                    data.put("count", count);
                    data.put("q", queryTxt);

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BING_IMAGE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(httpClient.build())
                            .build();
                    BingImageSearchAPI imgAPI = retrofit.create(BingImageSearchAPI.class);
                    Call<BingImageSearch> call = imgAPI.getParams(data);
                    call.enqueue(new Callback<BingImageSearch>() {
                        @Override
                        public void onResponse(Call<BingImageSearch> call, Response<BingImageSearch> response) {
                            Log.e(LOG_TAG, response.body().toString());
                            final TextView textView = (TextView) findViewById(R.id.textView);
                            textView.setText(response.body().getValue().get(0).getContentUrl());

                        }

                        @Override
                        public void onFailure(Call<BingImageSearch> call, Throwable t) {
                            final TextView textView = (TextView) findViewById(R.id.textView);
                            textView.setText("Something went wrong: " + t.getMessage());
                            Log.e(LOG_TAG, call.toString());
                        }
                    });
                }
            });
        }

    }

    private interface BingImageSearchAPI {

        @GET("search")
        Call<BingImageSearch> getParams(
                @QueryMap Map<String, String> params
        );

    }

}
