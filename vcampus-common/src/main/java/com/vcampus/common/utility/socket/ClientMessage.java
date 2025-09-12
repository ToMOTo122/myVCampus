package com.vcampus.common.utility.socket;

import com.vcampus.common.entity.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientMessage {
    private static final String SERVER_IP = "127.0.0.1"; // 或你的服务器IP地址
    private static final int SERVER_PORT = 8888; // 你的服务器端口

    public static Message sendAndReceive(Message request) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            // 发送请求消息
            oos.writeObject(request);
            oos.flush();

            // 接收响应消息
            return (Message) ois.readObject();
        }
    }
}