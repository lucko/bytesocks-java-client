package me.lucko.bytesocks.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class BytesocksClientTest {

    @Test
    @Disabled // don't run all the time
    public void testClient() throws Exception {
        AtomicInteger open = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();
        AtomicInteger text = new AtomicInteger();
        AtomicInteger close = new AtomicInteger();

        BytesocksClient client = BytesocksClient.create("spark-usersockets.lucko.me", "spark-plugin");
        BytesocksClient.Socket socket = client.createAndConnect(new BytesocksClient.Listener() {
            @Override
            public void onOpen() {
                open.incrementAndGet();

            }

            @Override
            public void onError(Throwable e) {
                error.incrementAndGet();
                fail(e);
            }

            @Override
            public void onText(String data) {
                text.incrementAndGet();
            }

            @Override
            public void onClose(int statusCode, String reason) {
                close.incrementAndGet();
            }
        });

        assertEquals(1, open.get());
        assertEquals(0, error.get());
        assertEquals(0, text.get());
        assertEquals(0, close.get());

        socket.send("hello");

        socket.close(1001, "hello");

        assertEquals(0, error.get());
    }

}
