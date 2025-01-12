#[cfg(test)]
pub const UPDATE_RECORDS_SNAP_SHOT_JSON_BASIC: &str = r#"{
    "data": {
        "snapshot": {
            "meta": {
                "views": [
                    {
                        "id": "viwkKhepmhhTM",
                        "name": "Grid view",
                        "rows": [
                            {
                                "recordId": "recVXri0Gw8xh"
                            },
                            {
                                "recordId": "recN33ikBaSww"
                            },
                            {
                                "recordId": "recCJ99FQPUsP"
                            }
                        ],
                        "type": 1,
                        "columns": [
                            {
                                "fieldId": "fldB6ulNG6taL",
                                "statType": 1
                            },
                            {
                                "fieldId": "fldsye3jGrqmA"
                            },
                            {
                                "fieldId": "flda9m6zhiZMW"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldz5wgqkk5Du"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldby3h7iXMkB"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldSaL0gdR8bF"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldKXUwEujc32"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldCPGi8aXPnJ"
                            }
                        ],
                        "autoSave": false,
                        "frozenColumnCount": 1,
                        "displayHiddenColumnWithinMirror": false
                    }
                ],
                "fieldMap": {
                    "fldB6ulNG6taL": {
                        "id": "fldB6ulNG6taL",
                        "name": "Title",
                        "type": 19,
                        "property": {
                            "defaultValue": ""
                        }
                    },
                    "fldCPGi8aXPnJ": {
                        "id": "fldCPGi8aXPnJ",
                        "name": "邮箱类型",
                        "type": 9,
                        "property": null
                    },
                    "fldKXUwEujc32": {
                        "id": "fldKXUwEujc32",
                        "name": "电话类型",
                        "type": 10,
                        "property": null
                    },
                    "fldSaL0gdR8bF": {
                        "id": "fldSaL0gdR8bF",
                        "name": "分数类型",
                        "type": 12,
                        "property": {
                            "max": 10,
                            "icon": "star"
                        }
                    },
                    "flda9m6zhiZMW": {
                        "id": "flda9m6zhiZMW",
                        "name": "Attachments",
                        "type": 6
                    },
                    "fldby3h7iXMkB": {
                        "id": "fldby3h7iXMkB",
                        "name": "复选框类型",
                        "type": 11,
                        "property": {
                            "icon": "white_check_mark"
                        }
                    },
                    "fldsye3jGrqmA": {
                        "id": "fldsye3jGrqmA",
                        "name": "Options",
                        "type": 4,
                        "property": {
                            "options": []
                        }
                    },
                    "fldz5wgqkk5Du": {
                        "id": "fldz5wgqkk5Du",
                        "name": "多行文本列",
                        "type": 1,
                        "property": null
                    }
                }
            },
            "recordMap": {
                "recCJ99FQPUsP": {
                    "id": "recCJ99FQPUsP",
                    "data": {
                        "fldB6ulNG6taL": [
                            {
                                "text": "c",
                                "type": 1
                            }
                        ]
                    },
                    "createdAt": 1695629151000,
                    "updatedAt": 1695629178000,
                    "revisionHistory": [
                        0,
                        3
                    ],
                    "recordMeta": {
                        "createdAt": 1695629151464,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3",
                        "fieldUpdatedMap": {
                            "fldB6ulNG6taL": {
                                "at": 1695629177991,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            }
                        }
                    },
                    "commentCount": 0
                },
                "recN33ikBaSww": {
                    "id": "recN33ikBaSww",
                    "data": {
                        "fldB6ulNG6taL": [
                            {
                                "text": "b",
                                "type": 1
                            }
                        ]
                    },
                    "createdAt": 1695629151000,
                    "updatedAt": 1695629177000,
                    "revisionHistory": [
                        0,
                        2
                    ],
                    "recordMeta": {
                        "createdAt": 1695629151464,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3",
                        "fieldUpdatedMap": {
                            "fldB6ulNG6taL": {
                                "at": 1695629177103,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            }
                        }
                    },
                    "commentCount": 0
                },
                "recVXri0Gw8xh": {
                    "id": "recVXri0Gw8xh",
                    "data": {
                        "fldB6ulNG6taL": [
                            {
                                "text": "a",
                                "type": 1
                            }
                        ],
                        "fldCPGi8aXPnJ": [
                            {
                                "text": "12345@vikadata.com",
                                "type": 1
                            }
                        ],
                        "fldKXUwEujc32": [
                            {
                                "text": "123456789",
                                "type": 1
                            }
                        ],
                        "fldSaL0gdR8bF": 6,
                        "fldby3h7iXMkB": true,
                        "fldz5wgqkk5Du": [
                            {
                                "text": "系统测试：\n1.UAT测试\n2.AT测试",
                                "type": 1
                            }
                        ]
                    },
                    "createdAt": 1695629151000,
                    "updatedAt": 1695632198000,
                    "revisionHistory": [
                        0,
                        1,
                        8,
                        10,
                        14,
                        16,
                        18
                    ],
                    "recordMeta": {
                        "createdAt": 1695629151464,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3",
                        "fieldUpdatedMap": {
                            "fldB6ulNG6taL": {
                                "at": 1695629176156,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldCPGi8aXPnJ": {
                                "at": 1695632198786,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldKXUwEujc32": {
                                "at": 1695632168981,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldSaL0gdR8bF": {
                                "at": 1695632099406,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldby3h7iXMkB": {
                                "at": 1695631999198,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldz5wgqkk5Du": {
                                "at": 1695630094920,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            }
                        }
                    },
                    "commentCount": 0
                },
                "recwwnDpKHqKC": {
                    "id": "recwwnDpKHqKC",
                    "data": {},
                    "createdAt": 1695632768000,
                    "updatedAt": 1695632768000,
                    "revisionHistory": [
                        21
                    ],
                    "recordMeta": {
                        "createdAt": 1695632768897,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3"
                    },
                    "commentCount": 0
                }
            },
            "datasheetId": "dst9TgbZAZXzp8ESoD"
        }
    }
}
"#;