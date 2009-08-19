package org.n52.wps.io.data.binding.literal;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import org.n52.wps.io.data.IData;

public class LiteralDateTimeBinding implements IData {
	private Date date;

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
	public Class<?> getSupportedClass() {
		return Date.class;
	}

}
