package com.quintus.labs.smarthome.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quintus.labs.smarthome.AwsIoTManager.AwsIoTManager;
import com.quintus.labs.smarthome.MyInterface.ISwitchChangedListener;
import com.quintus.labs.smarthome.MyService.AWS_IoT_Service;
import com.quintus.labs.smarthome.R;
import com.quintus.labs.smarthome.ShareDataPreference.DataLocalManager;
import com.quintus.labs.smarthome.adapter.SingleRoomAdapter;
import com.quintus.labs.smarthome.model.Room;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class RoomDetailsActivity extends AppCompatActivity {
    private AwsIoTManager awsIoTManager = AwsIoTManager.getInstance();
    private List<Room> roomList = new ArrayList<>();
    private SingleRoomAdapter mAdapter;
    private static final String topicToPublish = "$aws/things/ESP32_THING_4LED/shadow/update";
    private static final String topicToGet = "$aws/things/ESP32_THING_4LED/shadow/get";
    private static final String topicToSubscribe = "$aws/things/ESP32_THING_4LED/shadow/get/accepted";
//    private static final String topicToSubscribe = "$aws/things/ESP32_THING_4LED/shadow/update/accepted";
    private static final String topicToSGetData = "$aws/things/ESP32_THING_4LED/shadow/get/accepted";
    private String roomId, nameOfRoom;
    private TextView textViewRoomName, textViewTemperature, textViewHumidity;
    private Thread myThread;
    private boolean isUpdate1, isUpdate2, isUpdate3, isUpdate4, isAutoMode;
    private volatile int timeCount = 6;
    private volatile boolean isRunning = true;

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {

        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        textViewRoomName = findViewById(R.id.textViewRoomName);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewHumidity = findViewById(R.id.textViewHumidity);
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            return;
        }

        Room roomRecv = (Room) bundle.get("roomDetails");
        nameOfRoom = roomRecv.getName();
        roomId   = roomRecv.getId();
        textViewRoomName.setText(nameOfRoom);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new SingleRoomAdapter(roomList, new ISwitchChangedListener() {
            @Override
            public void onSwitchChangedItem(Room room, boolean isChecked) {
                timeCount = 0;
                if((room.getName().equals("Fan") || room.getName().equals("Light")) && isAutoMode){
                    return;
                }
                if(room.getName().equals("AutoMode")){
                    isAutoMode = isChecked;
                }

                String jsonName = nameOfRoom + room.getName();
                int jsonValue = isChecked ? 1 : 0;
                JSONObject jsonObject = new JSONObject();
                try {
                    JSONObject stateObject = new JSONObject();
                    JSONObject reportedObject = new JSONObject();
                    reportedObject.put(jsonName, jsonValue);
                    stateObject.put("desired", reportedObject);
                    jsonObject.put("state", stateObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String jsonString = jsonObject.toString();
                Toast.makeText(RoomDetailsActivity.this, room.getName() + " is" +(isChecked ? " on" : " off") , Toast.LENGTH_LONG).show();
                awsIoTManager.sendMessageToAwsIot(topicToPublish,jsonString);
                Log.e("Msg", jsonString);
//                room.setSwitchState(isChecked);
//                mAdapter.notifyDataSetChanged();
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        prepareRoomData();
        Intent intent = new Intent(this, AWS_IoT_Service.class);
        startService(intent);

        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!myThread.interrupted()) {
                    try {
//                         Update UI here
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UpdateUi();

                            }
                        });

//                        Log.e("HI","" +timeCount);
//                        awsIoTManager.sendMessageToAwsIot(topicToGet, "");


                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        myThread.start();
    }


    private void prepareRoomData() {
        switch (roomId) {
            case "1":
                prepareLivingRoomData();
                break;
            case "2":
                prepareKitchenData();
                break;
            case "3":
                prepareBedRoomData();
                break;
        }
    }

    private void prepareLivingRoomData() {
        Room room = new Room("0", "AutoMode", false,R.drawable.auto);
        roomList.add(room);
        Room room1 = new Room("2", "Fan",false, R.drawable.fan);
        roomList.add(room1);
        Room room2 = new Room("3", "Light",false,R.drawable.light_bulb);
        roomList.add(room2);
        Room room3 = new Room("4", "Door",false,R.drawable.door);
        roomList.add(room3);
        mAdapter.notifyDataSetChanged();
    }

    private void prepareKitchenData() {
        Room room = new Room("0", "Light", false,R.drawable.light_bulb);
        roomList.add(room);
        room = new Room("1", "Fan",false,R.drawable.fan);
        roomList.add(room);
        mAdapter.notifyDataSetChanged();
    }

    private void prepareBedRoomData() {
        Room room = new Room("0", "Light", false,R.drawable.light_bulb);
        roomList.add(room);
        room = new Room("1", "Fan",false,R.drawable.fan);
        roomList.add(room);
        mAdapter.notifyDataSetChanged();
    }

    public void onBackClicked(View view) {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        myThread.interrupt();
        isRunning = false;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this.myThread != null){
            this.myThread.interrupt();
        }
    }



    private void UpdateUi(){
        try {
            timeCount++;
            awsIoTManager.sendMessageToAwsIot(topicToGet, "");
            String msg = DataLocalManager.getDataFromTopic(topicToSubscribe);
//            DataLocalManager.removeDataOfTopic(topicToSubscribe);
            Log.e("Receive",msg);
            if(msg.isEmpty()){
                return;
            }
//            if(timeCount == 8) {
            try {
                JSONObject jsonObject = new JSONObject(msg);
                JSONObject reportedObject = jsonObject.getJSONObject("state").getJSONObject("reported");
                if (nameOfRoom.equals("LivingRoom")) {
                    int autoModeState, fanState, lightState, doorState;
                    float temperature, humidity;

                    if(reportedObject.has("LivingRoomAutoMode")){
                        autoModeState = reportedObject.getInt("LivingRoomAutoMode");
                        roomList.get(0).setSwitchState(autoModeState == 1);
                    }
                    if(reportedObject.has("LivingRoomFan")){
                        fanState = reportedObject.getInt("LivingRoomFan");
                        roomList.get(1).setSwitchState(fanState == 1);
                    }
                    if(reportedObject.has("LivingRoomFan")){
                        lightState = reportedObject.getInt("LivingRoomLight");
                        roomList.get(2).setSwitchState(lightState == 1);
                    }
                    if(reportedObject.has("LivingRoomDoor")){
                        doorState = reportedObject.getInt("LivingRoomDoor");
                        roomList.get(3).setSwitchState(doorState == 1);
                    }
                    if(reportedObject.has("LivingRoomTemperature")){
                        temperature = reportedObject.getInt("LivingRoomTemperature");
                        textViewTemperature.setText(temperature + "\u2103");
                    }
                    if(reportedObject.has("LivingRoomHumidity")){
                        humidity = reportedObject.getInt("LivingRoomHumidity");
                        textViewHumidity.setText(humidity + "%");
                    }
                    mAdapter.notifyDataSetChanged();
                }
                else if (nameOfRoom.equals("Kitchen")) {
                    int fanState, lightState;
                    float temperature, humidity;


                    if(reportedObject.has("KitchenTemperature")){
                        temperature = reportedObject.getInt("KitchenTemperature");
                        textViewTemperature.setText(temperature + "\u2103" );
                    }
                    if(reportedObject.has("KitchenHumidity")){
                        humidity = reportedObject.getInt("KitchenHumidity");
                        textViewHumidity.setText(humidity + "%");
                    }
                    if(reportedObject.has("KitchenFan")) {
                        fanState = reportedObject.getInt("KitchenFan");
                        roomList.get(1).setSwitchState(fanState == 1);
                    }
                    if(reportedObject.has("KitchenLight")){
                        lightState = reportedObject.getInt("KitchenLight");
                        roomList.get(0).setSwitchState(lightState == 1);
                    }

                    mAdapter.notifyDataSetChanged();
                }
                else if (nameOfRoom.equals("Bedroom")) {
                    int fanState, lightState;
                    float temperature, humidity;

//                    if(reportedObject.has("BedroomTemperature")){
                        temperature = reportedObject.getInt("BedroomTemperature");
                        textViewTemperature.setText(temperature + "\u2103");
//                    }
//                    if(reportedObject.has("BedroomHumidity")){
                        humidity = reportedObject.getInt("BedroomHumidity");
                        textViewHumidity.setText(humidity + "%");
//                    }

//                    if(reportedObject.has("BedroomFan")){
                        fanState = reportedObject.getInt("BedroomFan");
                        roomList.get(1).setSwitchState(fanState == 1);
//                    }
//                    if(reportedObject.has("BedroomLight")) {
                        lightState = reportedObject.getInt("BedroomLight");
                        roomList.get(0).setSwitchState(lightState == 1);
//                    }

                    mAdapter.notifyDataSetChanged();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
//                timeCount = 0;
//            }
        }
        catch (Exception e){
            Log.e("e",""+e);
        }
    }

}


