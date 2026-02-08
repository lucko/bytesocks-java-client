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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Implementation of {@link BytesocksClient} using the Java 11 HttpClient and WebSocket APIs.
 */
class BytesocksClientJdk implements BytesocksClient {

    /* The bytesocks urls */
    private final String httpUrl;
    private final String wsUrl;

    /** The client user agent */
    private final String userAgent;

    /** The HTTP client used for both HTTP and WebSocket connections */
    private final HttpClient httpClient;

    BytesocksClientJdk(String host, String userAgent, boolean secure) {
        this.httpUrl = (secure ? "https://" : "http://") + host + "/";
        this.wsUrl = (secure ? "wss://" : "ws://") + host + "/";
        this.userAgent = userAgent;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public Socket createAndConnect(Listener listener) throws Exception {
        String channelId = create();
        return connect(channelId, listener);
    }

    private String create() throws Exception {
        URI uri = URI.create(this.httpUrl + "create");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", this.userAgent)
                .GET()
                .build();

        HttpResponse<Void> response = this.httpClient.send(request, BodyHandlers.discarding());
        if (response.statusCode() != 201) {
            throw new RuntimeException("Request failed with status code: " + response.statusCode());
        }

        String channelId = response.headers().firstValue("Location").orElse(null);
        if (channelId == null) {
            throw new RuntimeException("Location header not returned in response");
        }

        return channelId;
    }

    @Override
    public Socket connect(String channelId, Listener listener) throws Exception {
        URI url = URI.create(this.wsUrl + channelId);

        CompletableFuture<WebSocket> future = this.httpClient.newWebSocketBuilder()
                .header("User-Agent", this.userAgent)
                .buildAsync(url, new WebSocket.Listener() {
                    private StringBuilder messageBuilder = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        listener.onOpen();
                        webSocket.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        this.messageBuilder.append(data);
                        if (last) {
                            listener.onText(this.messageBuilder.toString());
                            this.messageBuilder = new StringBuilder();
                        }
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        listener.onClose(statusCode, reason);
                        return null;
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        listener.onError(error);
                    }
                });

        WebSocket socket = future.join();
        return new SocketImpl(channelId, socket);
    }

    private static final class SocketImpl implements Socket {
        private final String id;
        private final WebSocket ws;

        private SocketImpl(String id, WebSocket ws) {
            this.id = id;
            this.ws = ws;
        }

        @Override
        public String channelId() {
            return this.id;
        }

        @Override
        public boolean isOpen() {
            return !this.ws.isOutputClosed();
        }

        @Override
        public void send(String msg) {
            this.ws.sendText(msg, true);
        }

        @Override
        public void close(int statusCode, String reason) {
            this.ws.abort();
        }

        @Override
        public void closeGracefully(int statusCode, String reason) {
            this.ws.sendClose(statusCode, reason);
        }
    }

}
