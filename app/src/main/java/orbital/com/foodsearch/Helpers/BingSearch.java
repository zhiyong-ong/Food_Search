package orbital.com.foodsearch.Helpers;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import orbital.com.foodsearch.Models.BingImageSearch;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by zhiyong on 15/6/2016.
 */

public class BingSearch {
    private static final String OCP_APIM_KEY = "e801fac4192d4741976e816b93bdcb48";
    private static final String BING_IMAGE_URL = "https://bingapis.azure-api.net/api/v5/images/";
    private static final String LOG_TAG = "FOODIES";

    //sample data
    private String queryTxt = "chicken rice";
    private String count = "10";
    private String offset = "0";
    private String markets = "en-us";
    private String safeSearch = "Moderate";
    private Context context = null;
    private ImageView img = null;
    private TextView txt = null;
    public String getQueryTxt() {
        return queryTxt;
    }

    public String getCount() {
        return count;
    }

    public String getOffset() {
        return offset;
    }

    public String getMarkets() {
        return markets;
    }

    public String getSafeSearch() {
        return safeSearch;
    }

    public BingSearch(String queryTxt, String count, String offset, String markets,String safeSearch,
                      Context context, ImageView img, TextView txt) {
        this.queryTxt = queryTxt;
        this.count = count;
        this.offset = offset;
        this.markets = markets;
        this.safeSearch = safeSearch;
        this.context = context;
        this.img = img;
        this.txt = txt;
    }

    public ArrayList<String[]> getImage() {

        final ArrayList<String[]> results = new ArrayList<>();
        //setting up logging messages regarding headers
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        //adding the header here with the api key
        httpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder().addHeader("Ocp-Apim-Subscription-Key", OCP_APIM_KEY).build();
                return chain.proceed(request);
            }
        });
        httpClient.addInterceptor(logging);

        // For adding in the parameters
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
        //get the pojos filled up
        BingSearch.BingImageSearchAPI imgAPI = retrofit.create(BingSearch.BingImageSearchAPI.class);
        Call<BingImageSearch> call = imgAPI.getParams(data);
        call.enqueue(new Callback<BingImageSearch>() {
            @Override
            public void onResponse(Call<BingImageSearch> call, Response<BingImageSearch> response) {
                Log.e(LOG_TAG, response.body().toString());
                for(int i = 0; i < Integer.parseInt(count); i++) {
                    String[] name = {response.body().getValue().get(i).getContentUrl(), response.body().getValue().get(i).getName()};
                    results.add(name);
                }

                Picasso.with(context)
                        .load(results.get(0)[0])
                        .into(img);
                txt.setText(results.get(0)[1]);
            }

            @Override
            public void onFailure(Call<BingImageSearch> call, Throwable t) {
                Log.e(LOG_TAG, call.toString());
                String[] error = {t.getMessage()};
                results.add(error);
            }
        });
        return results;
    }

    private interface BingImageSearchAPI {
        @GET("search")
        Call<BingImageSearch> getParams(
                @QueryMap Map<String, String> params
        );
    }


}
