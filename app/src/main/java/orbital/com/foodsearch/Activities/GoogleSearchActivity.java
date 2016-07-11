package orbital.com.foodsearch.Activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import orbital.com.foodsearch.Helpers.BingSearch;
import orbital.com.foodsearch.Helpers.BingTranslate;
import orbital.com.foodsearch.Models.ImageSearchPOJO.ImageSearchResponse;
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
    DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_search);

        database = FirebaseDatabase.getInstance().getReference();
        final Button btnDB = (Button) findViewById(R.id.buttonDB);
        if(btnDB != null) {
            btnDB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText edt = (EditText) findViewById(R.id.testDB);
                    String input = edt.getText().toString();
                    database.child("users").child(input).setValue(input);
                }
            });
        }
        final Button getDB = (Button) findViewById(R.id.retrieveDBBtn);
        if(getDB != null) {
            getDB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String query = ((EditText) findViewById(R.id.textQueryDB)).getText().toString();
                    final TextView txt = (TextView) findViewById(R.id.retrieveDB);
                    database.child("users").child(query).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                txt.setText("Null");
                            } else {
                                ImageSearchResponse img = dataSnapshot.getValue(ImageSearchResponse.class);
                                txt.setText(String.valueOf(img.toString()));
                                Log.e(LOG_TAG, "SNAPSHOT: " + dataSnapshot.toString());
                                Log.e(LOG_TAG, "ITERATOR: " + dataSnapshot.getChildren().iterator().toString());
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
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
                Call<ImageSearchResponse> call = bingImg.buildCall();
                call.enqueue(new Callback<ImageSearchResponse>() {
                    @Override
                    public void onResponse(Call<ImageSearchResponse> call, Response<ImageSearchResponse> response) {
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
                    public void onFailure(Call<ImageSearchResponse> call, Throwable t) {
                        Log.e(LOG_TAG, call.toString());
                        txt.setText(t.getMessage());
                    }
                });
                }
            });


        }
        final Button btnTranslate = (Button) findViewById(R.id.buttonTranslate);
        if(btnTranslate != null) {
            btnTranslate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String text = ((EditText) findViewById(R.id.translateText)).getText().toString();
                    class background extends AsyncTask<Void, Void, Void> {

                        String translatedTxt = "";
                        @Override
                        protected Void doInBackground(Void... params) {
                            translatedTxt = BingTranslate.getTranslatedText(text);
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void result) {
                            ((TextView) findViewById(R.id.translatedText)).setText(translatedTxt);
                            super.onPostExecute(result);
                        }
                    }
                    new background().execute();
                }
            });
        }
    }
}
