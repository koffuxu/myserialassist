package android_serialport_api;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android_serialport_api.sample.Application;

/**
 * Created by Administrator on 2017/5/27.
 */

public class Utils {
    private static final String TAG = "SerialUtils-utils";
    public static int grantPermission(File device){
        int result = 0;
        Log.d(TAG,"try to grant permission for:"+device.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder("/system/bin/sh");
        //java.lang.ProcessBuilder:  Creates operating system processes.
        pb.directory(new File("/"));//设置shell的当前目录。
        try {
            Process proc = pb.start();
            //获取输入流，可以通过它获取SHELL的输出。
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            //获取输出流，可以通过它向SHELL发送命令。
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc
                    .getOutputStream())), true);
            out.print("su root ");
            out.println("chmod 777 " + device.getAbsolutePath());//执行这一句时会弹出对话框（以下程序要求授予最高权限...），要求用户确认。
            out.println("exit");
            result = 1;
            // proc.waitFor();
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);   // 打印输出结果
            }
            while ((line = err.readLine()) != null) {
                System.out.println(line);  // 打印错误输出结果
                result = 0;
            }
            in.close();
            out.close();
            proc.destroy();
        } catch (Exception e) {
            System.out.println("exception:" + e);
        }
        return result;
    }
}
