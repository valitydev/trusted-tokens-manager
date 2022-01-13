package dev.vality.trusted.tokens.config;

import dev.vality.trusted.tokens.initializer.RiakClusterStartupInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class MockedStartupInitializers {

    @MockBean
    private RiakClusterStartupInitializer riakClusterStartupInitializer;

}
