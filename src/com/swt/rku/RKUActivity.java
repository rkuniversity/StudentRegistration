package com.swt.rku;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RKUActivity extends Activity implements UpdateRegisterListener{
	List<Student> list = new ArrayList<Student>();
	SearchAdapter adapter;
	EditText searchText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViewById(R.id.swt_logo).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(RKU.PoweredBy));
				startActivity(intent);
			}
		});
		findViewById(R.id.rk_logo).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getRegistered();
			}
		});		
		findViewById(R.id.refresh).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getRegistered();
			}
		});
		searchText = (EditText) findViewById(R.id.search_text);
		findViewById(R.id.search).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String search = searchText.getText().toString().trim();
				onSearch(search);
			}
		});

		ListView listView = (ListView) findViewById(R.id.listview);
		adapter = new SearchAdapter(this, R.layout.search_item, list);
		listView.setAdapter(adapter);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRegistered();
			}
		}, 500);
	}


	private void getRegistered() {

		new AsyncTask<Void, Void, String>() {
			ProgressDialog pDialog;
			protected void onPreExecute() {

				pDialog = ProgressDialog.show(RKUActivity.this, "",
						"Fetching total registered...");
			}

			@Override
			protected String doInBackground(Void... params) {

				try {
					HttpGet httpGet = new HttpGet(RKU.GetRegistered);
					HttpResponse response = new DefaultHttpClient()
							.execute(httpGet);
					InputStream is = response.getEntity().getContent();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(is));
					String line;
					StringBuilder sb = new StringBuilder();
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}

					br.close();
					is.close();
					br = null;
					is = null;
					response = null;
					httpGet = null;
					return sb.toString();
				} catch (Exception ex) {
					return ex.getMessage();
				}
			}

			protected void onPostExecute(String result) {
				if (result != null) {
					Log.e("error", result);
					((TextView) findViewById(R.id.totalView)).setText("Total Registered:"+result);
				}

				if (pDialog != null)
					pDialog.dismiss();

				pDialog = null;
			}
		}.execute();
	}

	private void onSearch(final String search) {

		new AsyncTask<Void, Void, String>() {
			ProgressDialog pDialog;
			protected void onPreExecute() {
				list.clear();
				adapter.notifyDataSetChanged();
				pDialog = ProgressDialog.show(RKUActivity.this, "",
						"Searching...");
			}

			@Override
			protected String doInBackground(Void... params) {

				try {
					HttpGet httpGet = new HttpGet(RKU.SearchStudent+"?search_keyword="
									+ search);
					HttpResponse response = new DefaultHttpClient()
							.execute(httpGet);
					InputStream is = response.getEntity().getContent();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(is));
					String line;
					StringBuilder sb = new StringBuilder();
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}

					br.close();
					is.close();
					br = null;
					is = null;
					response = null;
					httpGet = null;
					if(sb.toString().equals("[\"null\"]"))
						return "No result found";
					
					return extractJson(sb.toString());
				} catch (Exception ex) {
					return ex.getMessage();
				}
			}

			protected void onPostExecute(String result) {
				if (result != null) {
					Log.e("error", result);
					if (!result.equals(""))
						Toast.makeText(RKUActivity.this, result,
								Toast.LENGTH_LONG).show();
					else {
						adapter.notifyDataSetChanged();
					}
				}

				if (pDialog != null)
					pDialog.dismiss();

				pDialog = null;
			}

			private String extractJson(String response) throws JSONException {
				JSONArray jsonArr = new JSONArray(response);
				if (jsonArr.length() == 0)
					return "No result found";
				for (int i = 0; i < jsonArr.length(); i++) {
					JSONObject json = jsonArr.getJSONObject(i);
					Student stud = new Student();
					stud.id = json.getString("RegNo");
					stud.name = json.getString("Name");
					stud.stream = json.getString("Stream");
					stud.status = json.getString("Status");
					stud.medium = json.getString("Medium");
					stud.school = json.getString("SchoolName");
					stud.city = json.getString("City");
					stud.seatNo = json.getString("SeatNo");
					stud.Building = json.getString("Building");
					stud.RoomNo = json.getString("RoomNo");
					stud.isRegister = json.getString("Status").equals("Registered") ? true
							: false;
					list.add(stud);
				}
				return "";
			}
		}.execute();
	}

	@Override
	public void onTotalRegisterUpdate() {
		getRegistered();
	}
}