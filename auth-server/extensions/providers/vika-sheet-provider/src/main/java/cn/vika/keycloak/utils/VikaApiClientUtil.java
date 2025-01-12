package cn.vika.keycloak.utils;

import cn.vika.client.api.VikaApiClient;
import cn.vika.client.api.http.ApiCredential;
import cn.vika.client.api.model.Pager;
import cn.vika.client.api.model.Record;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.vika.keycloak.constants.CommonConstants.*;

/**
 * <p>
 * VikaApiClient工具类
 * </p>
 *
 * @author 胡海平(Humphrey Hu)
 * @date 2021/9/22 11:15:41
 */
public class VikaApiClientUtil {
    private final String dataSheetId;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final VikaApiClient vikaApiClient;

    public VikaApiClientUtil(ComponentModel componentModel) {
        MultivaluedHashMap<String, String> config = componentModel.getConfig();
        this.dataSheetId = config.getFirst(VIKA_CLIENT_DATA_SHEET);
        ApiCredential credential = new ApiCredential(config.getFirst(VIKA_CLIENT_TOKEN));
        vikaApiClient = new VikaApiClient(config.getFirst(VIKA_CLIENT_HOST), credential);
    }

    /**
     * 查找维格表中所有启用的记录
     * */
    public List<Record> getActiveRecords() {
        log.info("[VikaApiClientUtil] => request all active records from vika sheet.");
        Pager<Record> pager = vikaApiClient.getRecordApi()
                .getRecords(this.dataSheetId, 100);
        return pager.all().stream()
                .filter(this::isActive)
                .collect(Collectors.toList());
    }

    /**
     * 判断维格表记录是否处于启用状态
     * */
    private boolean isActive(Record record) {
        Map<String, Object> fields = record.getFields();
        Boolean enable = (Boolean) fields.get("enable");
        return Objects.nonNull(enable) && Boolean.TRUE.equals(enable);
    }

}
