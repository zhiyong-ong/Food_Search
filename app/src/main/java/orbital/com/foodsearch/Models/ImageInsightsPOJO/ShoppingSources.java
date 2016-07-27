package orbital.com.foodsearch.models.ImageInsightsPOJO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhiyong on 27/6/2016.
 */

public class ShoppingSources {
    private List<Offer> offers = new ArrayList<Offer>();

    /**
     *
     * @return
     * The offers
     */
    public List<Offer> getOffers() {
        return offers;
    }

    /**
     *
     * @param offers
     * The offers
     */
    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }
}
