package com.quintus.labs.smarthome.adapter;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.quintus.labs.smarthome.AwsIoTManager.AwsIoTManager;
import com.quintus.labs.smarthome.R;
import com.quintus.labs.smarthome.model.Room;
import com.quintus.labs.smarthome.ui.activity.RoomDetailsActivity;
import com.quintus.labs.smarthome.utils.SwitchButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.MyViewHolder> {
    AwsIoTManager awsIoTManager = AwsIoTManager.getInstance();
    private static final String topicToPublish = "$aws/things/ESP32_THING_4LED/shadow/update";
    Context context;
    private List<Room> roomList;
    public RoomAdapter(List<Room> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Room room = roomList.get(position);

        holder.title.setText(room.getName());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RoomDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putSerializable("roomDetails", room);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        holder.imageIcon.setImageResource(room.getResourceID());
        holder.switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(room.getName().equals("LivingRoom")){
                    int jsonValue = isChecked ? 1 : 0;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        JSONObject stateObject = new JSONObject();
                        JSONObject reportedObject = new JSONObject();
                        reportedObject.put("LivingRoomAutoMode", 0);
                        reportedObject.put("LivingRoomDoor", jsonValue);
                        reportedObject.put("LivingRoomFan", jsonValue);
                        reportedObject.put("LivingRoomLight", jsonValue);
                        stateObject.put("desired", reportedObject);
                        jsonObject.put("state", stateObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jsonString = jsonObject.toString();
                    awsIoTManager.sendMessageToAwsIot(topicToPublish,jsonString);
                }
                else if(room.getName().equals("Kitchen")){
                    int jsonValue = isChecked ? 1 : 0;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        JSONObject stateObject = new JSONObject();
                        JSONObject reportedObject = new JSONObject();
                        reportedObject.put("KitchenLight", jsonValue);
                        reportedObject.put("KitchenFan", jsonValue);
                        stateObject.put("desired", reportedObject);
                        jsonObject.put("state", stateObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jsonString = jsonObject.toString();
                    awsIoTManager.sendMessageToAwsIot(topicToPublish,jsonString);
                }
                else if(room.getName().equals("Bedroom")){
                    int jsonValue = isChecked ? 1 : 0;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        JSONObject stateObject = new JSONObject();
                        JSONObject reportedObject = new JSONObject();
                        reportedObject.put("BedroomFan", jsonValue);
                        reportedObject.put("BedroomLight", jsonValue);
                        stateObject.put("desired", reportedObject);
                        jsonObject.put("state", stateObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jsonString = jsonObject.toString();
                    awsIoTManager.sendMessageToAwsIot(topicToPublish,jsonString);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public CardView cardView;
        public ImageView imageIcon;
        public SwitchButton switchButton;
        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            cardView = view.findViewById(R.id.card_view);
            imageIcon = view.findViewById(R.id.image_view);
            switchButton = view.findViewById(R.id.switch_device_master);
        }
    }
}