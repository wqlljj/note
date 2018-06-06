package com.cloudminds.hc.metalib.bean;

import java.util.Arrays;

public class UltrasonicData {

	private int seq;
	private float[] distances;

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public float[] getDistances() {
		return distances;
	}

	public void setDistances(float[] distances) {
		this.distances = distances;
	}

	@Override
	public String toString() {
		return "UltrasonicData [seq=" + seq + ", distances=" + Arrays.toString(distances) + "]";
	}


}
