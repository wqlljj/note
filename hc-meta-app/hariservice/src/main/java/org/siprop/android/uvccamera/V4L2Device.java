package org.siprop.android.uvccamera;

import android.util.Log;

public class V4L2Device {
	public String name;
    public int ID;
    
    V4L2Device(String name, int id){
        this.name = name; 
        this.ID = id; 
    }

}