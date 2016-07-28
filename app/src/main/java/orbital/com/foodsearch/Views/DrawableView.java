package orbital.com.foodsearch.Views;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Models.OcrPOJO.Line;
import orbital.com.foodsearch.R;

/**
 * Extended ImageView to draw the bounding boxes. Overrides onDraw method and it has
 * a transparent background so as to overlay over another view.
 */
public class DrawableView extends FrameLayout {
    private View mRootView = null;

    private List<Rect> mRects = null;
    private List<String> mLineTexts = null;

    private int selectedIndex = -1;

    private int originalWidth = 0;
    private int originalHeight = 0;
    private Matrix mMatrix = new Matrix();
    private Float mAngle = 0.f;

    private Paint greenPaint = null;
    private Paint redPaint = null;

    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRects = new ArrayList<>();
        mLineTexts = new ArrayList<>();
        setupPaints(context);
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
        // To draw all the rects
        if (mRects != null && !mRects.isEmpty()) {
            canvas.drawColor(Color.TRANSPARENT);
            for (int i = 0; i < mRects.size(); i++) {
                Rect rect = mRects.get(i);
                canvas.save();
                canvas.rotate(mAngle, rect.centerX(), rect.centerY());
                if (i == selectedIndex) {
                    canvas.drawRect(rect, redPaint);
                } else {
                    canvas.drawRect(rect, greenPaint);
                }
                canvas.restore();
            }
        }
    }

    /**
     * Draws boxes on drawableView with
     * @param rootView rootView holding this view
     * @param imagePath Image path for the compressed image file
     * @param lines list of line to be drawn
     * @param angle textAngle as received from bing api
     */
    public void drawBoxes(View rootView, String imagePath, List<Line> lines,
                          Float angle, String lang) {
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
        getOriginalDimen(imagePath);
        addLinesForDraw(lines, lang);
        invalidate();
    }

    public void chooseRect(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        this.invalidate();
    }

    /**
     * This private method adds the drawables in the list by parsing the boundary
     * parameters and then scaling it and setting them as the drawables' bounds.
     * @param lines List of line to convert into drawables
     */
    private void addLinesForDraw(List<Line> lines, String lang) {
        mLineTexts.clear();
        mRects.clear();
        for (Line line : lines) {
            String text = line.getText(lang);
            if (text.trim().isEmpty()) {
                continue;
            }
            mLineTexts.add(text);
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
        }
    }

    private void getOriginalDimen(String imagePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        originalHeight = options.outHeight;
        originalWidth = options.outWidth;
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
                (float)originalWidth;
    }

    private float findYScale() {
        DrawableView drawableView = (DrawableView) mRootView.findViewById(R.id.drawable_view);
        return (float)drawableView.getHeight()/
                (float)originalHeight;
    }

    private void setupPaints(Context context) {
        greenPaint = new Paint();
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setColor(ContextCompat.getColor(context, R.color.basePaintColor));
        greenPaint.setStrokeWidth(4);
        redPaint = new Paint(greenPaint);
        redPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
    }

    public void selectIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public List<Rect> getmRects() {
        return mRects;
    }

    public List<String> getmLineTexts() {
        return mLineTexts;
    }
}
