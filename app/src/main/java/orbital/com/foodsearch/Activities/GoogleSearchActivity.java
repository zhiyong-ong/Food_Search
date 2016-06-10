package orbital.com.foodsearch.Activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import orbital.com.foodsearch.R;

public class GoogleSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_search);

        Button btn = (Button) findViewById(R.id.searchButton);
        final EditText searchText = (EditText) findViewById(R.id.inputSearch);
        if(btn != null) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    assert searchText != null;
                    String keyword= searchText.getText().toString();
                    intent.putExtra(SearchManager.QUERY, keyword);
                    startActivity(intent);
                }
            });
        }

    }

}
