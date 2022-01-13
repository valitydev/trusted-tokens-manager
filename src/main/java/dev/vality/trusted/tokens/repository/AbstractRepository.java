package dev.vality.trusted.tokens.repository;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.cap.Quorum;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import dev.vality.trusted.tokens.exception.RiakExecutionException;
import dev.vality.trusted.tokens.model.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRepository {

    private static final String APPLICATION_JSON = "application/json";
    private final RiakClient client;

    public String get(String key, String bucketName) {
        try {
            Location quoteObjectLocation = createLocation(bucketName, key);
            FetchValue fetch = new FetchValue.Builder(quoteObjectLocation)
                    .withOption(FetchValue.Option.R, Quorum.quorumQuorum())
                    .build();
            FetchValue.Response response = client.execute(fetch);
            RiakObject obj = response.getValue(RiakObject.class);
            return obj != null && obj.getValue() != null
                    ? obj.getValue().toString()
                    : null;
        } catch (InterruptedException e) {
            log.error("InterruptedException in Repository when get e: ", e);
            Thread.currentThread().interrupt();
            throw new RiakExecutionException(e);
        } catch (Exception e) {
            log.error("Exception in Repository when get e: ", e);
            throw new RiakExecutionException(e);
        }
    }

    public void create(Row row, String bucketName) {
        try {
            RiakObject quoteObject = new RiakObject()
                    .setContentType(APPLICATION_JSON)
                    .setValue(BinaryValue.create(row.getValue()));
            Location quoteObjectLocation = createLocation(bucketName, row.getKey());
            StoreValue storeOp = new StoreValue.Builder(quoteObject)
                    .withOption(StoreValue.Option.W, Quorum.oneQuorum())
                    .withLocation(quoteObjectLocation)
                    .build();
            client.execute(storeOp);
        } catch (InterruptedException e) {
            log.error("InterruptedException in Repository when create e: ", e);
            Thread.currentThread().interrupt();
            throw new RiakExecutionException(e);
        } catch (Exception e) {
            log.error("Exception in Repository when create e: ", e);
            throw new RiakExecutionException();
        }
    }

    private Location createLocation(String bucketName, String key) {
        Namespace quotesBucket = new Namespace(bucketName);
        return new Location(quotesBucket, key);
    }
}
