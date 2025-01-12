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

package com.apitable.enterprise.document.mapper;

import com.apitable.enterprise.document.model.DocumentDTO;
import com.apitable.enterprise.document.model.DocumentOperationDTO;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Document Mapper.
 * </p>
 *
 * @author Chambers
 */
public interface DocumentMapper {

    /**
     * Query space id.
     *
     * @param name              document name
     * @param includeDeleted    whether to include logical deletion
     * @return SpaceId
     */
    String selectSpaceIdByName(@Param("name") String name,
                               @Param("includeDeleted") boolean includeDeleted);

    /**
     * Query existing document names.
     *
     * @param names document names
     * @return Existing Document Name List
     * @author Chambers
     */
    List<String> selectExistNamesIncludeDelete(@Param("names") Collection<String> names);

    /**
     * Query document name.
     *
     * @param resourceIds   resource id list
     * @return Document Name List
     * @author Chambers
     */
    List<String> selectNameByResourceIdIn(@Param("resourceIds") Collection<String> resourceIds);

    /**
     * Query Document.
     *
     * @param name  document name
     * @return DocumentDTO
     * @author Chambers
     */
    DocumentDTO selectByName(@Param("name") String name);

    /**
     * Query deleted document name.
     *
     * @param resourceIds   resource id list
     * @param deleteWay     delete way
     * @return Document Name List
     * @author Chambers
     */
    List<String> selectDeletedNamesByResourceIds(@Param("resourceIds") Collection<String> resourceIds,
        @Param("deleteWay") String deleteWay);

    /**
     * Query deleted document name.
     *
     * @param names         document name list
     * @param deleteWay     delete way
     * @return Document Name List
     * @author Chambers
     */
    List<String> selectDeletedNamesByNames(@Param("names") Collection<String> names,
        @Param("deleteWay") String deleteWay);

    /**
     * Remove Documents.
     *
     * @param names     document name list
     * @param userId    user id
     * @param deleteWay delete way
     * @author Chambers
     */
    void remove(@Param("names") List<String> names,
        @Param("userId") Long userId, @Param("deleteWay") String deleteWay);

    /**
     * Recover Documents.
     *
     * @param names     document name list
     * @param userId    user id
     * @author Chambers
     */
    void recover(@Param("names") List<String> names, @Param("userId") Long userId);

    /**
     * Query Document List.
     *
     * @param minId min id
     * @return List<DocumentDTO>
     * @author Chambers
     */
    List<DocumentDTO> selectByIdGreaterThan(@Param("minId") Long minId,
        @Param("limit") Integer limit);

    /**
     * Query Document Operation MAX ID.
     *
     * @return MAX ID
     * @author Chambers
     */
    Long selectDocumentOperationMaxId();

    /**
     * Query Document Operation DTO List.
     *
     * @param minId min id
     * @return List<DocumentOperationDTO>
     * @author Chambers
     */
    List<DocumentOperationDTO> selectDocumentOperationDTOByIdGreaterThan(@Param("minId") Long minId,
        @Param("limit") Integer limit);

}
