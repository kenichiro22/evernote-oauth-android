package com.azuki3.evernote;

import java.io.IOException;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.evernote.oauth.consumer.SimpleOAuthRequest;

public class EvernoteActivity extends Activity {
	private static final String TAG = EvernoteActivity.class.getSimpleName();
	
	static final String consumerKey = "YOUR_CONSUMER_KEY";
	static final String consumerSecret = "YOUR_CONSUMER_SECRET";

	static final String urlBase = "https://sandbox.evernote.com";

	static final String requestTokenUrl = urlBase + "/oauth";
	static final String accessTokenUrl = urlBase + "/oauth";
	static final String authorizationUrlBase = urlBase + "/OAuth.action";
	static final String noteStoreUrlBase = urlBase + "/edam/note/";

	static final String callbackUrl = "myapp://oauth";

	private String requestToken;
	private String accessToken;
	private String verifier;
	private String shardId;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		this.accessToken = sp.getString(NoteActivity.ACCESS_TOKEN, null);
		this.shardId = sp.getString(NoteActivity.SHARD_ID, null);
		updateScreen();

		((Button) findViewById(R.id.oauth))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						SimpleOAuthRequest oauthRequestor = new SimpleOAuthRequest(
								requestTokenUrl, consumerKey, consumerSecret,
								null);

						// Set the callback URL
						oauthRequestor.setParameter("oauth_callback",
								callbackUrl);

						try {
							Log.d(EvernoteActivity.class.getSimpleName(),
									"Request: " + oauthRequestor.encode());
							Map<String, String> reply = oauthRequestor
									.sendRequest();
							EvernoteActivity.this.requestToken = reply.get("oauth_token");
							EvernoteActivity.this.shardId = reply.get("edam_shard");

							updateScreen();

							// îFèÿâÊñ Çï\é¶Ç∑ÇÈ
							String authorizationUrl = authorizationUrlBase
									+ "?oauth_token=" + requestToken;

							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse(authorizationUrl));
							startActivity(intent);
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				});
		
		Button note = (Button) findViewById(R.id.note);
		note.setEnabled(this.accessToken != null && this.shardId != null);
		note.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(EvernoteActivity.this, NoteActivity.class);
				i.putExtra(NoteActivity.ACCESS_TOKEN, accessToken);
				i.putExtra(NoteActivity.SHARD_ID, shardId);
				startActivity(i);
			}
		});
	}

	private void processCallback(Uri uri){
		if (uri != null && uri.toString().startsWith(callbackUrl)) {
			verifier = uri.getQueryParameter("oauth_verifier");

			SimpleOAuthRequest oauthRequestor = new SimpleOAuthRequest(
					requestTokenUrl, consumerKey, consumerSecret, null);
			oauthRequestor.setParameter("oauth_token", requestToken);
			oauthRequestor.setParameter("oauth_verifier", verifier);
			// out.println("Request: " + oauthRequestor.encode());
			Map<String, String> reply;
			try {
				reply = oauthRequestor.sendRequest();
				Log.d(TAG, "Reply: " + reply);
				this.accessToken = reply.get("oauth_token");
				this.shardId = reply.get("edam_shard");
				
				Editor editor = getPreferences(MODE_PRIVATE).edit();
				editor.putString(NoteActivity.ACCESS_TOKEN, accessToken);
				editor.putString(NoteActivity.SHARD_ID, shardId);
				editor.commit();
				
				updateScreen();				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	

	private void updateScreen() {
		((TextView) findViewById(R.id.requestToken))
				.setText(requestToken);
		((TextView) findViewById(R.id.shardId))
				.setText(shardId);
		((TextView) findViewById(R.id.accessToken))
		.setText(accessToken);
		
		((Button)findViewById(R.id.note)).setEnabled(this.accessToken != null && this.shardId != null);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		processCallback(intent.getData());
	}
}