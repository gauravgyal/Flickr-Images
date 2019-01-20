package com.gauravgoyal.flickrsearch.model


import java.util.ArrayList

data class PhotoServiceResponse(var data: ArrayList<PhotoModel>, var currentPage: Int, var totalPages: Int, var itemsPerPage: Int)
