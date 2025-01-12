package com.apitable.enterprise.stripe.config;

import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAdminNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAiAgentNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAllowApplyJoin;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAllowCopyData;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAllowDownload;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAllowEmbed;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAllowExport;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAllowInvitation;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAllowOrgApi;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAllowShare;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildApiCallNumsPerMonth;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildApiQpsNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildArchitectureViewNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildArchivedRowsPerSheet;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAuditQuery;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAuditQueryDays;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildAutomationRunNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildCalendarViewNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildCapacitySize;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildColumnsPerSheet;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildContactIsolation;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildControlFormBrandLogo;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildDashboardNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildFieldPermissionNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildFileNodeNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildForbidCreateOnCatalog;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildFormNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildGalleryViewNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildGanttViewNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildKanbanViewNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildMessageCreditNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildMirrorNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildNodePermissionNums;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildRainbowLabel;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildRemainRecordActivityDays;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildRemainTimeMachineDays;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildRemainTrashDays;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildRowsPerSheet;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildSeat;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildShowMobileNumber;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildSnapshotNumsPerSheet;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildSocialConnect;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildTotalRows;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildWatermark;
import static com.apitable.interfaces.billing.model.SubscriptionFeatures.buildWidgetNums;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionFeature;
import com.apitable.interfaces.billing.model.BooleanPlanFeature;
import com.apitable.interfaces.billing.model.DataSizePlanFeature;
import com.apitable.interfaces.billing.model.NumberPlanFeature;
import com.apitable.interfaces.billing.model.SubscriptionFeature;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import lombok.Data;
import org.springframework.util.unit.DataSize;

/**
 * plan feature object.
 */
@Data
public class PlanFeature {

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature seat;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature fileNodeNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature adminNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature nodePermissionNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature fieldPermissionNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature columnsPerSheet;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature rowsPerSheet;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature archivedRowsPerSheet;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature totalRows;

    @JsonDeserialize(using = DataSizePlanFeatureDeserializer.class)
    @JsonSerialize(using = DataSizeSerializer.class)
    private DataSizePlanFeature capacitySize;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature orgApi;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature apiQpsNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature apiCallNumsPerMonth;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature ganttViewNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature calendarViewNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature galleryViewNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature kanbanViewNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature architectureViewNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature mirrorNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature formNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature dashboardNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature widgetNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature aiAgentNums;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature messageCreditNumsPerMonth;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature automationRunNumsPerMonth;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature rainbowLabel;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature watermark;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature embedding;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature socialConnect;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature invitationOff;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature applyJoinOff;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature dataExport;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature dataCopy;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature nodeShare;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature mobileDisplayable;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature contactIsolation;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature controlOperationInWorkbench;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature attachmentDownload;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature remainTrashDays;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature remainTimeMachineDays;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature remainRecordActivityDays;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature snapshotNumsPerSheet;

    @JsonDeserialize(using = NumberPlanFeatureDeserializer.class)
    @JsonSerialize(using = NumberSerializer.class)
    private NumberPlanFeature auditQueryDays;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature auditQuery;

    @JsonDeserialize(using = BooleanPlanFeatureDeserializer.class)
    @JsonSerialize(using = BooleanSerializer.class)
    private BooleanPlanFeature controlFormBrandLogo;

    public SubscriptionFeature safeConvert() {
        BillingSubscriptionFeature subscriptionFeature = new BillingSubscriptionFeature();
        subscriptionFeature.setSeat(buildSeat(wrapperNumberPlanFeatureValue(seat)));
        subscriptionFeature.setFileNodeNums(
            buildFileNodeNums(wrapperNumberPlanFeatureValue(fileNodeNums))
        );
        subscriptionFeature.setAdminNums(buildAdminNums(wrapperNumberPlanFeatureValue(adminNums)));
        subscriptionFeature.setNodePermissionNums(
            buildNodePermissionNums(wrapperNumberPlanFeatureValue(nodePermissionNums))
        );
        subscriptionFeature.setFieldPermissionNums(
            buildFieldPermissionNums(wrapperNumberPlanFeatureValue(fieldPermissionNums))
        );
        subscriptionFeature.setColumnsPerSheet(
            buildColumnsPerSheet(wrapperNumberPlanFeatureValue(columnsPerSheet))
        );
        subscriptionFeature.setRowsPerSheet(
            buildRowsPerSheet(wrapperNumberPlanFeatureValue(rowsPerSheet))
        );
        subscriptionFeature.setArchivedRowsPerSheet(
            buildArchivedRowsPerSheet(wrapperNumberPlanFeatureValue(archivedRowsPerSheet))
        );
        subscriptionFeature.setSnapshotNumsPerSheet(
            buildSnapshotNumsPerSheet(wrapperNumberPlanFeatureValue(snapshotNumsPerSheet))
        );
        subscriptionFeature.setTotalRows(buildTotalRows(wrapperNumberPlanFeatureValue(totalRows)));
        subscriptionFeature.setCapacitySize(
            buildCapacitySize(wrapperDataSizePlanFeatureValue(capacitySize))
        );
        subscriptionFeature.setAllowOrgApi(
            buildAllowOrgApi(wrapperBooleanPlanFeatureValue(orgApi))
        );
        subscriptionFeature.setApiQpsNums(
            buildApiQpsNums(wrapperNumberPlanFeatureValue(apiQpsNums))
        );
        subscriptionFeature.setApiCallNumsPerMonth(
            buildApiCallNumsPerMonth(wrapperNumberPlanFeatureValue(apiCallNumsPerMonth))
        );
        subscriptionFeature.setGanttViewNums(
            buildGanttViewNums(wrapperNumberPlanFeatureValue(ganttViewNums))
        );
        subscriptionFeature.setCalendarViewNums(
            buildCalendarViewNums(wrapperNumberPlanFeatureValue(calendarViewNums))
        );
        subscriptionFeature.setGalleryViewNums(
            buildGalleryViewNums(wrapperNumberPlanFeatureValue(galleryViewNums))
        );
        subscriptionFeature.setKanbanViewNums(
            buildKanbanViewNums(wrapperNumberPlanFeatureValue(kanbanViewNums))
        );
        subscriptionFeature.setArchitectureViewNums(
            buildArchitectureViewNums(wrapperNumberPlanFeatureValue(architectureViewNums))
        );
        subscriptionFeature.setMirrorNums(
            buildMirrorNums(wrapperNumberPlanFeatureValue(mirrorNums))
        );
        subscriptionFeature.setFormNums(buildFormNums(wrapperNumberPlanFeatureValue(formNums)));
        subscriptionFeature.setDashboardNums(
            buildDashboardNums(wrapperNumberPlanFeatureValue(dashboardNums))
        );
        subscriptionFeature.setWidgetNums(
            buildWidgetNums(wrapperNumberPlanFeatureValue(widgetNums))
        );
        subscriptionFeature.setAutomationRunNumsPerMonth(
            buildAutomationRunNums(wrapperNumberPlanFeatureValue(automationRunNumsPerMonth))
        );
        subscriptionFeature.setAiAgentNums(
            buildAiAgentNums(wrapperNumberPlanFeatureValue(aiAgentNums))
        );
        subscriptionFeature.setMessageCreditNums(
            buildMessageCreditNums(messageCreditNumsPerMonth.getValue())
        );
        subscriptionFeature.setRainbowLabel(
            buildRainbowLabel(wrapperBooleanPlanFeatureValue(rainbowLabel))
        );
        subscriptionFeature.setWatermark(buildWatermark(wrapperBooleanPlanFeatureValue(watermark)));
        subscriptionFeature.setAllowEmbed(
            buildAllowEmbed(wrapperBooleanPlanFeatureValue(embedding))
        );
        subscriptionFeature.setSocialConnect(
            buildSocialConnect(wrapperBooleanPlanFeatureValue(socialConnect))
        );
        subscriptionFeature.setAllowInvitation(
            buildAllowInvitation(wrapperBooleanPlanFeatureValue(invitationOff))
        );
        subscriptionFeature.setAllowApplyJoin(
            buildAllowApplyJoin(wrapperBooleanPlanFeatureValue(applyJoinOff))
        );
        subscriptionFeature.setAllowExport(
            buildAllowExport(wrapperBooleanPlanFeatureValue(dataExport))
        );
        subscriptionFeature.setAllowCopyData(
            buildAllowCopyData(wrapperBooleanPlanFeatureValue(dataCopy))
        );
        subscriptionFeature.setAllowShare(
            buildAllowShare(wrapperBooleanPlanFeatureValue(nodeShare))
        );
        subscriptionFeature.setAllowDownload(
            buildAllowDownload(wrapperBooleanPlanFeatureValue(attachmentDownload))
        );
        subscriptionFeature.setShowMobileNumber(
            buildShowMobileNumber(wrapperBooleanPlanFeatureValue(mobileDisplayable))
        );
        subscriptionFeature.setContactIsolation(
            buildContactIsolation(wrapperBooleanPlanFeatureValue(contactIsolation))
        );
        subscriptionFeature.setForbidCreateOnCatalog(
            buildForbidCreateOnCatalog(
                wrapperBooleanPlanFeatureValue(controlOperationInWorkbench))
        );
        subscriptionFeature.setRemainTrashDays(
            buildRemainTrashDays(wrapperNumberPlanFeatureValue(remainTrashDays))
        );
        subscriptionFeature.setRemainTimeMachineDays(
            buildRemainTimeMachineDays(wrapperNumberPlanFeatureValue(remainTimeMachineDays))
        );
        subscriptionFeature.setRemainRecordActivityDays(
            buildRemainRecordActivityDays(wrapperNumberPlanFeatureValue(remainRecordActivityDays))
        );
        subscriptionFeature.setAuditQueryDays(
            buildAuditQueryDays(wrapperNumberPlanFeatureValue(auditQueryDays))
        );
        subscriptionFeature.setAuditQuery(
            buildAuditQuery(wrapperBooleanPlanFeatureValue(auditQuery))
        );
        subscriptionFeature.setControlFormBrandLogo(
            buildControlFormBrandLogo(wrapperBooleanPlanFeatureValue(controlFormBrandLogo))
        );

        return subscriptionFeature;
    }

    private static Long wrapperNumberPlanFeatureValue(NumberPlanFeature feature) {
        return feature == null ? 0L : feature.getValue();
    }

    private static DataSize wrapperDataSizePlanFeatureValue(DataSizePlanFeature feature) {
        return feature == null ? DataSize.ofBytes(0) : feature.getValue();
    }

    private static Boolean wrapperBooleanPlanFeatureValue(BooleanPlanFeature feature) {
        return feature != null && feature.getValue();
    }

    public static class NumberPlanFeatureDeserializer
        extends JsonDeserializer<NumberPlanFeature> {

        @Override
        public NumberPlanFeature deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException {
            String value = p.getValueAsString();
            return new NumberPlanFeature(Long.valueOf(value));
        }
    }

    public static class DataSizePlanFeatureDeserializer
        extends JsonDeserializer<DataSizePlanFeature> {

        @Override
        public DataSizePlanFeature deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException {
            String value = p.getValueAsString();
            return new DataSizePlanFeature(DataSize.parse(value));
        }
    }

    public static class BooleanPlanFeatureDeserializer
        extends JsonDeserializer<BooleanPlanFeature> {

        @Override
        public BooleanPlanFeature deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException {
            String value = p.getValueAsString();
            return StrUtil.isBlank(value) ? new BooleanPlanFeature(null) :
                new BooleanPlanFeature(Boolean.parseBoolean(value));
        }
    }

    public static class NumberSerializer extends JsonSerializer<NumberPlanFeature> {

        @Override
        public void serialize(NumberPlanFeature value, JsonGenerator gen,
                              SerializerProvider serializers)
            throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeNumber(value.getValue());
        }
    }

    public static class DataSizeSerializer extends JsonSerializer<DataSizePlanFeature> {

        @Override
        public void serialize(DataSizePlanFeature value, JsonGenerator gen,
                              SerializerProvider serializers)
            throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeNumber(value.getValue().toBytes());
        }
    }

    public static class BooleanSerializer extends JsonSerializer<BooleanPlanFeature> {

        @Override
        public void serialize(BooleanPlanFeature value, JsonGenerator gen,
                              SerializerProvider serializers)
            throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            gen.writeBoolean(value.getValue());
        }
    }
}
