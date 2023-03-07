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
 * Factory to create {@link BytesocksClient} instances.
 */
public class BytesocksClientFactory {

    private static final boolean SUPPORTED;

    static {
        boolean supported;
        try {
            supported = BytesocksClientImpl.isSupported();
        } catch (UnsupportedClassVersionError e) {
            supported = false;
        }
        SUPPORTED = supported;
    }

    private BytesocksClientFactory() {
        throw new AssertionError();
    }

    /**
     * Checks if creating a client is supported
     *
     * @return true if supported
     */
    public static boolean isSupported() {
        return SUPPORTED;
    }

    /**
     * Creates a new {@link BytesocksClient}.
     *
     * @param host the host
     * @param userAgent the user agent
     * @return the client
     */
    public static BytesocksClient newClient(String host, String userAgent) {
        return SUPPORTED ? new BytesocksClientImpl(host, userAgent) : null;
    }

}
