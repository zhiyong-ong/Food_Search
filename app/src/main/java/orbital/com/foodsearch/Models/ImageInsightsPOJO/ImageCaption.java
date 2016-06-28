package orbital.com.foodsearch.Models.ImageInsightsPOJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class ImageCaption {

    @SerializedName("caption")
    @Expose
    private String caption;
    @SerializedName("dataSourceUrl")
    @Expose
    private String dataSourceUrl;
    @SerializedName("relatedSearches")
    @Expose
    private List<RelatedSearch> relatedSearches = new ArrayList<RelatedSearch>();

    /**
     *
     * @return
     * The caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     *
     * @param caption
     * The caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     *
     * @return
     * The dataSourceUrl
     */
    public String getDataSourceUrl() {
        return dataSourceUrl;
    }

    /**
     *
     * @param dataSourceUrl
     * The dataSourceUrl
     */
    public void setDataSourceUrl(String dataSourceUrl) {
        this.dataSourceUrl = dataSourceUrl;
    }

    /**
     *
     * @return
     * The relatedSearches
     */
    public List<RelatedSearch> getRelatedSearches() {
        return relatedSearches;
    }

    /**
     *
     * @param relatedSearches
     * The relatedSearches
     */
    public void setRelatedSearches(List<RelatedSearch> relatedSearches) {
        this.relatedSearches = relatedSearches;
    }

}
