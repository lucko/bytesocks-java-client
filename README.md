# bytesocks-java-client

Java client library for [bytesocks](https://github.com/lucko/bytesocks).


Repo: `https://oss.sonatype.org/content/repositories/snapshots/`   
Artifact: `me.lucko:bytesocks-java-client:1.0-SNAPSHOT`

![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=me.lucko%3Abytesocks-java-client&metadataUrl=https%3A%2F%2Foss.sonatype.org%2Fcontent%2Frepositories%2Fsnapshots%2Fme%2Flucko%2Fbytesocks-java-client%2Fmaven-metadata.xml&style=flat-square)

### Example Usage

```java
// Create a client
String host = "bytesocks.example.com";
String userAgent = "github.com/example/your-application";

BytesocksClient client = BytesocksClientFactory.newClient(host, userAgent);

// create a new socket connection
BytesocksClient.Listener listener = new BytesocksClient.Listener() {
    @Override
    public void onText(String data) {
        System.out.println("Got data from channel: " + data);
    }
};

BytesocksClient.Socket socket = client.createAndConnect(listener);
System.out.println("Now connected to channel " + socket.getChannelId());

// Send a message to the channel
socket.send("Hello!");
```
