package com.cloudminds.hc.metalib.bean;

public class IoData {

	private int seq;
	private int io_status;

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int getIo_status() {
		return io_status;
	}

	public void setIo_status(int io_status) {
		this.io_status = io_status;
	}

	@Override
	public String toString() {
		return "IoData [seq=" + seq + ", io_status=" + io_status + "]";
	}

}
