package com.apitable.enterprise.infoflow.service.impl;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.infoflow.model.LinkIdentifyResponse;
import com.apitable.enterprise.infoflow.service.IFieldThridPartService;
import com.apitable.enterprise.social.properties.OneAccessProperties;
import com.apitable.internal.vo.UrlAwareContentVo;
import com.apitable.internal.vo.UrlAwareContentsVo;
import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class IFieldThridPartServiceImpl implements IFieldThridPartService {

    @Resource
    private OneAccessProperties oneAccessProperties;

    @Resource
    private RestClient restClient;

    @Override
    public UrlAwareContentsVo getUrlAwareContents(List<String> urls) {
        UrlAwareContentsVo contents = new UrlAwareContentsVo();
        Map<String, UrlAwareContentVo> urlToUrlAwareContents = new HashMap<>(urls.size());
        contents.setContents(urlToUrlAwareContents);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("urls", urls);
        try {
            String url = oneAccessProperties.getIamHost() + "/callback/link/identify";
            HttpHeaders header = new HttpHeaders();
            // post request needs to be set contentType
            header.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<LinkIdentifyResponse> responseEntity =
                restClient
                    .post()
                    .uri(url)
                    .headers(headers -> headers.addAll(header))
                    .body(paramMap)
                    .retrieve()
                    .toEntity(LinkIdentifyResponse.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException("response http status not in 200");
            }
            if (null == responseEntity.getBody()) {
                throw new BusinessException("datasheets response body is null");
            }
            if (responseEntity.getBody().getCode() != 200) {
                log.info("getParentNodes target:{} , responseEntity:{}", url,
                    responseEntity.getBody());
                throw new BusinessException("datasheets response code not eq 200");
            }

            //Set content by link identify
            contents.setContents(Arrays.stream(responseEntity.getBody().getData())
                .collect(Collectors.toMap(LinkIdentifyResponse.Data::getUrl, value -> {
                    UrlAwareContentVo urlAwareContentVo = new UrlAwareContentVo(true);
                    urlAwareContentVo.setTitle(value.getName());
                    urlAwareContentVo.setFavicon(value.getUrl());
                    return urlAwareContentVo;
                })));
            return contents;
        } catch (RestClientException e) {
            throw new BusinessException(
                "Failed to request java server, please check network or parameters ");
        }
    }
}
