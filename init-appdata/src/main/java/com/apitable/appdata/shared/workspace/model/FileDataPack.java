package com.apitable.appdata.shared.workspace.model;

import java.util.List;

import com.apitable.appdata.shared.asset.pojo.Asset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDataPack {

    private List<Asset> assets;

    private List<NodeDataPack> nodeDataPacks;
}
