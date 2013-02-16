package jaime.droid;

import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * 
 * Shared global state for the CarTunes application.
 * 
 * @author jaimeyap
 */
public class CarTunesApp extends Application {

  // Shared Constants.
  static final String PREFS_NAME = "CarTunesPreferences";
  static final String DEVICE_NAME_PREF = "deviceName";
  static final String DEVICE_ADDR_PREF = "deviceAddr";

  public Set<BluetoothDevice> getBondedDevices(Activity context) {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      // Device does not support Bluetooth

      // TODO(jaimeyap): Figure out what the appropriate mechanisms are for
      // error feedback on android.
      // For now continue because the emulator is shit.
      return null;
    }

    // Populate the ListView with the paired devices.
    return bluetoothAdapter.getBondedDevices();
  }
}
