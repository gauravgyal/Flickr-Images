package com.gauravgoyal.flickrsearch;

import com.gauravgoyal.flickrsearch.contract.ListContract;
import com.gauravgoyal.flickrsearch.contract.ListPresenter;
import com.gauravgoyal.flickrsearch.model.PhotoModel;
import com.gauravgoyal.flickrsearch.model.PhotoServiceResponse;
import com.gauravgoyal.flickrsearch.repo.PhotosRepo;
import com.gauravgoyal.flickrsearch.ui.fragments.BaseFragment;
import com.gauravgoyal.flickrsearch.util.utility.ExtKt;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

public class PresenterTestCase {


    private ListPresenter presenter;

    @Mock
    private ListPresenter mockedPresenter;

    @Mock
    private BaseFragment baseFragment;

    private PhotosRepo photosRepo;

    @Mock
    private ListContract.View view;

    @Mock
    private PhotosRepo.OnLoadListener loadListener;

    private PhotoServiceResponse response;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        presenter = new ListPresenter(view);
        photosRepo = new PhotosRepo("kitten",1, loadListener);
        response = new PhotoServiceResponse(new ArrayList<PhotoModel>(),1,10,100);
    }



    @Test
    public void fetchValidDataShouldLoadIntoView() {
        presenter.onLoadComplete(response);
        Mockito.verify(view, Mockito.times(1)).setLoadingIndicator(false);
        Mockito.verify(view, Mockito.times(1)).showList(response);
    }

    @Test
    public void fetchValidDataShouldLoadIntoErrorView() {
        presenter.onError();
        Mockito.verify(view, Mockito.times(1)).setLoadingIndicator(false);
        Mockito.verify(view, Mockito.times(1)).showErrorBox();
    }


    @Test
    public void testImageUrl() {
        PhotoModel photoModel = new PhotoModel("secret",1,"id", "server");
        String url = ExtKt.getImageUrl(photoModel);
        Assert.assertEquals(url,"http://farm1.static.flickr.com/server/id_secret.jpg");
    }

}
