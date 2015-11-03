package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;

import java.util.List;

public interface EntriesUpdateDAO {
    String tableName = "entries";

    @SqlUpdate("create table if not exists " + tableName + " (id serial primary key, entry bytea)")
    void ensureTableExists();

    @SqlBatch("insert into " + tableName + " (entry) values(:messages)")
    @BatchChunkSize(1000)
    void add(@Bind("messages") List<byte[]> messages);
}
