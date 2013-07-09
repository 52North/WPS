/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthes Rieke, 52North

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundations web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.ags.workspace;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.server.IServerContext;
import com.esri.arcgis.server.IServerObjectManager;
import com.esri.arcgis.server.ServerConnection;
import com.esri.arcgis.system.ServerInitializer;
import com.esri.arcgis.system.VarArray;

/**
 * Factory class for server contexts. This class caches available contexts
 * and initializes new contexts if needed.
 * 
 * @author matthes rieke
 *
 */
public class ServerContextFactory {
	
	private static final Object mutex = new Object();
	private static Logger LOGGER = LoggerFactory.getLogger(ServerContextFactory.class);
	private static List<LockedServerContext> contexts = new ArrayList<LockedServerContext>();

	
	private static IServerContext initializeContext() throws IOException {
		if (LOGGER.isInfoEnabled()) {
			if (System.getProperty("JINTEGRA_NATIVE_MODE") == null) {
				LOGGER.info("Running geoprocessor in DCOM Mode");
			} else {
				LOGGER.info("Running geoprocessor in Native Mode");
			} 
		}

		try {
			if (LOGGER.isInfoEnabled())
				LOGGER.info("Getting AGS connection object ...");
			ServerConnection connection = getAGSConnection();
			IServerObjectManager som = connection.getServerObjectManager();
			IServerContext context = som.createServerContext("", "");
			return context;
		}
		catch (AutomationException ae){
			LOGGER.error("Caught J-Integra AutomationException: " + ae.getMessage() + "\n");
			throw new IOException(ae);
		}

		catch (IOException e){
			LOGGER.error("Caught IOException: " + e.getMessage() + "\n");
			throw e;
		}
	}
	
	private static final ServerConnection getAGSConnection() throws IOException {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("initializing server ...");
		AGSPropertiesWrapper agsProps = AGSPropertiesWrapper.getInstance();
		ServerInitializer serverInitializer = new ServerInitializer();
		serverInitializer.initializeServer(agsProps.getDomain(), agsProps.getUser(), agsProps.getPass());
		
		ServerConnection connection = null;
		try {
			connection = new ServerConnection();
			connection.connect(agsProps.getIP());
			if (LOGGER.isInfoEnabled())
				LOGGER.info("server initialized!");

		} catch (UnknownHostException e) {
			LOGGER.error("UnknownHostException - Could not connect to AGS host " + agsProps.getDomain() + " with user " + agsProps.getUser());
			throw new IOException("Error connecting to ArcGIS Server.");
		} catch (IOException e) {
			LOGGER.error("IOException - Could not connect to AGS host " + agsProps.getDomain() + " with user " + agsProps.getUser());
			LOGGER.info("Please check firewall setup! - and maybe the folder permissions, too");
			throw new IOException("Error connecting to ArcGIS Server.");
		}

		return connection;
	}

	/**
	 * Returns a {@link LockedServerContext} object, either
	 * cached or newly created.
	 */
	public static LockedServerContext retrieveContext() throws IOException {
		LockedServerContext result = null;
		List<LockedServerContext> toBeRemoved = new ArrayList<LockedServerContext>();
		
		synchronized (mutex) {
			for (LockedServerContext lsc : contexts) {
				if (lsc.lockAndGet()) {
					if (!ensureLiveness(lsc)) {
						toBeRemoved.add(lsc);
						continue;
					}
					result = lsc;
					break;
				}
			}
			
			if (result == null) {
				result = new LockedServerContext(initializeContext());
				result.lockAndGet();
				contexts.add(result);
			}
			
			if (!toBeRemoved.isEmpty()) {
				contexts.removeAll(toBeRemoved);
			}
		}
		
		return result;
	}

	/**
	 * Checks if a context is still alive by
	 * trying to create a Arcobject instance.
	 */
	private static boolean ensureLiveness(LockedServerContext lsc) {
		try {
			Object object = lsc.getContext().createObject(VarArray.getClsid());
			return object != null && object instanceof VarArray;
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return the context and make it usable for other
	 * processes.
	 */
	public static void returnContext(LockedServerContext context) {
		context.releaseLock();
	}

	/**
	 * Release all cached contexts.
	 */
	public static void releaseAllCachedContexts() throws IOException {
		synchronized (mutex) {
			for (LockedServerContext lsc : contexts) {
				lsc.releaseContext();
			}
		}
	}
	
	/**
	 * Class for providing mutual exclusion access to an
	 * {@link IServerContext} object.
	 * 
	 * @author matthes rieke
	 *
	 */
	public static class LockedServerContext {
		
		IServerContext context;
		private boolean locked;
		
		public LockedServerContext(IServerContext c) {
			context = c;
		}
		
		public synchronized boolean lockAndGet() {
			if (this.locked) return false;
			
			this.locked = true;
			return true;
		}
		
		public synchronized void releaseLock() {
			this.locked = false;
		}

		public IServerContext getContext() {
			return context;
		}
		
		private void releaseContext() throws IOException {
			context.releaseContext();
		}
		
	}
}
