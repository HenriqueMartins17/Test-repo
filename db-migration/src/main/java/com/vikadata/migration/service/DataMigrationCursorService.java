package com.vikadata.migration.service;

import com.vikadata.migration.repository.DataMigrationCursorRepository;
import com.vikadata.migration.schema.DataMigrationCursor;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataMigrationCursorService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private DataMigrationCursorRepository dataMigrationCursorRepository;

    public DataMigrationCursor getMigrationCursor(String table) {
        return dataMigrationCursorRepository.findFirstByTable(table);
    }

    public List<DataMigrationCursor> getMigrationCursors() {
        return dataMigrationCursorRepository.findAll();
    }

    public void updateCursorOriginalCount(String table, Long originalCount) {
        Query query = Query.query(Criteria.where("table").is(table));
        Update update = new Update();
        update.set("originalCount", originalCount);
        mongoTemplate.upsert(query, update, DataMigrationCursor.class);
    }

    public void updateCursorId(String table, String cursorId, Integer incCount, boolean finished) {
        Query query = Query.query(Criteria.where("table").is(table));
        Update update = new Update();
        update.set("cursorId", cursorId);
        update.set("finished", finished);
        update.set("updatedAt", LocalDateTime.now());
        update.inc("migratedCount", incCount);
        mongoTemplate.upsert(query, update, DataMigrationCursor.class);
    }


}
