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

package com.apitable.enterprise.elink.infrastructure.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SignatureUtil {

    public static String toSHA256(String str) throws Exception {
        MessageDigest messageDigest;
        String encodeStr = "";
        messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
        encodeStr = byte2Hex(messageDigest.digest());
        return encodeStr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                result.append("0");
            }
            result.append(temp);
        }
        return result.toString();
    }

}
