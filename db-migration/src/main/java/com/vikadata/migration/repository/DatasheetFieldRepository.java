package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DatasheetFieldSchema;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasheetFieldRepository extends MongoRepository<DatasheetFieldSchema, Long> {

  @Query(value = "{'datasheetId' : { $in : ?0}}")
  List<DatasheetFieldSchema> findByDatasheetIds(List<String> datasheetIds);

}
