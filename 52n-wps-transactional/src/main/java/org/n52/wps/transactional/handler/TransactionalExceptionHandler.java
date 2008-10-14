package org.n52.wps.transactional.handler;

import java.io.OutputStream;
import java.io.PrintWriter;

public class TransactionalExceptionHandler {

	public static void handleException(PrintWriter writer, Exception exception) {
		writer.write("<Result>");
		writer.write(exception.getMessage());
		writer.write("</Result>");
		writer.flush();
		writer.close();
		
	}

}
