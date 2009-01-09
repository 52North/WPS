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

package org.n52.wps.grid.util;

import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;

/**
 * @author bastian
 * 
 */
public class DistributedUtlilities
{

	/**
	 * @param pFeatureCollection
	 * @param pCount
	 * @return
	 * @throws Exception
	 */
	public static FeatureCollection[] splitFeatureCollection(FeatureCollection pFeatureCollection, int pNumberOfChucks)
	{
		Object[] pFeatureArray = pFeatureCollection.toArray();
		FeatureCollection[] result = new FeatureCollection[pNumberOfChucks];
		int chunkSize = (int) Math.floor((double) pFeatureArray.length / (double) pNumberOfChucks);
		int currentFeatureCollection = -1;
		for (int i = 0; i < pFeatureArray.length; i++)
		{
			if (i % chunkSize == 0 && currentFeatureCollection < (pNumberOfChucks - 1))
			{
				currentFeatureCollection++;
				result[currentFeatureCollection] = DefaultFeatureCollections.newCollection();
			}
			result[currentFeatureCollection].add(pFeatureArray[i]);
		}
		return result;
	}
}
