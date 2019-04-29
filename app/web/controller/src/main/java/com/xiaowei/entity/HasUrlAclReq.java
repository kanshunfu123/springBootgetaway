package com.xiaowei.entity;

import lombok.*;

/**
 * Created by MOMO on 2019/1/22.
 */
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HasUrlAclReq {
    private String url;
    private String userInfo;
}
