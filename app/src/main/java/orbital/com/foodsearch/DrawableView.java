package orbital.com.foodsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Models.Line;

/**
 * Extended ImageView to draw the bounding boxes. Overrides onDraw method and it has
 * a transparent background so as to overlay over another view.
 */
public class DrawableView extends ImageView {
    private List<ShapeDrawable> mDrawables = null;
    private View mRootView = null;
    private Bitmap mOriginalBitmap = null;
    private float mAngle;

    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDrawables = new ArrayList<ShapeDrawable>();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawables != null && !mDrawables.isEmpty()) {
            for (ShapeDrawable drawable : mDrawables) {
                canvas.save();
                Rect rect = drawable.getBounds();
                canvas.rotate(mAngle, rect.centerX(), rect.centerY());
                drawable.draw(canvas);
                canvas.restore();
            }
        }
    }

    public void setLinesForDraw(View rootView, String imagePath, List<Line> lines, float angle) {
        mAngle = angle;
        mRootView = rootView;
        mOriginalBitmap = BitmapFactory.decodeFile(imagePath);
        for (Line line : lines) {
            String[] bounds = line.getBoundsArray();
            int x = Integer.parseInt(bounds[0]);
            int y = Integer.parseInt(bounds[1]);
            int width = Integer.parseInt(bounds[2]);
            int height = Integer.parseInt(bounds[3]);

            ShapeDrawable drawable = new ShapeDrawable();

            drawable.setBounds(scaleX(x), scaleY(y),
                    scaleX(x + width), scaleY(y + height));
            Paint paint = drawable.getPaint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(2);
            mDrawables.add(drawable);
        }
    }

    private int scaleX(int x) {
        DrawableView drawableView = (DrawableView) mRootView.findViewById(R.id.drawable_view);
        float scaleFactorX = (float)drawableView.getWidth()
                / (float)mOriginalBitmap.getWidth();
        return (int) (scaleFactorX * (float)x);
    }

    private int scaleY(int y) {
        DrawableView drawableView = (DrawableView) mRootView.findViewById(R.id.drawable_view);
        float scaleFactorY = (float)drawableView.getHeight()
                / (float) mOriginalBitmap.getHeight();
        Log.e("FOODIES", String.valueOf(scaleFactorY));
        return (int) (scaleFactorY * (float)y);
    }
}
