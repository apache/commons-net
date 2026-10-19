# How To Reproduce 

This reproducer refers to [this ticket](https://issues.apache.org/jira/browse/NET-718) which does report an "Unsupported or unrecognized SSL message" error. This error surfaced when we bumped from 2.8 to 2.9 [here](https://github.com/akka/alpakka/pull/2945).

While making the reproducer, a different error happens:

```
javax.net.ssl.SSLException: Connection reset
```

The scenario is when running an FTPS connection via proxy, as shown in the reproducer spec [FTPSProxyClientTest.java](./src/test/java/org/apache/commons/net/ftp/FTPSProxyClientTest.java). 

Before you run this spec, you have to have a proxy (squid) and an FTP server running.

## Squid + FTP on Linux

```
docker-compose up
```

## Squid + FTP on Mac

The squid part of docker-compose depends on `network_mode: host` which does not work on Mac. You have to run squid locally and only start ftp via docker-compose.

```
brew install squid
```

Once done, put the [content of the squid config file](src/test/resources/squid.conf) into `/usr/local/etc/squid.conf`.

```
brew services restart squid
```

Now start ftp via `docker-compose up ftp`.

---

Now, if you run the spec, you should see:

```
javax.net.ssl.SSLException: Connection reset

	at java.base/sun.security.ssl.Alert.createSSLException(Alert.java:127)
	at java.base/sun.security.ssl.TransportContext.fatal(TransportContext.java:326)
	at java.base/sun.security.ssl.TransportContext.fatal(TransportContext.java:269)
	at java.base/sun.security.ssl.TransportContext.fatal(TransportContext.java:264)
	at java.base/sun.security.ssl.SSLTransport.decode(SSLTransport.java:137)
	at java.base/sun.security.ssl.SSLSocketImpl.decode(SSLSocketImpl.java:1144)
	at java.base/sun.security.ssl.SSLSocketImpl.readHandshakeRecord(SSLSocketImpl.java:1055)
	at java.base/sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:395)
	at org.apache.commons.net.ftp.FTPSClient._openDataConnection_(FTPSClient.java:278)
	at org.apache.commons.net.ftp.FTPClient._openDataConnection_(FTPClient.java:639)
	at org.apache.commons.net.ftp.FTPClient.initiateListParsing(FTPClient.java:1989)
	at org.apache.commons.net.ftp.FTPClient.initiateListParsing(FTPClient.java:2085)
	at org.apache.commons.net.ftp.FTPClient.listFiles(FTPClient.java:2283)
	at org.apache.commons.net.ftp.FTPClient.listFiles(FTPClient.java:2249)
	at org.apache.commons.net.ftp.FTPSProxyClientTest.testListFiles(FTPSProxyClientTest.java:94)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:299)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:293)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.lang.Thread.run(Thread.java:834)
	Suppressed: java.net.SocketException: Broken pipe (Write failed)
		at java.base/java.net.SocketOutputStream.socketWrite0(Native Method)
		at java.base/java.net.SocketOutputStream.socketWrite(SocketOutputStream.java:110)
		at java.base/java.net.SocketOutputStream.write(SocketOutputStream.java:150)
		at java.base/sun.security.ssl.SSLSocketOutputRecord.encodeAlert(SSLSocketOutputRecord.java:81)
		at java.base/sun.security.ssl.TransportContext.fatal(TransportContext.java:357)
		... 25 more
Caused by: java.net.SocketException: Connection reset
	at java.base/java.net.SocketInputStream.read(SocketInputStream.java:186)
	at java.base/java.net.SocketInputStream.read(SocketInputStream.java:140)
	at java.base/sun.security.ssl.SSLSocketInputRecord.read(SSLSocketInputRecord.java:448)
	at java.base/sun.security.ssl.SSLSocketInputRecord.decode(SSLSocketInputRecord.java:165)
	at java.base/sun.security.ssl.SSLTransport.decode(SSLTransport.java:108)
	... 22 more


javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake

	at java.base/sun.security.ssl.SSLSocketImpl.handleEOF(SSLSocketImpl.java:1313)
	at java.base/sun.security.ssl.SSLSocketImpl.decode(SSLSocketImpl.java:1152)
	at java.base/sun.security.ssl.SSLSocketImpl.readHandshakeRecord(SSLSocketImpl.java:1055)
	at java.base/sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:395)
	at org.apache.commons.net.ftp.FTPSClient._openDataConnection_(FTPSClient.java:278)
	at org.apache.commons.net.ftp.FTPClient._openDataConnection_(FTPClient.java:639)
	at org.apache.commons.net.ftp.FTPClient.initiateListParsing(FTPClient.java:1989)
	at org.apache.commons.net.ftp.FTPClient.initiateListParsing(FTPClient.java:2085)
	at org.apache.commons.net.ftp.FTPClient.listFiles(FTPClient.java:2283)
	at org.apache.commons.net.ftp.FTPClient.listFiles(FTPClient.java:2249)
	at org.apache.commons.net.ftp.FTPSProxyClientTest.testListFiles(FTPSProxyClientTest.java:94)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:299)
	at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:293)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.lang.Thread.run(Thread.java:834)
	Suppressed: java.net.SocketException: Broken pipe (Write failed)
		at java.base/java.net.SocketOutputStream.socketWrite0(Native Method)
		at java.base/java.net.SocketOutputStream.socketWrite(SocketOutputStream.java:110)
		at java.base/java.net.SocketOutputStream.write(SocketOutputStream.java:150)
		at java.base/sun.security.ssl.SSLSocketOutputRecord.encodeAlert(SSLSocketOutputRecord.java:81)
		at java.base/sun.security.ssl.TransportContext.fatal(TransportContext.java:357)
		at java.base/sun.security.ssl.TransportContext.fatal(TransportContext.java:269)
		at java.base/sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:398)
		... 19 more
Caused by: java.io.EOFException: SSL peer shut down incorrectly
	at java.base/sun.security.ssl.SSLSocketInputRecord.decode(SSLSocketInputRecord.java:167)
	at java.base/sun.security.ssl.SSLTransport.decode(SSLTransport.java:108)
	at java.base/sun.security.ssl.SSLSocketImpl.decode(SSLSocketImpl.java:1144)
	... 21 more

SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Loading file:/Users/sebalf/dev/sebastian-alfers/commons-net/target/test-classes/org/apache/commons/net/ftpsserver/ftpserver.jks

Process finished with exit code 255

```

---

## Possible Solution a

When I uncomment [this lines](https://github.com/apache/commons-net/blob/master/src/main/java/org/apache/commons/net/ftp/FTPSClient.java#L794-L796) the test passes:

```
if (getProxy() != null) {
    sslSocket = context.getSocketFactory().createSocket(socket, getPassiveHost(), getPassivePort(), true);
}
```

## Possible Solution b

Re-inroduce the call to `super._openDataConnection_(command, arg);` made [here](https://github.com/apache/commons-net/pull/90/files#diff-b4292a5bd3e39f502d24bce1eb934384a951a120080c870cdc68c0585a78c6e9R269).