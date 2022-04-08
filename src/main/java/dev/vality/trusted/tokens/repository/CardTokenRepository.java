package dev.vality.trusted.tokens.repository;

import com.basho.riak.client.api.RiakClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.trusted.tokens.exception.RiakExecutionException;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CardTokenRepository extends AbstractRepository {

    private final ObjectMapper objectMapper;

    @Value("${riak-config.bucket.token}")
    private String bucket;

    public CardTokenRepository(ObjectMapper objectMapper, RiakClient client) {
        super(client);
        this.objectMapper = objectMapper;
    }

    public CardTokenData get(String key) {
        try {
            log.info("CardTokenRepository get bucket: {} key: {}", bucket, key);
            String value = get(key, bucket);
            return value != null
                    ? objectMapper.readValue(value, CardTokenData.class)
                    : null;
        } catch (Exception e) {
            log.error("Exception in CardTokenRepository when get e: ", e);
            throw new RiakExecutionException(e);
        }
    }

    public void create(Row row) {
        try {
            log.info("CardTokenRepository create in row: {}", row);
            create(row, bucket);
        } catch (Exception e) {
            log.error("Exception in CardTokenRepository when create e: ", e);
            throw new RiakExecutionException();
        }
    }
}
