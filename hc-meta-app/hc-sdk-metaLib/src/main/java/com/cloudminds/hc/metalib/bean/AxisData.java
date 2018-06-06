package com.cloudminds.hc.metalib.bean;

public class AxisData {
	private int seq;
	private float acc_x;
	private float acc_y;
	private float acc_z;
	private float gyro_x;
	private float gyro_y;
	private float gyro_z;
	private float comp_x;
	private float comp_y;
	private float comp_z;

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public float getAcc_x() {
		return acc_x;
	}

	public void setAcc_x(float acc_x) {
		this.acc_x = acc_x;
	}

	public float getAcc_y() {
		return acc_y;
	}

	public void setAcc_y(float acc_y) {
		this.acc_y = acc_y;
	}

	public float getAcc_z() {
		return acc_z;
	}

	public void setAcc_z(float acc_z) {
		this.acc_z = acc_z;
	}

	public float getGyro_x() {
		return gyro_x;
	}

	public void setGyro_x(float gyro_x) {
		this.gyro_x = gyro_x;
	}

	public float getGyro_y() {
		return gyro_y;
	}

	public void setGyro_y(float gyro_y) {
		this.gyro_y = gyro_y;
	}

	public float getGyro_z() {
		return gyro_z;
	}

	public void setGyro_z(float gyro_z) {
		this.gyro_z = gyro_z;
	}

	public float getComp_x() {
		return comp_x;
	}

	public void setComp_x(float comp_x) {
		this.comp_x = comp_x;
	}

	public float getComp_y() {
		return comp_y;
	}

	public void setComp_y(float comp_y) {
		this.comp_y = comp_y;
	}

	public float getComp_z() {
		return comp_z;
	}

	public void setComp_z(float comp_z) {
		this.comp_z = comp_z;
	}

	@Override
	public String toString() {
		return "AxisData [seq=" + seq + ", acc_x=" + acc_x + ", acc_y=" + acc_y + ", acc_z=" + acc_z + ", gyro_x="
				+ gyro_x + ", gyro_y=" + gyro_y + ", gyro_z=" + gyro_z + ", comp_x=" + comp_x + ", comp_y=" + comp_y
				+ ", comp_z=" + comp_z + "]";
	}

}
