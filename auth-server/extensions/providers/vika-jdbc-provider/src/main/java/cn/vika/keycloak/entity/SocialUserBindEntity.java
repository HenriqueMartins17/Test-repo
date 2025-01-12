package cn.vika.keycloak.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * <p>
 * 用户第三方平台关联对象
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/18 11:34
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SocialUserBindEntity implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    private Long id;
    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;
    /**
     * 三方平台内部唯一标志
     */
    @Column(name = "union_id")
    private String unionId;
}
