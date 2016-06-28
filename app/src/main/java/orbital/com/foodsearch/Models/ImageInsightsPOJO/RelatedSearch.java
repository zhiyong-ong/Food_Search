package orbital.com.foodsearch.Models.ImageInsightsPOJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class RelatedSearch {
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("displayText")
    @Expose
    private String displayText;
    @SerializedName("webSearchUrl")
    @Expose
    private String webSearchUrl;
    @SerializedName("webSearchUrlPingSuffix")
    @Expose
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
