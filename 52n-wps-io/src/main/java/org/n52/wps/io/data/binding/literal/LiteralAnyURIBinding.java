package org.n52.wps.io.data.binding.literal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.n52.wps.io.data.ILiteralData;

public class LiteralAnyURIBinding implements ILiteralData {
	private transient URI uri;

	public LiteralAnyURIBinding(URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return uri;
	}

	@Override
	public URI getPayload() {
		return uri;
	}

	@Override
	public Class<?> getSupportedClass() {
		return URI.class;
	}

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(uri.toString());
	}

	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{
        try {
            uri = new URI((String) oos.readObject());
        } catch (URISyntaxException ex) { }
	}

}
