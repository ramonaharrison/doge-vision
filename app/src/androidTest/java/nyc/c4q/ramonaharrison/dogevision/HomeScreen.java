package nyc.c4q.ramonaharrison.dogevision;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Ramona Harrison
 * on 11/22/15.
 */

public class HomeScreen {

    HomeScreen()
    {
        Espresso.onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    public void clickCameraButton() {
        Espresso.onView(withId(R.id.fab)).perform(ViewActions.click());
    }

}
