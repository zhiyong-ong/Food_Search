package orbital.com.foodsearch.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import orbital.com.foodsearch.Helpers.BingSearch;
import orbital.com.foodsearch.Models.BingSearchResponse;
import orbital.com.foodsearch.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleSearchActivity extends AppCompatActivity {

    private static final String OCP_APIM_KEY = "e801fac4192d4741976e816b93bdcb48";
    private static final String BING_IMAGE_URL = "https://bingapis.azure-api.net/api/v5/images/";
    private static final String LOG_TAG = "FOODIES";
    //sample data
    private final String queryTxt = "chicken rice";
    private final String count = "10";
    private final String offset = "0";
    private final String markets = "en-us";
    private final String safeSearch = "Moderate";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_search);

        final Button btn = (Button) findViewById(R.id.button);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                final EditText edt = (EditText) findViewById(R.id.editText);
                String input = edt.getText().toString();
                final ImageView imgView = (ImageView) findViewById(R.id.imgView);
                final TextView txt = (TextView) findViewById(R.id.textView);
                BingSearch bingImg = new BingSearch(input);
                //get call from the helper class
                Call<BingSearchResponse> call = bingImg.buildCall();
                call.enqueue(new Callback<BingSearchResponse>() {
                    @Override
                    public void onResponse(Call<BingSearchResponse> call, Response<BingSearchResponse> response) {
                        //get arraylist to store the results
                        final ArrayList<String[]> results = new ArrayList<>();
                        Log.e(LOG_TAG, response.body().toString());

                        for(int i = 0; i < Integer.parseInt(count); i++) {
                            String[] name = {response.body().getImageValues().get(i).getContentUrl(),
                                    response.body().getImageValues().get(i).getName()};
                            results.add(name);
                        }

                        Picasso.with(context)
                                .load(results.get(0)[0])
                                .into(imgView);
                        Log.e(LOG_TAG, results.get(0)[0]);
                        Log.e(LOG_TAG, results.get(0)[1]);
                        txt.setText(results.get(0)[1]);
                    }

                    @Override
                    public void onFailure(Call<BingSearchResponse> call, Throwable t) {
                        Log.e(LOG_TAG, call.toString());
                        txt.setText(t.getMessage());
                    }
                });
                }
            });
        }
    }
}
