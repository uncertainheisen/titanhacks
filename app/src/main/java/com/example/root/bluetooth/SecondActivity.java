/**
 * Created by khamar on 23/3/2018.
 */

package com.example.root.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import org.apache.commons.math.ArgumentOutsideDomainException;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.UUID;

//import umich.cse.yctung.androidlibsvm.LibSVM;

//import static com.example.nithin.androidgk.CreateDataset.linearInterp;
//import static com.example.nithin.androidgk.CreateDataset.linspace;

public class SecondActivity extends Activity{
    private static final String TAG = "bluetooth";

    Button btrain, bwork;
    TextView tvalue, wvalue;
    EditText message;
    Handler h;
    public boolean start = false;

    String workstr="";    //present data of work

    byte[] buffer = new byte[256];  // buffer store for the stream
    int bytes;
    final int RECIEVE_MESSAGE = 1;		// Status  for Handler
    //private BluetoothAdapter btAdapter = null;
    private BluetoothSocket socket = null;
    //private StringBuilder sb = new StringBuilder();
    private StringBuilder sb1 = new StringBuilder();
    private StringBuilder sb2 = new StringBuilder();
    String sbprint="";
    //private ConnectedThread mConnectedThread;

    // SPP UUID service
   // private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "98:D3:34:90:B8:02";
    File train, work;
    FileWriter mFileWriter;

    String dir = "data";
    String myData = "";
    String mes = null;
    int check = 0;
    String[] workstring=null,trainstring=null;
    String rawtrain,trainstringspace,gestureName;
    int[] workint = new int[3];
    int[] trainint = new int[]{0,0,0};
    int startindex,endindex,flag=0;
    int i;


    //Memeber Fields
    private BluetoothAdapter btAdapter = null;
   // private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;

    // UUID service - This is the type of Bluetooth device that the BT module is
    // It is very likely yours will be the same, if not google UUID for your manufacturer
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module
    public String newAddress = null;
    public String deviceName = null;

    /** Called when the activity is first created. */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        tvalue = (TextView) findViewById(R.id.tvalue);
        wvalue = (TextView) findViewById(R.id.wvalue);
        message = (EditText) findViewById(R.id.message);
        tvalue.setText("");
        wvalue.setText("");



        train = new File(getExternalFilesDir(dir), "train.txt");
        work = new File(getExternalFilesDir(dir), "work.txt");
        //Intent mIntent = getIntent();
        //deviceName = mIntent.getStringExtra("device_info");



        //Initialising buttons in the view
        //mDetect = (Button) findViewById(R.id.mDetect);
        btrain = (Button) findViewById(R.id.train);
        bwork = (Button) findViewById(R.id.work);

        //getting the bluetooth adapter value and calling checkBTstate function
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:													// if receive massage
                        //byte[] readBuf = (byte[]) msg.obj;
                        //String strIncom = new String(readBuf, 0, msg.arg1);					// create string from bytes array
                        String recv = (String) msg.obj;


                    if(start)
                    {
                        //String test=null;
                        sb1.append(recv);
                        // append string
                        //sb2.append(recv);
                        //tvalue.setText(recv);


                        /*String[] split = recv.split("$");
                        String firstSubString = split[0];
                        String secondSubString = split[1];

                        sb1.append(firstSubString);
                        sb1.append(secondSubString);*/




                        int startOfLineIndex = sb1.indexOf("#");
                        int endOfLineIndex = sb1.indexOf("$",startOfLineIndex);							// determine the end-of-line
                        if (endOfLineIndex > startOfLineIndex) {                                           // if end-of-line,
                          // sb1.append("\n");
                            //String sbprint = sb2.substring(0, endOfLineIndex);
                            sbprint = sb1.substring(startOfLineIndex+1, endOfLineIndex);				// extract string
                            //sb1.delete(0, sb1.length());										// and clear
                            if(check == 1)
                                tvalue.setText(sbprint); 	        // update TextView
                            else if(check == 2) {
                                wvalue.setText(sbprint);
                                //workstr = sbprint;
                            }
                            sb1.delete(0,sb1.length());
                            //sb2.delete(0,sb2.length());
                            //btnOff.setEnabled(true);
                            //btnOn.setEnabled(true);
                        }


                    }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        /**************************************************************************************************************************8
         *  Buttons are set up with onclick listeners so when pressed a method is called
         *  In this case send data is called with a value and a toast is made
         *  to give visual feedback of the selection made
         */

        btrain.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (message.getText().toString().matches(""))
                        Toast.makeText(getApplicationContext(), "Enter the value in 'message' field", Toast.LENGTH_SHORT).show();
                    else {
                        try {
                                mFileWriter = new FileWriter(train,true);
                            }
                        catch(IOException e) {
                            e.printStackTrace();
                        }
                        mes = message.getText().toString() + " - ";
                        //sb1.append(mes);
                        Log.d("Debug: ", "key pressed\n");

                        check = 1;
                        start = true;
                        //sendData("k");

                                                   /*try {
                                // Read from the InputStream
                                Log.d("Debug: ", "Reading in run()\n");
                                bytes = inStream.read(buffer); // Get number of bytes and message in "buffer"
                                String str = buffer.toString();
                                tvalue.setText(str);
                                sb1.append(str);
                                //h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		// Send to message queue Handler
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/

                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("Debug: ", "Stoping...\n");
                   start = false;
                    check = 0;
                    Log.d("Debug: ", "stopped receiving\n");
                    try {
                        mFileWriter.append(mes.toString());
                        mFileWriter.append(sbprint.toString() + "\n");
                        mFileWriter.close();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    sb1.delete(0, sb1.length());

                }
                return true;
            }
        });

        bwork.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    try {
                        mFileWriter = new FileWriter(work,true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("FileNotFoundException occured!");
                    }
                    Log.d("Debug: ", "key pressed\n");
                    mes = " ";
                    check = 2;
                    start = true;
                    /*while (start) {
                        try {
                            // Read from the InputStream
                            Log.d("Debug: ", "Reading in run()\n");
                            bytes = inStream.read(buffer); // Get number of bytes and message in "buffer"
                            String str = buffer.toString();
                            wvalue.setText(str);
                            sb1.append(str);
                            //h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		// Send to message queue Handler
                        } catch (IOException e) {
                            break;
                        }
                    }*/
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("Debug: ", "Stoping...\n");
                    start = false;
                    check = 0;
                    Log.d("Debug: ", "stopped receiving\n");
                    try {
                        PrintWriter writer = new PrintWriter(work);

                        writer.flush();
                        writer.close();
                        mFileWriter.write(sbprint.toString() + "\n");
                        mFileWriter.close();
                        //split sbprint across space into integer array of 3
                        //open train.txt for reading
                        //for line 1 to n
                        //remove text and store it in gesture name
                        //remove till hyphen and store it in string1
                        //split string1 across space into another integer array of 3
                        // check with arr1 and arr2
                        //if equals print text break
                        //else repeat loop

                        tvalue.setText(sbprint);



                        workstring = sbprint.split(" ");


                        for(i=0;i<3;i++) {
                            try {
                                workint[i] = Integer.parseInt(workstring[i]);
                            } catch (NumberFormatException nfe) {
                                //tv.setText("Could not parse " + nfe);
                            }
                        }

                        tvalue.setText("parsed");



                        FileInputStream fis = new FileInputStream(train);
                        DataInputStream in = new DataInputStream(fis);
                        BufferedReader br =
                                new BufferedReader(new InputStreamReader(in));

                        String strLine = br.readLine();;



//                        for(i=0;i<10;i++) {
                        while(strLine != null) {
                            flag=0;
                            int length = 0;
                            rawtrain = "";

                            rawtrain = rawtrain + strLine;
                            length = rawtrain.length();
                            strLine = br.readLine();

                            tvalue.setText(rawtrain);

                            startindex = 0;
                            endindex = rawtrain.indexOf("-", startindex);

                            gestureName = rawtrain.substring(startindex, endindex);

                            tvalue.setText(gestureName);

                            startindex = endindex;
                            endindex = length;

                            tvalue.setText("" + endindex);
                            trainstringspace = rawtrain.substring(startindex + 2, endindex);

                            tvalue.setText(trainstringspace);


                            trainstring = trainstringspace.split(" ");

                            for (i = 0; i < 3; i++) {
                                try {
                                    trainint[i] = Integer.parseInt(trainstring[i]);
                                } catch (NumberFormatException nfe) {
                                    //tv.setText("Could not parse " + nfe);
                                }
                            }

                            tvalue.setText("before compare");

                            if ((workint[0] >= 0 && trainint[0] >= 0) || (workint[0] < 0 && trainint[0] < 0)) {
                                for (i = 1; i < 3; i++) {
                                    if (workint[i] >= trainint[i] - 3 && workint[i] <= trainint[i] + 3) {
                                        flag++;
                                    }
                                }
                            }


                            tvalue.setText("after");
                            tvalue.setText("" + flag);
                            tvalue.setText("" + workint[0] + " " + trainint[0] + " " + workint[1] + " " + trainint[1] + workint[2] + " " + trainint[2]);

                            if (flag == 2) {
                                tvalue.setText(gestureName);
                                break;
                            }


                        }
                        if(flag<2) {
                            tvalue.setText("No gesture matched");
                        }








                       in.close();

















                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    sb1.delete(0, sb1.length());
                    //inputText.setText(myData);
                }
                return true;
            }
        });


    }

    /*public void clearOutput(){
        File output = new File(appFolderPath + "/androidGK_test.txt");
        FileWriter outputWriter = null;
        try{
            outputWriter = new FileWriter(output);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        try{
            outputWriter.write("");
            outputWriter.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }*/

    /*public String displayOutput(){
        File output = new File(appFolderPath + "/output.txt");
        Scanner mScanner = null;
        //FileReader outputReader = null;
        char outputLetter = '\0';
        int i = 0;
        try{
            //outputReader = new FileReader(output);
            mScanner = new Scanner(output);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        if(mScanner.hasNext())
            if(probability==0)
                outputLetter = (char) Byte.parseByte(mScanner.nextLine());
            else if(probability==1) {
                mScanner.nextLine();
                if (mScanner.hasNext())
                    outputLetter = (char) Byte.parseByte(mScanner.nextLine().split(" ")[0]);

            }
        status.setText("predicted letter");
        String outputLetterInString = String.valueOf(outputLetter);
        outputField.setText(outputLetterInString);
        mScanner.close();
        return outputLetterInString;
    }*/


    @Override
    public void onResume() {
        super.onResume();
        // connection methods are best here in case program goes into the background etc

        //connection status shows connection is re-establishing
        //connectionStatus.setText("Connected to: " + deviceName.split("\n")[0] + "(re-establishing)");
        //Get MAC address from MainActivity
        Intent intent = getIntent();
        newAddress = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        // Set up a pointer to the remote device using its address.
        BluetoothDevice device = btAdapter.getRemoteDevice(newAddress);

        //Attempt to create a bluetooth socket for comms
        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Could not create Bluetooth socket", Toast.LENGTH_SHORT).show();
        }

        // Establish the connection.
        try {
            socket.connect();
        } catch (IOException e) {
            try {
                socket.close();        //If IO exception occurs attempt to close socket
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - Could not close Bluetooth socket", Toast.LENGTH_SHORT).show();
            }
        }
        ConnectedThread mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Create a data stream so we can talk to the device

        //connectionStatus.setText("Connected to: " + deviceName.split("\n")[0]);
        //When activity is resumed, attempt to send a piece of junk data ('x') so that it will fail if not connected
        // i.e don't wait for a user to press button to recognise connection failure
    }

    @Override
    public void onPause() {
        super.onPause();
        //Pausing can be the end of an app if the device kills it or the user doesn't open it again
        //close all connections so resources are not wasted


        //Close BT socket to device
        try     {
            socket.close();
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - Failed to close Bluetooth socket", Toast.LENGTH_SHORT).show();
        }
    }
    //takes the UUID and creates a comms socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    //same as in device list activity
    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "ERROR - Device does not support bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    // Method to send data
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
     /*   // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {

                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, 18);
                    Log.i(TAG, "READ: " + readMessage);
                    // Send the obtained bytes to the UI Activity via handler
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                finish();

            }
        }
    }


}
