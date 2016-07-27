package orbital.com.foodsearch.models.ImageInsightsPOJO;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class BestRepresentativeQuery {

    private String text;
    private String displayText;
    private String webSearchUrl;
    private String webSearchUrlPingSuffix;

    /**
     *
     * @return
     * The text
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     * The text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     *
     * @return
     * The displayText
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     *
     * @param displayText
     * The displayText
     */
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    /**
     *
     * @return
     * The webSearchUrl
     */
    public String getWebSearchUrl() {
        return webSearchUrl;
    }

    /**
     *
     * @param webSearchUrl
     * The webSearchUrl
     */
    public void setWebSearchUrl(String webSearchUrl) {
        this.webSearchUrl = webSearchUrl;
    }

    /**
     *
     * @return
     * The webSearchUrlPingSuffix
     */
    public String getWebSearchUrlPingSuffix() {
        return webSearchUrlPingSuffix;
    }

    /**
     *
     * @param webSearchUrlPingSuffix
     * The webSearchUrlPingSuffix
     */
    public void setWebSearchUrlPingSuffix(String webSearchUrlPingSuffix) {
        this.webSearchUrlPingSuffix = webSearchUrlPingSuffix;
    }

}
