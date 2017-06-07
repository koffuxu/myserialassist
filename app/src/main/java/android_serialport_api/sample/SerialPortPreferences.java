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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import android_serialport_api.SerialPortFinder;

import static android.content.ContentValues.TAG;

public class SerialPortPreferences extends PreferenceActivity {

	private Application mApplication;
	private SerialPortFinder mSerialPortFinder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = (Application) getApplication();
		mSerialPortFinder = mApplication.mSerialPortFinder;

		addPreferencesFromResource(R.xml.serial_port_preferences);

		// Devices
		final ListPreference devices = (ListPreference)findPreference("DEVICE");
        String[] entries = mSerialPortFinder.getAllDevices();
        if(entries == null){
            Log.e(Application.TAG, "the all devices enteries is null, force to finished");
            DisplayError(R.string.error_dev);
            return;
        }
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
		devices.setEntries(entries);
		devices.setEntryValues(entryValues);
		devices.setSummary(devices.getValue());
		devices.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String)newValue);
				return true;
			}
		});

		// Baud rates
		final ListPreference baudrates = (ListPreference)findPreference("BAUDRATE");
		baudrates.setSummary(baudrates.getValue());
		baudrates.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((String)newValue);
				return true;
			}
		});


	}

    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SerialPortPreferences.this.finish();
            }
        });
        b.show();
    }

    @Override
    protected void onDestroy() {
        Log.d(Application.TAG,"SerialPortPreferences onDestroy");
        super.onDestroy();
    }
}
