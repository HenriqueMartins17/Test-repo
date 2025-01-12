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

package com.vikadata.scheduler.space.enums;

/**
 * node type
 *
 * @author Chambers
 * @since 2019/10/12
 */
public enum NodeType {

    /**
     * root node
     */
    ROOT(0),

    /**
     * folder
     */
    FOLDER(1),

    /**
     * number table
     */
    DATASHEET(2),

    /**
     * collection form
     */
    FORM(3),

    /**
     * dash board
     */
    DASHBOARD(4),

    /**
     * mirror image
     */
    MIRROR(5),

    /**
     * Vig number pages, page design based on Vig table
     */
    DATAPAGE(6),

    /**
     * canvas
     */
    CANVAS(7),

    /**
     * Common editor documentation
     */
    WORD_DOC(8),

    /**
     * Static resource files
     */
    ASSET_FILE(98),

    /**
     * Doc
     */
    DATADOC(99);

    private int nodeType;

    NodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public static NodeType toEnum(int code) {
        for (NodeType e : NodeType.values()) {
            if (e.getNodeType() == code) {
                return e;
            }
        }
        throw new RuntimeException("Unknown node type");
    }

    /**
     * exclude root and folder type
     */
    public boolean isFileNode() {
        return nodeType > 1;
    }
}
