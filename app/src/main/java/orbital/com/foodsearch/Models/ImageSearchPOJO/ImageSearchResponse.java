package orbital.com.foodsearch.Models.ImageSearchPOJO;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhiyong on 11/6/2016.
 */

public class ImageSearchResponse {
    private String type;
    private Instrumentation instrumentation;
    private String readLink;
    private String webSearchUrl;
    private Integer totalEstimatedMatches;

    @SerializedName("value")
    private List<ImageValue> imageValues = new ArrayList<ImageValue>();

    private List<QueryExpansion> queryExpansions = new ArrayList<QueryExpansion>();
    private Integer nextOffsetAddCount;
    private List<PivotSuggestion> pivotSuggestions = new ArrayList<PivotSuggestion>();
    private Boolean displayShoppingSourcesBadges;
    private Boolean displayRecipeSourcesBadges;

    private String searchQuery;
    private String translatedQuery;
//    /**
//     *
//     * @return
//     *     The type
//     */
//    public String getType() {
//        return type;
//    }
//
//    /**
//     *
//     * @param type
//     *     The _type
//     */
//    public void setType(String type) {
//        this.type = type;
//    }
//
//    /**
//     *
//     * @return
//     *     The instrumentation
//     */
//    public Instrumentation getInstrumentation() {
//        return instrumentation;
//    }
//
//    /**
//     *
//     * @param instrumentation
//     *     The instrumentation
//     */
//    public void setInstrumentation(Instrumentation instrumentation) {
//        this.instrumentation = instrumentation;
//    }
//
//    /**
//     *
//     * @return
//     *     The readLink
//     */
//    public String getReadLink() {
//        return readLink;
//    }
//
//    /**
//     *
//     * @param readLink
//     *     The readLink
//     */
//    public void setReadLink(String readLink) {
//        this.readLink = readLink;
//    }
//
//    /**
//     *
//     * @return
//     *     The webSearchUrl
//     */
//    public String getWebSearchUrl() {
//        return webSearchUrl;
//    }
//
//    /**
//     *
//     * @param webSearchUrl
//     *     The webSearchUrl
//     */
//    public void setWebSearchUrl(String webSearchUrl) {
//        this.webSearchUrl = webSearchUrl;
//    }
//
//    /**
//     *
//     * @return
//     *     The totalEstimatedMatches
//     */
//    public Integer getTotalEstimatedMatches() {
//        return totalEstimatedMatches;
//    }
//
//    /**
//     *
//     * @param totalEstimatedMatches
//     *     The totalEstimatedMatches
//     */
//    public void setTotalEstimatedMatches(Integer totalEstimatedMatches) {
//        this.totalEstimatedMatches = totalEstimatedMatches;
//    }

    /**
     *
     * @return
     *     The imageValue
     */
    public List<ImageValue> getImageValues() {
        return imageValues;
    }

    /**
     *
     * @param imageValue
     *     The imageValue
     */
    public void setImageValues(List<ImageValue> imageValue) {
        this.imageValues = imageValue;
    }

//    /**
//     *
//     * @return
//     *     The queryExpansions
//     */
//    public List<QueryExpansion> getQueryExpansions() {
//        return queryExpansions;
//    }
//
//    /**
//     *
//     * @param queryExpansions
//     *     The queryExpansions
//     */
//    public void setQueryExpansions(List<QueryExpansion> queryExpansions) {
//        this.queryExpansions = queryExpansions;
//    }
//
//    /**
//     *
//     * @return
//     *     The nextOffsetAddCount
//     */
//    public Integer getNextOffsetAddCount() {
//        return nextOffsetAddCount;
//    }
//
//    /**
//     *
//     * @param nextOffsetAddCount
//     *     The nextOffsetAddCount
//     */
//    public void setNextOffsetAddCount(Integer nextOffsetAddCount) {
//        this.nextOffsetAddCount = nextOffsetAddCount;
//    }
//
//    /**
//     *
//     * @return
//     *     The pivotSuggestions
//     */
//    public List<PivotSuggestion> getPivotSuggestions() {
//        return pivotSuggestions;
//    }
//
//    /**
//     *
//     * @param pivotSuggestions
//     *     The pivotSuggestions
//     */
//    public void setPivotSuggestions(List<PivotSuggestion> pivotSuggestions) {
//        this.pivotSuggestions = pivotSuggestions;
//    }
//
//    /**
//     *
//     * @return
//     *     The displayShoppingSourcesBadges
//     */
//    public Boolean getDisplayShoppingSourcesBadges() {
//        return displayShoppingSourcesBadges;
//    }
//
//    /**
//     *
//     * @param displayShoppingSourcesBadges
//     *     The displayShoppingSourcesBadges
//     */
//    public void setDisplayShoppingSourcesBadges(Boolean displayShoppingSourcesBadges) {
//        this.displayShoppingSourcesBadges = displayShoppingSourcesBadges;
//    }
//
//    /**
//     *
//     * @return
//     *     The displayRecipeSourcesBadges
//     */
//    public Boolean getDisplayRecipeSourcesBadges() {
//        return displayRecipeSourcesBadges;
//    }
//
//    /**
//     *
//     * @param displayRecipeSourcesBadges
//     *     The displayRecipeSourcesBadges
//     */
//    public void setDisplayRecipeSourcesBadges(Boolean displayRecipeSourcesBadges) {
//        this.displayRecipeSourcesBadges = displayRecipeSourcesBadges;
//    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getTranslatedQuery() {
        return translatedQuery;
    }

    public void setTranslatedQuery(String translatedQuery) {
        this.translatedQuery = translatedQuery;
    }
}
