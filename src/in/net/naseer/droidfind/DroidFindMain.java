package in.net.naseer.droidfind;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface; //import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class DroidFindMain extends Activity implements OnClickListener,
		OnLongClickListener {
	private static final String TAG = "DroidFind";
	private static final int MENU_WHITELIST = 0;
	private static final int MENU_ADDALERTED = 1;
	private static final int MENU_CLEARLIST = 2;
	private static final int MENU_ABOUT = 3;

	private static final String DROIDFIND_PREFS = "droidfind_preferences";
	private static final String PREF_ALERTEDNUMBER = "alerted_number";
	private static final String PREF_WHITELIST = "whitelist";

	private String phoneNumber = "";
	private String simSerial = "";
	private String imsi = "";
	private AlertDialog addAlertedDialog;

	private void updateWhiteListInfo(boolean whitelisted) {

		ImageView img = (ImageView) findViewById(R.id.sim_serial_img);
		TextView tv = (TextView) findViewById(R.id.sim_serial_whitelist);
		if (whitelisted) {
			tv.setText(R.string.sim_serial_whitelisted);
			img.setImageResource(R.drawable.check);
		} else {
			tv.setText(R.string.sim_serial_not_whitelisted);
			img.setImageResource(R.drawable.uncheck);

		}

	}

	private void whitelistSim() {
		Log.d(TAG, " SIM Serial " + simSerial + " saved to preferences");
		String prevList = getSharedPreferences(DROIDFIND_PREFS, MODE_PRIVATE)
				.getString(PREF_WHITELIST, "");
		if (prevList == null)
			prevList = "";
		if (!prevList.contains(simSerial)) {

			String newList = prevList + simSerial + " ";
			getSharedPreferences(DROIDFIND_PREFS, MODE_PRIVATE).edit()
					.putString(PREF_WHITELIST, newList).commit();
			updateWhiteListInfo(true);
		}
	}

	private void addSimAlert() {

		new AlertDialog.Builder(this)
				.setMessage(R.string.sim_alert_msg)
				.setTitle(R.string.sim_alert_title)
				.setCancelable(true)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								whitelistSim();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	private void updateAlertedNumber() {

		String alertedNum = getSharedPreferences(DROIDFIND_PREFS, MODE_PRIVATE)
				.getString(PREF_ALERTEDNUMBER, "");

		TextView alertedTitle = (TextView) findViewById(R.id.alerted_number_title);
		TextView alertedData = (TextView) findViewById(R.id.alerted_number_data);
		if (alertedNum.equals("")) {
			alertedTitle.setText(R.string.alerted_number_not_set);
			alertedData.setText("");

		} else {
			alertedTitle.setText(R.string.alerted_number_set);
			alertedData.setText(alertedNum);
		}
	}

	private void updateSimStatus() {
		String simSerialPrefs = getSharedPreferences(DROIDFIND_PREFS,
				MODE_PRIVATE).getString(PREF_WHITELIST, "");

		if (simSerial.equalsIgnoreCase("Unknown")) {
			updateWhiteListInfo(false);
		} else {
			if (simSerialPrefs.contains(simSerial)) {
				updateWhiteListInfo(true);
			}
		}
	}

	private void addAlertedNumber() {

		View alertedView = View.inflate(getApplicationContext(),
				R.layout.add_alerted_view, null);
		EditText enterNumberField = ((EditText) alertedView
				.findViewById(R.id.alertedNumber_edit));
		enterNumberField.setText(getSharedPreferences(DROIDFIND_PREFS,
				MODE_PRIVATE).getString(PREF_ALERTEDNUMBER, ""));

		addAlertedDialog = new AlertDialog.Builder(this)
				.setMessage(R.string.addAlerted_alert_msg)
				.setTitle(R.string.addAlerted_alert_title)
				.setCancelable(true)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(alertedView)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								EditText enterNumberField = ((EditText) addAlertedDialog
										.findViewById(R.id.alertedNumber_edit));
								String phoneNumber = enterNumberField.getText()
										.toString();
								Log.d(TAG, "Alerted Number is: " + phoneNumber);
								getSharedPreferences(DROIDFIND_PREFS,
										MODE_PRIVATE)
										.edit()
										.putString(PREF_ALERTEDNUMBER,
												phoneNumber).commit();
								updateAlertedNumber();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// View serviceButton = findViewById(R.id.service_button);
		// serviceButton.setOnClickListener(this);
		View textLayout = findViewById(R.id.phone_number_layout);
		textLayout.setOnLongClickListener(this);
		View simSerialLayout = findViewById(R.id.sim_serial_layout);
		simSerialLayout.setOnLongClickListener(this);
		View imsiLayout = findViewById(R.id.imsi_layout);
		imsiLayout.setOnLongClickListener(this);
		View addAlertedLayout = findViewById(R.id.alerted_number_layout);
		addAlertedLayout.setOnLongClickListener(this);
		addAlertedLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addAlertedNumber();
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		phoneNumber = mTelephonyMgr.getLine1Number();
		simSerial = mTelephonyMgr.getSimSerialNumber();
		imsi = mTelephonyMgr.getSubscriberId();

		if (phoneNumber == null || phoneNumber.equals(""))
			phoneNumber = "Unknown";
		if (simSerial == null || simSerial.equals(""))
			simSerial = "Unknown";
		if (imsi == null || imsi.equals(""))
			imsi = "Unknown";

		((TextView) findViewById(R.id.phone_number_data)).setText(phoneNumber);
		((TextView) findViewById(R.id.sim_serial_data)).setText(simSerial);
		((TextView) findViewById(R.id.imsi_data)).setText(imsi);

		updateAlertedNumber();
		updateSimStatus();

	}

	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, MENU_WHITELIST, 0, "Whitelist SIM").setIcon(
				android.R.drawable.ic_menu_agenda);
		menu.add(0, MENU_ADDALERTED, 0, "Add Alerted Number").setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(0, MENU_CLEARLIST, 0, "Clear SIM Whitelist").setIcon(
				android.R.drawable.ic_menu_delete);
		menu.add(0, MENU_ABOUT, 0, "About").setIcon(
				android.R.drawable.ic_menu_info_details);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADDALERTED:
			addAlertedNumber();
			return true;
		case MENU_WHITELIST:
			addSimAlert();
			return true;
		case MENU_CLEARLIST:
			getSharedPreferences(DROIDFIND_PREFS, MODE_PRIVATE).edit()
					.putString(PREF_WHITELIST, "").commit();
			updateWhiteListInfo(false);
			Toast.makeText(this, R.string.whitelist_cleared, Toast.LENGTH_LONG)
					.show();

			return true;
		case MENU_ABOUT:
			new AlertDialog.Builder(this)
					.setMessage(R.string.about_msg)
					.setTitle(R.string.about_title)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setNeutralButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).show();

			return true;
		}
		return false;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		/*
		 * case (R.id.service_button): Log.d(TAG, "Service Button clicked");
		 * Intent j = new Intent(this, DroidFindService.class); startService(j);
		 * addAlertedNumber();
		 */
		}
	}

	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case (R.id.phone_number_layout):
		case (R.id.sim_serial_layout):
		case (R.id.imsi_layout):
			Log.d(TAG, "Long click on LinearLayout");
			addSimAlert();
			break;
		case (R.id.alerted_number_layout):
			addAlertedNumber();
			break;
		}
		return true;
	}
}