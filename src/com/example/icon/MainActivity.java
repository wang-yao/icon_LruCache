package com.example.icon;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends Activity {

	List<String> list;
	GridView gridView;
	ImageAdapter imageAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initData();
		gridView = (GridView) findViewById(R.id.gridView1);
		imageAdapter = new ImageAdapter(this, list);
		gridView.setAdapter(imageAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String path = list.get(position);
				Toast.makeText(MainActivity.this, path+"", 0).show();
			}
		});
	}

	private void initData() {

		Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			list = new ArrayList<String>();
			while (cursor.moveToNext()) {
				String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.DATA));
				if (path != null) {
					if (!"".equals(path))
						list.add(path);
				}
			}
			cursor.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
