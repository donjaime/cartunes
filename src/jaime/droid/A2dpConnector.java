package jaime.droid;

import java.lang.reflect.Method;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class A2dpConnector {

  /**
   * Callback invoked when the connector successfully connects an A2DP device.
   */
  public interface Callback {
    void onConnected(BluetoothDevice device);

    void onFail(BluetoothDevice device);
  }

  private Callback nullCallback = new Callback() {
    @Override
    public void onConnected(BluetoothDevice device) {
    }

    @Override
    public void onFail(BluetoothDevice device) {
    }
  };

  private final Context context;
  private final BluetoothAdapter mBluetoothAdapter;

  public A2dpConnector(Context context, BluetoothAdapter mBluetoothAdapter) {
    this.context = context;
    this.mBluetoothAdapter = mBluetoothAdapter;
  }

  public void connect(final BluetoothDevice device) {
    connect(device, nullCallback);
  }

  public void connect(final BluetoothDevice device, final Callback callback) {
    BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
      private BluetoothA2dp mBluetoothA2dp;

      public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (profile == BluetoothProfile.A2DP) {
          mBluetoothA2dp = (BluetoothA2dp) proxy;
          if (BluetoothProfile.STATE_CONNECTED == mBluetoothA2dp
              .getConnectionState(device)) {

            // Nothing to do.
            callback.onConnected(device);
            return;
          }

          // connect Method is hidden. So we must use reflection.
          Class<?> c = mBluetoothA2dp.getClass();
          try {
            Method m = c.getMethod("connect", BluetoothDevice.class);
            Object[] args = new Object[1];
            args[0] = device;
            m.invoke(mBluetoothA2dp, args);
          } catch (NoSuchMethodException e) {
            // This means the method does not exist on this android SDK version.

            // TODO(jaimeyap): Figure out a decent way to do error handling on
            // Android.
            e.printStackTrace();
            callback.onFail(device);
          } catch (Exception e) {
            // TODO(jaimeyap): Figure out a decent way to do error handling on
            // Android.
            e.printStackTrace();
            callback.onFail(device);
          } finally {
            // Close proxy connection after use.
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP,
                mBluetoothA2dp);
          }

          callback.onConnected(device);
        }
      }

      public void onServiceDisconnected(int profile) {
        if (profile == BluetoothProfile.A2DP) {
          mBluetoothA2dp = null;
        }
      }
    };

    // Establish connection to the proxy.
    mBluetoothAdapter.getProfileProxy(context, mProfileListener,
        BluetoothProfile.A2DP);
  }
}
