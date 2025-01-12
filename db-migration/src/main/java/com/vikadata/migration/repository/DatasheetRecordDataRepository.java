package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DatasheetRecordDataSchema;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasheetRecordDataRepository extends MongoRepository<DatasheetRecordDataSchema, Long> {

  @Query(value = "{'datasheetId' : { $in : ?0}, 'recordId' : { $in : ?1}}")
  List<DatasheetRecordDataSchema> findByRecordIds(List<String> datasheetIds, List<String> recordIds);

}
