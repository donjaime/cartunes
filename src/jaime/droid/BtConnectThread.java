package jaime.droid;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BtConnectThread extends Thread {
  private final BluetoothSocket mmSocket;
  private final BluetoothDevice mmDevice;
  private final BluetoothAdapter mBluetoothAdapter;

  public BtConnectThread(BluetoothAdapter mBluetoothAdapter,
      BluetoothDevice device) {
    this.mBluetoothAdapter = mBluetoothAdapter;
    this.mmDevice = device;

    // Use a temporary object that is later assigned to mmSocket,
    // because mmSocket is final
    BluetoothSocket tmp = null;

    // Get a BluetoothSocket to connect with the given BluetoothDevice
    try {
      // MY_UUID is the app's UUID string, also used by the server code
      // TODO(jaimeyap): Figure out how to obtain the appropriate UUID here.
      tmp = device.createRfcommSocketToServiceRecord(null);
    } catch (IOException e) {
    }
    mmSocket = tmp;
  }

  public void run() {
    // Cancel discovery because it will slow down the connection
    mBluetoothAdapter.cancelDiscovery();

    try {
      // Connect the device through the socket. This will block
      // until it succeeds or throws an exception
      mmSocket.connect();
    } catch (IOException connectException) {
      // Unable to connect; close the socket and get out
      try {
        mmSocket.close();
      } catch (IOException closeException) {
      }
      return;
    }

    // Do work to manage the connection (in a separate thread)
    // manageConnectedSocket(mmSocket);
  }

  /** Will cancel an in-progress connection, and close the socket */
  public void cancel() {
    try {
      mmSocket.close();
    } catch (IOException e) {
    }
  }
}
