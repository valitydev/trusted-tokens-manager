package dev.vality.trusted.tokens;

import com.basho.riak.client.api.RiakClient;
import dev.vality.trusted.tokens.initializer.EventStreamsPool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PreDestroy;

@ServletComponentScan
@SpringBootApplication
@RequiredArgsConstructor
public class TrustedTokensApplication extends SpringApplication {

    private final RiakClient client;
    private final EventStreamsPool eventStreamsPool;

    public static void main(String[] args) {
        SpringApplication.run(TrustedTokensApplication.class, args);
    }

    @PreDestroy
    public void preDestroy() {
        eventStreamsPool.cleanAll();
        client.shutdown();
    }
}
