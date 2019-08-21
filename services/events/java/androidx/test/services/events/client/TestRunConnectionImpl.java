package androidx.test.services.events.client;

import android.os.RemoteException;
import androidx.test.services.events.run.ITestRunEvent;
import androidx.test.services.events.run.TestRunEvent;

/** The Connection implementation of the new {@link ITestRunEvent} service. */
public class TestRunConnectionImpl extends ConnectionBase<ITestRunEvent>
    implements OrchestratorConnection, TestRunService {

  TestRunConnectionImpl(
      String serviceName,
      String servicePackage,
      ServiceFromBinder<ITestRunEvent> serviceFromBinder,
      TestEventClientConnectListener listener) {
    super(serviceName, servicePackage, serviceFromBinder, listener);
  }

  /** {@inheritDoc} */
  @Override
  public void send(TestRunEvent testRunEvent) throws RemoteException {
    service.send(testRunEvent);
  }
}
