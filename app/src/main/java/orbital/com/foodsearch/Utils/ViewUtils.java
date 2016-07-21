package orbital.com.foodsearch.Utils;

import android.view.View;

import orbital.com.foodsearch.R;

import static orbital.com.foodsearch.Utils.AnimUtils.brightenOverlay;

/**
 * Created by Abel on 7/20/2016.
 */

public class ViewUtils {
    public static void startSearchProgress(View rootView) {
        AnimUtils.darkenOverlay(rootView.findViewById(R.id.drawable_overlay));
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.VISIBLE);
    }

    public static void endSearchProgress(View rootView) {
        AnimUtils.brightenOverlay(rootView.findViewById(R.id.drawable_overlay));
        rootView.findViewById(R.id.searchbar_progress).setVisibility(View.GONE);
    }

    public static void startOcrProgress(View rootView) {
        AnimUtils.darkenOverlay(rootView.findViewById(R.id.drawable_overlay));
        AnimUtils.fadeIn(rootView.findViewById(R.id.progress_bar), AnimUtils.PROGRESS_BAR_DURATION);
    }

    public static void finishOcrProgress(View rootView) {
        rootView.findViewById(R.id.drawable_overlay).setClickable(false);
        brightenOverlay(rootView.findViewById(R.id.drawable_overlay));
        AnimUtils.fadeOut(rootView.findViewById(R.id.progress_bar), AnimUtils.PROGRESS_BAR_DURATION);
    }
}
