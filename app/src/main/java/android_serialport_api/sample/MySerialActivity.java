//package android.serialport;
package android_serialport_api.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import android_serialport_api.HexDump;

public class MySerialActivity extends SerialPortActivity {
    /** Called when the activity is first created. */
    private static final String TAG = "SerialPort";	
    EditText mSend;
    EditText mReception;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myserial);

        mSend = (EditText) findViewById(R.id.EditSend);
        mReception= (EditText) findViewById(R.id.EditTextEmission);

        final Button btSendAssic= (Button)findViewById(R.id.BtSendAssic);
        final Button btSendHex= (Button)findViewById(R.id.BtSendHex);
        final Button btCleanRev = (Button) findViewById(R.id.BtCleanRev);
        final Button btCleanSed = (Button) findViewById(R.id.BtCleanSed);
        btSendAssic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int i;
                CharSequence t = mSend.getText();
                Log.i(TAG, "send Text:"+mSend.getText());
                char[] text = new char[t.length()];
                for (i=0; i<t.length(); i++) {
                    text[i] = t.charAt(i);
                }
                try {
                    mOutputStream.write(new String(text).getBytes());
                    mOutputStream.write('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "send as assic",
                    Toast.LENGTH_SHORT).show();

            }
        });

        btSendHex.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int len;
                int resultFlag = 0;
                CharSequence t = mSend.getText();
                len = t.length();
                byte[] b = new byte[len/2];
                //check the string is Hex String
                if(len % 2 != 0){
                    Log.i(TAG, "The counts of String is not EVEN!");
                    Toast.makeText(getApplicationContext(), "Error: hex not EVEN counts!",
                            Toast.LENGTH_SHORT).show();
                    return ;
                }
                for (int i=0; i<len; i++) {
                    int value = Character.digit(t.charAt(i), 16);
                    if(value < 0){
                        Log.e(TAG, "please input the HEX number from 1 to F");
                        resultFlag = -1;
                        break;
                    }
                }
                //convert hex string
                if(resultFlag != -1){
                    b = HexDump.hexStringToByteArray(t.toString());
                    try {
                        mOutputStream.write(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "send as hex",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error when send as hex",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        btCleanRev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReception.setText("");
            }
        });
        btCleanSed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSend.setText("");
            }
        });
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
