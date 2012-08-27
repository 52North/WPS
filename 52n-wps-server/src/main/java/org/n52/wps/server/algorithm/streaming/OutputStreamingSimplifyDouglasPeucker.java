package org.n52.wps.server.algorithm.streaming;


public class OutputStreamingSimplifyDouglasPeucker extends AbstractVectorOutputStreamingAlgorithm {

	@Override
	public String getBaseAlgorithmName() {
		return "org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm";
	}

	@Override
	public String getInputStreamableIdentifier() {
		return "FEATURES";
	}

	@Override
	public String getOutputIdentifier() {
		return "SIMPLIFIED_FEATURES";
	}

}
