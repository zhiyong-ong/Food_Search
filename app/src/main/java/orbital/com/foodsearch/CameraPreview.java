package orbital.com.foodsearch;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.TextureView;

/**
 * Created by Abel on 5/9/2016.
 */
public class CameraPreview extends TextureView implements SurfaceHolder.Callback {


    public CameraPreview(Context context) {
        super(context);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
