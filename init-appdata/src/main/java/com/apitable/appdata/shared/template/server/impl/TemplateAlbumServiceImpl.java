package com.apitable.appdata.shared.template.server.impl;

import com.apitable.appdata.shared.constants.CommonConstants;
import com.apitable.appdata.shared.template.mapper.TemplateAlbumMapper;
import com.apitable.appdata.shared.template.mapper.TemplateAlbumRelMapper;
import com.apitable.appdata.shared.template.pojo.TemplateAlbum;
import com.apitable.appdata.shared.template.pojo.TemplateAlbumRel;
import com.apitable.appdata.shared.template.server.ITemplateAlbumService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TemplateAlbumServiceImpl implements ITemplateAlbumService {

    @Resource
    private TemplateAlbumMapper templateAlbumMapper;

    @Resource
    private TemplateAlbumRelMapper templateAlbumRelMapper;

    @Override
    public List<TemplateAlbum> getAllTemplateAlbum() {
        return templateAlbumMapper.selectAllTemplateAlbum();
    }

    @Override
    public void parseTemplateAlbumData(Map<String, String> newTemplateIdMap, List<TemplateAlbum> albums, List<TemplateAlbumRel> albumRelations) {
        templateAlbumMapper.delete();
        templateAlbumRelMapper.delete();
        if (albums.isEmpty()) {
            return;
        }
        for (TemplateAlbum property : albums) {
            property.setId(IdWorker.getId());
        }
        templateAlbumMapper.insertBatch(CommonConstants.INIT_ACCOUNT_USER_ID, albums);
        for (TemplateAlbumRel albumRel : albumRelations) {
            albumRel.setId(IdWorker.getId());
            if (newTemplateIdMap.containsKey(albumRel.getRelateId())) {
                albumRel.setRelateId(newTemplateIdMap.get(albumRel.getRelateId()));
            }
        }
        templateAlbumRelMapper.insertBatch(albumRelations);
    }
}
