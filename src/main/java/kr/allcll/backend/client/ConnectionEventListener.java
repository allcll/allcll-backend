package kr.allcll.backend.client;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Protocol;

@Slf4j
public class ConnectionEventListener extends EventListener {

    private long connectStartNanos;

    @Override
    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        connectStartNanos = System.nanoTime();
        log.info("[CONN] new connection to {}", inetSocketAddress);
    }

    @Override
    public void connectEnd(Call call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - connectStartNanos);
        log.info("[CONN] connected: {}ms, host={}, protocol={}", elapsedMs, inetSocketAddress, protocol);
    }

    @Override
    public void connectionAcquired(Call call, Connection connection) {
        log.info("[CONN] acquired: {}", connection);
    }

    @Override
    public void connectionReleased(Call call, Connection connection) {
        log.info("[CONN] released: {}", connection);
    }
}
