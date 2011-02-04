package org.n52.wps.server.request.strategy;

import java.io.InputStream;

import org.n52.wps.server.ExceptionReport;

import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputType;

public interface IReferenceStrategy {
	public boolean isApplicable(InputType input);
	public InputStream fetchData(InputType input) throws ExceptionReport;
}
