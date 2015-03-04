package com.crouzet.cavalec.heydude.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.model.Message;

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
        super(context, R.layout.list_item_chat, values);

        list = values;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // First let's verify the convertView is not null
        if (rowView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_chat, parent, false);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) rowView.findViewById(R.id.list_item_chat_name);
            viewHolder.msg = (TextView) rowView.findViewById(R.id.list_item_chat_msg);
            viewHolder.date = (TextView) rowView.findViewById(R.id.list_item_chat_time);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Message m = list.get(position);

        holder.msg.setText(m.getMsg());
        holder.name.setText(m.getAuthorName() + ":");
        holder.date.setText(formatDate(m.getSended()));

        return rowView;
    }

    private String formatDate(Date d) {
        Date now = new Date();

        if (new SimpleDateFormat("dd-MM-yyyy").format(now).compareTo(new SimpleDateFormat("dd-MM-yyy").format(d)) == 0) {
            return new SimpleDateFormat("HH:mm").format(d);
        }
        return new SimpleDateFormat("dd-MM-yyy HH:mm").format(d);
}

    static class ViewHolder {
        public TextView msg, name, date;
    }
}
