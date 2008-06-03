/***************************************************************
 This implementation provides a framework to publish processes to the
 web through the  OGC Web Processing Service interface. The framework 
 is extensible in terms of processes and data handlers. It is compliant 
 to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
 Theodor Foerster, ITC, Enschede, the Netherlands
 Carsten Priess, Institute for geoinformatics, University of
 Muenster, Germany
 Timon Ter Braak, University of Twente, the Netherlands


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.server.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.opengis.wps.x100.ProcessFailedType;
import net.opengis.wps.x100.StatusType;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;


/*
 * import org.apache.log4j.Logger; import
 * org.n52.wps.server.request.ExecuteRequest; import
 * org.n52.wps.server.response.structures.ProcessStartedType; import
 * org.n52.wps.server.response.structures.StatusType;
 */

/**
 * After the client Request is accepted, it should be executed. To prevent
 * resource-exhaustion, this ThreadPoolExecutor stores the Requests in a queue,
 * and handles only a couple of them at a time. To tune the performance one can
 * alter the parameters of this pool.
 * 
 * Proper pool size estimation: N = Number of processors WT = Average waiting
 * time of a task ST = Average service time of a task #Threads = N * (1 + WT/ST)
 * 
 * @author Timon ter Braak
 */
public class RequestExecutor extends ThreadPoolExecutor {

	public static final int MIN_POOL_SIZE = 10;
	public static final int MAX_POOL_SIZE = 20;
	// When the number of threads is greater than the MIN_POOL_SIZE, this is the maximum 
	// time that excess idle threads will wait for new tasks before terminating.
	public static final int KEEP_ALIVE_SECONDS = 1000;
	public static final int MAX_QUEUED_TASKS = 100;

	private static Logger LOGGER = Logger.getLogger(RequestExecutor.class);

	/**
	 * Create a RequestExecutor.
	 */
	public RequestExecutor() {
		super(MIN_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_SECONDS,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(
						MAX_QUEUED_TASKS));
	}

	/**
	 * Before Executing the Request this method is called.
	 * 
	 * @param t
	 *            The Thread which executes the Request.
	 * @param r
	 *            The Request.
	 * @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread,
	 *      java.lang.Runnable)
	 */
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		// set a lower priority
		t.setPriority(Thread.MIN_PRIORITY);
		LOGGER.debug("beforeExecute called");
		StatusType status = StatusType.Factory.newInstance();
		status.addNewProcessStarted().setPercentCompleted(33);
		((WPSTask<?>) r).getRequest().getExecuteResponseBuilder().setStatus(status);
	}

	/**
	 * After Execution of the Request this method is called.
	 * 
	 * @param r
	 *            The Request.
	 * @param t
	 *            The Exception that was thrown (if one exists, otherwise null).
	 * @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable,
	 *      java.lang.Throwable)
	 */
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		LOGGER.debug("afterExecute called");
		String errMsg = null;
		if (t != null) {
			errMsg = t.getMessage();
		} else {
			try {
				// do nothing with result here
				// only need to check for the status of the computation
				((WPSTask<?>) r).get();
				StatusType status = StatusType.Factory.newInstance();
				status.setProcessSucceeded("The service succesfully processed the request.");
				((WPSTask<?>) r).getRequest().getExecuteResponseBuilder().setStatus(status);
			} catch (CancellationException ce) {
				LOGGER.error("Task cancelled: " + ce.getMessage());
				errMsg = ce.getMessage();
			} catch (ExecutionException ee) {
				LOGGER.error("Task execution failed: " + ee.getMessage());
				errMsg = ee.getMessage();
			} catch (InterruptedException ie) {
				// Thread.currentThread().interrupt(); // ignore/reset
				LOGGER.error("Task interrupted: " + ie.getMessage());
				errMsg = ie.getMessage();
			}
		}

		if (errMsg != null) {
			ProcessFailedType statusFailed = ProcessFailedType.Factory.newInstance();
			ExceptionReport report = new ExceptionReport(
					"Unknown error during execution of the request: "
					+ errMsg,
			ExceptionReport.NO_APPLICABLE_CODE);
			statusFailed.setExceptionReport(report.getExceptionDocument().getExceptionReport());
			StatusType status = StatusType.Factory.newInstance();
			((WPSTask<?>) r).getRequest().getExecuteResponseBuilder().setStatus(status);
			// throw exception?
			//throw report;
			
		} else {
			// store to database?
//			try {
//				new ExecuteResponse(((WPSTask<?>) r).getRequest());
//			} catch (ExceptionReport e) {
//				LOGGER.error(e.getMessage());
//			}
		}

	}
}
