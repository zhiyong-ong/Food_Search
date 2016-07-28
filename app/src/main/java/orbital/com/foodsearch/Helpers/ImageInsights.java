package orbital.com.foodsearch.Helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import orbital.com.foodsearch.Misc.GlobalVar;
import orbital.com.foodsearch.Models.ImageInsightsPOJO.ImageInsightsResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class ImageInsights {
    //Zhiyong api key. renew on 11/7/16
    //private static final String OCP_APIM_KEY = "e801fac4192d4741976e816b93bdcb48";
    //private static final String OCP_APIM_KEY = "80940a827ff74b1f882d9b18ef6b8110";
    //private static final String OCP_APIM_KEY = "ccfba34562094f9480bf01f74750b423";

    //Abel's one
    //private static final String OCP_APIM_KEY = "df4bfc35ad584f8c856052cb46f213dd";

    private static final String BING_IMAGE_URL = "https://api.cognitive.microsoft.com/bing/v5.0/images/";
    private static final String LOG_TAG = "FOODIES";

    //init params
    private String insights = null;
    private String modulesRequested = null;

    public ImageInsights(String insights, String modulesRequested) {
        this.insights = insights;
        this.modulesRequested = modulesRequested;
    }

    public String getQueryTxt() {
        return insights;
    }

    public String getModulesRequested() {
        return modulesRequested;
    }

    public Call<ImageInsightsResponse> buildCall() {

        final ArrayList<String[]> results = new ArrayList<>();
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

        data.put("insightsToken", insights);
        data.put("modulesRequested", modulesRequested);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BING_IMAGE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        //get the pojos filled up
        ImageInsights.BingImageInsightsAPI imgAPI = retrofit.create(ImageInsights.BingImageInsightsAPI.class);
        Call<ImageInsightsResponse> call = imgAPI.getParams(data);
        return call;
    }

    private interface BingImageInsightsAPI {
        @POST("search")
        Call<ImageInsightsResponse> getParams(
                @QueryMap Map<String, String> params
        );
    }
}
