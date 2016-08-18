package orbital.com.menusnap.Models.ImageInsightsPOJO;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class RelatedCollection {

    private String name;
    private String url;
    private String description;
    private String thumbnailUrl;
    private Creator creator;
    private String source;
    private int imagesCount;
    private int followersCount;

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

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
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
     * The creator
     */
    public Creator getCreator() {
        return creator;
    }

    /**
     *
     * @param creator
     * The creator
     */
    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    /**
     *
     * @return
     * The source
     */
    public String getSource() {
        return source;
    }

    /**
     *
     * @param source
     * The source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     *
     * @return
     * The imagesCount
     */
    public int getImagesCount() {
        return imagesCount;
    }

    /**
     *
     * @param imagesCount
     * The imagesCount
     */
    public void setImagesCount(int imagesCount) {
        this.imagesCount = imagesCount;
    }

    /**
     *
     * @return
     * The followersCount
     */
    public int getFollowersCount() {
        return followersCount;
    }

    /**
     *
     * @param followersCount
     * The followersCount
     */
    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }
}
