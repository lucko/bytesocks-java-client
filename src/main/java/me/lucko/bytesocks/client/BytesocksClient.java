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

/**
 * A client that can interact with bytesocks.
 *
 * @see <a href="https://github.com/lucko/bytesocks">https://github.com/lucko/bytesocks</a>
 */
public interface BytesocksClient {

    /**
     * Creates a new {@link BytesocksClient}.
     *
     * @param host the host
     * @param userAgent the user agent
     * @return the client
     */
    static BytesocksClient create(String host, String userAgent) {
        return new BytesocksClientImpl(host, userAgent);
    }

    /**
     * Creates a new bytesocks channel and returns a socket connected to it.
     *
     * @param listener the listener
     * @return the socket
     * @throws Exception if something goes wrong
     */
    Socket createAndConnect(Listener listener) throws Exception;

    /**
     * Connects to an existing bytesocks channel.
     *
     * @param channelId the channel id
     * @param listener the listener
     * @return the socket
     * @throws Exception if something goes wrong
     */
    Socket connect(String channelId, Listener listener) throws Exception;

    /**
     * A socket connected to a bytesocks channel.
     */
    interface Socket {

        /**
         * Gets the id of the connected channel.
         *
         * @return the id of the channel
         */
        String channelId();

        /**
         * Gets if the socket is open.
         *
         * @return true if the socket is open
         */
        boolean isOpen();

        /**
         * Sends a message to the channel using the socket.
         *
         * @param msg the message to send
         */
        void send(String msg);

        /**
         * Closes the socket connection.
         *
         * @param statusCode the status code
         * @param reason the reason
         */
        void close(int statusCode, String reason);

        /**
         * Sends a message to gracefully close the socket connection.
         *
         * @param statusCode the status code
         * @param reason the reason
         */
        void closeGracefully(int statusCode, String reason);
    }

    /**
     * Socket listener
     */
    interface Listener {

        default void onOpen() {}

        default void onError(Throwable error) {}

        default void onText(String data) {}

        default void onClose(int statusCode, String reason) {}
    }

}
