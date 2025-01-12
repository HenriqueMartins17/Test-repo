package com.apitable.appdata.shared.template.server;

import java.util.List;
import java.util.Map;

import com.apitable.appdata.shared.template.pojo.TemplateAlbum;
import com.apitable.appdata.shared.template.pojo.TemplateAlbumRel;

public interface ITemplateAlbumService {

    List<TemplateAlbum> getAllTemplateAlbum();

    void parseTemplateAlbumData(Map<String, String> newTemplateIdMap, List<TemplateAlbum> albums, List<TemplateAlbumRel> albumRelations);
}
