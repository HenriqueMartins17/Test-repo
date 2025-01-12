#[cfg(test)]
pub const MOCK_DATASHEET_PACK_JSON_FOR_GET_RECORD: &str = r#"
{
    "snapshot": {
        "meta": {
            "fieldMap": {
                "fldjm35K4244q": {
                    "id": "fldjm35K4244q",
                    "name": "电话",
                    "type": 10
                },
                "fld2RON4AAFJs": {
                    "id": "fld2RON4AAFJs",
                    "name": "百分比",
                    "type": 18,
                    "property": {
                        "precision": 3
                    }
                },
                "fldbElOhmrgVj": {
                    "id": "fldbElOhmrgVj",
                    "name": "数字",
                    "type": 2,
                    "property": {
                        "precision": 0,
                        "commaStyle": ","
                    }
                },
                "fldG038TbYQnm": {
                    "id": "fldG038TbYQnm",
                    "name": "多行文本 2",
                    "type": 1
                },
                "fldCtI5OA1Juj": {
                    "id": "fldCtI5OA1Juj",
                    "name": "URL",
                    "type": 8,
                    "property": {}
                },
                "fldD82z1NGndr": {
                    "id": "fldD82z1NGndr",
                    "name": "修改时间",
                    "type": 22,
                    "property": {
                      "dateFormat": 0,
                      "timeFormat": 1,
                      "collectType": 0,
                      "datasheetId": "dstMUyAamjZxi7EPci",
                      "includeTime": false,
                      "fieldIdCollection": []
                    }
                },
                "fldRTqT2c57Z0": {
                    "id": "fldRTqT2c57Z0",
                    "name": "神奇引用",
                    "type": 14,
                    "property": {
                        "datasheetId": "dstMUyAamjZxi7EPci",
                        "relatedLinkFieldId": "fld65cVnWdsJB",
                        "lookUpTargetFieldId": "fldywkRiobJD1",
                        "rollUpType": "VALUES",
                        "formatting": {
                            "formatType": 2,
                            "precision": 0,
                            "symbol": "$"
                        },
                        "openFilter": false,
                        "lookUpLimit": "ALL"
                    }
                },
                "fldSKRRCUPfpS": {
                    "id": "fldSKRRCUPfpS",
                    "name": "单选",
                    "type": 3,
                    "property": {
                        "options": [
                            {
                                "id": "opt9HgRA8WRvE",
                                "name": "得到的",
                                "color": 0
                            },
                            {
                                "id": "optqUUd4g0fQp",
                                "name": "得到",
                                "color": 1
                            }
                        ]
                    }
                },
                "fldns2pBdCBuY": {
                    "id": "fldns2pBdCBuY",
                    "name": "标题",
                    "type": 19,
                    "property": {}
                },
                "fld3HuTid3mo7": {
                    "id": "fld3HuTid3mo7",
                    "name": "日期",
                    "type": 5,
                    "property": {
                        "dateFormat": 1,
                        "timeFormat": 1,
                        "includeTime": false,
                        "autoFill": false
                    }
                },
                "fldhYUukLZsZV": {
                    "id": "fldhYUukLZsZV",
                    "name": "选项",
                    "type": 4,
                    "property": {
                        "options": [
                            {
                                "id": "optVDsdkpIuIZ",
                                "name": "111",
                                "color": 0
                            },
                            {
                                "id": "optzzJkMCjtOe",
                                "name": "2222",
                                "color": 1
                            },
                            {
                                "id": "optoYLalVnEzk",
                                "name": "hello",
                                "color": 2
                            }
                        ]
                    }
                },
                "fld65cVnWdsJB": {
                    "id": "fld65cVnWdsJB",
                    "name": "神奇关联",
                    "type": 7,
                    "property": {
                        "foreignDatasheetId": "dsttz8V4j2zvtMX0L3",
                        "brotherFieldId": "fldLeq3dPM1x7"
                    }
                },
                "fldaecpp88nkq": {
                    "id": "fldaecpp88nkq",
                    "name": "附件",
                    "type": 6
                },
                "fldRTMMcxFH4W": {
                    "id": "fldRTMMcxFH4W",
                    "name": "create_by",
                    "type": 23,
                    "property": {
                        "datasheetId": "dstMUyAamjZxi7EPci",
                        "subscription": false,
                        "uuids": [
                            "082476dc25834402ba979d62804480cd"
                        ]
                    }
                },
                "fldFyWoTaUYZU": {
                    "id": "fldFyWoTaUYZU",
                    "name": "勾选",
                    "type": 11,
                    "property": {
                        "icon": "white_check_mark"
                    }
                },
                "fldWkHEjmh925": {
                    "id": "fldWkHEjmh925",
                    "name": "货币",
                    "type": 17,
                    "property": {
                        "precision": 2,
                        "symbol": "$"
                    }
                },
                "fld3yOP9Fexc6": {
                    "id": "fld3yOP9Fexc6",
                    "name": "邮箱",
                    "type": 9
                },
                "fldbL9rFZEs0F": {
                    "id": "fldbL9rFZEs0F",
                    "name": "评分",
                    "type": 12,
                    "property": {
                        "icon": "star",
                        "max": 5
                    }
                },
                "fldec4mK8vtSW": {
                    "id": "fldec4mK8vtSW",
                    "name": "修改人",
                    "type": 24,
                    "property": {
                        "uuids": [
                            "082476dc25834402ba979d62804480cd",
                            "2e331e481a7a4125896709ab3526da9a"
                        ],
                        "collectType": 0,
                        "datasheetId": "dstMUyAamjZxi7EPci",
                        "fieldIdCollection": []
                    }
                },
                "fldrvuUOjX2bP": {
                    "id": "fldrvuUOjX2bP",
                    "name": "级联器",
                    "type": 25,
                    "property": {
                        "showAll": true,
                        "linkedDatasheetId": "dstdSpnZ1lXsQcWKcK",
                        "linkedViewId": "viwjNXPlhtvSk",
                        "linkedFields": [
                            {
                                "id": "fldQRr8gTkl1Y",
                                "name": "一级部门",
                                "type": 19
                            },
                            {
                                "id": "fldcNeo5DmEnl",
                                "name": "二级部门",
                                "type": 19
                            },
                            {
                                "id": "fldEx2FAZvvro",
                                "name": "三级部门",
                                "type": 19
                            }
                        ],
                        "fullLinkedFields": [
                            {
                                "id": "fldQRr8gTkl1Y",
                                "name": "一级部门",
                                "type": 19
                            },
                            {
                                "id": "fldcNeo5DmEnl",
                                "name": "二级部门",
                                "type": 19
                            },
                            {
                                "id": "fldEx2FAZvvro",
                                "name": "三级部门",
                                "type": 19
                            }
                        ]
                    }
                },
                "fldwdrAZuF32v": {
                    "id": "fldwdrAZuF32v",
                    "name": "多行文本",
                    "type": 1
                },
                "fldabJ9NkGRoS": {
                    "id": "fldabJ9NkGRoS",
                    "name": "自增数字",
                    "type": 20,
                    "property": {
                        "datasheetId": "dstMUyAamjZxi7EPci",
                        "nextId": 3,
                        "viewIdx": 0
                    }
                },
                "fldjrE1btb8aF": {
                    "id": "fldjrE1btb8aF",
                    "name": "智能公式",
                    "type": 16,
                    "property": {
                        "datasheetId": "dstMUyAamjZxi7EPci",
                        "expression": "CONCATENATE({fldRTMMcxFH4W},{fldns2pBdCBuY})"
                    }
                },
                "fldjztHYWLV1G": {
                    "id": "fldjztHYWLV1G",
                    "name": "member",
                    "type": 13,
                    "property": {
                        "isMulti": true,
                        "shouldSendMsg": true,
                        "subscription": false,
                        "unitIds": [
                            "1688817023401119745",
                            "1689549079210176514"
                        ]
                    }
                },
                "fldzX5RYDJdVB": {
                    "id": "fldzX5RYDJdVB",
                    "name": "创建时间",
                    "type": 21,
                    "property": {
                        "dateFormat": 0,
                        "timeFormat": 1,
                        "datasheetId": "dstMUyAamjZxi7EPci",
                        "includeTime": false
                    }
                }
            },
            "views": [
                {
                    "id": "viwM01QyCWxlD",
                    "name": "维格视图",
                    "type": 1,
                    "columns": [
                        {
                            "fieldId": "fldns2pBdCBuY",
                            "hidden": null,
                            "statType": 1
                        },
                        {
                            "fieldId": "fldabJ9NkGRoS",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldwdrAZuF32v",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldjm35K4244q",
                            "hidden": false
                        },
                        {
                            "fieldId": "fld3yOP9Fexc6",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldbElOhmrgVj",
                            "hidden": false,
                            "statType": 8
                        },
                        {
                            "fieldId": "fldhYUukLZsZV",
                            "hidden": null
                        },
                        {
                            "fieldId": "fldaecpp88nkq",
                            "hidden": null
                        },
                        {
                            "fieldId": "fldCtI5OA1Juj",
                            "hidden": false
                        },
                        {
                            "fieldId": "fld65cVnWdsJB",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldjztHYWLV1G",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldRTMMcxFH4W",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldSKRRCUPfpS",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldWkHEjmh925",
                            "hidden": false,
                            "statType": 8
                        },
                        {
                            "fieldId": "fld2RON4AAFJs",
                            "hidden": false
                        },
                        {
                            "fieldId": "fld3HuTid3mo7",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldFyWoTaUYZU",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldbL9rFZEs0F",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldjrE1btb8aF",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldzX5RYDJdVB",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldD82z1NGndr",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldRTqT2c57Z0",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldec4mK8vtSW",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldrvuUOjX2bP",
                            "hidden": false
                        },
                        {
                            "fieldId": "fldG038TbYQnm",
                            "hidden": false
                        }
                    ],
                    "rows": [
                        {
                            "recordId": "recJWt7jTShzD",
                            "hidden": null
                        },
                        {
                            "recordId": "reczhwDjAj46k",
                            "hidden": null
                        }
                    ],
                    "sortInfo": {
                        "rules": [
                            {
                                "fieldId": "fldns2pBdCBuY",
                                "desc": false
                            }
                        ],
                        "keepSort": true
                    },
                    "displayHiddenColumnWithinMirror": false,
                    "autoSave": false,
                    "groupInfo": [
                        {
                            "fieldId": "fldns2pBdCBuY",
                            "desc": true
                        }
                    ],
                    "frozenColumnCount": 1,
                    "rowHeightLevel": 1,
                    "autoHeadHeight": false
                }
            ]
        },
        "recordMap": {
            "recJWt7jTShzD": {
                "id": "recJWt7jTShzD",
                "commentCount": 0,
                "data": {
                    "fld2RON4AAFJs": 11.11012,
                    "fld3HuTid3mo7": 1692201600000,
                    "fld3yOP9Fexc6": [
                        {
                            "text": "1@qq.com",
                            "type": 1
                        }
                    ],
                    "fld65cVnWdsJB": [
                        "recj0azGMmkQS",
                        "recCaRl8RGBGE",
                        "recjcbCJnfXEl"
                    ],
                    "fldCtI5OA1Juj": [
                        {
                            "text": "https://www.baidu.com/",
                            "title": "https://www.baidu.com/",
                            "type": 2
                        }
                    ],
                    "fldFyWoTaUYZU": true,
                    "fldSKRRCUPfpS": "opt9HgRA8WRvE",
                    "fldWkHEjmh925": 1111.01,
                    "fldaecpp88nkq": [
                        {
                            "bucket": "QNY1",
                            "height": 1080,
                            "id": "atcTowrZcTpk3",
                            "mimeType": "image/png",
                            "name": "1827824615-1827824615-7850446944248791040-3655772686-10057-A-0-1-imgplus-0001.png",
                            "size": 2337098,
                            "token": "space/2023/08/09/21b24e25834a401da2827621314f9c0c",
                            "width": 1920
                        },
                        {
                            "bucket": "QNY1",
                            "height": 1080,
                            "id": "atcktH5nwAmhN",
                            "mimeType": "image/png",
                            "name": "1827824615-1827824615-7850446944248791040-3655772686-10057-A-0-1-imgplus-0001.png",
                            "size": 2337098,
                            "token": "space/2023/08/09/21b24e25834a401da2827621314f9c0c",
                            "width": 1920
                        }
                    ],
                    "fldbElOhmrgVj": 1111111111,
                    "fldbL9rFZEs0F": 3,
                    "fldhYUukLZsZV": [
                        "optoYLalVnEzk",
                        "optVDsdkpIuIZ"
                    ],
                    "fldjm35K4244q": [
                        {
                            "text": "1111",
                            "type": 1
                        }
                    ],
                    "fldjztHYWLV1G": [
                        "1688817023401119745",
                        "1689549079210176514"
                    ],
                    "fldns2pBdCBuY": [
                        {
                            "text": "test",
                            "type": 1
                        }
                    ],
                    "fldrvuUOjX2bP": [
                        {
                            "text": "互联网事业部/平台产品组/产品规划小组",
                            "type": 1
                        }
                    ],
                    "fldwdrAZuF32v": [
                        {
                            "text": "a\nb\nc\nd\ne",
                            "type": 1
                        }
                    ]
                },
                "createdAt": 1691482351000,
                "updatedAt": 1692673916000,
                "revisionHistory": [
                    1,
                    3,
                    21,
                    23,
                    27,
                    28,
                    29,
                    43,
                    44,
                    46,
                    56,
                    58,
                    60,
                    61,
                    64,
                    65,
                    70,
                    74,
                    77,
                    80,
                    83,
                    86,
                    87,
                    88,
                    89,
                    90,
                    91,
                    93,
                    96,
                    97,
                    99,
                    101,
                    104,
                    116,
                    118,
                    121,
                    122,
                    125,
                    126,
                    128,
                    130,
                    131,
                    133,
                    136,
                    137,
                    138,
                    140,
                    141,
                    146,
                    155,
                    156,
                    157,
                    158,
                    159,
                    160,
                    161,
                    162,
                    163,
                    170
                ],
                "recordMeta": {
                    "fieldUpdatedMap": {
                        "fldaecpp88nkq": {
                            "at": 1691567822761,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fld65cVnWdsJB": {
                            "at": 1692609325682,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldwdrAZuF32v": {
                            "at": 1692181111527,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fld3yOP9Fexc6": {
                            "at": 1691994015469,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fld3HuTid3mo7": {
                            "at": 1691993902595,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldabJ9NkGRoS": {
                            "at": null,
                            "by": null,
                            "autoNumber": 1
                        },
                        "fld2RON4AAFJs": {
                            "at": 1692673916573,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldWkHEjmh925": {
                            "at": 1692239651110,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldhYUukLZsZV": {
                            "at": 1692182540625,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldbL9rFZEs0F": {
                            "at": 1691993963582,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldjm35K4244q": {
                            "at": 1691993987834,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldjztHYWLV1G": {
                            "at": 1691654943336,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldbElOhmrgVj": {
                            "at": 1692182710273,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldrvuUOjX2bP": {
                            "at": 1692583088136,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldCtI5OA1Juj": {
                            "at": 1691567805300,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldFyWoTaUYZU": {
                            "at": 1691993937894,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldns2pBdCBuY": {
                            "at": 1692242373338,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldKxfEFhaioY": {
                            "at": 1692096556427,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldSKRRCUPfpS": {
                            "at": 1691993734426,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        }
                    },
                    "createdBy": "082476dc25834402ba979d62804480cd",
                    "updatedBy": null,
                    "createdAt": 1691482351148,
                    "updatedAt": null
                }
            },
            "reczhwDjAj46k": {
                "id": "reczhwDjAj46k",
                "commentCount": 0,
                "data": {
                    "fld2RON4AAFJs": 0.22,
                    "fld3HuTid3mo7": 1691942400000,
                    "fld65cVnWdsJB": [
                        "recCaRl8RGBGE"
                    ],
                    "fldCtI5OA1Juj": [
                        {
                            "text": "https://sina.cn/index/feed?from=touch&Ver=10",
                            "title": "https://sina.cn/index/feed?from=touch&Ver=10",
                            "type": 2
                        }
                    ],
                    "fldSKRRCUPfpS": "optqUUd4g0fQp",
                    "fldWkHEjmh925": 2222,
                    "fldaecpp88nkq": [
                        {
                            "bucket": "QNY1",
                            "height": 0,
                            "id": "atcbJdxKVyL1w",
                            "mimeType": "application/pdf",
                            "name": "sample.pdf",
                            "preview": "space/2023/08/15/85b64d0ab040405185072c049877ec1e",
                            "size": 3028,
                            "token": "space/2023/08/15/fcade27d30644d0db8c9d5cc87a322ca",
                            "width": 0
                        }
                    ],
                    "fldbElOhmrgVj": 1.22,
                    "fldhYUukLZsZV": [
                        "optVDsdkpIuIZ"
                    ],
                    "fldns2pBdCBuY": [
                        {
                            "text": "test",
                            "type": 1
                        }
                    ],
                    "fldrvuUOjX2bP": [
                        {
                            "text": "综合部/行政组/人力资源小组",
                            "type": 1
                        }
                    ],
                    "fldwdrAZuF32v": [
                        {
                            "text": "b",
                            "type": 1
                        }
                    ]
                },
                "createdAt": 1691567346000,
                "updatedAt": 1692583100000,
                "revisionHistory": [
                    24,
                    52,
                    54,
                    72,
                    75,
                    78,
                    81,
                    84,
                    92,
                    94,
                    104,
                    114,
                    115,
                    123,
                    124,
                    129,
                    142,
                    143,
                    147
                ],
                "recordMeta": {
                    "fieldUpdatedMap": {
                        "fld65cVnWdsJB": {
                            "at": 1692096531050,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fld3HuTid3mo7": {
                            "at": 1691993907565,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldSKRRCUPfpS": {
                            "at": 1691993741157,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldabJ9NkGRoS": {
                            "at": null,
                            "by": null,
                            "autoNumber": 2
                        },
                        "fldaecpp88nkq": {
                            "at": 1692095982387,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldhYUukLZsZV": {
                            "at": 1691579383877,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldbElOhmrgVj": {
                            "at": 1691993790196,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldrvuUOjX2bP": {
                            "at": 1692583100025,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldWkHEjmh925": {
                            "at": 1691993836284,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fld2RON4AAFJs": {
                            "at": 1691993874969,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldCtI5OA1Juj": {
                            "at": 1691579403753,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldns2pBdCBuY": {
                            "at": 1692242381499,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldFyWoTaUYZU": {
                            "at": 1691993939366,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        },
                        "fldwdrAZuF32v": {
                            "at": 1692181094188,
                            "by": "082476dc25834402ba979d62804480cd",
                            "autoNumber": null
                        }
                    },
                    "createdBy": "082476dc25834402ba979d62804480cd",
                    "updatedBy": null,
                    "createdAt": 1691567346794,
                    "updatedAt": null
                }
            }
        },
        "datasheetId": "dstMUyAamjZxi7EPci"
    },
    "datasheet": {
        "id": "dstMUyAamjZxi7EPci",
        "name": "fusionApi测试",
        "description": "{}",
        "parentId": "fod15zex9R7c3",
        "icon": "",
        "nodeShared": false,
        "nodePermitSet": false,
        "nodeFavorite": false,
        "spaceId": "spc2qi5CvEWqw",
        "role": "manager",
        "permissions": {
            "isDeleted": false,
            "allowEditConfigurable": true,
            "allowSaveConfigurable": true,
            "cellEditable": true,
            "childCreatable": true,
            "columnCountEditable": true,
            "columnHideable": true,
            "columnSortable": true,
            "columnWidthEditable": true,
            "copyable": true,
            "datasheetId": "dstMUyAamjZxi7EPci",
            "descriptionEditable": true,
            "editable": true,
            "exportable": true,
            "fieldCreatable": true,
            "fieldGroupable": true,
            "fieldPermissionManageable": true,
            "fieldPropertyEditable": true,
            "fieldRemovable": true,
            "fieldRenamable": true,
            "fieldSortable": true,
            "iconEditable": true,
            "importable": true,
            "manageable": true,
            "movable": true,
            "nodeId": "dstMUyAamjZxi7EPci",
            "readable": true,
            "removable": true,
            "renamable": true,
            "rowCreatable": true,
            "rowHighEditable": true,
            "rowRemovable": true,
            "rowSortable": true,
            "sharable": true,
            "templateCreatable": true,
            "viewColorOptionEditable": true,
            "viewCreatable": true,
            "viewExportable": true,
            "viewFilterable": true,
            "viewKeyFieldEditable": true,
            "viewLayoutEditable": true,
            "viewLockManageable": true,
            "viewManualSaveManageable": true,
            "viewMovable": true,
            "viewOptionSaveEditable": true,
            "viewRemovable": true,
            "viewRenamable": true,
            "viewStyleEditable": true
        },
        "revision": 170,
        "isGhostNode": false,
        "extra": {
            "showRecordHistory": true
        }
    },
    "foreignDatasheetMap": {
        "dsttz8V4j2zvtMX0L3": {
            "snapshot": {
                "meta": {
                    "fieldMap": {
                        "fldLeq3dPM1x7": {
                            "id": "fldLeq3dPM1x7",
                            "name": "新建表格",
                            "type": 7,
                            "property": {
                                "foreignDatasheetId": "dstMUyAamjZxi7EPci",
                                "brotherFieldId": "fld65cVnWdsJB"
                            }
                        },
                        "fldFMb1ffnQRX": {
                            "id": "fldFMb1ffnQRX",
                            "name": "标题",
                            "type": 19,
                            "property": {}
                        },
                        "fldBZ7h2FrW2w": {
                            "id": "fldBZ7h2FrW2w",
                            "name": "选项",
                            "type": 4,
                            "property": {
                                "options": []
                            }
                        },
                        "fldywkRiobJD1": {
                            "id": "fldywkRiobJD1",
                            "name": "附件",
                            "type": 6
                        }
                    },
                    "views": [
                        {
                            "id": "viwXGErHY9VJe",
                            "name": "维格视图",
                            "type": 1,
                            "columns": [
                                {
                                    "fieldId": "fldFMb1ffnQRX",
                                    "hidden": null,
                                    "statType": 1
                                },
                                {
                                    "fieldId": "fldBZ7h2FrW2w",
                                    "hidden": null
                                },
                                {
                                    "fieldId": "fldywkRiobJD1",
                                    "hidden": null
                                },
                                {
                                    "fieldId": "fldLeq3dPM1x7",
                                    "hidden": false
                                }
                            ],
                            "rows": [
                                {
                                    "recordId": "recj0azGMmkQS",
                                    "hidden": null
                                },
                                {
                                    "recordId": "recCaRl8RGBGE",
                                    "hidden": null
                                },
                                {
                                    "recordId": "recjcbCJnfXEl",
                                    "hidden": null
                                }
                            ],
                            "displayHiddenColumnWithinMirror": false,
                            "autoSave": false,
                            "frozenColumnCount": 1
                        }
                    ]
                },
                "recordMap": {
                    "recCaRl8RGBGE": {
                        "id": "recCaRl8RGBGE",
                        "commentCount": 0,
                        "data": {
                            "fldFMb1ffnQRX": [
                                {
                                    "text": "2",
                                    "type": 1
                                }
                            ],
                            "fldLeq3dPM1x7": [
                                "reczhwDjAj46k",
                                "recJWt7jTShzD"
                            ],
                            "fldywkRiobJD1": [
                                {
                                    "bucket": "QNY1",
                                    "height": 110,
                                    "id": "atcGdPSe2aANv",
                                    "mimeType": "image/png",
                                    "name": "image.png",
                                    "size": 8468,
                                    "token": "space/2023/08/21/c3b12a3377e84316a53a4427f626a9fc",
                                    "width": 460
                                }
                            ]
                        },
                        "createdAt": 1691649851000,
                        "updatedAt": 1692609325000,
                        "revisionHistory": [
                            0,
                            3,
                            7,
                            9,
                            10,
                            13,
                            18,
                            21
                        ],
                        "recordMeta": {
                            "fieldUpdatedMap": {
                                "fldLeq3dPM1x7": {
                                    "at": 1692609325105,
                                    "by": "082476dc25834402ba979d62804480cd",
                                    "autoNumber": null
                                },
                                "fldywkRiobJD1": {
                                    "at": 1692607973216,
                                    "by": "082476dc25834402ba979d62804480cd",
                                    "autoNumber": null
                                },
                                "fldFMb1ffnQRX": {
                                    "at": 1692096521945,
                                    "by": "082476dc25834402ba979d62804480cd",
                                    "autoNumber": null
                                }
                            },
                            "createdBy": "082476dc25834402ba979d62804480cd",
                            "updatedBy": null,
                            "createdAt": 1691649851920,
                            "updatedAt": null
                        }
                    },
                    "recj0azGMmkQS": {
                        "id": "recj0azGMmkQS",
                        "commentCount": 0,
                        "data": {
                            "fldFMb1ffnQRX": [
                                {
                                    "text": "1",
                                    "type": 1
                                }
                            ],
                            "fldLeq3dPM1x7": [
                                "recJWt7jTShzD"
                            ],
                            "fldywkRiobJD1": [
                                {
                                    "bucket": "QNY1",
                                    "height": 232,
                                    "id": "atcvDzYm8DDXK",
                                    "mimeType": "image/png",
                                    "name": "image.png",
                                    "size": 22653,
                                    "token": "space/2023/08/21/cf5e13539906411caaebd535f601d2c7",
                                    "width": 510
                                },
                                {
                                    "bucket": "QNY1",
                                    "height": 554,
                                    "id": "atcp1eBDxdXTO",
                                    "mimeType": "image/png",
                                    "name": "image.png",
                                    "size": 12140,
                                    "token": "space/2023/08/21/4351dbae8552470db0f7abc77902245e",
                                    "width": 548
                                }
                            ]
                        },
                        "createdAt": 1691649851000,
                        "updatedAt": 1692609323000,
                        "revisionHistory": [
                            0,
                            2,
                            5,
                            6,
                            11,
                            12,
                            14,
                            15,
                            17,
                            20
                        ],
                        "recordMeta": {
                            "fieldUpdatedMap": {
                                "fldywkRiobJD1": {
                                    "at": 1692607956383,
                                    "by": "082476dc25834402ba979d62804480cd",
                                    "autoNumber": null
                                },
                                "fldLeq3dPM1x7": {
                                    "at": 1692609323919,
                                    "by": "082476dc25834402ba979d62804480cd",
                                    "autoNumber": null
                                },
                                "fldFMb1ffnQRX": {
                                    "at": 1692096517535,
                                    "by": "082476dc25834402ba979d62804480cd",
                                    "autoNumber": null
                                }
                            },
                            "createdBy": "082476dc25834402ba979d62804480cd",
                            "updatedBy": null,
                            "createdAt": 1691649851920,
                            "updatedAt": null
                        }
                    },
                    "recjcbCJnfXEl": {
                        "id": "recjcbCJnfXEl",
                        "commentCount": 0,
                        "data": {
                            "fldFMb1ffnQRX": [
                                {
                                    "text": "3",
                                    "type": 1
                                }
                            ],
                            "fldLeq3dPM1x7": [
                                "recJWt7jTShzD"
                            ]
                        },
                        "createdAt": 1691649851000,
                        "updatedAt": 1692609325000,
                        "revisionHistory": [
                            0,
                            4,
                            8,
                            16,
                            19,
                            22
                        ],
                        "recordMeta": {
                            "fieldUpdatedMap": {
                                "fldFMb1ffnQRX": {
                                    "at": 1692096525905,
                                    "by": "082476dc25834402ba979d62804480cd",
                                    "autoNumber": null
                                },
                                "fldLeq3dPM1x7": {
                                    "at": 1692609325699,
                                    "by": "082476dc25834402ba979d62804480cd",
                                    "autoNumber": null
                                }
                            },
                            "createdBy": "082476dc25834402ba979d62804480cd",
                            "updatedBy": null,
                            "createdAt": 1691649851920,
                            "updatedAt": null
                        }
                    }
                },
                "datasheetId": "dsttz8V4j2zvtMX0L3"
            },
            "datasheet": {
                "description": "{}",
                "extra": {
                    "showRecordHistory": true
                },
                "icon": "",
                "id": "dsttz8V4j2zvtMX0L3",
                "isGhostNode": false,
                "name": "测试2",
                "nodeFavorite": false,
                "nodePermitSet": false,
                "nodeShared": false,
                "parentId": "fod15zex9R7c3",
                "permissions": {
                    "allowEditConfigurable": true,
                    "allowSaveConfigurable": true,
                    "cellEditable": true,
                    "childCreatable": true,
                    "columnCountEditable": true,
                    "columnHideable": true,
                    "columnSortable": true,
                    "columnWidthEditable": true,
                    "copyable": true,
                    "datasheetId": "dsttz8V4j2zvtMX0L3",
                    "descriptionEditable": true,
                    "editable": true,
                    "exportable": true,
                    "fieldCreatable": true,
                    "fieldGroupable": true,
                    "fieldPermissionManageable": true,
                    "fieldPropertyEditable": true,
                    "fieldRemovable": true,
                    "fieldRenamable": true,
                    "fieldSortable": true,
                    "iconEditable": true,
                    "importable": true,
                    "isDeleted": false,
                    "manageable": true,
                    "movable": true,
                    "nodeId": "dsttz8V4j2zvtMX0L3",
                    "readable": true,
                    "removable": true,
                    "renamable": true,
                    "rowCreatable": true,
                    "rowHighEditable": true,
                    "rowRemovable": true,
                    "rowSortable": true,
                    "sharable": true,
                    "templateCreatable": true,
                    "viewColorOptionEditable": true,
                    "viewCreatable": true,
                    "viewExportable": true,
                    "viewFilterable": true,
                    "viewKeyFieldEditable": true,
                    "viewLayoutEditable": true,
                    "viewLockManageable": true,
                    "viewManualSaveManageable": true,
                    "viewMovable": true,
                    "viewOptionSaveEditable": true,
                    "viewRemovable": true,
                    "viewRenamable": true,
                    "viewStyleEditable": true
                },
                "revision": 22,
                "role": "manager",
                "spaceId": "spc2qi5CvEWqw"
            }
        }
    },
    "units": [
        {
            "unitId": "1688817023401119745",
            "type": 3,
            "name": "pengcheng",
            "uuid": "082476dc25834402ba979d62804480cd",
            "userId": "082476dc25834402ba979d62804480cd",
            "avatar": null,
            "isActive": 1,
            "isDeleted": 0,
            "nickName": "pengcheng",
            "avatarColor": 3,
            "isMemberNameModified": true,
            "originalUnitId": "8b01ed1f6629abf1a76e172e6082f8fc"
        },
        {
            "unitId": "1689549079210176514",
            "type": 3,
            "name": "liushuang",
            "uuid": "93ae987a39104019a8718f318e7b3073",
            "userId": "93ae987a39104019a8718f318e7b3073",
            "avatar": null,
            "isActive": 1,
            "isDeleted": 0,
            "nickName": "liushuang",
            "avatarColor": 9,
            "isMemberNameModified": true,
            "originalUnitId": "a1ca6e96dfe5101564b132d3ee105da7"
        },
        {
            "unitId": "1688817023401119745",
            "type": 3,
            "name": "pengcheng",
            "uuid": "082476dc25834402ba979d62804480cd",
            "userId": "082476dc25834402ba979d62804480cd",
            "avatar": null,
            "isActive": 1,
            "isDeleted": 0,
            "nickName": "pengcheng",
            "avatarColor": 3,
            "isMemberNameModified": true,
            "isNickNameModified": true,
            "originalUnitId": "8b01ed1f6629abf1a76e172e6082f8fc"
        }
    ]
}
"#;
