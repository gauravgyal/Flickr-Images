package com.gauravgoyal.flickrsearch.contract;

public interface BaseContract {

    interface Presenter {

        void create();

        void startSearch(String searchText, int page);

        void start();

        void stop();

        void destroy();

    }
}
