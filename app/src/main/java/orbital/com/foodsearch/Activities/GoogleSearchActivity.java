package orbital.com.foodsearch.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import orbital.com.foodsearch.Helpers.BingSearch;
import orbital.com.foodsearch.R;

public class GoogleSearchActivity extends AppCompatActivity {

    private static final String OCP_APIM_KEY = "e801fac4192d4741976e816b93bdcb48";
    private static final String BING_IMAGE_URL = "https://bingapis.azure-api.net/api/v5/images/";
    private static final String LOG_TAG = "FOODIES";
    private final Context context = this;

    //sample data
    private final String queryTxt = "chicken rice";
    private final String count = "10";
    private final String offset = "0";
    private final String markets = "en-us";
    private final String safeSearch = "Moderate";

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
                    ImageView imgView = (ImageView) findViewById(R.id.imgView);
                    TextView txt = (TextView) findViewById(R.id.textView);
                    BingSearch bingImg = new BingSearch(input, count, offset, markets, safeSearch, getApplicationContext(), imgView, txt);
                    ArrayList<String[]> results = bingImg.getImage();

                }
            });
        }
    }
}
