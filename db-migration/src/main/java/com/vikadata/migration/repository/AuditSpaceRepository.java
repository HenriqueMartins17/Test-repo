package com.vikadata.migration.repository;

import com.vikadata.migration.schema.AuditSpaceSchema;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AuditSpaceRepository extends MongoRepository<AuditSpaceSchema, Long> {

  @Query(value = "{'_id': { $gt: ObjectId(?0) }}", sort = "{_id: 1}")
  List<AuditSpaceSchema> findByCursorIdInPage(String id, Pageable pageable);

}
