package com.vikadata.migration.repository;

import com.vikadata.migration.schema.NodeRecentlyBrowsedSchema;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * recently browsed node mongo repository
 */
@Repository
public interface NodeRecentlyBrowsedRepository extends MongoRepository<NodeRecentlyBrowsedSchema, String> {

    @Query(value = "{'_id': { $gt: ObjectId(?0) }}", sort = "{_id: 1}")
    List<NodeRecentlyBrowsedSchema> findByCursorIdInPage(String id, Pageable pageable);

}