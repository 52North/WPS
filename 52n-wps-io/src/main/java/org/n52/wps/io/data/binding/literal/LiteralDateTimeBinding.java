package org.n52.wps.io.data.binding.literal;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import org.n52.wps.io.data.ILiteralData;

public class LiteralDateTimeBinding implements ILiteralData {
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
	public Class<?> getSupportedClass() {
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
