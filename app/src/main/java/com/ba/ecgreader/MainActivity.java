package com.ba.ecgreader;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Trace;
import android.service.autofill.Dataset;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

//import static com.ba.ecgreader.HomeActivity.usbService;


//import static com.ba.ecgreader.HomeActivity.printLog;

public class MainActivity extends DemoBase implements
        OnChartValueSelectedListener, View.OnTouchListener, ZoomEventHandler {
    //private final LineChart[] charts = new LineChart[4];
//public static Queue<Integer> queue = new ;
    public Thread thread_data_from_Serial_buffer, thread_add_entry;
    boolean add_entry = false, data_from_Serial_buffer = false;
    public MyCountDownTimer myCountDownTimer;
    public static final int FILES = 0;
    public static final int ENTRIES = 1;
    public static final int IGNORE = 2;
    public static int waiting_for = IGNORE;
    public boolean AUTOSCROLL = true;
    private LineChart chart;
    static LinkedList<String> linkedList = new LinkedList<>();
    private CheckBox checkBox;
    private boolean firstTime = true;

    public int xRangeVisble = 1024;
    static Sync.Syncronization2 syn = new Sync.Syncronization2();
    private int debug=0;

    static class OneStringTask implements Runnable {
        String data;

        OneStringTask(String s) {
            data = s;
        }

        public void run() {
            try {
                syn.produce(data);
            } catch (Exception e) {
            }
        }
    }


    public String consume() {
        String data = null;
        try {
            data = syn.consume();
            return data;
        } catch (Exception ee) {
        }
        return data;
    }

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

    String cmd = "";
    TextView tv, tv_text, tv_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        setTitle("ECG Reader Chart");


        //printLog(this);
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this)); // add this to your activity page
        Log.d("MainActivity", "oncreate");
        //usbService.list.clear();
        //tv = findViewById(R.id.tv);
        mHandler = new MyHandler(this);
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
        //  usbService.setHandler(mHandler);
//        chart = findViewById(R.id.chart);


        tv_text = findViewById(R.id.tv);
        tv_error = findViewById(R.id.tv_error);
//        tv_text.setMovementMethod(new ScrollingMovementMethod());

//        charts[0] = findViewById(R.id.chart1);
//        charts[1] = findViewById(R.id.chart2);
//        charts[2] = findViewById(R.id.chart3);
//        charts[3] = findViewById(R.id.chart4);

//        Typeface mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Bold.ttf");

        // for (int i = 0; i < charts.length; i++) {
//////***************************************/////
//        LineData data = getData(100, 100);
//        //   data.setValueTypeface(mTf);
//
//        // add some transparency to the color with "& 0x90FFFFFF"
//        setupChart(chart, data, color);
////////////****************************************////


        chartSetup();
//        chart.setOnTouchListener(this);
//        setEventHandler(this);

        //}

//        Bundle extras = getIntent().getExtras();
//        if (extras == null) {
//            cmd = null;
//        } else {
//            cmd = (String) extras.get("CMD");
//            //cmd = cmd.replace("\n", "");
//        }
////        Toast.makeText(MainActivity.this, cmd, Toast.LENGTH_LONG).show();
//
//        if (usbService != null) {
//            usbService.write(cmd.getBytes());
//        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Don't generate garbage runnables inside the loop.
                String data = "";
                while (true) {
                    try {

                        //Log.d("syn.consume", "start");
                        // = UsbService.sync.consume();
                        if (usbService == null) {
                            //Thread.sleep(200);
                        } else if (!usbService.list.isEmpty()) {
                            data += usbService.list.removeFirst();
                            String s = data;
                            int index = data.indexOf("\n");
                            if (index > 0) {
                                String lines[] = data.substring(0, index).split("\\r?\\n");

                                ///String data = "";

//                        synchronized (linkedList) {
//                            if (linkedList.size() > 0)
//                                data = linkedList.removeFirst();
//                        }
                                //Log.d("syn.consume", data);

                                for (int i = 0; i < lines.length; i++) {

                                    runOnUiThread(new OneShotTask(lines[i]));
                                    //Thread.sleep(1);
                                }
//                                if(s.endsWith("\n")){
//                                    runOnUiThread(new OneShotTask(lines[lines.length-1]));
//                                    Thread.sleep(40);
//                                }
//                                else
//                                {
//                                    data = lines[lines.length-1];
//
//                                }
                                if (index < data.length())
                                    data = data.substring(index + 1, data.length());
                            }
                            //Thread.sleep(50);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        })
        //.start();
        ;
        //Toast.makeText(MainActivity.this,"before",Toast.LENGTH_SHORT);

//            Log.d("MainActivity", "Oncreate end");
//        }


    }

    private void chartSetup() {
        chart = findViewById(R.id.chart);
        chart.setOnChartValueSelectedListener(this);

        // enable description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        int pinkColor = Color.parseColor("#f49ac1");
        chart.setBackgroundColor(pinkColor);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(true);
        // modify the legend ...
        l.setForm(LegendForm.LINE);
        l.setTypeface(tfLight);
        l.setTextColor(Color.RED);

        chart.getAxisLeft();
        XAxis xl = chart.getXAxis();
        xl.setDrawLabels(false);
        xl.setTypeface(tfLight);
        xl.setDrawLimitLinesBehindData(true);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        chart.setScaleXEnabled(true);
        xl.setEnabled(false);
//        xl.setGridColor(Color.RED);
        xl.setGranularityEnabled(false);
        xl.setGranularity(1f);
        xl.setGridColor(Color.RED);
        //chart.setScaleX(5);
        //xl.setGridColor(Color.argb(120, 230, 234, 231));
//        xl.removeAllLimitLines();
//        xl.setLabelCount(5,false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(tfLight);
        leftAxis.setDrawLimitLinesBehindData(true);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setGridColor(Color.RED);
        leftAxis.setAxisMaximum(5f);
        leftAxis.setAxisMinimum(0.0f);
//        leftAxis.setAxisMaximum(125f);
//        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1);
        leftAxis.setGranularityEnabled(false);
        leftAxis.setGridLineWidth(1f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setEnabled(true);
        leftAxis.setLabelCount(5,false);
        for (int i = 0; i <= 5; i++) {
            LimitLine line = new LimitLine(i);
            line.setLineColor(Color.RED);
            leftAxis.addLimitLine(line);
        }

//        leftAxis.setEnabled(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setAxisMaximum(5f);
        rightAxis.setAxisMinimum(-5f);
//        rightAxis.setEnabled(false);
////
        rightAxis.setTypeface(tfLight);
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setGridColor(Color.RED);
        rightAxis.setAxisMaximum(5f);
        rightAxis.setAxisMinimum(-5f);
        rightAxis.setGranularity(1f);
        rightAxis.setGranularityEnabled(true);
        rightAxis.setGridLineWidth(1f);
        rightAxis.setDrawGridLines(false);
        rightAxis.setEnabled(false);

//        leftAxis.removeAllLimitLines();
//        rightAxis.removeAllLimitLines();


        chart.moveViewToX(0);
//        chart.setMaxVisibleValueCount(50);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_READY: // USB Ready
                    add_entry = true;
                    data_from_Serial_buffer = true;
                    f();
                    tv_text.setText("usb_ready");
//                    String cmd = "1r*\n";
//                    if (usbService != null)
//                        usbService.write(cmd.getBytes());
                    break;
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED

//                    String cmd = "4d*{" + "test123.txt" + "}\n";
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    tv_text.setText("usb disconnected...");

//                    thread_add_entry.stop();
//                    thread_data_from_Serial_buffer.stop();
                    add_entry = false;
                    data_from_Serial_buffer = false;

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
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_serial_com);
//
//        mHandler = new MyHandler(this);
//
//        display = (TextView) findViewById(R.id.textView);
//        editText = (EditText) findViewById(R.id.edittext);
//        Button sendButton = (Button) findViewById(R.id.button3);
//        sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!editText.getText().toString().equals("")) {
//                    String data = editText.getText().toString();
//                    if (usbService != null) { // if UsbService was correctly binded, Send data
//                        usbService.write(data.getBytes());
//                    }
//                }
//            }
//        });
//    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
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
        filter.addAction(UsbService.ACTION_USB_READY);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private Thread thread;
    List<Integer> list = new ArrayList<Integer>();
    static String MSG = "";


    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    public static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        //public static int i = 0;
        public static List l = new ArrayList<String>();


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
//                    String data = (String) msg.obj;
//
//                    MSG += data;
//                    //String lines[] = data.split("\\r?\\n");
//                    //MSG = MSG.replace("SYSTEM~1", "");
//                    MSG = MSG.replace("\\r?\\n", ",");
//                    String s = data;
//                    int index = data.indexOf("\n");
//                    if (index > 0) {
//                        final String lines[] = data.substring(0, index).split("\\r?\\n");
//
//                        ///String data = "";
//
////                        synchronized (linkedList) {
////                            if (linkedList.size() > 0)
////                                data = linkedList.removeFirst();
////                        }
//                        Log.d("syn.consume", data);
//
//                        for (int i = 0; i < lines.length; i++) {
//                            final String ss = lines[i];
//                            mActivity.get().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        mActivity.get().addEntry(Integer.parseInt(ss));
//
//                                    } catch (Exception e) {
//                                    }
//                                }
//                            });
//                            try {
//                                Thread.sleep(50);
//                            } catch (Exception e) {
//                            }
//                        }
//
//
//
//
//                        if (index < data.length())
//                            data = data.substring(index + 1, data.length());
//                    }

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

    private final int color = Color.rgb(0, 0, 255);

    private void setupChart(LineChart chart, LineData data, int color) {

        //((LineDataSet) data.getDataSetByIndex(0)).setCircleHoleColor(Color.RED);

        // no description text
        chart.getDescription().setEnabled(false);

        // chart.setDrawHorizontalGrid(false);
        //
        // enable / disable grid background
        chart.setDrawGridBackground(false);
//        chart.setGridBackgroundColor(Color.RED);
//        chart.getRenderer().getGridPaint().setGridColor(Color.WHITE & 0x70FFFFFF);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
//        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

//        chart.setBackgroundColor(color);

        // set custom chart offsets (automatic offset calculation is hereby disabled)
        chart.setViewPortOffsets(80, 0, 10, 0);

        // add data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);

        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setSpaceTop(40);
        chart.getAxisLeft().setSpaceBottom(40);
        chart.getAxisRight().setEnabled(false);


        chart.getXAxis().setAxisLineColor(Color.RED);
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();

            xAxis.setEnabled(true);
            // vertical grid lines
            xAxis.setDrawGridLines(false);
            //xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setGridColor(Color.BLUE);
        }

        chart.setVisibleXRangeMaximum(20);

        // animate calls invalidate()...
        //chart.animateX(1500);
    }

//    private LineData getData(int count, float range) {
//
//        ArrayList<Entry> values = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//            float val = (float) (Math.random() * range) + 3;
//            values.add(new Entry(i, val));
//        }
//
//        // create a dataset and give it a type
//        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
//        // set1.setFillAlpha(110);
//        // set1.setFillColor(Color.RED);
//
//        set1.setLineWidth(2f);
//        set1.setCircleRadius(5f);
//        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
//        //? LineDataSet.Mode.LINEAR
//        //: LineDataSet.Mode.CUBIC_BEZIER);
//        set1.setCircleHoleRadius(2.5f);
//        set1.setColor(Color.BLACK);
//        set1.setCircleColor(Color.BLACK);
//        set1.setHighLightColor(Color.RED);
//        set1.setDrawValues(false);
//        set1.setDrawCircles(false);
//
//        // create a data object with the data sets
//        return new LineData(set1);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.viewGithub: {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/RealtimeLineChartActivity.java"));
//                startActivity(i);
//                break;
//            }
//            case R.id.actionAdd: {
//                addEntry();
//                break;
//            }
//            case R.id.actionClear: {
//                chart.clearValues();
//                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
//                break;
//            }
//            case R.id.actionFeedMultiple: {
//                feedMultiple();
//                break;
//            }
//            case R.id.actionSave: {
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    saveToGallery();
//                } else {
//                    requestStoragePermission(chart);
//                }
//                break;
//            }
//        }
//        return true;
//    }

    String data_cmd = "";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.viewGithub: {
                if(chart.getLineData().getDataSetByIndex(0).removeFirst()){
                    tv_error.setText("cleared");
                }
//
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/RealtimeLineChartActivity.java"));
//                startActivity(i);
                break;
            }
            case R.id.actionAdd: {
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        Log.d("MainActivity", "before");
                        String string = "";
                        while (true) {
                            try {
                                string += UsbService.sync.consume();
                                int index = string.lastIndexOf("\n");
                                String lines[] = string.substring(0, index).split("\\r?\\n");
//                                        Log.d("MainActivity","consumed");
                                for (int i = 0; i < lines.length; i++) {
                                    syn.produce(lines[i]);
                                }
                                string = string.substring(index + 1, string.length());
                                //Log.d("MainActivity", "produce");
                                //Thread.sleep(50);
                            } catch (Exception ee) {
                                //Log.d("MainActivity", "error");

                            }

                        }
                    }
                };
                new Thread(run).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        feedData();
                    }
                }).start();
                //addEntry();
//                try {
//
//                    String string = "";
//                    //data = data.replaceAll("[^\\d.]", ",");
//                    //String[] str = data.split(",");
//                    for (int i = 0; i < usbService.list.size(); i++) {
//                        string = string + usbService.list.get(i);
//                    }
//
//                    String lines[] = string.split("\\r?\\n");
////                string.split("")
//                    //TextView tv = (TextView) findViewById(R.id.tv);
////                for (int i = 0; i < lines.length; i++) {
//
//                    feedMultiple(lines);
////                }
//
//
//                    //Toast.makeText(this, MyHandler.i, Toast.LENGTH_SHORT).show();
//                    //feedMultiple(list);
//                    //Toast.makeText(this, "list " + usbService.list.size() , Toast.LENGTH_SHORT).show();
////                tv.setText(Integer.toString(usbService.list.size()));
//                    //tv.setText(string);
//                } catch (Exception e) {
//                }
                break;
            }
            case R.id.autoScroll: {
//                CheckBox checkBox = findViewById(R.id.autoScroll);
                if (item.isChecked()) {
                    // If item already checked then unchecked it
                    item.setChecked(false);
//                    mItalic = false;
                    AUTOSCROLL = true;
                } else {
                    // If item is unchecked then checked it
                    item.setChecked(true);
//                    mItalic = true;
                    AUTOSCROLL = false;
                }

                break;
            }
            case R.id.actionFeedMultiple: {
                popupDialog();
                //usbService.write(cmd.getBytes());
                break;
            }
            case R.id.actionSave: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery();
                } else {
                    requestStoragePermission(chart);
                }
                break;
            }
        }
        return true;
    }

    private void popupDialog() {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                data_cmd = "4d*{" + "test123.txt" + "}\n";
                                if (usbService != null) { // if UsbService was correctly binded, Send data
                                    waiting_for = ENTRIES;
                                    usbService.write(data_cmd.getBytes());
                                }

                                Toast.makeText(MainActivity.this, "data is written!", Toast.LENGTH_SHORT).show();

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
int i=0,xx=100;
    private void addEntry(int num) {


        try {


            LineData data = chart.getData();

            if (data != null) {

                ILineDataSet set = data.getDataSetByIndex(0);


                // set.addEntry(...); // can be called as well

                if (set == null) {
                    set = createSet();
//                    ((LineDataSet)set).setDrawCircles(true);
//                    ((LineDataSet)set).setDrawCircleHole(true);
//                    ((LineDataSet)set).enableDashedLine(0.1f,0,0);
                    data.addDataSet(set);
//                    set.setDrawValues(true);
//                    set.setDrawFilled(true);


//                    set
//                    data.enableDashedLine(0, 1, 0);

                }

                if(chart.getData().getDataSetByIndex(0).getEntryCount() > xx){
                    removeLastEntry();
                    xx+=10;
                }

                float x = (float) (set.getEntryCount());
                // float mapped_reading = (float) ((Values.Peek() / 680) * 4.5 + 0); //(value - fromSource) / (toSource - fromSource) * (toTarget - fromTarget) + fromTarget
//                data.addEntry(new Entry(x, (float) (num%10) -5), 0);
//                data.addEntry(new Entry(x, (float) (((float)num) / 1024.0f) * 5.0f), 0);
//                data.addEntry(new Entry(x, (float) ((float)num) /*/ 1024.0f) * 5.0f*/), 0);
                data.addEntry(new Entry(x, (float) ((((float)num) / 680) * 4.5 + 0)), 0); ///mapping from 0 - 1024 to 0 - 4.5
//                (value - fromSource) / (toSource - fromSource) * (toTarget - fromTarget) + fromTarget
//                if(data.getEntryCount() > 200) {
//                    if (data.getDataSetByIndex(0).removeEntry(0+i)) {
//                        tv_error.setText("cleared");
//                    } else {
//                        tv_error.setText("not");
//                    }
//                    i++;
//                }
//                LimitLine l = new LimitLine(x);
//                l.setLineColor(Color.RED);
//                l.setLabel("");
//                l.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//                chart.getXAxis().addLimitLine(l);


                if (set.getEntryCount() % 30 == 0) {
                    LimitLine l = new LimitLine(x);
                    l.setLineColor(Color.RED);
                    l.setLabel("");
                    l.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                    chart.getXAxis().addLimitLine(l);
                }

                //ChartUtils.removeOutdatedEntries((LineDataSet)set);
//                else if (set.getEntryCount() % 5 == 0) {
//                    LimitLine l = new LimitLine(x);
//                    l.setLineColor(Color.RED);
//                    l.setLabel("");
//                    l.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//                    chart.getXAxis().addLimitLine(l);
//                }

                //            if (set.getEntryCount() % 10 == 0) {
//                LimitLine l = new LimitLine(set.getEntryCount());
//                l.setLineColor(Color.argb(255,240,234,230));
//                chart.getXAxis().addLimitLine(l);
//            }
                data.notifyDataChanged();

                // let the chart know it's data has changed
                chart.notifyDataSetChanged();

                //chart.invalidate();
                // chart.setVisibleYRange(30, AxisDependency.LEFT);

                if (firstTime) {
                    chart.moveViewToX(0);
                    firstTime = false;
                }
                // move to the latest entry
//                if (AUTOSCROLL) {
//                    chart.moveViewToX(set.getEntryCount());
//                }


//                chart.setVisibleXRangeMaximum(xRangeVisble+1000);
                chart.setVisibleXRangeMaximum(320);
//                //
                chart.moveViewToX(data.getEntryCount()+500/*, 0, AxisDependency.LEFT*/);
                chart.setMaxVisibleValueCount(500);
                //chart.getAxisRight().removeAllLimitLines();
                // this automatically refreshes the chart (calls invalidate())
                // chart.moveViewTo(data.getXValCount()-7, 55f,
                // AxisDependency.LEFT);
                // chart.moveViewToX(set.getEntryCount()-1);
                //tv_text.setText(Float.toString(x));

            }

        } catch (Exception e) {
            tv_text.setText("error....");
//            new AlertDialog.Builder(MainActivity.this)
//                    .setTitle("تنبيه")
//                    .setMessage("تأكد من اتصال الجهاز بالموبايل...!")
//
//                    // Specifying a listener allows you to take an action before dismissing the dialog.
//                    // The dialog is automatically dismissed when a dialog button is clicked.
//                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            // Continue with delete operation
//                        }
//                    })
//
//                    // A null listener allows the button to dismiss the dialog and take no further action.
//                    .setNegativeButton(android.R.string.no, null)
//                    .show();
        }
    }


    private void addEntryThread(final int dataEntry) {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry(0);
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                LineData data = chart.getData();

                if (data != null) {

                    ILineDataSet set = data.getDataSetByIndex(0);

                    // set.addEntry(...); // can be called as well

                    if (set == null) {
                        set = createSet();
                        data.addDataSet(set);
                    }

                    float x = (float) (set.getEntryCount() * 0.20);
                    data.addEntry(new Entry(x, (float) dataEntry / 102f), 0);
//            if (x*5 % 0.2 == 0) {
                    LimitLine l = new LimitLine(x);
                    l.setLineColor(Color.RED);
                    l.setLabel(Float.toString(x));
                    l.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                    chart.getXAxis().addLimitLine(l);
//            }
//            if (set.getEntryCount() % 10 == 0) {
//                LimitLine l = new LimitLine(set.getEntryCount());
//                l.setLineColor(Color.argb(255,240,234,230));
//                chart.getXAxis().addLimitLine(l);
//            }
                    data.notifyDataChanged();

                    // let the chart know it's data has changed
                    chart.notifyDataSetChanged();

                    // limit the number of visible entries
                    chart.setVisibleXRangeMaximum(0.5f);
                    // chart.setVisibleYRange(30, AxisDependency.LEFT);

                    // move to the latest entry
                    chart.moveViewToX(0);

                    // this automatically refreshes the chart (calls invalidate())
                    // chart.moveViewTo(data.getXValCount()-7, 55f,
                    // AxisDependency.LEFT);

                }
            }
        });

        thread.start();

    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(AxisDependency.LEFT);
        set.setColor(Color.BLACK);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawCircles(false);
        set.setFillColor(Color.WHITE);

        return set;
    }


//    private void feedMultiple(String [] lines) {
//
//        if (thread != null)
//            thread.interrupt();
//
//        final Runnable runnable = new Runnable(lines) {
//
//            @Override
//            public void run() {
//                addEntry();
//            }
//        };
//
//        thread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                //for (int i = 0; i < 1000; i++) {
//
//                    // Don't generate garbage runnables inside the loop.
//                    runOnUiThread(runnable);
//
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                //}
//            }
//        });
//
//        thread.start();
//    }

    class OneShotTask implements Runnable {
        String num;

        OneShotTask(String s) {
            num = s;

            //i = ii;
        }

        public void run() {
//                int size = num;

            // Don't generate garbage runnables inside the loop.
            try {
                //Toast.makeText(MainActivity.this,"oneShot",Toast.LENGTH_LONG);
                //Log.d("oneShot", "start");
                //Toast.makeText(MainActivity.this, "data", Toast.LENGTH_LONG).show();

                //tv_text.append(num+"\n");
                debug++;
                num = num.trim();
                tv_text.setText(Integer.toString(syn.list.size()));
                //if(Integer(num))
                //num=num.replace(" ","");
                addEntry(Integer.valueOf(num));
//
//
//                tv_text.setText();
                //tv.setText(tv.getText() + num+"\n");
                //Log.d("oneShot", "end");
            } catch (Exception e) {
                if(firstTime) {
                    tv_error.setText("error....\'" + num + "\'"+ e.getMessage());
                    firstTime=false;
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("تنبيه")
                            .setMessage(e.getMessage())

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
                tv_text.setText(Integer.toString(debug)+" "+num);

//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle("تنبيه")
//                        .setMessage("لا يوجد بيانات للعرض\nتأكد من اتصال الجهاز بالموبايل...!")
//
//                        // Specifying a listener allows you to take an action before dismissing the dialog.
//                        // The dialog is automatically dismissed when a dialog button is clicked.
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // Continue with delete operation
//                            }
//                        })
//
//                        // A null listener allows the button to dismiss the dialog and take no further action.
//                        .setNegativeButton(android.R.string.no, null)
//                        .show();
            }
        }
    }

    void fff() {
        long currentTimeMillis = System.currentTimeMillis();
        String last_data = "0";
        //     boolean add_entry = false, data_from_Serial_buffer = false;
        while (add_entry) {
            try {
                //Log.d("syn.consume", "start");

//                if (System.currentTimeMillis() - currentTimeMillis < 2) {
//                    //Thread.sleep(1);
//                } else {
                String data = syn.consume();
                if (data == null) {
                    //data = "-100";
//                    data = "0";
//                    data = last_data;
                } else {
                    runOnUiThread(new OneShotTask(data));
                    //last_data = data;
                }
                //Log.d("syn.consume", data);
                //                String data = syn.consume();
//                runOnUiThread(new OneShotTask(data));
                //currentTimeMillis = System.currentTimeMillis();
//                }
                Thread.sleep(12);
            } catch (Exception ee) {
            }
        }
    }
    int ii = 0;
    private void removeLastEntry() {

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set != null) {

                Entry e = set.getEntryForXValue(ii, Float.NaN);
                ii++;
                data.removeEntry(e, 0);
                // or remove by index
                //set.removeEntryByXValue(0);
                data.notifyDataChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
        }
    }


    private void feedData() {
        fff();

//
//        if (thread != null)
//            thread.interrupt();
        /*********************/


        /***************/
//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // Don't generate garbage runnables inside the loop.
//                long currentTimeMillis= System.currentTimeMillis();
//                String last_data = "0";
//                while (true) {
//                    try {
//                        //Log.d("syn.consume", "start");
//
//                        if(System.currentTimeMillis() - currentTimeMillis  <2){
//                            Thread.sleep(1);
//                        }else {
//                            String data = syn.consume();
//                            if(data == null){
//                                //data = "-100";
//                                data = last_data;
//                            }else{
//                                last_data = data;
//                            }
//                            //Log.d("syn.consume", data);
//                            //                String data = syn.consume();
//                            runOnUiThread(new OneShotTask(data));
//                            currentTimeMillis= System.currentTimeMillis();
//                        }
//                        //Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        //e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        thread.start();
    }


    @Override
    protected void onPause() {

        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
        if (thread != null) {
            thread.interrupt();
        }
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void saveToGallery() {
        saveToGallery(chart, "RealtimeLineChartActivity");
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
////        if (thread != null) {
////            thread.interrupt();
////        }
//    }


//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        MyCanvas canvas = new MyCanvas(this);
////        canvas.setBackgroundColor(Color.RED);
//        setContentView(R.layout.activity_main);
//
//
////        setContentView(canvas);
////        BottomNavigationView navView = findViewById(R.id.nav_view);
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//
//
////        Pie pie = AnyChart.pie();
////
////        List<DataEntry> data = new ArrayList<>();
////        data.add(new ValueDataEntry("John", 10000));
////        data.add(new ValueDataEntry("Jake", 12000));
////        data.add(new ValueDataEntry("Peter", 18000));
////
////        pie.data(data);
////
////        AnyChartView anyChartView = (AnyChartView) findViewById(R.id.any_chart_view);
////        anyChartView.setChart(pie);
//
//    }


    void f() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                //Log.d("MainActivity", "before");

                String string = "";
                while (data_from_Serial_buffer) {
                    try {
                        string += UsbService.sync.consume();
                        int index = string.lastIndexOf("\n");
                        String lines[] = string.substring(0, index).split("\\r?\\n");
//                                        Log.d("MainActivity","consumed");
                        for (int i = 0; i < lines.length; i++) {
                            syn.produce(lines[i]);
                        }

                        string = string.substring(index + 1, string.length());
                        //Log.d("MainActivity", "produce");

                        Thread.sleep(2);
                    } catch (Exception ee) {
                        //Log.d("MainActivity", "error");
                    }

                }
            }
        };
        thread_data_from_Serial_buffer = new Thread(run);
        thread_data_from_Serial_buffer.start();
        String cmd = "1r*\n";
        if (usbService != null)
            usbService.write(cmd.getBytes());
//        myCountDownTimer = new MyCountDownTimer(1000000L, 4);
//        myCountDownTimer.start();

        thread_add_entry = new Thread(new Runnable() {
            @Override
            public void run() {


                feedData();
            }
        });
        thread_add_entry.start();

        /*

        thread_data_from_Serial_buffer
        thread_add_entry
         */
        //addEntry();
//                try {
//
//                    String string = "";
//                    //data = data.replaceAll("[^\\d.]", ",");
//                    //String[] str = data.split(",");
//                    for (int i = 0; i < usbService.list.size(); i++) {
//                        string = string + usbService.list.get(i);
//                    }
//
//                    String lines[] = string.split("\\r?\\n");
////                string.split("")
//                    //TextView tv = (TextView) findViewById(R.id.tv);
////                for (int i = 0; i < lines.length; i++) {
//
//                    feedMultiple(lines);
////                }
//
//
//                    //Toast.makeText(this, MyHandler.i, Toast.LENGTH_SHORT).show();
//                    //feedMultiple(list);
//                    //Toast.makeText(this, "list " + usbService.list.size() , Toast.LENGTH_SHORT).show();
////                tv.setText(Integer.toString(usbService.list.size()));
//                    //tv.setText(string);
//                } catch (Exception e) {
//                }
    }

    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String data = null;
            try {
                data = syn.consume();
                if (data != null)
                    runOnUiThread(new OneShotTask(data));
//                    addEntry(Integer.parseInt(data));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


        @Override
        public void onFinish() {
//            myCountDownTimer = new MyCountDownTimer(10000000L, 4);
//            myCountDownTimer.start();
        }
    }
    private float mPrimStartTouchEventX = -1;
    private float mPrimStartTouchEventY = -1;
    private float mSecStartTouchEventX = -1;
    private float mSecStartTouchEventY = -1;
    private float mPrimSecStartTouchDistance = 0;
    private float last_mPrimSecStartTouchDistance = -1;
    private int mPtrCount = 0;
    private float last_x1, last_x2;
    private float last_y1, last_y2, zi = 0, zo = 0;
    private ZOOM STATE = ZOOM.OUT;

    private ZoomEventHandler eventHandler;

    public void setEventHandler(ZoomEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void onZoomIn() {
        Toast.makeText(this, "Zoom In", Toast.LENGTH_SHORT);
        Log.e("TAG", "zoom in = " + mPrimSecStartTouchDistance);
        xRangeVisble-=5;
        if(xRangeVisble <= 20 )
            xRangeVisble = 20;
        //chart.setVisibleXRangeMaximum(xRangeVisble);

    }

    @Override
    public void onZoomOut() {
        Toast.makeText(this, "Zoom Out", Toast.LENGTH_SHORT);
        Log.e("TAG", "zoom out = " + mPrimSecStartTouchDistance);

        xRangeVisble+=5;
        if(xRangeVisble >= chart.getData().getEntryCount() )
            xRangeVisble = chart.getData().getEntryCount();
        // /*jhj*/    chart.setVisibleXRangeMaximum(xRangeVisble);
    }


    enum ZOOM {
        IN,
        OUT
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = (event.getAction() & MotionEvent.ACTION_MASK);

        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                mPtrCount++;
                zi = 0;
                zo = 0;
                if (mPtrCount == 1) {
                    mPrimStartTouchEventX = event.getX(0);
                    mPrimStartTouchEventY = event.getY(0);
                    //Log.e("TAG", String.format("POINTER ONE X = %.5f, Y = %.5f", mPrimStartTouchEventX, mPrimStartTouchEventY));
                }
                if (mPtrCount == 2) {
                    // Starting distance between fingers
                    mSecStartTouchEventX = event.getX(1);
                    mSecStartTouchEventY = event.getY(1);
                    mPrimSecStartTouchDistance = (float) Math.sqrt(mSecStartTouchEventX * mPrimStartTouchEventX + mSecStartTouchEventY * mPrimStartTouchEventY);
                    //mPrimSecStartTouchDistance = distance(event, 0, 1);
                    //if(mPrimSecStartTouchDistance > 100)
                    //Log.e("TAG", String.format("POINTER TWO X = %.5f, Y = %.5f", mSecStartTouchEventX, mSecStartTouchEventY));
                    //Log.e("TAG", String.format("dis = %.5f",mPrimSecStartTouchDistance));
                    //if(dis)
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mPtrCount--;
                if (mPtrCount < 2) {
                    mSecStartTouchEventX = -1;
                    mSecStartTouchEventY = -1;
                }
                if (mPtrCount < 1) {
                    mPrimStartTouchEventX = -1;
                    mPrimStartTouchEventY = -1;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mPtrCount >= 2) {
//                    float x1 = event.getX(0);
//                    float y1 = event.getY(0);
//                    float x2 = event.getX(1);
//                    float y2 = event.getY(1);
//                    if (last_x1 == 0 && last_x2 == 0 && last_y1 == 0 && last_y2 == 0) {
//                        last_x1 = x1;
//                        last_x2 = x2;
//                        last_y1 = y1;
//                        last_y2 = y2;
//                    }
                    //if (Math.abs(x1 - last_x2))
                    //Math.abs(last_x1 - last_x2) > Math.abs(last_x1 - last_x2)
                    mPrimSecStartTouchDistance = distance(event, 0, 1);
                    if (last_mPrimSecStartTouchDistance == -1) {
                        last_mPrimSecStartTouchDistance = mPrimSecStartTouchDistance;
                    }
                    if (last_mPrimSecStartTouchDistance < mPrimSecStartTouchDistance /*&& STATE == ZOOM.OUT*/) {
                        zi++;
                        if (zi > 15) {
                            zo = 0;
                            zi = 0;
                            eventHandler.onZoomIn();
                            last_mPrimSecStartTouchDistance = -1;
                            STATE = ZOOM.IN;
                        }
                    } else if (last_mPrimSecStartTouchDistance > mPrimSecStartTouchDistance /*&& STATE == ZOOM.IN*/) {
                        zo++;
                        if (zo > 15) {
                            zo = 0;
                            zi = 0;
                            STATE = ZOOM.OUT;
                            eventHandler.onZoomOut();
                            last_mPrimSecStartTouchDistance = -1;
                        }
                    }
                    last_mPrimSecStartTouchDistance = mPrimSecStartTouchDistance;


//                    last_x1 = x1;
//                    last_x2 = x2;
//                    last_y1 = y1;
//                    last_y2 = y2;
                    //Log.e("TAG", "dis = " + mPrimSecStartTouchDistance);
                }
                break;

        }

        return true;
    }

    public float distance(MotionEvent event, int first, int second) {

        if (event.getPointerCount() >= 2) {
            final float x = event.getX(first) - event.getX(second);
            final float y = event.getY(first) - event.getY(second);

            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

}
