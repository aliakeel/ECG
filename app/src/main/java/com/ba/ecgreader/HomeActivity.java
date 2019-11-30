package com.ba.ecgreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

public class HomeActivity extends AppCompatActivity {
    static String TAG = "ECG_Reader";
    public ListView list;
    public TextView tv_online_readings;
    public TextView tv_file_readings;
    public static ArrayAdapter<String> ad;

    static public UsbService usbService;
    private TextView display;
    private EditText editText;
    private MyHandler mHandler;

    static String MSG = "";

    static class OneStringTask implements Runnable {
        String name;

        OneStringTask(String s) {
            name = s;
        }

        public void run() {
            ad.add(name);
            ad.notifyDataSetChanged();
        }
    }


    public class ExceptionHandler implements
            java.lang.Thread.UncaughtExceptionHandler {
        private final Context myContext;
        private final String LINE_SEPARATOR = "\n";
        Thread.UncaughtExceptionHandler defaultUEH;

        public ExceptionHandler(Context con) {
            myContext = con;
            defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        }

        @SuppressWarnings("deprecation")
        public void uncaughtException(Thread thread, Throwable exception) {

            StringWriter stackTrace = new StringWriter();
            exception.printStackTrace(new PrintWriter(stackTrace));
            StringBuilder errorReport = new StringBuilder();
            errorReport.append("************ CAUSE OF ERROR ************\n\n");
            errorReport.append(stackTrace.toString());

            errorReport.append("\n************ DEVICE INFORMATION ***********\n");
            errorReport.append("Brand: ");
            errorReport.append(Build.BRAND);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("Device: ");
            errorReport.append(Build.DEVICE);
            errorReport.append(LINE_SEPARATOR);
            errorReport.append("Model: ");
            errorReport.append(Build.MODEL);
            errorReport.append(LINE_SEPARATOR);

            File root = android.os.Environment.getExternalStorageDirectory();
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(
                    new Date());

            File dir = new File(root.getAbsolutePath());
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, "log.txt");

            try {
                BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
                buf.append(currentDateTimeString + ":" + errorReport.toString());
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            defaultUEH.uncaughtException(thread, exception);
            System.exit(0);
        }

    }


    public static void printLog(Context context) {
        String filename = context.getExternalFilesDir(null).getPath() + File.separator + "my_app.txt";
        String command = "logcat -f " + filename + " -v time -d *:V";

        Log.d(TAG, "command: " + command);

        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        mHandler = new MyHandler(this);
        //printLog(this);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this)); // add this to your activity page

        list = (ListView) findViewById(R.id.list);

        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it

        tv_file_readings = findViewById(R.id.file_readings);

        tv_online_readings = findViewById(R.id.online_readings);
        tv_online_readings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usbService != null) {
                    String cmd = "1r*";
                    Intent intentApp = new Intent(HomeActivity.this, MainActivity.class);
                    intentApp.putExtra("CMD", cmd);
                    startActivity(intentApp);
                }
            }
        });

        ad = new ArrayAdapter<String>(this, R.layout.row_list_item, R.id.text_item);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String fileName = ad.getItem(i);
                fileName = fileName.replaceAll("\\s", "");
                String cmd = "4d*{" + fileName + ".TXT}\n";
//                Toast.makeText(HomeActivity.this, cmd, Toast.LENGTH_SHORT).show();
                Intent intentApp = new Intent(HomeActivity.this, MainActivity.class);
                intentApp.putExtra("CMD", cmd);
                startActivity(intentApp);
            }
        });
        list.setAdapter(ad);
    }


    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    break;
                case UsbService.ACTION_USB_READY: // USB Ready
                    String cmd = "2n*";
                    if (usbService != null)
                        usbService.write(cmd.getBytes());
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

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }


    public void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
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

    public void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_READY);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    public final ServiceConnection usbConnection = new ServiceConnection() {
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

    Thread thread;

    public static class MyHandler extends Handler {
        private final WeakReference<HomeActivity> mActivity;

        public MyHandler(HomeActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        //public static int i = 0;
        public static List l = new ArrayList<String>();

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    MSG += data;
                    //String lines[] = data.split("\\r?\\n");
                    MSG = MSG.replace("SYSTEM~1", "");
                    MSG = MSG.replace("\\r?\\n", "");
                    final String[] lines = MSG.split(".TXT");

                    for (int i = 0; i < lines.length - 1; i++) {
                        final int z = i;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int ii = z;
                                mActivity.get().runOnUiThread(new OneStringTask(lines[ii]));
                            }
                        }).start();
                        ;
                    }
                    if (!MSG.endsWith(".TXT"))
                        MSG = lines[lines.length - 1];
                    else
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                mActivity.get().runOnUiThread(new OneStringTask(lines[lines.length - 1]));
                            }
                        }).start();

//                    switch (waiting_for) {
//                        case FILES:
//
//                            waiting_for = IGNORE;
//                        case ENTRIES:
//                            try {
//
//                                l.add(data);
//                            } catch (Exception e) {
////
//                            }
//                        default:
//                    }
                    /**
                     *  Add Data from sensor to the Chart
                     */

                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

}
