package dev.vality.trusted.tokens.initializer;

import com.basho.riak.client.core.RiakCluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiakClusterStartupInitializer {

    private final RiakCluster riakCluster;

    @EventListener(value = ApplicationReadyEvent.class)
    @Order
    public void onStartup() {
        log.info("Starting RiakCluster");
        riakCluster.start();
    }
}
