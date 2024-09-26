package com.quintus.labs.smarthome.MyInterface;

import com.quintus.labs.smarthome.model.Room;

public interface ISwitchChangedListener {
    void onSwitchChangedItem(Room room, boolean isChecked);
}
