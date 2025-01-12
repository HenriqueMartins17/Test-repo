package cn.vika.keycloak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Vika API user info
 * </p>
 *
 * @author 胡海平(Humphrey Hu)
 * @date 2021/9/22 18:07:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VikaApiUserDto {
    private String uuid;
    private String nickName;
    private String email;
    private String password;
}
