package orbital.com.foodsearch.Models.ImageInsightsPOJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import orbital.com.foodsearch.Models.ImageSearchPOJO.Thumbnail;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class PagesIncluding {

    @SerializedName("imageId")
    @Expose
    private String imageId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("webSearchUrl")
    @Expose
    private String webSearchUrl;
    @SerializedName("webSearchUrlPingSuffix")
    @Expose
    private String webSearchUrlPingSuffix;
    @SerializedName("thumbnailUrl")
    @Expose
    private String thumbnailUrl;
    @SerializedName("contentUrl")
    @Expose
    private String contentUrl;
    @SerializedName("hostPageUrl")
    @Expose
    private String hostPageUrl;
    @SerializedName("hostPageUrlPingSuffix")
    @Expose
    private String hostPageUrlPingSuffix;
    @SerializedName("contentSize")
    @Expose
    private String contentSize;
    @SerializedName("encodingFormat")
    @Expose
    private String encodingFormat;
    @SerializedName("hostPageDisplayUrl")
    @Expose
    private String hostPageDisplayUrl;
    @SerializedName("width")
    @Expose
    private int width;
    @SerializedName("height")
    @Expose
    private int height;
    @SerializedName("thumbnail")
    @Expose
    private Thumbnail thumbnail;
    @SerializedName("imageInsightsToken")
    @Expose
    private String imageInsightsToken;

    /**
     *
     * @return
     * The imageId
     */
    public String getImageId() {
        return imageId;
    }

    /**
     *
     * @param imageId
     * The imageId
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
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

    /**
     *
     * @return
     * The thumbnailUrl
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     *
     * @param thumbnailUrl
     * The thumbnailUrl
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     *
     * @return
     * The contentUrl
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     *
     * @param contentUrl
     * The contentUrl
     */
    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    /**
     *
     * @return
     * The hostPageUrl
     */
    public String getHostPageUrl() {
        return hostPageUrl;
    }

    /**
     *
     * @param hostPageUrl
     * The hostPageUrl
     */
    public void setHostPageUrl(String hostPageUrl) {
        this.hostPageUrl = hostPageUrl;
    }

    /**
     *
     * @return
     * The hostPageUrlPingSuffix
     */
    public String getHostPageUrlPingSuffix() {
        return hostPageUrlPingSuffix;
    }

    /**
     *
     * @param hostPageUrlPingSuffix
     * The hostPageUrlPingSuffix
     */
    public void setHostPageUrlPingSuffix(String hostPageUrlPingSuffix) {
        this.hostPageUrlPingSuffix = hostPageUrlPingSuffix;
    }

    /**
     *
     * @return
     * The contentSize
     */
    public String getContentSize() {
        return contentSize;
    }

    /**
     *
     * @param contentSize
     * The contentSize
     */
    public void setContentSize(String contentSize) {
        this.contentSize = contentSize;
    }

    /**
     *
     * @return
     * The encodingFormat
     */
    public String getEncodingFormat() {
        return encodingFormat;
    }

    /**
     *
     * @param encodingFormat
     * The encodingFormat
     */
    public void setEncodingFormat(String encodingFormat) {
        this.encodingFormat = encodingFormat;
    }

    /**
     *
     * @return
     * The hostPageDisplayUrl
     */
    public String getHostPageDisplayUrl() {
        return hostPageDisplayUrl;
    }

    /**
     *
     * @param hostPageDisplayUrl
     * The hostPageDisplayUrl
     */
    public void setHostPageDisplayUrl(String hostPageDisplayUrl) {
        this.hostPageDisplayUrl = hostPageDisplayUrl;
    }

    /**
     *
     * @return
     * The width
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @param width
     * The width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     *
     * @return
     * The height
     */
    public int getHeight() {
        return height;
    }

    /**
     *
     * @param height
     * The height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     *
     * @return
     * The thumbnail
     */
    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    /**
     *
     * @param thumbnail
     * The thumbnail
     */
    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     *
     * @return
     * The imageInsightsToken
     */
    public String getImageInsightsToken() {
        return imageInsightsToken;
    }

    /**
     *
     * @param imageInsightsToken
     * The imageInsightsToken
     */
    public void setImageInsightsToken(String imageInsightsToken) {
        this.imageInsightsToken = imageInsightsToken;
    }
}
