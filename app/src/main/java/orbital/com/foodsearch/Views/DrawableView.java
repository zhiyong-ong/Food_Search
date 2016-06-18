package orbital.com.foodsearch.Views;

import android.animation.ObjectAnimator;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import orbital.com.foodsearch.Helpers.BingSearch;
import orbital.com.foodsearch.Models.BingImageSearch;
import orbital.com.foodsearch.Models.Line;
import orbital.com.foodsearch.R;
import retrofit2.Call;

/**
 * Extended ImageView to draw the bounding boxes. Overrides onDraw method and it has
 * a transparent background so as to overlay over another view.
 */
public class DrawableView extends ImageView implements View.OnTouchListener{
    private View mRootView = null;

    private List<Rect> mRects = null;
    private List<String> mLineTexts = null;
    private int selectedIndex = -1;

    private Bitmap mOriginalBitmap = null;
    private Matrix mMatrix = new Matrix();
    private Float mAngle = 0.f;

    private Paint greenPaint = null;
    private Paint redPaint = null;

    //query params for bing search
    private final String count = "10";
    private final String offset = "0";
    private final String markets = "en-us";
    private final String safeSearch = "Moderate";
    private static final String LOG_TAG = "FOODIES";
    private Context context = null;

    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mRects = new ArrayList<Rect>();
        mLineTexts = new ArrayList<String>();
        setupPaints();
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
     * @param rootView rootView holding  this view
     * @param imagePath image path for the compressed image file
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
        mOriginalBitmap = BitmapFactory.decodeFile(imagePath);
        addLinesForDraw(lines, lang);
        invalidate();
    }

    /**
     * This private method adds the drawables in the list by parsing the boundary
     * parameters and then scaling it and setting them as the drawables' bounds.
     * @param lines List of line to convert into drawables
     */
    private void addLinesForDraw(List<Line> lines, String lang) {
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
            String text = line.getText(lang);
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        for (int i = 0; i < mRects.size(); i++) {
            Rect rect = mRects.get(i);
            if (rect.contains(x, y)){
                selectRect(rect, i);
                break;
            }
        }
        return true;
    }

    private void selectRect(Rect rect, int i){
        String searchParam = mLineTexts.get(i);
        final String finalSearchParam = searchParam;
        Snackbar.make(this, searchParam, Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.CYAN)
                .setAction(R.string.search, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BingSearch bingImg  = new BingSearch(finalSearchParam, count, offset, markets, safeSearch);
                        Call<BingImageSearch> call = bingImg.getImage();
                        /*call.enqueue(new Callback<BingImageSearch>() {
                             @Override
                             public void onResponse(Call<BingImageSearch> call, Response<BingImageSearch> response) {
                                 //get arraylist to store the results
                                 final ArrayList<String[]> results = new ArrayList<>();
                                 Log.e(LOG_TAG, response.body().toString());

                                 for (int i = 0; i < Integer.parseInt(count); i++) {
                                     String[] name = {response.body().getImageValues().get(i).getContentUrl(),
                                             response.body().getImageValues().get(i).getName()};
                                     results.add(name);
                                 }

                                 Picasso.with(context)
                                         .load(results.get(0)[0])
                                         .into(imgView);
                                 Log.e(LOG_TAG, results.get(0)[0]);
                                 Log.e(LOG_TAG, results.get(0)[1]);
                                 txt.setText(results.get(0)[1]);
                             }
                            @Override
                            public void onFailure(Call<BingImageSearch> call, Throwable t) {
                                Log.e(LOG_TAG, call.toString());
                                txt.setText(t.getMessage());
                            }
                                     });
                                     */
                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, finalSearchParam);
                        getContext().startActivity(intent);
                        }
                })
                .show();
        selectedIndex = i;
        invalidate();
        RecyclerView recyclerView = (RecyclerView)mRootView.findViewById(R.id.recycler_view);
        ObjectAnimator anim = ObjectAnimator.ofFloat(recyclerView,
                View.TRANSLATION_Y, 0);
        anim.setDuration(1000);
        anim.start();
    }

    private void setupPaints() {
        greenPaint = new Paint();
        greenPaint.setStyle(Paint.Style.STROKE);
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStrokeWidth(3);
        redPaint = new Paint(greenPaint);
        redPaint.setColor(Color.RED);
    }
}
