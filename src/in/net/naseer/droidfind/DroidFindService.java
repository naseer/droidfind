/*
 * Copyright 2010 Naseer Ahmed
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package in.net.naseer.droidfind;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DroidFindService extends Service implements LocationListener {
	private static final String TAG = "DroidFind";
	private static final String DROIDFIND_PREFS = "droidfind_preferences";
	private static final String PREF_ALERTEDNUMBER = "alerted_number";
	private static final String PREF_WHITELIST = "whitelist";

	private Location curLoc;
	private LocationManager mLocationMgr;
	private String alertedNumber;
	private String whitelist;
	private String simSerial;

	private void sendSMS(String msg) {
		Log.d(TAG, "Alerted number: " + alertedNumber);
		Log.d(TAG, "msg: " + msg);
		SmsManager sms = SmsManager.getDefault();
		try {
			sms.sendMultipartTextMessage(alertedNumber, null,
					sms.divideMessage(msg), null, null);
		} catch (Exception e) {
			Log.d(TAG, "Unable to send SMS: " + e.getMessage());
		}
	}

	private String getSimInfo() {

		String simMsg = "";
		TelephonyManager mTelephonyMgr;

		try {
			mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

			String phoneNumber = mTelephonyMgr.getLine1Number();
			simSerial = mTelephonyMgr.getSimSerialNumber();
			String imsi = mTelephonyMgr.getSubscriberId();
			String cellLocation = mTelephonyMgr.getCellLocation().toString();
			String deviceId = mTelephonyMgr.getDeviceId();

			if (phoneNumber == null || phoneNumber.equals(""))
				phoneNumber = "Unknown";
			if (simSerial == null || simSerial.equals(""))
				simSerial = "Unknown";
			if (imsi == null || imsi.equals(""))
				imsi = "Unknown";
			if (cellLocation == null || cellLocation.equals(""))
				cellLocation = "Unknown";
			if (deviceId == null || deviceId.equals(""))
				deviceId = "Unknown";
			// Location Info

			simMsg += "Information for Device ID: " + deviceId + "\n";
			simMsg += "New SIM details:\nSIM Serial: " + simSerial + "\n"
					+ "Phone Number: " + phoneNumber + "\n";
			simMsg += "Subscriber ID: " + imsi + "\n";
			simMsg += "Cell Location: " + cellLocation + "\n";
		} catch (Exception e) {
			Log.d(TAG, "Failed to get SIM Info " + e.getMessage());
			simMsg += "Failed to get SIM Info\n";
		}
		return simMsg;

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "DroidFindService started");
		alertedNumber = getSharedPreferences(DROIDFIND_PREFS, MODE_PRIVATE)
				.getString(PREF_ALERTEDNUMBER, "");
		whitelist = getSharedPreferences(DROIDFIND_PREFS, MODE_PRIVATE)
				.getString(PREF_WHITELIST, "");
		try {
			String simMsg = getSimInfo();
			if (whitelist != null && whitelist.contains(simSerial)) {
				Log.d(TAG, "SIM is whitelisted. Stopping Service");
				stopSelf();
			} else {
				sendSMS(simMsg + "Will now attempt to locate phone\n");

				mLocationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
				if (mLocationMgr.isProviderEnabled("gps"))
					mLocationMgr.requestLocationUpdates("gps", 60000, 1, this);
				else {
					String sms = simMsg
							+ "Location services switched off, unable to locate phone";
					sendSMS(sms);
					Log.d(TAG, sms);
					Log.d(TAG, "Stopping Service");
					stopSelf();
				}
			}

		} catch (Exception e) {
			Log.d(TAG, "Fatal exception, could not send location");
			stopSelf();
		}
	}

	public void onLocationChanged(Location location) {
		Log.d(TAG, "Got new location");
		curLoc = location;
		String locMsg = "";
		locMsg += "Current Location(GPS): " + curLoc.getLatitude() + ","
				+ curLoc.getLongitude() + "\n";
		Log.d(TAG, locMsg);
		sendSMS(getSimInfo() + locMsg);
		mLocationMgr.removeUpdates(this);
		Log.d(TAG, "Stopping Service");
		stopSelf();
	}

	public void onProviderDisabled(String provider) {
		return;
	}

	public void onProviderEnabled(String provider) {
		return;
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		return;

	}

};
