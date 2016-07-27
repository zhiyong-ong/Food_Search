package orbital.com.foodsearch.models.ImageSearchPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abel on 6/15/2016.
 */

public class PivotSuggestion {
    private String pivot;
    private List<Suggestion> suggestions = new ArrayList<Suggestion>();

    /**
     *
     * @return
     *     The pivot
     */
    public String getPivot() {
        return pivot;
    }

    /**
     *
     * @param pivot
     *     The pivot
     */
    public void setPivot(String pivot) {
        this.pivot = pivot;
    }

    /**
     *
     * @return
     *     The suggestions
     */
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    /**
     *
     * @param suggestions
     *     The suggestions
     */
    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }
}
