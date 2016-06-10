package orbital.com.foodsearch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Models.Line;

/**
 * TODO: document your custom view class.
 */
public class DrawableView extends ImageView {
    private List<ShapeDrawable> mDrawables = null;


    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawables = new ArrayList<ShapeDrawable>();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawables != null && !mDrawables.isEmpty()) {
            for (ShapeDrawable drawable : mDrawables) {
                drawable.draw(canvas);
                Log.d("FOODIES", "drawing");
            }
        }
    }

    public void setLines(List<Line> lines) {
        for (Line line : lines) {
            String[] bounds = line.getBoundsArray();
            int x = Integer.parseInt(bounds[0]);
            int y = Integer.parseInt(bounds[1]);
            int width = Integer.parseInt(bounds[2]);
            int height = Integer.parseInt(bounds[3]);
            ShapeDrawable drawable = new ShapeDrawable();
            drawable.setBounds(x, y, x + width, y + height);
            Paint paint = drawable.getPaint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(2);
            mDrawables.add(drawable);
            Log.e("FOODIES", "added: x: " + x + " y: " + y);
        }
    }
}
