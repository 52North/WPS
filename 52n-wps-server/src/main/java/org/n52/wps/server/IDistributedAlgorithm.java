/*******************************************************************************
 * Copyright (C) 2008
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
 * 
 * Author: Bastian Baranski (Bastian.Baranski@uni-muenster.de)
 * Created: 03.09.2008
 * Modified: 03.09.2008
 *
 ******************************************************************************/

package org.n52.wps.server;

import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;

/**
 * @author bastian
 * 
 */
public interface IDistributedAlgorithm extends IAlgorithm
{
	/**
	 * @param pExecuteDocument
	 * @return
	 * @throws ExceptionReport
	 * @throws RuntimeException
	 */
	WebProcessingServiceOutput run(ExecuteDocument pExecuteDocument) throws ExceptionReport, RuntimeException;
	
	/**
	 * @param pInput
	 * @param pCount
	 * @return
	 */
	List<WebProcessingServiceInput> split(WebProcessingServiceInput pInput, int pMaximumNumberOfNodes);
	
	/**
	 * @param pOutput
	 * @return
	 */
	public WebProcessingServiceOutput merge(List<WebProcessingServiceOutput> pOutput);

	/**
	 * @author bastian
	 * 
	 */
	public class WebProcessingServiceInput
	{
		public Map layers;
		public Map parameters;

		/**
		 * @param pLayers
		 * @param pParameters
		 */
		public WebProcessingServiceInput(Map pLayers, Map pParameters)
		{
			layers = pLayers;
			parameters = pParameters;
		}

		/**
		 * @return
		 */
		public Map getLayers()
		{
			return layers;
		}

		/**
		 * @return
		 */
		public Map getParameters()
		{
			return parameters;
		}
	}

	/**
	 * @author bastian
	 * 
	 */
	public class WebProcessingServiceOutput
	{
		public Map data;

		/**
		 * @param pData
		 */
		public WebProcessingServiceOutput(Map pData)
		{
			data = pData;
		}

		/**
		 * @return
		 */
		public Map getData()
		{
			return data;
		}
	}
}
