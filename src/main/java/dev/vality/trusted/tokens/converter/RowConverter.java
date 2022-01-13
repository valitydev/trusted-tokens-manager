package dev.vality.trusted.tokens.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.trusted.tokens.model.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RowConverter {

    private final ObjectMapper objectMapper;

    public Row convert(String key, Object value) {
        try {
            Row row = new Row();
            row.setKey(key);
            row.setValue(objectMapper.writeValueAsString(value));
            return row;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
