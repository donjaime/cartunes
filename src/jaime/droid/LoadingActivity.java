package jaime.droid;

import java.util.Set;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingActivity extends Activity {

  private static final long OPEN_MUSIC_DELAY = 1500;

  /** Simply needs to be > 0 */
  private static final int REQUEST_ENABLE_BT = 1;

  // Views
  private ProgressBar mProgress;
  private TextView mProgressText;
  private TextView mDefaultDeviceText;

  private ValueAnimator mAnimator;
  private BluetoothDevice mDefaultDevice;
  private A2dpConnector.Callback mProgressTextUpdater;

  private void initViewRefs() {
    mProgress = (ProgressBar) findViewById(R.id.progressBar);
    mProgressText = (TextView) findViewById(R.id.progressLabel);
    mDefaultDeviceText = (TextView) findViewById(R.id.pairedDevice);
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    initViewRefs();
    mAnimator = ValueAnimator.ofFloat(0f, 1f);
    mProgressTextUpdater = new A2dpConnector.Callback() {

      @Override
      public void onConnected(BluetoothDevice device) {
        mProgressText.setText(R.string.progress_open_music);
      }

      @Override
      public void onFail(BluetoothDevice device) {
        mProgressText.setText("failed to connect bluetooth device :(");
      }
    };
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    if (hasFocus) {

      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (bluetoothAdapter == null) {
        // Device does not support Bluetooth

        // TODO(jaimeyap): Figure out what the appropriate mechanisms are for
        // error feedback on android.
        // For now continue because the emulator is shit.
        return;
      }

      // Enable bluetooth if it isn't.
      if (!bluetoothAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(
            BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        return;
      }

      restorePreferences();
      startProgressAnimation();
    }
  }

  private void startProgressAnimation() {
    mAnimator.setDuration(OPEN_MUSIC_DELAY);
    mAnimator.addUpdateListener(new AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mProgress.setProgress((int) (animation.getAnimatedFraction() * 100f));
      }
    });
    mAnimator.addListener(new AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animation) {
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        ActivityUtils.openMusic(LoadingActivity.this);
      }

      @Override
      public void onAnimationCancel(Animator animation) {
      }

      @Override
      public void onAnimationRepeat(Animator animation) {
      }
    });
    mAnimator.start();
  }

  private void restorePreferences() {
    SharedPreferences settings = getSharedPreferences(CarTunesApp.PREFS_NAME, 0);
    String defaultDeviceName = settings.getString(CarTunesApp.DEVICE_NAME_PREF,
        null);
    String defaultDeviceAddr = settings.getString(CarTunesApp.DEVICE_ADDR_PREF,
        null);

    if (defaultDeviceAddr != null && defaultDeviceName != null) {
      mDefaultDevice = getDefaultDevice(defaultDeviceName, defaultDeviceAddr);

      // Simple error handling.
      if (mDefaultDevice == null) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your device does not seem to support Bluetooth.")
            .setCancelable(false)
            .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                LoadingActivity.this.finish();
              }
            });
        AlertDialog alert = builder.create();
        alert.show();
        return;
      }

      // Display the default device
      mDefaultDeviceText.setText(mDefaultDevice.getName() + "\n"
          + mDefaultDevice.getAddress());

      // Attempt to bond the default device.
      new A2dpConnector(this, BluetoothAdapter.getDefaultAdapter()).connect(
          mDefaultDevice, mProgressTextUpdater);
    } else {

      // No device specified. Send user to pick one.
      showDeviceList(null);
    }
  }

  private BluetoothDevice getDefaultDevice(String defaultDeviceName,
      String defaultDeviceAddr) {
    CarTunesApp appContext = (CarTunesApp) getApplicationContext();
    Set<BluetoothDevice> pairedDevices = appContext.getBondedDevices(this);

    if (pairedDevices != null) {

      for (BluetoothDevice device : pairedDevices) {
        if (device.getName().equals(defaultDeviceName)
            && device.getAddress().equals(defaultDeviceAddr)) {
          return device;
        }
      }
    }

    // TODO(jaimeyap): Figure out how to do proper error handling and device
    // versioning on Android.
    return null;
  }

  /**
   * onClick handler for the root View for the screen.
   * 
   * Navigates to the {@link ChangeDefaultDeviceActivity}.
   */
  public void showDeviceList(View view) {
    mAnimator.cancel();

    Intent changeDefaultDevice = new Intent(this,
        ChangeDefaultDeviceActivity.class);
    startActivity(changeDefaultDevice);
  }
}