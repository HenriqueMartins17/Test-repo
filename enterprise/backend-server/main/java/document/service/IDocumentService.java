/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.document.service;

import com.apitable.enterprise.document.enums.DeleteWay;
import com.apitable.enterprise.document.model.DocumentView;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Document Server.
 * </p>
 *
 * @author Chambers
 */
public interface IDocumentService {

    /**
     * Get space id.
     *
     * @param documentName      document name
     * @param includeDeleted    whether to include logical deletion
     * @return space id
     * @author Chambers
     */
    String getSpaceIdByDocumentName(String documentName, boolean includeDeleted);

    /**
     * Get new document name.
     *
     * @return new document name
     * @author Chambers
     */
    String getNewDocumentName();

    /**
     * Get document name list.
     *
     * @param resourceIds   resource ids
     * @return document name list
     * @author Chambers
     */
    List<String> getNamesByResourceIds(Collection<String> resourceIds);

    /**
     * Get document view.
     *
     * @param documentName  document name
     * @return DocumentView
     * @author Chambers
     */
    DocumentView getDocumentView(String documentName);

    /**
     * Remove document.
     *
     * @param userId        user id
     * @param documentNames document name list
     * @param deleteWay     document delete way
     * @author Chambers
     */
    void remove(Long userId, List<String> documentNames, DeleteWay deleteWay);

    /**
     * Recover document.
     *
     * @param userId        user id
     * @param documentNames document name list
     * @author Chambers
     */
    void recover(Long userId, List<String> documentNames);
}
