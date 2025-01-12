package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DatasheetWidgetPanelSchema;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface DatasheetWidgetPanelRepository extends MongoRepository<DatasheetWidgetPanelSchema, Long> {

  @Query(value = "{'datasheetId' : { $in : ?0}}")
  List<DatasheetWidgetPanelSchema> findByDatasheetIds(List<String> datasheetIds);

}
