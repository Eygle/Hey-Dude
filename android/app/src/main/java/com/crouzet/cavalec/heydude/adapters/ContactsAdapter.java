package com.crouzet.cavalec.heydude.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.crouzet.cavalec.heydude.HeyDudeSessionVariables;
import com.crouzet.cavalec.heydude.model.Contact;
import com.crouzet.cavalec.heydude.R;
import com.crouzet.cavalec.heydude.utils.HeyDudeRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Johan on 19/02/2015.
 */
public class ContactsAdapter extends ArrayAdapter<Contact> {

    private List<Contact> list;
    private Context context;

    public ContactsAdapter(Context context, List<Contact> values) {
        super(context, R.layout.list_item_contact, values);

        list = values;
        this.context = context;

        updateContactList();
    }

    private void updateContactList() {
        RequestParams params = new RequestParams();
        params.put("uid", HeyDudeSessionVariables.uid);
        params.put("since", String.valueOf(HeyDudeSessionVariables.contactsLastUpdate));
        params.put("os", "android");

        JsonHttpResponseHandler jsonHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                super.onSuccess(statusCode, headers, response);
                try {
                    HeyDudeSessionVariables.contactsLastUpdate = response.getLong("since");
                    //new UpdateData().execute(response);
                } catch (Exception e){e.printStackTrace();}
            }
        };

        HeyDudeRestClient.get(HeyDudeRestClient.API, params, jsonHandler, 30000);

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // First let's verify the convertView is not null
        if (rowView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_contact, parent, false);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) rowView.findViewById(R.id.name);

            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Contact c = list.get(position);

        holder.name.setText(c.getFirstName());

        return rowView;
    }

    static class ViewHolder {
        public TextView name;
    }

    /**
     * Async Task to update channels data
     *//*
    private class UpdateData extends AsyncTask<JSONObject, Integer, Boolean> {

        protected Boolean doInBackground(JSONObject... data) {
            return ManageDataFromEPG.update(data[0]);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = new Intent();
                intent.setAction(SupermoteConstants.BROADCAST_REFRESH_LIST);
                sendBroadcast(intent);
            } else {
                Log.e("Supermote", "An error occurred during channels refresh...");
            }
        }
    }*/

}
