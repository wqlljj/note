package com.cloudminds.hc.metalib.bean;

public class DeviceInfo {

	private int	seq;
	private int 	devId;		/* uid */
	Version dev_ver;	/* dev firmware version */
	Version hw_ver;	/* hw version */

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int getDevId() {
		return devId;
	}

	public void setDevId(int dev_id) {
		this.devId = dev_id;
	}

	public Version getDevVersion() {
		return dev_ver;
	}
	public Version getHwVersion() {
		return hw_ver;
	}

	public void setDevVersion(Version version) {
		this.dev_ver = version;
	}

	public void setDevVersion(int major, int minor) {
		if(dev_ver==null){
			dev_ver = new Version();
			dev_ver.setMajor(major);
			dev_ver.setMinor(minor);
		}else{
			dev_ver.setMajor(major);
			dev_ver.setMinor(minor);
		}
	}

	public void setHwVersion(int major, int minor) {
		if(hw_ver==null){
			hw_ver = new Version();
			hw_ver.setMajor(major);
			hw_ver.setMinor(minor);
		}else{
			hw_ver.setMajor(major);
			hw_ver.setMinor(minor);
		}
	}

	@Override
	public String toString() {
		return "DeviceInfo [seq=" + seq + ", dev_id=" + devId +", dev version=" + dev_ver +", HW version=" + hw_ver + "]";
	}

}
