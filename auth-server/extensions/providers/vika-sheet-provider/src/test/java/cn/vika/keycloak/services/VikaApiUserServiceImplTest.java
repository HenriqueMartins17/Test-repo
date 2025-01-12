package cn.vika.keycloak.services;

import cn.vika.client.api.model.Record;
import cn.vika.keycloak.dto.VikaApiUserDto;
import cn.vika.keycloak.utils.VikaApiClientUtil;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VikaApiUserServiceImplTest {
    @InjectMocks
    private VikaApiUserServiceImpl vikaApiUserService;
    @Mock
    private VikaApiClientUtil vikaApiClientUtil;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);
        vikaApiClientUtil = mock(VikaApiClientUtil.class);
        vikaApiUserService = new VikaApiUserServiceImpl(vikaApiClientUtil);
        mockVikaApiClient();
    }

    private void mockVikaApiClient() {
        List<Record> records = new ArrayList<>();

        Map<String, Object> fields1 = new HashMap<>();
        fields1.put("nickname", "A1");
        fields1.put("email", "a1@183.com");
        fields1.put("password", "PWD001");
        fields1.put("enable", true);
        Record record1 = new Record();
        record1.setRecordId("record-1");
        record1.setFields(fields1);
        records.add(record1);

        Map<String, Object> fields2 = new HashMap<>();
        fields2.put("nickname", "A2");
        fields2.put("email", "a2@183.com");
        fields2.put("password", "PWD002");
        fields2.put("enable", true);
        Record record2 = new Record();
        record2.setRecordId("record-2");
        record2.setFields(fields2);
        records.add(record2);

        when(vikaApiClientUtil.getActiveRecords()).thenReturn(records);
    }

    @Test
    public void should_return_record_when_findUserByEmail_given_an_active_record() {
        // given
        String email = "A1@183.com";

        // when
        VikaApiUserDto resultUser = vikaApiUserService.findUserByEmail(email);

        // should
        assertThat(resultUser).isEqualTo(VikaApiUserDto.builder()
                .uuid("record-1")
                .nickName("A1")
                .email("a1@183.com")
                .password("PWD001")
                .build());
    }

    @Test
    public void should_return_record_when_findUserByEmail_ignore_case_given_an_active_record() {
        // given
        String email = "a1@183.com";

        // when
        VikaApiUserDto resultUser = vikaApiUserService.findUserByEmail(email);

        // should
        assertThat(resultUser).isEqualTo(VikaApiUserDto.builder()
                .uuid("record-1")
                .nickName("A1")
                .email("a1@183.com")
                .password("PWD001")
                .build());
    }
}