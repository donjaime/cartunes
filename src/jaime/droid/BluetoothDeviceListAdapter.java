package jaime.droid;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BluetoothDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

  public BluetoothDeviceListAdapter(Context context) {
    super(context, R.layout.paired_list_item);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.paired_list_item, parent, false);
    TextView textView = (TextView) rowView.findViewById(R.id.deviceRow);
    BluetoothDevice device = getItem(position);
    String s = device.getName() + "\n" + device.getAddress();
    textView.setText(s);
    return rowView;
  }
}
