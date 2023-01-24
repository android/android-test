package androidx.test.gradletests.runner;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;

/**
 * Simplest possible test
 */
public class SimpleTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("androidx.test.gradletests.runner", appContext.getPackageName());
    }
}
