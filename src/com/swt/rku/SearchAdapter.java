package com.swt.rku;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SearchAdapter extends ArrayAdapter<Student> {
	private LayoutInflater inflater;
	private int resourceId;
	private List<Student> list = new ArrayList<Student>();
	private Context context;

	public SearchAdapter(Context context, int textViewResourceId,
			List<Student> objects) {
		super(context, textViewResourceId, objects);
		this.resourceId = textViewResourceId;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		list = objects;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(resourceId, null);
			Holder holder = new Holder();
			holder.idView = (TextView) convertView
					.findViewById(R.id.student_id);
			holder.nameView = (TextView) convertView
					.findViewById(R.id.student_name);

			holder.streamView = (TextView) convertView
					.findViewById(R.id.stream);
			holder.mediumView = (TextView) convertView
					.findViewById(R.id.medium);
			holder.schoolView = (TextView) convertView
					.findViewById(R.id.school);
			holder.seatNoView = (TextView) convertView
					.findViewById(R.id.seatNo);
			holder.RoomNo = (TextView) convertView
					.findViewById(R.id.RoomNo);
			holder.Building = (TextView) convertView
			.findViewById(R.id.Building);

			holder.registerBtn = (Button) convertView
					.findViewById(R.id.register_btn);
			holder.registerBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Student stud = (Student) v.getTag();
					
					
					if (!stud.isRegister)
					{	
						onRegister(v);
					
					}
					
					else
						showSeatNo(stud);
				}
			});
			holder.registerBtn.setTag(list.get(position));
			convertView.setTag(holder);
		}

		Holder holder = (Holder) convertView.getTag();
		holder.idView.setText("Register No:" + list.get(position).id);
		holder.nameView.setText("Name:        " + list.get(position).name);
		holder.streamView.setText("Status:        " + list.get(position).stream);
		holder.seatNoView.setText("Seat No:" + list.get(position).seatNo);
		holder.mediumView.setText("Medium:    " + list.get(position).medium);
		holder.schoolView.setText("School:  " + list.get(position).school);
		holder.Building.setText("Building:  " + list.get(position).Building);
		holder.RoomNo.setText("RoomNo:  " + list.get(position).RoomNo);

		// if (list.get(position).isRegister)
		// holder.registerBtn.setEnabled(false);
		// else
		// holder.registerBtn.setEnabled(true);

		return convertView;
	}

	private class Holder {
		TextView idView;
		TextView nameView;
		TextView streamView;
		TextView mediumView;
		TextView schoolView;
		//TextView statusView;
		TextView seatNoView;
		TextView RoomNo;
		TextView Building;
		Button registerBtn;
	}

	private void onRegister(final View view) {
		new AsyncTask<Void, Void, String>() {
			Student stud;
			@Override
			protected String doInBackground(Void... params) {
				stud = (Student) view.getTag();
				try {
					HttpGet httpGet = new HttpGet(RKU.GenerateSeatNo+"?RegNo="
									+ stud.id);
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
					
					return extractJson(sb.toString());
				} catch (Exception ex) {
					return ex.getMessage();
				}
			}

			private String extractJson(String string) throws JSONException {
				JSONObject mainObj = new JSONObject(string);
				stud.seatNo = mainObj.getString("SeatNo");
				stud.Building = mainObj.getString("Building");
				stud.RoomNo = mainObj.getString("RoomNo");
				return "";
			}

			protected void onPostExecute(String result) {
				if (result != null) {
					Log.e("error", result);
					// RKUActivity rku = new RKUActivity();

					if(result.equals("")){
						Student stud = (Student) view.getTag();
						stud.isRegister = !stud.isRegister;

						view.setTag(stud);
	
						notifyDataSetChanged();
						showSeatNo(stud);
					}else
					{
					//	Toast.makeText(context,"All Class full..." , Toast.LENGTH_LONG).show();
					}
				}

			}

		}.execute();
	}

	private void showSeatNo(Student stud) {
		StringBuilder sb  = new StringBuilder("Seat No:      ")
			.append(stud.seatNo)
			.append("\nRoom No:   ")
			.append(stud.RoomNo)
			.append("\nBuilding No:")
			.append(stud.Building);
		new AlertDialog.Builder(context).setTitle("Student Detail")
			.setMessage(sb.toString())
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						((RKUActivity) context).onTotalRegisterUpdate();


					}
				}).create().show();
	}
}
