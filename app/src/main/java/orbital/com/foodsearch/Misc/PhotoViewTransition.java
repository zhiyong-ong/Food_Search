package orbital.com.foodsearch.Misc;

import android.annotation.TargetApi;
import android.os.Build;
import android.transition.ChangeImageTransform;
import android.transition.TransitionValues;
import android.view.View;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Abel on 7/23/2016.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PhotoViewTransition extends ChangeImageTransform {
    private static void updateAttacher(TransitionValues transitionValues) {
        View view = transitionValues.view;
        if (view instanceof PhotoView) {
            PhotoView imageView = (PhotoView) view;
            PhotoViewAttacher photoViewAttacher = (PhotoViewAttacher) imageView.getIPhotoViewImplementation();
            photoViewAttacher.update();
        }
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        updateAttacher(transitionValues);
        super.captureStartValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        updateAttacher(transitionValues);
        super.captureEndValues(transitionValues);
    }
}