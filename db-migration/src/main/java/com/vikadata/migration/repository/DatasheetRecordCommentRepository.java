package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DatasheetRecordCommentSchema;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasheetRecordCommentRepository extends MongoRepository<DatasheetRecordCommentSchema, Long> {

  @Query(value = "{'_id': { $gt: ObjectId(?0) }, 'spaceId': 'spczdmQDfBAn5'}", sort = "{_id: 1}")
  List<DatasheetRecordCommentSchema> findByCursorIdInPage(String id, Pageable pageable);

  @Query(value = "{'spaceId': 'spczdmQDfBAn5'}", sort = "{_id: 1}")
  List<DatasheetRecordCommentSchema> findInPage(Pageable pageable);
}
