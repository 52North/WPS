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

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import net.opengis.wps.x100.StatusType;

import org.n52.wps.server.request.ExecuteRequest;

/**
 * A wrapper around a {@link ExecuteRequest}. Task can be scheduled for
 * execution in the {@link RequestExecutor}. This task enables the executor to
 * get the {@link ExecuteRequest} and the {@link StatusType} of the
 * {@link org.n52.wps.server.responseExecuteResponse}. With this construction,
 * the status of the request can be altered during execution.
 * 
 * @author Timon ter Braak
 * 
 * @param <W>
 *            Should be of type Request.
 */
public class WPSTask<W> extends FutureTask<W> {

	private ExecuteRequest request;

	/**
	 * Create a new wrapper around an {@link ExecuteRequest}.
	 * 
	 * @param callable
	 *            An ExecuteRequest
	 */
	public WPSTask(Callable<W> callable) {
		super(callable);
		this.request = (ExecuteRequest) callable;
	}

	/**
	 * Get the Request of this wrapper.
	 * 
	 * @return The Request
	 */
	public synchronized ExecuteRequest getRequest() {
		return this.request;
	}
}
