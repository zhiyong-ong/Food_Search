package orbital.com.foodsearch.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by zhiyong on 13/6/2016.
 */

public class QueryExpansion {
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("displayText")
    @Expose
    private String displayText;
    @SerializedName("webSearchUrl")
    @Expose
    private String webSearchUrl;
    @SerializedName("searchLink")
    @Expose
    private String searchLink;
    @SerializedName("thumbnail")
    @Expose
    private Thumbnail_ thumbnail;

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
     * The searchLink
     */
    public String getSearchLink() {
        return searchLink;
    }

    /**
     *
     * @param searchLink
     * The searchLink
     */
    public void setSearchLink(String searchLink) {
        this.searchLink = searchLink;
    }

    /**
     *
     * @return
     * The thumbnail
     */
    public Thumbnail_ getThumbnail() {
        return thumbnail;
    }

    /**
     *
     * @param thumbnail
     * The thumbnail
     */
    public void setThumbnail(Thumbnail_ thumbnail) {
        this.thumbnail = thumbnail;
    }
}
