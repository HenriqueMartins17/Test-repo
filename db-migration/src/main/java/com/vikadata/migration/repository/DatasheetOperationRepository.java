package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DatasheetOperationSchema;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasheetOperationRepository extends MongoRepository<DatasheetOperationSchema, Long> {

  @Query(value = "{'datasheetId' : { $in : ?0}, 'revision' : { $in : ?1}}")
  List<DatasheetOperationSchema> findByDatasheetIdInAndRevisionIn(List<String> datasheetIds, List<Long> revisions);

}
