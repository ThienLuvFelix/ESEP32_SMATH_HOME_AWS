package com.quintus.labs.smarthome.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.quintus.labs.smarthome.MyInterface.ISwitchChangedListener;
import com.quintus.labs.smarthome.R;
import com.quintus.labs.smarthome.model.Room;
import com.quintus.labs.smarthome.utils.SwitchButton;

import java.util.List;

public class SingleRoomAdapter extends RecyclerView.Adapter<SingleRoomAdapter.MyViewHolder> {
    private ISwitchChangedListener iSwitchChangedListener;
    Context context;
    private List<Room> roomList;
    Switch switchButton;
    public SingleRoomAdapter(List<Room> roomList, ISwitchChangedListener iSwitchChangedListener) {
        this.roomList = roomList;
        this.iSwitchChangedListener = iSwitchChangedListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_room_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Room room = roomList.get(position);
        holder.title.setText(room.getName());
        holder.switchButtonDevice.setChecked(room.getSwitchState());
        holder.iconDevice.setImageResource(room.getResourceID());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(holder.switchButtonDevice.isChecked()){
                    iSwitchChangedListener.onSwitchChangedItem(room, false);
                    //holder.switchButtonDevice.setChecked(false);
                }
                else{
                    iSwitchChangedListener.onSwitchChangedItem(room, true);
                    //holder.switchButtonDevice.setChecked(true);
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
        public SwitchButton switchButtonDevice;
        public ImageView iconDevice;
        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            cardView = view.findViewById(R.id.card_view);
            switchButtonDevice = view.findViewById(R.id.switch_device);
            iconDevice = view.findViewById(R.id.iconDevice);
        }
    }
}