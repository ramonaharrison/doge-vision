package nyc.c4q.ramonaharrison.dogevision;

import android.test.ActivityInstrumentationTestCase2;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public ApplicationTest() throws Exception {
        super(MainActivity.class);
        //setUp();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    private void waitForHomeScreen(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testShareButton() {
        waitForHomeScreen(5000);
        new HomeScreen().clickCameraButton();
    }


}