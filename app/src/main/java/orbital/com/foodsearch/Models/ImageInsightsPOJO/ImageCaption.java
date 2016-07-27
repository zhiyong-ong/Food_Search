package orbital.com.foodsearch.models.ImageInsightsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class ImageCaption {

    private String caption;
    private String dataSourceUrl;
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
