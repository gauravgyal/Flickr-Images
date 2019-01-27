package com.gauravgoyal.flickrsearch.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.InstrumentationRegistry
import org.junit.*

class ImageGridViewModelTest{

    private lateinit var viewModel: ImageGridViewModel
    private lateinit var context: Context

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()



    @Before
    fun setUp() {
        context = InstrumentationRegistry.getTargetContext()
        viewModel = ImageGridViewModel()

    }

    @After
    fun clear() {

    }

    @Test
    @Throws(InterruptedException::class)
    fun testDefaultValues() {
       viewModel.getPhotoList("uber",1)

    }
}