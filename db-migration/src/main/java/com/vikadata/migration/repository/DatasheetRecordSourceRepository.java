package com.vikadata.migration.repository;

import com.vikadata.migration.schema.DatasheetRecordSourceSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatasheetRecordSourceRepository extends MongoRepository<DatasheetRecordSourceSchema, Long> {

}
