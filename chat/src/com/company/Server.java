package com.company;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Server {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();

        ServerSocketChannel serverCh = ServerSocketChannel.open();
        serverCh.configureBlocking(false);
        serverCh.bind(new InetSocketAddress(9000));
        serverCh.register(selector, SelectionKey.OP_ACCEPT);

        ArrayList<SocketChannel> clientChList = new ArrayList<>();
        System.out.println("Server Listening on port 9000");
        while (true) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();

                if (key.isAcceptable()) {
                    ServerSocketChannel ch = (ServerSocketChannel) key.channel();
                    SocketChannel clientCh = ch.accept();
                    clientChList.add(clientCh);
                    System.out.println("Connect from " + clientCh.getRemoteAddress());
                    clientCh.configureBlocking(false);
                    clientCh.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    SocketChannel ch = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(20);
                    ch.configureBlocking(false);
                    buffer.clear();
                    int n = ch.read(buffer);
                    if (n == -1) {
                        ch.close();
                        continue;
                    }
                    buffer.flip();
                    String message = new String(buffer.array());

                    for (SocketChannel client : clientChList) {
                        ByteBuffer buffer2 = ByteBuffer.allocate(20);
                        buffer2.put(message.getBytes());
                        buffer2.flip();
                        client.write(buffer2);
                    }
                }
                it.remove();
            }
        }
    }
}
