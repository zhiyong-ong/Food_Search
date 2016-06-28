package orbital.com.foodsearch.Models.ImageInsightsPOJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class Thumbnail_ {
    @SerializedName("url")
    @Expose
    private String url;

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
