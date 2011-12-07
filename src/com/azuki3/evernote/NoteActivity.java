package com.azuki3.evernote;

import java.io.File;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.android.edam.TAndroidHttpClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.userstore.Constants;

public class NoteActivity extends Activity {
	private static final String TAG = "NoteActivity";

	static final String SHARD_ID = "shardId";
	static final String ACCESS_TOKEN = "accessToken";
	
	private static final String EVERNOTE_HOST = "sandbox.evernote.com";
	private static final String NOTESTORE_URL_BASE = "https://" + EVERNOTE_HOST + "/edam/note/";

	private static final String USER_AGENT = "kenichiro22/EvernoteOauthSample (Android) " + Constants.EDAM_VERSION_MAJOR + "." + Constants.EDAM_VERSION_MINOR;

	private NoteStore.Client noteStore;

	private String accessToken;
	private String shardId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note);

		Intent i = getIntent();
		this.accessToken = i.getStringExtra(ACCESS_TOKEN);
		this.shardId = i.getStringExtra(SHARD_ID);
		
		Toast.makeText(this, accessToken + ":" + shardId, Toast.LENGTH_LONG).show();

		setupApi();
		
		ListView list = (ListView) findViewById(R.id.noteList);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, R.id.title);
		list.setAdapter(adapter);
		try {
			Notebook nb = noteStore.getDefaultNotebook(accessToken);
			NoteFilter filter = new NoteFilter();
			filter.setNotebookGuid(nb.getGuid());
			NoteList noteList = noteStore.findNotes(accessToken, filter, 0, 10);
			for (Note note : noteList.getNotes()) {
				adapter.add(note.getTitle());
				Log.d(TAG, note.getTitle());
			}
		} catch (EDAMUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EDAMSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EDAMNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupApi() {
		try {
			String noteStoreUrl = NOTESTORE_URL_BASE + shardId;
			TAndroidHttpClient noteStoreTrans = new TAndroidHttpClient(
					noteStoreUrl, USER_AGENT, getTempDir());
			TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
			this.noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
		} catch (Throwable t) {
			Log.e(TAG, getString(R.string.err_api_setup), t);
			Toast.makeText(this, R.string.err_api_setup, Toast.LENGTH_LONG).show();
		}
	}
	
	private File getTempDir() {
		return new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
	}
}
