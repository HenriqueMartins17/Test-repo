/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vikadata.scheduler.space.oss;

import java.io.IOException;
import java.io.InputStream;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Configuration.ResumableUploadAPIVersion;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class QiniuService {

    private static final int MULTIPART_UPLOAD_THRESHOLD = 100 * 1024 * 1024;

    private static final int RESUMABLE_UPLOAD_MAX_CONCURRENT_COUNT = 8;

    private static final int MINIMUM_UPLOAD_PART_SIZE = 10 * 1024 * 1024;

    private static final String CACHE_CONTROL_VALUE = "max-age=2592000,must-revalidate";

    private final String downloadDomain;

    private Auth auth;

    private final BucketManager bucketManager;

    private final UploadManager uploadManager;

    public QiniuService(Auth auth, String downloadDomain) {
        this.downloadDomain = downloadDomain;
        this.auth = auth;
        Configuration configuration = new Configuration(Region.region2());
        configuration.putThreshold = MULTIPART_UPLOAD_THRESHOLD;
        configuration.resumableUploadMaxConcurrentTaskCount = RESUMABLE_UPLOAD_MAX_CONCURRENT_COUNT;
        configuration.resumableUploadAPIVersion = ResumableUploadAPIVersion.V2;
        configuration.resumableUploadAPIV2BlockSize = MINIMUM_UPLOAD_PART_SIZE;
        this.bucketManager = new BucketManager(auth, configuration);
        this.uploadManager = new UploadManager(configuration);
    }

    public OssObject getObject(String bucketName, String keyPath) {
        try {
            // domain   download domain, eg: qiniu.com【must】
            // useHttps whether to use https【must】
            // key      download resources stored in qiniu cloud key【must】
            DownloadUrl downloadUrl = new DownloadUrl(downloadDomain, true, keyPath);
            String urlString = downloadUrl.buildURL();
            InputStream in = URLUtil.getStream(URLUtil.url(urlString));
            FileInfo fileInfo = bucketManager.stat(bucketName, keyPath);
            return new OssObject(fileInfo.md5, fileInfo.fsize, fileInfo.mimeType, in);
        }
        catch (QiniuException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void uploadStreamForObject(String bucketName, InputStream in, String keyPath, String mimeType, String digest) throws IOException {
        // limit upload file types
        StringMap policy = new StringMap();
        policy.put("mimeLimit", "!text/html");
        String uploadToken = auth.uploadToken(bucketName, keyPath, 3600, policy, true);
        try {
            StringMap params = new StringMap();
            params.put("Cache-Control", CACHE_CONTROL_VALUE);
            if (StrUtil.isNotEmpty(mimeType)) {
                params.put("Content-Type", mimeType);
            }
            if (StrUtil.isNotBlank(digest)) {
                // Upload the md5 value of the block content. If the server is specified, it will be verified. If not, it will not be verified
                params.put("Content-MD5", digest);
            }
            log.info("Upload Start......");
            StopWatch stopWatch = new StopWatch("time consuming for uploading tasks");
            stopWatch.start();
            Response response = uploadManager.put(in, keyPath, uploadToken, params, null);
            // analyze the result of successful upload
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            stopWatch.stop();
            log.info(stopWatch.prettyPrint());
            log.info("Upload succeeded: {} - {}", putRet.key, putRet.hash);

        }
        catch (QiniuException ex) {
            log.error("Upload failed", ex);
            throw new IOException(ex.error(), ex);
        }
    }
}
