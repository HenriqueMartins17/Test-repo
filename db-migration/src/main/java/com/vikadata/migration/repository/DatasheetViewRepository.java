package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DatasheetViewSchema;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasheetViewRepository extends MongoRepository<DatasheetViewSchema, Long> {

  @Query(value = "{'datasheetId' : { $in : ?0}}")
  List<DatasheetViewSchema> findByDatasheetIds(List<String> datasheetIds);

}
