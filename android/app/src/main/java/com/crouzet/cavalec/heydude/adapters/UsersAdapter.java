package com.crouzet.cavalec.heydude.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Johan on 19/02/2015.
 * Adapter used to manage and display online users
 */
public class UsersAdapter extends ArrayAdapter<User> {
    // List of users
    private List<User> list;
    // Parent context
    private Context context;

    public UsersAdapter(Context context, List<User> values) {
        super(context, R.layout.list_item_user, values);

        list = values;
        this.context = context;
    }

    /**
     * Return view for each user
     * @param position position in list view
     * @param convertView previous view
     * @param parent parent
     * @return new view
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // First let's verify the convertView is not null
        if (rowView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_user, parent, false);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) rowView.findViewById(R.id.user_login);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.user_image);
            viewHolder.email = (TextView) rowView.findViewById(R.id.user_email);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        User u = list.get(position);

        holder.name.setText(u.getName());
        holder.email.setText(u.getEmail());
        Picasso.with(context).load(u.getImage()).resize(
                (int)context.getResources().getDimension(R.dimen.user_picture_width),
                (int)context.getResources().getDimension(R.dimen.user_picture_height)
        ).centerCrop().into(holder.image);

        return rowView;
    }

    /**
     * Used for list view optimisation
     */
    static class ViewHolder {
        public TextView name, email;
        public ImageView image;
    }
}
