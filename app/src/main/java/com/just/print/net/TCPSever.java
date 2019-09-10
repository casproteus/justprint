package com.just.print.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

public class TCPSever extends Thread {
    Executor mainExecutor;
    final int port;
    TcpServerReadOver tcp;

    TCPSever(int port, TcpServerReadOver callback, Executor mainExecutor) {
        this.port = port;
        this.tcp = callback;
        this.mainExecutor = mainExecutor;
    }

    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!Thread.interrupted() && server != null) {
            try {
                Socket socket = server.accept();
                System.err.println("new client "
                        + socket.getRemoteSocketAddress());
                mainExecutor.execute(new Task(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Task implements Runnable {
        final Socket socket;

        Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            InputStream in = null;
            ByteArrayOutputStream oss = new ByteArrayOutputStream();
            try {
                in = socket.getInputStream();
                byte[] buff = new byte[10240];
                int l = 0;
                if (in != null)
                    try {
                        while ((l = in.read(buff)) != -1) {
                            oss.write(buff, 0, l);
                        }
                    } catch (Exception e) {
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
            tcp.onReadOver(socket.getInetAddress().getHostName(),
                    oss.toByteArray());
        }

    }

    interface TcpServerReadOver {
        void onReadOver(String host, byte[] input);
    }
}
