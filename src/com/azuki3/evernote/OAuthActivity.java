package com.azuki3.evernote;

import java.util.Map;

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

public class OAuthActivity extends AbstractEvernoteActivity {
	private static final String TAG = OAuthActivity.class.getSimpleName();

	 private static final String consumerKey = "YOUR_CONSUMER_KEY";
	 private static final String consumerSecret = "YOUR_CONSUMER_SECRET";

	protected static final String REQUEST_TOKEN_URL = URL_BASE + "/oauth";
	protected static final String ACCESS_TOKEN_URL = URL_BASE + "/oauth";
	protected static final String AUTHORIZATION_URL_BASE = URL_BASE + "/OAuth.action";

	protected static final String CALLBACK_URL = "myapp://oauth";

	private String requestToken;

	private Button oauthBtn;
	private Button noteBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		loadToken();

		this.oauthBtn = ((Button) findViewById(R.id.oauth));
		this.oauthBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SimpleOAuthRequest oauthRequestor = getSimpleOAuthRequest();
				oauthRequestor.setParameter("oauth_callback", CALLBACK_URL);

				try {
					Map<String, String> reply = new OauthRequestTask(OAuthActivity.this)
							.execute(oauthRequestor).get();
					if (reply != null) {
						OAuthActivity.this.requestToken = reply.get("oauth_token");
						OAuthActivity.this.shardId = reply.get("edam_shard");

						updateScreen();

						// îFèÿâÊñ Çï\é¶Ç∑ÇÈ
						String authorizationUrl = AUTHORIZATION_URL_BASE
								+ "?oauth_token=" + requestToken;
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(authorizationUrl));
						startActivity(intent);
					}
				} catch (Throwable e) {
					Log.e(TAG, getString(R.string.err), e);
				}
			}
		});

		this.noteBtn = (Button) findViewById(R.id.note);
		this.noteBtn.setEnabled(this.accessToken != null
				&& this.shardId != null);
		this.noteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(OAuthActivity.this, NoteActivity.class);
				i.putExtra(ACCESS_TOKEN, accessToken);
				i.putExtra(SHARD_ID, shardId);
				startActivity(i);
			}
		});
		updateScreen();
	}

	private void processCallback(Uri uri) {
		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {

			Map<String, String> reply;
			try {
				String verifier = uri.getQueryParameter("oauth_verifier");
				SimpleOAuthRequest oauthRequestor = getSimpleOAuthRequest();
				oauthRequestor.setParameter("oauth_token", requestToken);
				oauthRequestor.setParameter("oauth_verifier", verifier);
				reply = new OauthRequestTask(OAuthActivity.this).execute(oauthRequestor).get();
				Log.d(TAG, "Reply: " + reply);
				this.accessToken = reply.get("oauth_token");
				this.shardId = reply.get("edam_shard");

				storeToken();
				updateScreen();
			} catch (Throwable e) {
				Log.e(TAG, getString(R.string.err), e);
			}
		}
	}

	private SimpleOAuthRequest getSimpleOAuthRequest() {
		return new SimpleOAuthRequest(
				REQUEST_TOKEN_URL, consumerKey, consumerSecret, null);
	}

	private void storeToken() {
		Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putString(ACCESS_TOKEN, accessToken);
		editor.putString(SHARD_ID, shardId);
		editor.commit();
	}

	private void loadToken() {
		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		this.accessToken = sp.getString(ACCESS_TOKEN, null);
		this.shardId = sp.getString(SHARD_ID, null);
	}

	private void updateScreen() {
		((TextView) findViewById(R.id.requestToken)).setText(requestToken);
		((TextView) findViewById(R.id.shardId)).setText(shardId);
		((TextView) findViewById(R.id.accessToken)).setText(accessToken);
		((Button) findViewById(R.id.note)).setEnabled(this.accessToken != null
				&& this.shardId != null);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// singleInstanceÇÃèÍçáÇ…ÇÕÇ©callbackÇ≈onNewIntentÇ™åƒÇŒÇÍÇÈ
		processCallback(intent.getData());
	}
}