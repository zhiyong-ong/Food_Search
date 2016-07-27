package orbital.com.foodsearch.models.ImageInsightsPOJO;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class Instrumentation {

    private String pingUrlBase;
    private String pageLoadPingUrl;

    /**
     *
     * @return
     * The pingUrlBase
     */
    public String getPingUrlBase() {
        return pingUrlBase;
    }

    /**
     *
     * @param pingUrlBase
     * The pingUrlBase
     */
    public void setPingUrlBase(String pingUrlBase) {
        this.pingUrlBase = pingUrlBase;
    }

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
