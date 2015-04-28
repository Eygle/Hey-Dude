package com.crouzet.cavalec.heydude.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.crouzet.cavalec.heydude.HeyDudeConstants;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Google Cloud Messaging Intent and broadcast manager
 */
public class GcmIntentService extends IntentService {

	private static final String TAG = GcmIntentService.class.getSimpleName();

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        /**
         * The getMessageType() intent parameter must be the intent you received
         * in your BroadcastReceiver.
         */
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {  /** has effect of unparcelling Bundle */
		    /**
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
			if (GoogleCloudMessaging.
					MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				//receivedGcmNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.
					MESSAGE_TYPE_DELETED.equals(messageType)) {
				//receivedGcmNotification("Deleted messages on server: " + extras.toString());
				/** If it's a regular GCM message, do some work. */
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				/** Post notification of received message. */
				receivedGcmNotification(extras);
				Log.i(TAG, "Received: " + extras.toString());
			}
		}
		/** Release the wake lock provided by the WakefulBroadcastReceiver. */
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

    /**
     * Put the message into a notification and post it.
     */
	private void receivedGcmNotification(Bundle extras) {
        Intent intent = null;

        String action = extras.getString("action");
        switch (action) {
            case "send_msg":
                intent = new Intent(HeyDudeConstants.BROADCAST_RECEIVE_MSG);
                intent.putExtra(HeyDudeConstants.BROADCAST_DATA_MSG, extras.getString("message"));
                intent.putExtra(HeyDudeConstants.BROADCAST_DATA_IV, extras.getString("iv"));
                break;
            case "send_key":
                intent = new Intent(HeyDudeConstants.BROADCAST_RECEIVE_KEY);
                intent.putExtra(HeyDudeConstants.BROADCAST_DATA_KEY, extras.getString("key"));
                break;
            case "refresh_user_list":
                intent = new Intent(HeyDudeConstants.BROADCAST_REFRESH_USER_LIST);
                intent.putExtra(HeyDudeConstants.BROADCAST_DATA_USER_LIST, extras.getString("list"));
                break;
            case "call":
                intent = new Intent(HeyDudeConstants.BROADCAST_RECEIVE_CALL);
                intent.putExtra(HeyDudeConstants.BROADCAST_DATA_DEST, extras.getString("dest"));
                break;
            case "hangup":
                intent = new Intent(HeyDudeConstants.BROADCAST_RECEIVE_HANGUP);
                intent.putExtra(HeyDudeConstants.BROADCAST_DATA_DEST, extras.getString("dest"));
                break;
            case "answer":
                intent = new Intent(HeyDudeConstants.BROADCAST_RECEIVE_CALL_ANSWER);
                intent.putExtra(HeyDudeConstants.BROADCAST_DATA_CALL_STATUS, extras.getString("status"));
                String key = extras.getString("key", null);
                if (key != null) {
                    intent.putExtra(HeyDudeConstants.BROADCAST_DATA_KEY, key);
                }
                break;
        }

        if (intent != null) {
            sendOrderedBroadcast(intent, null);
        }
	}
}