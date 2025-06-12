/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.test.espresso.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.base.IdlingResourceRegistry.IdleNotificationCallback;
import androidx.test.espresso.internal.inject.TargetContext;
import androidx.test.espresso.util.concurrent.ListeningExecutorService;
import androidx.test.espresso.util.concurrent.ThreadFactoryBuilder;
import androidx.test.internal.platform.ServiceLoaderWrapper;
import androidx.test.internal.platform.os.ControlledLooper;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.platform.tracing.Tracing;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitor;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Dagger module for creating the implementation classes within the base package.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
@Module(includes = {PlatformTestStorageModule.class})
public class BaseLayerModule {

  @Provides
  public ActivityLifecycleMonitor provideLifecycleMonitor() {
    return ActivityLifecycleMonitorRegistry.getInstance();
  }

  @Provides
  @TargetContext
  public Context provideTargetContext() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  @Provides
  @Singleton
  public Looper provideMainLooper() {
    return Looper.getMainLooper();
  }

  @Provides
  @Singleton
  @CompatAsyncTask
  public IdleNotifier<Runnable> provideCompatAsyncTaskMonitor(
      ThreadPoolExecutorExtractor extractor) {
    ThreadPoolExecutor compatThreadPool = extractor.getCompatAsyncTaskThreadPool();
    if (compatThreadPool != null) {
      return new AsyncTaskPoolMonitor(compatThreadPool).asIdleNotifier();
    } else {
      return new NoopRunnableIdleNotifier();
    }
  }

  @Provides
  @Singleton
  @MainThread
  public Executor provideMainThreadExecutor(Looper mainLooper) {
    final Handler handler = new Handler(mainLooper);
    return new Executor() {
      @Override
      public void execute(Runnable runnable) {
        handler.post(runnable);
      }
    };
  }

  @Provides
  public IdleNotifier<IdleNotificationCallback> provideDynamicNotifer(
      IdlingResourceRegistry dynamicRegistry) {
    // Since a dynamic notifier will be created for each Espresso interaction this is a good time
    // to sync the IdlingRegistry with IdlingResourceRegistry.
    dynamicRegistry.sync(
        IdlingRegistry.getInstance().getResources(), IdlingRegistry.getInstance().getLoopers());
    return dynamicRegistry.asIdleNotifier();
  }

  @Provides
  @Singleton
  @SdkAsyncTask
  public IdleNotifier<Runnable> provideSdkAsyncTaskMonitor(ThreadPoolExecutorExtractor extractor) {
    return new AsyncTaskPoolMonitor(extractor.getAsyncTaskThreadPool()).asIdleNotifier();
  }

  @Provides
  public ActiveRootLister provideActiveRootLister(RootsOracle rootsOracle) {
    return rootsOracle;
  }

  @Provides
  @Singleton
  public EventInjector provideEventInjector() {
    // Android uses input manager to inject events.
    // Instrumentation does not check if the event presses went through by checking the
    // boolean return value of injectInputEvent, which is why we created this class to better
    // handle lost/dropped press events. Instrumentation cannot be used as a fallback strategy,
    // since this will be executed on the main thread.
    return new EventInjector(new InputManagerEventInjectionStrategy());
  }

  /** Holder for AtomicReference<FailureHandler> which allows updating it at runtime. */
  @Singleton
  public static class FailureHandlerHolder {
    private final AtomicReference<FailureHandler> holder;

    @Inject
    public FailureHandlerHolder(@Default FailureHandler defaultHandler) {
      holder = new AtomicReference<FailureHandler>(defaultHandler);
    }

    public void update(FailureHandler handler) {
      holder.set(handler);
    }

    public FailureHandler get() {
      return holder.get();
    }
  }

  @Provides
  FailureHandler provideFailureHandler(FailureHandlerHolder holder) {
    return holder.get();
  }

  @Provides
  @Singleton
  public ListeningExecutorService provideRemoteExecutor() {
    return new ListeningExecutorService(
        new ThreadPoolExecutor(
            0 /*corePoolSize*/,
            5 /*maximumPoolSize*/,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactoryBuilder().setNameFormat("Espresso Remote #%d").build()));
  }

  @Provides
  @Default
  FailureHandler provideFailureHander(DefaultFailureHandler impl) {
    return impl;
  }

  @Provides
  DefaultFailureHandler provideDefaultFailureHander(@TargetContext Context context) {
    return new DefaultFailureHandler(context, true);
  }

  @Provides
  @Singleton
  public ControlledLooper provideControlledLooper() {
    // load a service loaded provided ControlledLooper if available, otherwise return a no-op
    return ServiceLoaderWrapper.loadSingleService(
        ControlledLooper.class, () -> ControlledLooper.NO_OP_CONTROLLED_LOOPER);
  }

  @Provides
  @Singleton
  Tracing providesTracing() {
    return Tracing.getInstance();
  }
}
