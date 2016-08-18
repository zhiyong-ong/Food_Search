package orbital.com.menusnap.Helpers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import orbital.com.menusnap.Activities.MainActivity;
import orbital.com.menusnap.Misc.GlobalVar;
import orbital.com.menusnap.Models.ImageSearchPOJO.ImageSearchResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by zhiyong on 15/6/2016.
 * returns a call when buildCall method is used. call can be enqueued. asynchronous is automatic
 */

public class BingSearch {
    private static final String BING_IMAGE_URL = "https://api.cognitive.microsoft.com/bing/v5.0/images/";
    private static final String LOG_TAG = "FOODIES";

    //sample data
    private String queryTxt = null;
    private String count = null;
    private String offset = "0";
    private String markets = "en-us";
    private String safeSearch = "Moderate";


    public BingSearch(String queryTxt, String count) {
        this.queryTxt = queryTxt;
        this.count = count;
    }
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

    public Call<ImageSearchResponse> buildCall() {

        //setting up logging messages regarding headers
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        //adding the header here with the api key
        httpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request().newBuilder().addHeader("Ocp-Apim-Subscription-Key", GlobalVar.getImageKey()).build();
                return chain.proceed(request);
            }
        });
        httpClient.addInterceptor(logging);

        // For adding in the parameters
        Map<String, String> data = new HashMap<>();
        data.put("safeSearch", safeSearch);
        data.put("imageType", "Photo");
        data.put("setLang", MainActivity.BASE_LANGUAGE);
        data.put("mkt", MainActivity.MARKET_CODE);
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
        Call<ImageSearchResponse> call = imgAPI.getParams(data);
        return call;
    }

    private interface BingImageSearchAPI {
        @GET("search")
        Call<ImageSearchResponse> getParams(
                @QueryMap Map<String, String> params
        );
    }




}
