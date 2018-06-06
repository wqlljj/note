package com.cloudminds.hc.metalib.bean;

public class Version {

	private int major;
	private int minor;

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	@Override
	public String toString() {
		return "Version [major=" + major + ", minor=" + minor + "]";
	}

}
