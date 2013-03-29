package edu.buffalo.cse.cse486586.simpledht;
import java.security.NoSuchAlgorithmException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class OnLDumpClickListener implements OnClickListener {

	private static final String TAG = OnTestClickListener.class.getName();
	private static final int TEST_CNT = 50;
	public static final String KEY_FIELD = "key";
	public static final String VALUE_FIELD = "value";

	private final TextView mTextView;
	private final ContentResolver mContentResolver;
	private final Uri mUri;

	public OnLDumpClickListener(TextView _tv, ContentResolver _cr) {
		mTextView = _tv;
		mContentResolver = _cr;
		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
	}
		
	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
	
	@Override
	public void onClick(View arg0) {
		new Task().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private class Task extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params) {
	    	Uri selectAllUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
			Cursor resultCursor = mContentResolver.query(selectAllUri, null, "ALL", null, "");
			int keyIndex = resultCursor.getColumnIndex(OnTestClickListener.KEY_FIELD);
			int valueIndex = resultCursor.getColumnIndex(OnTestClickListener.VALUE_FIELD);
			for (boolean hasItem = resultCursor.moveToFirst(); hasItem; hasItem = resultCursor.moveToNext()) {
				String keyWithUri = resultCursor.getString(keyIndex);
				String[] values = keyWithUri.split("_");
				String key = values[1];
				String value = resultCursor.getString(valueIndex);
				Log.v(TAG, "Key and value are: " + key + " : " + value);
				publishProgress(key + ":" + value + "\n");
			}
			return null;
		}
		
		protected void onProgressUpdate(String...strings) {
			mTextView.append(strings[0]);

			return;
		}

	}

}
