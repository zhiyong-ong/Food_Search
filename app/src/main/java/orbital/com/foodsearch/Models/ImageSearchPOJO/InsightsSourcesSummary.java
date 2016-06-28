package orbital.com.foodsearch.Models.ImageSearchPOJO;

/**
 * Created by zhiyong on 13/6/2016.
 */

public class InsightsSourcesSummary {
    private Integer shoppingSourcesCount;
    private Integer recipeSourcesCount;

    /**
     *
     * @return
     *     The shoppingSourcesCount
     */
    public Integer getShoppingSourcesCount() {
        return shoppingSourcesCount;
    }

    /**
     *
     * @param shoppingSourcesCount
     *     The shoppingSourcesCount
     */
    public void setShoppingSourcesCount(Integer shoppingSourcesCount) {
        this.shoppingSourcesCount = shoppingSourcesCount;
    }

    /**
     *
     * @return
     *     The recipeSourcesCount
     */
    public Integer getRecipeSourcesCount() {
        return recipeSourcesCount;
    }

    /**
     *
     * @param recipeSourcesCount
     *     The recipeSourcesCount
     */
    public void setRecipeSourcesCount(Integer recipeSourcesCount) {
        this.recipeSourcesCount = recipeSourcesCount;
    }
}
