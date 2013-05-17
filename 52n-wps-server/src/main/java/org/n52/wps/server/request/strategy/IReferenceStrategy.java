package org.n52.wps.server.request.strategy;


import org.n52.wps.server.ExceptionReport;

import net.opengis.wps.x100.InputType;

public interface IReferenceStrategy {
	public boolean isApplicable(InputType input);
	public ReferenceInputStream fetchData(InputType input) throws ExceptionReport;
}
