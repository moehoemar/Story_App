package com.moehoemar.storyapp.views

import android.util.Log
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.moehoemar.storyapp.R
import com.moehoemar.storyapp.utils.EspressoIdlingResource
import com.moehoemar.storyapp.views.story.StoryActivity
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LoginLogoutTest {
    private val validEmailExample = "compose@gmail.com"
    private val validPasswordExample = "compose123"
    private val invalidFormatEmailExample = "composeableajah"
    private val invalidFormatPasswordExample = "comp0se"
    private val wrongEmailExample = "composetesting@gmail.com"
    private val wrongPasswordExample = "composengetest"

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @Test
    fun testA_testFailedLoginEmptyInput() {
        onView(withId(R.id.login_button)).check(matches(not(isEnabled())))
        Log.d("LoginLogoutTest", "testA finished successfully.")
    }

    @Test
    fun testB_testFailedLoginWithInvalidFormat() {
        onView(withId(R.id.ed_login_email)).perform(typeText(invalidFormatEmailExample))
        closeSoftKeyboard()

        onView(withId(R.id.ed_login_password)).perform(typeText(invalidFormatPasswordExample))
        closeSoftKeyboard()

        onView(withId(R.id.login_button)).check(matches(not(isEnabled())))
        onView(withId(R.id.login_button)).perform(click())
    }

    @Test
    fun testC_testFailedLoginWithWrongCredentials() {
        onView(withId(R.id.ed_login_email)).perform(typeText(wrongEmailExample))
        closeSoftKeyboard()

        onView(withId(R.id.ed_login_password)).perform(typeText(wrongPasswordExample))
        closeSoftKeyboard()

        onView(withId(R.id.login_button)).perform(click())

        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
        onView(withId(R.id.login_button)).check(matches(isDisplayed()))
    }

    @Test
    fun testE_testLoginLogout() {
        onView(withId(R.id.ed_login_email)).perform(typeText(validEmailExample))
        closeSoftKeyboard()

        onView(withId(R.id.ed_login_password)).perform(typeText(validPasswordExample))
        closeSoftKeyboard()

        onView(withId(R.id.login_button)).perform(click())

        Intents.intended(hasComponent(StoryActivity::class.java.name))

        onView(withId(R.id.action_logout)).perform(click())
        onView(withText(R.string.logout)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.yes)).inRoot(isDialog()).perform(click())

        Intents.intended(hasComponent(MainActivity::class.java.name))
    }
    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }
}