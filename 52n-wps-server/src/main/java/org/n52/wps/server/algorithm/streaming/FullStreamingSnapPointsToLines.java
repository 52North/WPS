package org.n52.wps.server.algorithm.streaming;

public class FullStreamingSnapPointsToLines extends AbstractVectorFullStreamingAlgorithm {

	@Override
	public String getBaseAlgorithmName() {
		return "org.n52.wps.server.algorithm.SnapPointsToLinesAlgorithm";
	}

	@Override
	public String getInputStreamableIdentifier() {
		return "Points";
	}

	@Override
	public String getOutputIdentifier() {
		return "result";
	}

	@Override
	public int getTimeSlot() {
		/* How often should the playlist be read if it is still open */
		return 1000; // In milliseconds
	}

}
