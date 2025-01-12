package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DataMigrationCursor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * 迁移记录ORM层
 */
public interface DataMigrationCursorRepository extends MongoRepository<DataMigrationCursor, Long> {

    @Query(sort = "{dataId: -1}")
    DataMigrationCursor findFirstByTable(String table);

}
