package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DatasheetSchema;
import com.vikadata.migration.schema.NodeRecentlyBrowsedSchema;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasheetRepository extends MongoRepository<DatasheetSchema, Long> {

  @Query(value = "{'_id': { $gt: ObjectId(?0) }, 'spaceId': 'spczdmQDfBAn5'}", sort = "{_id: 1}")
  List<DatasheetSchema> findByCursorIdInPage(String id, Pageable pageable);

  @Query(value = "{'spaceId': 'spczdmQDfBAn5'}", sort = "{_id: 1}")
  List<DatasheetSchema> findInPage(Pageable pageable);

}
