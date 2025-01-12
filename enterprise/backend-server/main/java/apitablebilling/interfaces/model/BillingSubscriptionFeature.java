/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.apitablebilling.interfaces.model;

import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.AdminNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.AiAgentNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ApiCallNumsPerMonth;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ApiQpsNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ArchitectureViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ArchivedRowsPerSheet;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.AutomationRunNumsPerMonth;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.CalendarViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.CapacitySize;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.ColumnsPerSheet;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.DashboardNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.FieldPermissionNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.FileNodeNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.FormNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.GalleryViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.GanttViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.KanbanViewNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.MessageCreditNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.MirrorNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.NodePermissionNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.RowsPerSheet;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.Seat;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.SnapshotNumsPerSheet;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.TotalRows;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.ConsumeFeatures.WidgetNums;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SolidFeatures.AuditQueryDays;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SolidFeatures.RemainRecordActivityDays;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SolidFeatures.RemainTimeMachineDays;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SolidFeatures.RemainTrashDays;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowApplyJoin;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowCopyData;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowDownload;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowEmbed;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowExport;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowInvitation;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowOrgApi;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AllowShare;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.AuditQuery;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.ContactIsolation;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.ControlFormBrandLogo;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.ForbidCreateOnCatalog;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.RainbowLabel;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.ShowMobileNumber;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.SocialConnect;
import com.apitable.interfaces.billing.model.SubscriptionFeatures.SubscribeFeatures.Watermark;
import lombok.Data;

/**
 * billing subscription feature.
 */
@Data
public class BillingSubscriptionFeature implements SubscriptionFeature {

    // consume
    private Seat seat;
    private CapacitySize capacitySize;
    private AdminNums adminNums;
    private FileNodeNums fileNodeNums;
    private ColumnsPerSheet columnsPerSheet;
    private RowsPerSheet rowsPerSheet;
    private ArchivedRowsPerSheet archivedRowsPerSheet;
    private SnapshotNumsPerSheet snapshotNumsPerSheet;
    private TotalRows totalRows;
    private MirrorNums mirrorNums;
    private FormNums formNums;
    private DashboardNums dashboardNums;
    private WidgetNums widgetNums;
    private GalleryViewNums galleryViewNums;
    private KanbanViewNums kanbanViewNums;
    private GanttViewNums ganttViewNums;
    private CalendarViewNums calendarViewNums;

    private ArchitectureViewNums architectureViewNums;
    private FieldPermissionNums fieldPermissionNums;
    private NodePermissionNums nodePermissionNums;
    private ApiQpsNums apiQpsNums;
    private ApiCallNumsPerMonth apiCallNumsPerMonth;
    private AiAgentNums aiAgentNums;
    private MessageCreditNums messageCreditNums;
    private AutomationRunNumsPerMonth automationRunNumsPerMonth;

    // subscribe
    private SocialConnect socialConnect;
    private RainbowLabel rainbowLabel;
    private Watermark watermark;
    private AllowInvitation allowInvitation;
    private AllowApplyJoin allowApplyJoin;
    private AllowShare allowShare;
    private AllowExport allowExport;
    private AllowDownload allowDownload;
    private AllowCopyData allowCopyData;
    private AllowEmbed allowEmbed;
    private ShowMobileNumber showMobileNumber;
    private ContactIsolation contactIsolation;
    private ForbidCreateOnCatalog forbidCreateOnCatalog;
    private AllowOrgApi allowOrgApi;
    private ControlFormBrandLogo controlFormBrandLogo;
    // solid
    private RemainTrashDays remainTrashDays;
    private RemainTimeMachineDays remainTimeMachineDays;
    private RemainRecordActivityDays remainRecordActivityDays;
    private AuditQueryDays auditQueryDays;
    private AuditQuery auditQuery;
}
