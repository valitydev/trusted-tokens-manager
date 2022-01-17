package dev.vality.trusted.tokens.repository;

import com.basho.riak.client.api.RiakClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.trusted.tokens.ConditionTemplate;
import dev.vality.trusted.tokens.exception.RiakExecutionException;
import dev.vality.trusted.tokens.model.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConditionTemplateRepository extends AbstractRepository {

    private final ObjectMapper objectMapper;
    @Value("${riak.bucket.template}")
    private String bucket;

    public ConditionTemplateRepository(RiakClient client, ObjectMapper objectMapper) {
        super(client);
        this.objectMapper = objectMapper;
    }

    public ConditionTemplate get(String key) {
        try {
            log.info("ConditionTemplateRepository get bucket: {} key: {}", bucket, key);
            String value = get(key, bucket);
            return value != null
                    ? objectMapper.readValue(value, ConditionTemplate.class)
                    : null;
        } catch (Exception e) {
            log.error("Exception in ConditionTemplateRepository when get e: ", e);
            throw new RiakExecutionException(e);
        }
    }

    public void create(Row row) {
        log.info("ConditionTemplateRepository create in row: {}", row);
        create(row, bucket);
    }
}
