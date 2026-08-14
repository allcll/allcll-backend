package kr.allcll.backend.client;

import java.net.InetSocketAddress;
import java.net.Proxy;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;

@Slf4j
public class ConnectionEventListener extends EventListener {

    @Override
    public void connectStart(Call call, InetSocketAddress inetSocketAddress, Proxy proxy) {
        log.info("[CONN] new connection to {}", inetSocketAddress);
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
