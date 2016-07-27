package orbital.com.foodsearch.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;

import orbital.com.foodsearch.R;

/**
 * Created by Abel on 6/15/2016.
 */

public class ScrimTransformation implements Transformation {
    private Context mContext;
    private ImageView mImageview;

    public ScrimTransformation(Context Context, ImageView imageView) {
        mContext = Context;
        mImageview = imageView;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Drawable scrim;
        if (Build.VERSION.SDK_INT >= 21) {
            scrim = mContext.getResources().getDrawable(
                    R.drawable.scrim, null);
        } else {
            scrim = mContext.getResources().getDrawable(R.drawable.scrim);
        }
        scrim.setBounds(mImageview.getLeft(), mImageview.getTop(),
                mImageview.getRight(), mImageview.getBottom());
        Canvas canvas = new Canvas(source);
        scrim.draw(canvas);
        return source;
    }

    @Override
    public String key() {
        return "ScrimTransformation";
    }
}
