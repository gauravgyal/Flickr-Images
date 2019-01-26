package roboelectric;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import com.gauravgoyal.flickrsearch.BuildConfig;
import com.gauravgoyal.flickrsearch.R;
import com.gauravgoyal.flickrsearch.model.PhotoModel;
import com.gauravgoyal.flickrsearch.ui.fragments.ImageGridFragment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import java.util.ArrayList;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ImageGridFragmentTest {

    @Test
    public void aTest() {
        ImageGridFragment fragment = new ImageGridFragment();

        // Add it to a blank activity
        SupportFragmentTestUtil.startFragment(fragment);
        RecyclerView view = fragment.getView().findViewById(R.id.gridView);
        int visiblity = fragment.getView().findViewById(R.id.gridView).getVisibility();

        Assert.assertEquals(visiblity, View.VISIBLE);

        ArrayList<PhotoModel> photoModels = new ArrayList<>();
        photoModels.add(new PhotoModel("s1", 1, "f1", "se1"));
        photoModels.add(new PhotoModel("s1", 1, "f1", "se1"));
        photoModels.add(new PhotoModel("s1", 1, "f1", "se1"));


        fragment.loadPhotos(photoModels);

        Assert.assertEquals(view.getAdapter().getItemCount(), photoModels.size());
    }

}

