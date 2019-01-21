package com.gauravgoyal.flickrsearch.contract;


import com.gauravgoyal.flickrsearch.model.PhotoServiceResponse;


public interface ListContract {

    interface View {

        void setLoadingIndicator(boolean active);

        void showList(PhotoServiceResponse response);

        void showErrorBox();

    }

    interface Presenter extends BaseContract.Presenter {

        void loadPhotos(String searchText, int page);

    }

}
