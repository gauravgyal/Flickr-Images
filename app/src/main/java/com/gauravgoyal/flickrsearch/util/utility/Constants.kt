package com.gauravgoyal.flickrsearch.util.utility

import com.gauravgoyal.flickrsearch.BuildConfig




const val API_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key="+BuildConfig.FlickrApiKey+"&format=json&nojsoncallback=1&safe_search=1&text=%s&page=%d"


const val IMAGE_CACHE_DIR = "thumbs"

const val DEFAULT_SEARCH_KEY = "uber"

const val SEARCH_KEY = "searchkey"