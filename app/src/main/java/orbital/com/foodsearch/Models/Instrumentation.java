package orbital.com.foodsearch.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhiyong on 13/6/2016.
 */

public class Instrumentation {

    @SerializedName("pageLoadPingUrl")
    @Expose
    private String pageLoadPingUrl;

    /**
     *
     * @return
     * The pageLoadPingUrl
     */
    public String getPageLoadPingUrl() {
        return pageLoadPingUrl;
    }

    /**
     *
     * @param pageLoadPingUrl
     * The pageLoadPingUrl
     */
    public void setPageLoadPingUrl(String pageLoadPingUrl) {
        this.pageLoadPingUrl = pageLoadPingUrl;
    }

}
