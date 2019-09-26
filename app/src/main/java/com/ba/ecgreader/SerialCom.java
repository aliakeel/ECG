package com.ba.ecgreader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
//import com.hoho.android.usbserial.driver.UsbSerialDriver;
//import com.hoho.android.usbserial.driver.UsbSerialPort;
//import com.hoho.android.usbserial.driver.UsbSerialProber;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SerialCom extends AppCompatActivity {
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


//
//    TextView textView;
//    EditText editText1;
//    UsbManager usbManager;
//    UsbDevice device;
//    UsbSerialDevice serialPort;
//    UsbDeviceConnection connection;
//
//    public void connect(View view) {
//        try {
//            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//            UsbDevice device = null;
//            UsbDeviceConnection connection = null;
//            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
//            if (!usbDevices.isEmpty()) {
//
//                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
//                    device = entry.getValue();
//
//                    connection = usbManager.openDevice(device);
//                }
//            }
//
//            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
//            if (serialPort != null) {
//                if (serialPort.open()) {
//                    serialPort.setBaudRate(115200);
//                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_7);
//                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
//                    serialPort.setParity(UsbSerialInterface.PARITY_EVEN);
//                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
//                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "Not Open", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(this, "No Driver", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception cc) {
//            Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void clear(View view) {
//        try {
//
//            TextView Text = (TextView) findViewById(R.id.textView);
//            Text.setText("");
//        } catch (Exception e) {
//        }
//    }
//
//    public void disconnect(View view) {
//
//        try {
//            serialPort.close();
//        } catch (Exception e) {
//            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void click(View view) {
//        try {
//            EditText mEdit = (EditText) findViewById(R.id.edittext);
//            String ss = mEdit.getText().toString();
//            serialPort.write(ss.getBytes());
//            serialPort.read(mCallback);
//        } catch (Exception cc) {
//            Toast.makeText(this, "Write/Read Error", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
//        @Override
//        public void onReceivedData(byte[] arg0) {
//            try {
//                String data = new String(arg0, "UTF-8");
//
//                String ss = "";
//                tvAppend(ss, data);
//            } catch (Exception e) {
//                Toast.makeText(SerialCom.this, "Receive Error", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };
//
//    private void tvAppend(String tv, final CharSequence text) {
//        //final TextView ftv = tv;
//        //final CharSequence ftext = text;
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    TextView Text = (TextView) findViewById(R.id.textView);
//                    Text.setText(Text.getText() + "" + text);
//
//                } catch (Exception e) {
//                    Toast.makeText(SerialCom.this, "Error", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_com);

        mHandler = new MyHandler(this);

        display = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.edittext);
        Button sendButton = (Button) findViewById(R.id.button3);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    String data = editText.getText().toString();
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        usbService.write(data.getBytes());
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }


    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<SerialCom> mActivity;

        public MyHandler(SerialCom activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
