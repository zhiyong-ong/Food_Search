package orbital.com.menusnap.Models.ImageSearchPOJO;

/**
 * Created by Abel on 6/15/2016.
 */

public class Suggestion {
    private String text;
    private String displayText;
    private String webSearchUrl;
    private String searchLink;
    private Thumbnail thumbnail;

    /**
     *
     * @return
     *     The text
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     *     The text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     *
     * @return
     *     The displayText
     */
    public String getDisplayText() {
        return displayText;
    }

    /**
     *
     * @param displayText
     *     The displayText
     */
    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    /**
     *
     * @return
     *     The webSearchUrl
     */
    public String getWebSearchUrl() {
        return webSearchUrl;
    }

    /**
     *
     * @param webSearchUrl
     *     The webSearchUrl
     */
    public void setWebSearchUrl(String webSearchUrl) {
        this.webSearchUrl = webSearchUrl;
    }

    /**
     *
     * @return
     *     The searchLink
     */
    public String getSearchLink() {
        return searchLink;
    }

    /**
     *
     * @param searchLink
     *     The searchLink
     */
    public void setSearchLink(String searchLink) {
        this.searchLink = searchLink;
    }

    /**
     *
     * @return
     *     The thumbnail
     */
    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    /**
     *
     * @param thumbnail
     *     The thumbnail
     */
    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

}
