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
