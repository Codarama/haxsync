package org.codarama.haxsync.utilities.intents;

import android.app.Activity;

import junit.framework.Assert;

import org.codarama.haxsync.BuildConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class IntentUtilTest {

    @Test
    public void test() {
        Assert.assertTrue(true);
    }

}
