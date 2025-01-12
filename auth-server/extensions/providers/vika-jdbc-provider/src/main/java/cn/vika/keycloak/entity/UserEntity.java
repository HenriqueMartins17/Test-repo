package cn.vika.keycloak.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * <p>
 * 用户信息对象
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/08/18 11:34
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    private Long id;
    /**
     * uuid
     */
    @Column(name = "uuid")
    private String uuid;
    /**
     * 密码
     */
    @Column(name = "password")
    private String password;
    /**
     * 昵称
     */
    @Column(name = "nick_name")
    private String nickName;
    /**
     * 邮箱
     */
    @Column(name = "email")
    private String email;
    /**
     * 手机号码
     */
    @Column(name = "mobile_phone")
    private String mobilePhone;
    /**
     * 头像
     */
    @Column(name = "avatar")
    private String avatar;
}
