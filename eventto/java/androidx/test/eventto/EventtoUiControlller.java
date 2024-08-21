package androidx.test.eventto;

import androidx.test.espresso.UiController;
import androidx.test.espresso.InjectEventSecurityException;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.os.Handler;
import static android.os.Looper.getMainLooper;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import android.util.Log;
import java.util.concurrent.atomic.AtomicBoolean;
import androidx.test.espresso.base.Interrogator;
import androidx.test.espresso.base.EventInjector;

class EventtoUiController {
//        implements UiController {
//   private final Handler mainHandler;
//   private final QueueIdler queueIdler;
//   private final ExecutorService keyEventExecutor =
//           Executors.newSingleThreadExecutor(
//                   new ThreadFactoryBuilder().setNameFormat("Eventto Key Event #%d").build());
//   private final EventInjector eventInjector;
//
//   EventtoUiController() {
//      this.mainHandler = new Handler(getMainLooper());
//      this.queueIdler = new QueueIdler();
//      this.eventInjector = new EventInjector(new InputManagerEventInjectionStrategy());
//   }
//
//   @Override
//   public boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
//      Log.i("EventtoUiController", "injectMotionEvent using ui automation");
//
//      getInstrumentation().getUiAutomation().injectInputEvent(event, false);
//      return true;
//   }
//
//   @Override
//   public boolean injectKeyEvent(KeyEvent event) {
//      throw new UnsupportedOperationException();
//   }
//
//   @Override
//   public boolean injectString(String str) throws InjectEventSecurityException {
//      throw new UnsupportedOperationException();
//   }
//
//   @Override
//   public void loopMainThreadUntilIdle() {
//      queueIdler.idle();
//   }
//
//   @Override
//   public void loopMainThreadForAtLeast(long millisDelay) {
//      AtomicBoolean elapsedTimeReached = new AtomicBoolean(false);
//      mainHandler.postDelayed(() -> elapsedTimeReached.set(true), millisDelay);
//      queueIdler.idleUntil(() -> elapsedTimeReached.get()) ;
//      loopMainThreadUntilIdle();
//   }
}
