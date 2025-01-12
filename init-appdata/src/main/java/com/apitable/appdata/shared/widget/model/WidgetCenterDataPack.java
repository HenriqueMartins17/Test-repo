package com.apitable.appdata.shared.widget.model;

import java.util.List;
import java.util.Set;

import com.apitable.appdata.shared.widget.pojo.WidgetPackage;
import com.apitable.appdata.shared.widget.pojo.WidgetPackageAuthSpace;
import com.apitable.appdata.shared.widget.pojo.WidgetPackageRelease;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WidgetCenterDataPack {

    private Set<String> assetTokens;

    private List<WidgetPackage> widgetPackages;

    private List<WidgetPackageRelease> widgetPackageReleases;

    private List<WidgetPackageAuthSpace> widgetPackageAuthSpaces;

}
