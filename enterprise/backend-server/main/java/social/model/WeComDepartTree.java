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

package com.apitable.enterprise.social.model;

import com.apitable.core.support.tree.v2.Tree;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * WeCom Department Tree Structure.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor
public class WeComDepartTree implements Tree {

    private static final long serialVersionUID = 3860593887489174660L;

    private String id;

    private String name;

    private String enName;

    private String parentId;

    private Long order;

    private int level;

    private List<WeComDepartTree> children;

    /**
     * WeComDepartTree.
     */
    public WeComDepartTree(String id, String name, String enName, String parentId, Long order) {
        this.id = id;
        this.name = name;
        this.enName = enName;
        this.parentId = parentId;
        this.order = order;
    }

    @JsonIgnore
    @Override
    public List getChildren() {
        return this.children;
    }

    @Override
    public void setChildren(List childrenNodes) {
        this.children = childrenNodes;
    }

}
