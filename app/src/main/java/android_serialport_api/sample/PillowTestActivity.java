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
import android.os.Handler;
import android.os.Message;
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
    private String CmdHead = "ZTAT+";
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
                //sendZTATComand("SETRGB=255,0,0");
				//test at response
				String resp = "+OK=GETRGB,255,0,0\r\n";
				try {
					mOutputStream.write(resp.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	private enum handler_key {
		/** 更新界面 */
		PARSER_COMMAND,
		DISCONNECT,
	}

	protected void updateUI(String[] strArry) {
		String CODE = strArry[0];
		switch (CODE ) {
			case "GETRGB":
			    Log.d(TAG, "set RGB:("+strArry[1]+","+strArry[2]+","+strArry[3]+")");
				break;
			default:
				break;
		}
	}

	Handler parserHander = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key){
				case PARSER_COMMAND:
					// TODO: 2017/6/11
					parserATZTResponse(msg.obj.toString());
					break;
				default:
					break;
			}
		}
	};

	//ATZT response frame format
	/*
	 * +<RSP>=[op][para1],[para2], [para3] ,[para4]…<CR><LF>
	 * STATE=VALUE
	 * RSP: OK / ERR
	 * op: =
	 */
	private void parserATZTResponse(String rawCommand) {
		Log.d(TAG, "begain to parser raw command:"+rawCommand);
        if( rawCommand.startsWith("+OK")) {
			//get the subString
			int index = rawCommand.indexOf('=');
			String subRawCommand = rawCommand.substring(index+1,rawCommand.length());
			String[] code = subRawCommand.split(",");
			//for debug begin
			for(int i = 0; i<code.length;i++)
				Log.d(TAG,"code index "+i+" is:"+code[i]);

			updateUI(code);
		} else {
			//TODO show ERROR Alter
			Log.d(TAG, "Received a error response of:"+rawCommand);
		}

	}

	/**
	 * 1, get the valid String: +...../r
	 * 2, get the value
	 * @param buffer
	 * @param size
	 */
	StringBuilder reponseStr = new StringBuilder();
	boolean matchHead=false, matchTail=false;
	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		Log.d(TAG, "Received " + size +" byte Data:" + new String(buffer,0,size));
        String fragStr = new String(buffer, 0, size);
		boolean loopTag = false;
		int headIndex = 0;
		//just match tail is /r
		for (int i=0; i<fragStr.length(); i++){
			char c = fragStr.charAt(i);
			//match head 'S'
			if ( (matchHead==false) && (c == '+')){
                matchHead = true;
				loopTag = true;
                headIndex = i;
			}
			//match tail 'B'
			if(matchHead && (c == '\r')){
				if(loopTag == true ) {
                    //match in same frag string
					Log.d(TAG, ">>> frag match head && tail:"+fragStr.substring(headIndex,i+1) + "; at index:"+i);
					reponseStr.append(fragStr.substring(headIndex,i+1));
					matchTail = true;
				} else {
					Log.d(TAG, ">>> frag match tail:"+fragStr.substring(0,i+1) + "; at index:"+i);
					//match head and tail in different frag string
					reponseStr.append(fragStr.substring(0,i+1));
					matchTail = true;
				}
				matchHead = false;
				break;
			} else {
				//
			}

		}
		if(matchTail == true) { //match head and tail
            String rawComand = reponseStr.toString();
			Log.d(TAG, ">>>Bingo<<<take the AT Reponse Command:"+rawComand);
			reponseStr.setLength(0);
			matchTail = false;
			//send message to post process
			Message msg = Message.obtain();
			msg.what = handler_key.PARSER_COMMAND.ordinal();
			msg.obj = rawComand;
			parserHander.sendMessage(msg);
			//parserHander.sendEmptyMessage(handler_key.PARSER_COMMAND.ordinal());
		} else if ((matchHead == true) /*&& (loopTag == true)*/){ //match head
			Log.d(TAG, ">>> Frag  match Head:"+fragStr.substring(headIndex,fragStr.length())+"; at head index:"+headIndex);
			reponseStr.append(fragStr.substring(headIndex,fragStr.length()));
			Log.d(TAG,"###BUG, reponseStr:" + reponseStr);
		} else {//match nothing
			Log.d(TAG, "XXX this frag was nothing");
		}

		/**
		//use Linenumber Reader
		try {
			while(mLineReader.ready()){
				Log.d(TAG, "get a line data: " + mLineReader.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}*/


        /*
		runOnUiThread(new Runnable() {
			public void run() {
				if (mReception != null) {
					mReception.append(new String(buffer, 0, size));
				}
			}
		});
		*/
	}

}
