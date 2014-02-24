package com.example.survey2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class UserId extends Activity {
	
	public final static String EXTRA_USERID = "com.example.survey2.USERID";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userid);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user_id, menu);
		return true;
	}
	
	public void submitUserId(View view) {
		Intent intent = new Intent(this, DisplayContext.class);
		EditText editText = (EditText) findViewById(R.id.user_id);
		String userid = editText.getText().toString();
		intent.putExtra(EXTRA_USERID, userid);
		startActivity(intent);
	}

}
