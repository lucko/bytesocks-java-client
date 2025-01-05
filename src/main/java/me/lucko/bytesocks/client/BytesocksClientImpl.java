/*
 * This file is part of bytesocks-java-client.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.bytesocks.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of {@link BytesocksClient}.
 */
class BytesocksClientImpl implements BytesocksClient {

    /* The bytesocks urls */
    private final String httpUrl;
    private final String wsUrl;

    /** The client user agent */
    private final String userAgent;

    BytesocksClientImpl(String host, String userAgent) {
        this.httpUrl = "https://" + host + "/";
        this.wsUrl = "wss://" + host + "/";
        this.userAgent = userAgent;
    }

    @Override
    public Socket createAndConnect(Listener listener) throws Exception {
        String channelId = create();
        return connect(channelId, listener);
    }

    private String create() throws IOException {
        URL url = URI.create(this.httpUrl + "create").toURL();
        HttpURLConnection connection = ((HttpURLConnection) url.openConnection());

        try {
            connection.setRequestProperty("User-Agent", this.userAgent);
            connection.connect();

            if (connection.getResponseCode() != 201) {
                throw new RuntimeException("Request failed: " + connection.getResponseMessage());
            }

            String channelId = connection.getHeaderField("Location");
            if (channelId == null) {
                throw new RuntimeException("Location header not returned: " + connection);
            }

            return channelId;

        } finally {
            connection.disconnect();
        }
    }

    @Override
    public Socket connect(String channelId, Listener listener) throws Exception {
        URI url = URI.create(this.wsUrl + channelId);

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", this.userAgent);

        WebSocketClient socket = new WebSocketClient(url, headers) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                listener.onOpen();
            }

            @Override
            public void onMessage(String message) {
                listener.onText(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                listener.onClose(code, reason);
            }

            @Override
            public void onError(Exception ex) {
                listener.onError(ex);
            }
        };
        socket.setDaemon(true);
        socket.connectBlocking();

        return new SocketImpl(channelId, socket);
    }

    private static final class SocketImpl implements Socket {
        private final String id;
        private final WebSocketClient ws;

        private SocketImpl(String id, WebSocketClient ws) {
            this.id = id;
            this.ws = ws;
        }

        @Override
        public String channelId() {
            return this.id;
        }

        @Override
        public boolean isOpen() {
            return this.ws.isOpen();
        }

        @Override
        public void send(String msg) {
            this.ws.send(msg);
        }

        @Override
        public void close(int statusCode, String reason) {
            this.ws.closeConnection(statusCode, reason);
        }

        @Override
        public void closeGracefully(int statusCode, String reason) {
            this.ws.close(statusCode, reason);
        }
    }

}
