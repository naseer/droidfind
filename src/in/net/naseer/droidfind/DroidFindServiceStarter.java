package in.net.naseer.droidfind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DroidFindServiceStarter extends BroadcastReceiver {
	static final String TAG = "DroidFind";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Intent received only when the system boot is completed
		Log.d(TAG, "Boot completed, Starting DroidFind service");
		Intent j = new Intent(context, DroidFindService.class);
		context.startService(j);

	}
}