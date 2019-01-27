package com.gauravgoyal.flickrsearch.ui.activity


import androidx.test.espresso.DataInteraction
import androidx.test.espresso.ViewInteraction
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*

import com.gauravgoyal.flickrsearch.R

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.`is`

@LargeTest
@RunWith(AndroidJUnit4::class)
class ImageGridActivityTest3 {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(ImageGridActivity::class.java)

    @Test
    fun imageGridActivityTest3() {
        val appCompatImageView = onView(
allOf(withId(R.id.search_button), withContentDescription("Search"),
childAtPosition(
allOf(withId(R.id.search_bar),
childAtPosition(
withId(R.id.search),
0)),
1),
isDisplayed()))
        appCompatImageView.perform(click())
        
        val searchAutoComplete = onView(
allOf(withId(R.id.search_src_text),
childAtPosition(
allOf(withId(R.id.search_plate),
childAtPosition(
withId(R.id.search_edit_frame),
1)),
0),
isDisplayed()))
        searchAutoComplete.perform(replaceText("uber"), closeSoftKeyboard())
        
        val searchAutoComplete2 = onView(
allOf(withId(R.id.search_src_text), withText("uber"),
childAtPosition(
allOf(withId(R.id.search_plate),
childAtPosition(
withId(R.id.search_edit_frame),
1)),
0),
isDisplayed()))
        searchAutoComplete2.perform(pressImeActionButton())
        
        val imageView = onView(
allOf(withId(R.id.img),
childAtPosition(
allOf(withId(R.id.gridView),
childAtPosition(
IsInstanceOf.instanceOf(android.view.ViewGroup::class.java),
0)),
0),
isDisplayed()))
        imageView.check(matches(isDisplayed()))
        }
    
    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
    }
