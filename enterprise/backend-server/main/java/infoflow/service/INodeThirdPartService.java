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

package com.apitable.enterprise.infoflow.service;


import com.apitable.enterprise.infoflow.model.NodesResponse;

public interface INodeThirdPartService {

    /**
     * query child node information
     *
     * @param spaceId space id
     * @param userId user id
     * @param nodeId node id
     * @return NodesResponse
     */
    NodesResponse getChildNodesByNodeId(String spaceId, String nodeId, Long userId);
}
