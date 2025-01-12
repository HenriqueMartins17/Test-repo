package com.apitable.appdata.shared.workspace.model;

import java.util.ArrayList;
import java.util.List;

import com.apitable.appdata.shared.workspace.pojo.Datasheet;
import com.apitable.appdata.shared.workspace.pojo.DatasheetMeta;
import com.apitable.appdata.shared.workspace.pojo.DatasheetRecord;
import com.apitable.appdata.shared.workspace.pojo.DatasheetWidget;
import com.apitable.appdata.shared.workspace.pojo.ResourceMeta;
import com.apitable.appdata.shared.widget.pojo.Widget;
import lombok.Data;

@Data
public class ResourceDataPack {

    private List<ResourceMeta> resourceMetas = new ArrayList<>();

    private List<Datasheet> datasheets = new ArrayList<>();

    private List<DatasheetMeta> datasheetMetas = new ArrayList<>();

    private List<DatasheetRecord> datasheetRecords = new ArrayList<>();

    private List<DatasheetWidget> datasheetWidgets = new ArrayList<>();

    private List<Widget> widgets = new ArrayList<>();
}
