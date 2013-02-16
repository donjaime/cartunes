package jaime.droid;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;

public class ActivityUtils {
  private ActivityUtils() {
  }

  /**
   * onClick handler for the openMusicButton.
   * 
   * Handles the button click to open the music app.
   */
  public static void openMusic(Activity context) {

    // Note: Despite being deprecated, new new way doesn't yet work.
    // new Intent(Intent.CATEGORY_APP_MUSIC);
    Intent musicIntent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
    context.startActivity(musicIntent);
  }
}
