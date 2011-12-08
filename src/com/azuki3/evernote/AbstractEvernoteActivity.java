package com.azuki3.evernote;

import android.app.Activity;

public class AbstractEvernoteActivity extends Activity {

	protected static final String URL_BASE = "https://sandbox.evernote.com";
	protected static final String NOTESTORE_URL_BASE = URL_BASE + "/edam/note/";

	protected static final String SHARD_ID = "shardId";
	protected static final String ACCESS_TOKEN = "accessToken";

	protected String accessToken;
	protected String shardId;

	public AbstractEvernoteActivity() {
		super();
	}

}