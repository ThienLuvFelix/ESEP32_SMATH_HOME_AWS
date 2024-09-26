package com.quintus.labs.smarthome.model;

import java.io.Serializable;

/**
 * Smart Home
 * https://github.com/quintuslabs/SmartHome
 * Created on 27-OCT-2019.
 * Created by : Santosh Kumar Dash:- http://santoshdash.epizy.com
 */

public class Room implements Serializable {
    String id;
    String name;
    boolean switchState;
    int resourceID;

    public Room(String id, String name, boolean switchState, int resourceID) {
        this.id = id;
        this.name = name;
        this.switchState = switchState;
        this.resourceID = resourceID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getSwitchState() {
        return switchState;
    }

    public void setSwitchState(boolean switchState) {
        this.switchState = switchState;
    }

    public int getResourceID() {
        return resourceID;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }
}
