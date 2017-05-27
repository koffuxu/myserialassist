package android_serialport_api;

import java.io.File;

/**
 * Created by Administrator on 2017/5/27.
 */

public class Utils {
    public static void grantPermission(File device){
        /* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}

			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}


    }
}
