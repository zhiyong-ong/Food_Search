package orbital.com.foodsearch.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Models.Line;
import orbital.com.foodsearch.R;

/**
 * Extended ImageView to draw the bounding boxes. Overrides onDraw method and it has
 * a transparent background so as to overlay over another view.
 */
public class DrawableView extends ImageView implements View.OnTouchListener{
    private List<Rect> mRects = null;
    private List<String> mLineTexts = null;
    private View mRootView = null;
    private Bitmap mOriginalBitmap = null;
    private Float mAngle = 0.f;
    private Matrix mMatrix = new Matrix();

    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRects = new ArrayList<Rect>();
        mLineTexts = new ArrayList<String>();
    }

    /**
     * onDraw is called when view is created/refreshed or when invalidate is called.
     * We draw all the drawables in the list of drawables here.
     * Canvas is rotated to make for the angle. (Using centerX and centerY as pivot is
     * not 100% accurate but it looks close enough for now)
     * @param canvas
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRects != null && !mRects.isEmpty()) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(3);
            for (Rect rect : mRects) {
                canvas.save();
                canvas.rotate(mAngle, rect.centerX(), rect.centerY());
                canvas.drawRect(rect, paint);
                canvas.restore();
            }
        }
    }

    /**
     * Draws boxes on drawableView with
     * @param rootView rootView holding  this view
     * @param imagePath image path for the compressed image file
     * @param lines list of line to be drawn
     * @param angle textAngle as received from bing api
     */
    public void drawBoxes(View rootView, String imagePath, List<Line> lines, Float angle) {
        if (lines == null || lines.isEmpty()) {
            Snackbar.make(this, R.string.no_text_found, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (angle != null) {
            mAngle = angle;
        }
        if (mMatrix == null) {
            mMatrix = new Matrix();
        } else {
            mMatrix.reset();
        }
        mRootView = rootView;
        mOriginalBitmap = BitmapFactory.decodeFile(imagePath);
        addLinesForDraw(lines);
        invalidate();
    }

    /**
     * This private method adds the drawables in the list by parsing the boundary
     * parameters and then scaling it and setting them as the drawables' bounds.
     * @param lines List of line to convert into drawables
     */
    private void addLinesForDraw(List<Line> lines) {
        for (Line line : lines) {
            String[] bounds = line.getBoundsArray();
            int x = Integer.parseInt(bounds[0]);
            int y = Integer.parseInt(bounds[1]);
            int width = Integer.parseInt(bounds[2]);
            int height = Integer.parseInt(bounds[3]);
            // Scale using matrix. Rotation can still be improved using matrix transform
            Rect drawRect = new Rect();
            RectF rectF = new RectF(x, y, x + width, y + height);
            scaleRect(drawRect, rectF);
            mRects.add(drawRect);
            String text = line.getText();
            mLineTexts.add(text);
        }
    }

    /**
     * This method sets scale on matrix to findXScale and findYScale then
     * maps it to rectF which is then rounded into outputRect
     * @param outputRect The rect to be used as output for the scaling
     * @param rectF The input rectF to be mapped
     */
    private void scaleRect(Rect outputRect, RectF rectF) {
        mMatrix.setScale(findXScale(), findYScale());
        mMatrix.mapRect(rectF);
        rectF.round(outputRect);
    }

    private float findXScale() {
        DrawableView drawableView = (DrawableView) mRootView.findViewById(R.id.drawable_view);
        return (float)drawableView.getWidth()/
                (float)mOriginalBitmap.getWidth();
    }

    private float findYScale() {
        DrawableView drawableView = (DrawableView) mRootView.findViewById(R.id.drawable_view);
        return (float)drawableView.getHeight()/
                (float)mOriginalBitmap.getHeight();
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
        return (int) (scaleFactorY * (float)y);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        for (int i = 0; i < mRects.size(); i++) {
            Rect rect = mRects.get(i);
            if (rect.contains(x, y)){
                Snackbar.make(v, mLineTexts.get(i), Snackbar.LENGTH_LONG).show();
            }
        }
        return true;
    }
}
