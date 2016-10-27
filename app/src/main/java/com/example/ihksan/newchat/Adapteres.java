package com.example.ihksan.newchat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by
 * Name         : Ihksan Sukmawan
 * Email        : iksandecade@gmail.com
 * Company      : Meridian.Id
 * Date         : 24/10/16
 * Project      : NewChat
 */

public class Adapteres extends RecyclerView.Adapter<Adapteres.Holder> {
    List<Model> modelList;
    Context context;
    String uId = "001";

    public Adapteres(List<Model> modelList, Context context) {
        this.modelList = modelList;
        this.context = context;
    }

    public static String getClock(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat df2 = new SimpleDateFormat("hh:mm");
        String dateText = df2.format(date);
        return dateText;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        String name = modelList.get(position).getName();
        String id = modelList.get(position).getUserId();
        String message = modelList.get(position).getMessage();
        Boolean image = modelList.get(position).getImage();
        Boolean audio = modelList.get(position).getAudio();
        long timestamp = modelList.get(position).getTimeStamp();
        if (audio) {
            holder.audio.setVisibility(View.VISIBLE);
            holder.message.setVisibility(View.GONE);
        } else if (image) {

        } else {
            holder.tvMessage.setText(message);
            holder.tvTimeStamp.setText(getClock(timestamp));
        }

        if (checkSender(id)) {
            holder.right.setVisibility(View.VISIBLE);
            holder.left.setVisibility(View.GONE);
            holder.container.setGravity(Gravity.RIGHT);
        } else {
            holder.right.setVisibility(View.GONE);
            holder.left.setVisibility(View.VISIBLE);
            holder.container.setGravity(Gravity.LEFT);
        }
    }

    boolean checkSender(String id) {
        boolean result = false;
        if (id.equals(uId)) {
            result = true;
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTimeStamp;
        FrameLayout left;
        FrameLayout right;
        RelativeLayout container;
        LinearLayout message;
        LinearLayout audio;

        public Holder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
            tvTimeStamp = (TextView) itemView.findViewById(R.id.tvTimeStamp);
            left = (FrameLayout) itemView.findViewById(R.id.left_arrow);
            right = (FrameLayout) itemView.findViewById(R.id.right_arrow);
            container = (RelativeLayout) itemView.findViewById(R.id.message_container);
            message = (LinearLayout) itemView.findViewById(R.id.message);
            audio = (LinearLayout) itemView.findViewById(R.id.audio);
        }
    }
}
