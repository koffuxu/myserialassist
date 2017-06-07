/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api.sample;

import java.io.IOException;

import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class PillowTestActivity extends SerialPortActivity implements View.OnClickListener {

	private static final String TAG = "PillowTest";
	EditText mReception;
    private String CmdHead = "ztat AT+";
    private String CmdTail = "\r\n";

	private int sendZTATComand(String rawcmd){
        int res = -1;
        String command = CmdHead + rawcmd + CmdTail;
        Log.d(TAG,"the command is:" + command);
        try {
            mOutputStream.write(command.getBytes());
            res = 1;
        } catch (IOException e) {
            e.printStackTrace();
            res = -1;
        }
        Toast.makeText(getApplicationContext(), "send : "+ command,
                Toast.LENGTH_SHORT).show();
        return res;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pillow);
		//for receiver
		mReception= (EditText) findViewById(R.id.EditTextReceiver);
		// for LED Test
		Button btSetLedR = (Button) findViewById(R.id.BtSetLedR);
		Button btSetLedG = (Button) findViewById(R.id.BtSetLedG);
		Button btSetLedB = (Button) findViewById(R.id.BtSetLedB);
		Button btGetLed = (Button) findViewById(R.id.BtGetLed);
		btSetLedR.setOnClickListener(this);
		btSetLedG.setOnClickListener(this);
		btSetLedB.setOnClickListener(this);
		btGetLed.setOnClickListener(this);

	}
	@Override
	public void onClick(View v) {
        switch (v.getId()) {
			case R.id.BtSetLedR:
				Log.d(TAG, "set led R");
                sendZTATComand("ZTLED=SETRGB:255,0,0");
				break;
			case R.id.BtSetLedG:
                Log.d(TAG, "set led G");
                sendZTATComand("ZTLED=SETRGB:0,255,0");
				break;
            case R.id.BtSetLedB:
                Log.d(TAG, "set led B");
                sendZTATComand("ZTLED=SETRGB:0,0,255");
                break;
            case R.id.BtGetLed:
                Log.d(TAG, "get led");
                sendZTATComand("LED=GETRGB");
                break;
		}

	}

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
       Log.d(TAG, "Received " + size +"byte Data:" + new String(buffer,0,size));
		runOnUiThread(new Runnable() {
			public void run() {
				if (mReception != null) {
					mReception.append(new String(buffer, 0, size));
				}
			}
		});
	}

}
