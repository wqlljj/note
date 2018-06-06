package com.cloudminds.hc.metalib.bean;

public class BatteryData {

	public enum battery_status{
		//start with 0
		NOT_IN_CHARGING,IN_CHARGING,IN_FAST_CHARGING
	}
	
	
	private int seq;
	private float health;//电池老化程度 60%
	private float capacity;//residue_capacity / capacity 表示剩余百分比 10%
	private float residue_capacity;
	private float voltage;
	private float current;
	private int  status;//0不充电，1普通充电，2快速充电
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public float getCapacity() {
		return capacity;
	}
	public void setCapacity(float capacity) {
		this.capacity = capacity;
	}
	public float getResidue_capacity() {
		return residue_capacity;
	}
	public void setResidue_capacity(float residue_capacity) {
		this.residue_capacity = residue_capacity;
	}
	public float getVoltage() {
		return voltage;
	}
	public void setVoltage(float voltage) {
		this.voltage = voltage;
	}
	public float getCurrent() {
		return current;
	}
	public void setCurrent(float current) {
		this.current = current;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	public float getHealth() {
		return health;
	}

	public void setHealth(float health) {
		this.health = health;
	}

	@Override
	public String toString() {
		return "BatteryData [seq=" + seq + ", health=" + health + ", capacity=" + capacity + ", residue_capacity=" + residue_capacity
				+ ", voltage=" + voltage + ", current=" + current + ", status=" + status + "]";
	}
	
}
