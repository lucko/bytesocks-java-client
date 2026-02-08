package me.lucko.bytesocks.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@Execution(ExecutionMode.CONCURRENT)
public class BytesocksClientTest {

    public static final String from1 = "hello from 1 - " + generateRandom(10000);
    public static final String from2 = "hello from 2 - " + generateRandom(10000);

    public static String generateRandom(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) (Math.random() * 26 + 'a'));
        }
        return sb.toString();
    }

    @RepeatedTest(100)
    @Disabled // don't run all the time
    public void testClientBulk() throws Exception {
        AtomicInteger open = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();
        AtomicInteger text1 = new AtomicInteger();
        AtomicInteger text2 = new AtomicInteger();
        AtomicInteger close = new AtomicInteger();


        Thread.sleep(ThreadLocalRandom.current().nextInt(100));

        BytesocksClient client = BytesocksClient.createInsecure("localhost:3000", "spark-plugin");
        BytesocksClient.Socket socket1 = client.createAndConnect(new BytesocksClient.Listener() {
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
                if (data.equals(from2)) {
                    text1.incrementAndGet();
                } else {
                    error.incrementAndGet();
                    fail();
                }
            }

            @Override
            public void onClose(int statusCode, String reason) {
                close.incrementAndGet();
            }
        });

        Thread.sleep(500 + ThreadLocalRandom.current().nextInt(100));

        BytesocksClient.Socket socket2 = client.connect(socket1.channelId(), new BytesocksClient.Listener() {
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
                if (data.equals(from1)) {
                    text2.incrementAndGet();
                } else {
                    error.incrementAndGet();
                    fail();
                }
            }

            @Override
            public void onClose(int statusCode, String reason) {
                close.incrementAndGet();
            }
        });

        Thread.sleep(500 + ThreadLocalRandom.current().nextInt(100));

        assertEquals(2, open.get());
        assertEquals(0, error.get());
        assertEquals(0, text1.get());
        assertEquals(0, text2.get());
        assertEquals(0, close.get());

        for (int i = 0; i < 20; i++) {
            socket1.send(from1);
            Thread.sleep(500 + ThreadLocalRandom.current().nextInt(100));

            assertEquals(0, error.get());
            assertEquals(0 + i, text1.get());
            assertEquals(1 + i, text2.get());


            socket2.send(from2);
            Thread.sleep(500 + ThreadLocalRandom.current().nextInt(100));

            assertEquals(0, error.get());
            assertEquals(1 + i, text1.get());
            assertEquals(1 + i, text2.get());
        }

        socket1.close(1001, "hello");
        socket2.close(1001, "hello");

        Thread.sleep(1000);

        assertEquals(0, error.get());
    }

}
