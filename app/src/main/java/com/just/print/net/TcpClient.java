package com.just.print.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpClient implements Runnable {

    final String ip;
    final int port;
    final byte[] output;

    TcpClient(String ip, int port, byte[] output) {
        this.ip = ip;
        this.port = port;
        this.output = output;
    }

    @Override
    public void run() {
        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        try {
            socket = new Socket(ip, port);
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (os != null && output != null)
            try {
                os.write(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (os != null)
            try {
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
