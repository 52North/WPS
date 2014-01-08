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
package org.n52.wps.io.data.binding.literal;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

public class LiteralDateTimeBinding extends AbstractLiteralDataBinding {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4336688658437832346L;
	private transient Date date;

	public LiteralDateTimeBinding(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public Time getTime() {
		return new Time(date.getTime());
	}

	public Timestamp getTimestamp() {
		return new Timestamp(date.getTime());
	}

	@Override
	public Date getPayload() {
		return date;
	}

	@Override
	public Class<Date> getSupportedClass() {
		return Date.class;
	}
	
	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(new Long(date.getTime()).toString());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
		date = new Date( ((Long) oos.readObject()).longValue() );
	}

}
