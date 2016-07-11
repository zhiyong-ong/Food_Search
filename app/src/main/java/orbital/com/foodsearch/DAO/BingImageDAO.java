package orbital.com.foodsearch.DAO;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import orbital.com.foodsearch.Models.ImageInsightsPOJO.ImageInsightsResponse;
import orbital.com.foodsearch.Models.ImageSearchPOJO.ImageSearchResponse;

/**
 * Created by zhiyong on 10/7/2016.
 * Data Access Object for ONLY the Bing Image Search Response.
 */

public class BingImageDAO {

    private DatabaseReference database;

    public BingImageDAO() {
        database = FirebaseDatabase.getInstance().getReference();
    }

    public void persistImage(ImageSearchResponse img) {
        database.child("images").child(img.getSearchQuery()).setValue(img);
    }

    public void persistImageInsight(ImageInsightsResponse imgInsights) {
        database.child("imageinsights").child(imgInsights.getImageInsightsToken()).setValue(imgInsights);
    }

}
