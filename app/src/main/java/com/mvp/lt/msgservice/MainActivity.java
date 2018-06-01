package com.mvp.lt.msgservice;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MainActivity extends AppCompatActivity {

    public static ServerSocket serverSocket = null;
    public static TextView mTextView, left_msg, service_ip, right_msg, name;
    private String IP = "";
    String servicebuffer = "";
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0x11) {
                Bundle bundle = msg.getData();
                String msgString = bundle.getString("msg");
                left_msg.append(msgString + "\n");
            }
        }

        ;
    };

    Button send;
    EditText ed1;
    String geted1;
    private String ip;
    private String mNameString = "小二";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //服务器
        service_ip = (TextView) findViewById(R.id.service_ip);
        name = (TextView) findViewById(R.id.name);

        send = (Button) findViewById(R.id.send);
        ed1 = (EditText) findViewById(R.id.ed1);

        //客户端
        mTextView = (TextView) findViewById(R.id.textsss);
        left_msg = (TextView) findViewById(R.id.left_msg);
        //
        right_msg = (TextView) findViewById(R.id.right_msg);


        IP = getlocalip();
        mTextView.setText("本机ip地址:" + IP);
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mNameString = name.getText().toString();
                ip = service_ip.getText().toString();
                geted1 = ed1.getText().toString();
                right_msg.append(mNameString + ":" + geted1 + "\n");
                //启动线程 向服务器发送和接收信息
                if (TextUtils.isEmpty(geted1)) {
                    geted1 = " ";
                }
                new MyThread(mNameString + ":" + geted1).start();
            }
        });
        //接收消息
        new Thread() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.clear();
                OutputStream output;
                String str = "";
                try {
                    serverSocket = new ServerSocket(30000);
                    while (true) {
                        Message msg = new Message();
                        msg.what = 0x11;
                        try {
                            Socket socket = serverSocket.accept();
                            output = socket.getOutputStream();
                            output.write(str.getBytes("UTF-8"));
                            output.flush();
                            socket.shutdownOutput();
                            BufferedReader bff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String line = null;
                            servicebuffer = "";
                            while ((line = bff.readLine()) != null) {
                                servicebuffer = line + servicebuffer;
                            }
                            bundle.putString("msg", servicebuffer.toString());
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                            bff.close();
                            output.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            ;
        }.start();
    }

    //发送消息
    Socket socket = null;
    String buffer = "";

    class MyThread extends Thread {

        public String txt1;

        public MyThread(String str) {
            txt1 = str;
        }

        @Override
        public void run() {
            //定义消息
            Message msg = new Message();
            msg.what = 0x12;
            Bundle bundle = new Bundle();
            bundle.clear();
            try {
                //连接服务器 并设置连接超时为1秒
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, 30000), 1000); //端口号为30000
                //获取输入输出流
                OutputStream ou = socket.getOutputStream();
                BufferedReader bff = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
//                //读取服务器信息发来的消息
//                String line = null;
//                buffer = "";
//                while ((line = bff.readLine()) != null) {
//                    buffer = line + buffer;
//                }

                //向服务器发送信息
                ou.write(txt1.getBytes("UTF-8"));
                ou.flush();
//                bundle.putString("msg", buffer.toString());
//                msg.setData(bundle);
//               // 发送消息 修改UI线程中的组件
//                mHandler.sendMessage(msg);
                //关闭各种输入输出流
                bff.close();
                ou.close();
                socket.close();
            } catch (SocketTimeoutException aa) {
                //连接超时 在UI界面显示消息
                bundle.putString("msg", "服务器连接失败！请检查网络是否打开");
                msg.setData(bundle);
                //发送消息 修改UI线程中的组件
                mHandler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 或取本机的ip地址
     */
    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        //  Log.d(Tag, "int ip "+ipAddress);
        if (ipAddress == 0) {
            return null;
        }
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }
}
