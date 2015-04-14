package com.crouzet.cavalec.heydude.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.model.Message;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Johan on 26/02/2015.
 */
public class ChatAdapter extends ArrayAdapter<Message> {

    private List<Message> list;
    private Context context;

    public ChatAdapter(Context context, List<Message> values) {
        super(context, R.layout.list_item_chat_dest, values);

        list = values;
        this.context = context;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (list.get(position).getAuthorName().equals(HeyDudeSessionVariables.name)) ? 0 : 1;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        int type = getItemViewType(position);

        // First let's verify the convertView is not null
        if (rowView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(type == 0 ? R.layout.list_item_chat_author : R.layout.list_item_chat_dest, parent, false);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.msg = (TextView) rowView.findViewById(R.id.list_item_chat_msg);
            viewHolder.date = (TextView) rowView.findViewById(R.id.list_item_chat_time);
            viewHolder.img = (ImageView) rowView.findViewById(R.id.list_item_chat_image);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Message m = list.get(position);

        holder.msg.setText(m.getMessage());
        holder.date.setText(formatDate(m.getDate()));
        Picasso.with(context).load(m.getImage()).resize(
                (int)context.getResources().getDimension(R.dimen.chat_picture_width),
                (int)context.getResources().getDimension(R.dimen.chat_picture_height)
        ).centerCrop().into(holder.img);

        return rowView;
    }

    private String formatDate(Date d) {
        Date now = new Date();
        String nowStr = new SimpleDateFormat("ddMMyy").format(now);
        String dStr = new SimpleDateFormat("ddMMyy").format(d);
        final int day = 24 * 3600 * 1000;

        if (nowStr.compareTo(dStr) == 0) {
            return new SimpleDateFormat("HH:mm").format(d);
        }
        if (new SimpleDateFormat("ddMMyy").format(new Date(now.getTime() - day)).compareTo(dStr) == 0) {
            return "Yesterday " + new SimpleDateFormat("HH:mm").format(d);
        }

        for (int i = 2; i < 7; i++) {
            if (new SimpleDateFormat("ddMMyy").format(new Date(now.getTime() - i * day)).compareTo(dStr) == 0) {
                String str = new SimpleDateFormat("E HH:mm").format(d);
                return str.substring(0, 1).toUpperCase() + str.substring(1);
            }
        }

        return new SimpleDateFormat("dd MMM HH:mm").format(d);
}

    static class ViewHolder {
        public TextView msg, date;
        public ImageView img;
    }
}
