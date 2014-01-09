/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.server.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

	/**
	 * Create a RequestExecutor.
	 */
	public RequestExecutor() {
		super(MIN_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_SECONDS,
				TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(
						MAX_QUEUED_TASKS));
	}

}