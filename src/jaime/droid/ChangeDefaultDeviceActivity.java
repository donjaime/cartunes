package jaime.droid;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ChangeDefaultDeviceActivity extends Activity {

  private A2dpConnector.Callback mConnectionCallback;

  // Initialized by bindPairedBtDevices.
  private ListView mPairedDevicesList;
  private BluetoothDeviceListAdapter mDeviceListAdapter;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.device_list);

    mConnectionCallback = new A2dpConnector.Callback() {
      @Override
      public void onConnected(BluetoothDevice device) {
        ActivityUtils.openMusic(ChangeDefaultDeviceActivity.this);
      }

      @Override
      public void onFail(BluetoothDevice device) {
        // TODO(jaimeyap): Tell the user?
        ActivityUtils.openMusic(ChangeDefaultDeviceActivity.this);
      }
    };

    mDeviceListAdapter = new BluetoothDeviceListAdapter(this);

    // Data binding of the list of bonded bluetooth devices to the list view.
    if (dataBindPairedBtDevicesList()) {

      // Bluetooth adapter and ListView state initialized. We are safe to
      // reference them.

      // Handle selecting an entry.
      mPairedDevicesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      mPairedDevicesList.setOnItemClickListener(new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
          mPairedDevicesList.setSelection(position);
          BluetoothDevice device = mDeviceListAdapter.getItem(position);

          // Persist this selection to app storage so we remember it next time.
          SharedPreferences settings = getSharedPreferences(
              CarTunesApp.PREFS_NAME, 0);
          SharedPreferences.Editor editor = settings.edit();
          editor.putString(CarTunesApp.DEVICE_NAME_PREF, device.getName());
          editor.putString(CarTunesApp.DEVICE_ADDR_PREF, device.getAddress());
          editor.commit();

          // Ensure the device is paired and open music afterwards.
          new A2dpConnector(ChangeDefaultDeviceActivity.this, BluetoothAdapter
              .getDefaultAdapter()).connect(device, mConnectionCallback);

        }
      });
    }
  }

  /**
   * Looks up the list of paired bluetooth devices and binds it to the paired
   * devices ListView.
   * 
   * @return whether or not we successfully bound the bluetooth device list to
   *         the ListView.
   */
  private boolean dataBindPairedBtDevicesList() {
    CarTunesApp appContext = (CarTunesApp) getApplicationContext();

    // Populate the ListView with the paired devices.
    Set<BluetoothDevice> pairedDevices = appContext.getBondedDevices(this);

    if (pairedDevices == null) {
      // TODO(jaimeyap): Figure out how to do proper error handling and device
      // versioning on Android.

      return false;
    }

    for (BluetoothDevice device : pairedDevices) {
      // Add the name and address to an array adapter to show in a ListView
      mDeviceListAdapter.add(device);
    }

    mPairedDevicesList = (ListView) findViewById(R.id.pairedDevicesList);
    mPairedDevicesList.setAdapter(mDeviceListAdapter);
    return true;
  }

  /**
   * onClick handler for the openMusicButton.
   * 
   * Handles the button click to open the music app.
   */
  public void openMusic(View view) {
    ActivityUtils.openMusic(this);
  }
}
