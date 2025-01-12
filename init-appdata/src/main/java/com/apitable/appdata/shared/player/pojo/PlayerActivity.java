package com.apitable.appdata.shared.player.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerActivity {

    private Long id;

    private Long userId;

    private String actions;

}
