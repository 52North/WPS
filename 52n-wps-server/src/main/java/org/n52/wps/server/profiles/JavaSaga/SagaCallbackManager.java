package org.n52.wps.server.profiles.JavaSaga;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.AuthorizationFailedException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.monitoring.Callback;
import org.ogf.saga.monitoring.Metric;
import org.ogf.saga.monitoring.Monitorable;

public class SagaCallbackManager implements Callback {

	@Override
	public boolean cb(Monitorable monitorable, Metric metric, Context context)
			throws NotImplementedException, AuthorizationFailedException {
		try {
			String value = metric.getAttribute(Metric.VALUE);
			String name = metric.getAttribute(Metric.NAME);
			System.out.println("Callback called for metric " + name + ", value = "
					+ value);
		} catch (Throwable e) {
			System.err.println("error" + e);
			e.printStackTrace(System.err);
		}

		// Keep the callback.
		return true;
	}

}
