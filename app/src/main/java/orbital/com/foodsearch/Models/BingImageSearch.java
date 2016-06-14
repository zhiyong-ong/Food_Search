package orbital.com.foodsearch.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhiyong on 11/6/2016.
 */

public class BingImageSearch {
    @SerializedName("_type")
    @Expose
    private String type;
    @SerializedName("instrumentation")
    @Expose
    private Instrumentation instrumentation;
    @SerializedName("webSearchUrl")
    @Expose
    private String webSearchUrl;
    @SerializedName("totalEstimatedMatches")
    @Expose
    private Integer totalEstimatedMatches;
    @SerializedName("value")
    @Expose
    private List<Value> value = new ArrayList<Value>();
    @SerializedName("queryExpansions")
    @Expose
    private List<QueryExpansion> queryExpansions = new ArrayList<QueryExpansion>();

    /**
     *
     * @return
     * The type
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     * The _type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     * The instrumentation
     */
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    /**
     *
     * @param instrumentation
     * The instrumentation
     */
    public void setInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
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
     * The totalEstimatedMatches
     */
    public Integer getTotalEstimatedMatches() {
        return totalEstimatedMatches;
    }

    /**
     *
     * @param totalEstimatedMatches
     * The totalEstimatedMatches
     */
    public void setTotalEstimatedMatches(Integer totalEstimatedMatches) {
        this.totalEstimatedMatches = totalEstimatedMatches;
    }

    /**
     *
     * @return
     * The value
     */
    public List<Value> getValue() {
        return value;
    }

    /**
     *
     * @param value
     * The value
     */
    public void setValue(List<Value> value) {
        this.value = value;
    }

    /**
     *
     * @return
     * The queryExpansions
     */
    public List<QueryExpansion> getQueryExpansions() {
        return queryExpansions;
    }

    /**
     *
     * @param queryExpansions
     * The queryExpansions
     */
    public void setQueryExpansions(List<QueryExpansion> queryExpansions) {
        this.queryExpansions = queryExpansions;
    }

}
