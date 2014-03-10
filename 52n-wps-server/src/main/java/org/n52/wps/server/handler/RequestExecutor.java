/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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