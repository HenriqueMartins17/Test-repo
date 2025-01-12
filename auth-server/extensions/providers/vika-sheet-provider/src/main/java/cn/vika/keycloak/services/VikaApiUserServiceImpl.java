package cn.vika.keycloak.services;

import cn.vika.client.api.model.Record;
import cn.vika.keycloak.dto.VikaApiUserDto;
import cn.vika.keycloak.utils.VikaApiClientUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Vika社区小号查询服务
 * </p>
 *
 * @author 胡海平(Humphrey Hu)
 * @date 2021/9/22 11:02:24
 */
public class VikaApiUserServiceImpl implements VikaApiUserService {
    private final VikaApiClientUtil vikaApiClientUtil;

    public VikaApiUserServiceImpl(VikaApiClientUtil vikaApiClientUtil) {
        this.vikaApiClientUtil = vikaApiClientUtil;
    }

    @Override
    public VikaApiUserDto findUserByEmail(String email) {
        List<Record> activeRecords = vikaApiClientUtil.getActiveRecords();
        Record targetRecord = activeRecords.stream()
                .filter(record -> matchEmail(record, email))
                .findFirst().orElse(null);
        return assembleVikaApiUserInfo(targetRecord);
    }

    /**
     * Email输入是否匹配
     * */
    private boolean matchEmail(Record record, String accountValue) {
        Map<String, Object> fields = record.getFields();
        String accountFromVika = (String) fields.get("email");
        return accountFromVika.equalsIgnoreCase(accountValue);
    }

    /**
     * 组装vika user info对象
     * */
    private VikaApiUserDto assembleVikaApiUserInfo(Record record) {
        if (Objects.isNull(record)) {
            return null;
        }
        Map<String, Object> fields = record.getFields();
        return VikaApiUserDto.builder()
                .uuid(record.getRecordId())
                .nickName((String) fields.get("nickname"))
                .email((String) fields.get("email"))
                .password((String) fields.get("password"))
                .build();
    }
}
