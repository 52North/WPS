package org.n52.wps.server.algorithm.streaming;

import java.util.ArrayList;
import java.util.List;

public class OutputStreamingNDWI extends AbstractRasterOutputStreamingAlgorithm {

	@Override
	public String getBaseAlgorithmName() {
		return "org.n52.wps.server.algorithm.raster.ndwi";
	}

	@Override
	public List<String> getInputStreamableIdentifiers() {
		ArrayList<String> identifiers = new ArrayList<String>();
		identifiers.add("NIR");
		identifiers.add("SWIR");
		return identifiers;
	}

	@Override
	public String getOutputIdentifier() {
		return "NDWI";
	}

}
