package orbital.com.foodsearch.Helpers;

import java.io.IOException;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import orbital.com.foodsearch.Activities.OcrActivity;
import orbital.com.foodsearch.Models.OcrPOJO.BingOcrResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;

/**
 * Created by Abel on 6/18/2016.
 */

public class BingOcr {
    private static final String OCR_BASE_URL = "https://api.projectoxford.ai/vision/v1.0/ocr/";
    private static final String API_KEY = OcrActivity.OCR_KEY;

    private static final Interceptor interceptor = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null || originalRequest.header("Content-Type") != null
                    || originalRequest.header("Ocp-Apim-Subscription-Key")!= null) {
                return chain.proceed(originalRequest);
            }
            return chain.proceed(originalRequest
                    .newBuilder()
                    .addHeader("Ocp-Apim-Subscription-Key", API_KEY)
                    .addHeader("Content-Type", "multipart/form-data")
                    .build());
        }
    };
    private static final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    private static final OkHttpClient client = clientBuilder.addInterceptor(interceptor)
            .build();


    /**
     * This method sets up the POST call query and enqueues it for async up/download.
     * @param rawImage raw IMAGE_KEY binary data to be uploaded via POST
     */
    public static Call<BingOcrResponse> buildCall(byte[] rawImage) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(OCR_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OcrAPI ocrAPI = retrofit.create(OcrAPI.class);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),
                rawImage);
        // For params:
        // Map<String, String> params = new HashMap<String, String>();
        return ocrAPI.processImage(requestBody);
    }

    /**
     * This interface works with retrofit to abstract the API calls into
     * a java interface
     */
    private interface OcrAPI {
        // Request method and URL specified in the annotation
        // Callback for the parsed response is the last parameter
        @Multipart
        @POST("./")
        Call<BingOcrResponse> processImage(@Part("image") RequestBody image,
                                           @QueryMap Map<String, String> params);

        @Multipart
        @POST("./")
        Call<BingOcrResponse> processImage(@Part("image") RequestBody image);
    }

}
