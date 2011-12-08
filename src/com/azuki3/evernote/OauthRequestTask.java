package com.azuki3.evernote;

import java.io.IOException;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.evernote.oauth.consumer.SimpleOAuthRequest;

public class OauthRequestTask extends AsyncTask<SimpleOAuthRequest, Void, Map<String, String>> {
	
	private static final String TAG = "OauthRequestTask";
	private Context context;
	private ProgressDialog dialog;
	
	public OauthRequestTask(Context context){
		this.context = context;
	}
	
	@Override
	protected Map<String, String> doInBackground(SimpleOAuthRequest... params) {
		SimpleOAuthRequest oauthRequest = params[0];
		Map<String, String> reply = null;
		try {
			Log.d(TAG, "Request: " + oauthRequest.encode());
			reply = oauthRequest.sendRequest();
		} catch (IOException e) {
			Log.e(TAG, context.getString(R.string.err), e);
		}
		return reply;
	}

	@Override
	protected void onPostExecute(Map<String, String> result) {
		this.dialog.dismiss();
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setTitle("Requesting token");
		dialog.show();
	}

}
