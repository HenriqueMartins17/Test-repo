#[cfg(test)]
pub const UPDATE_RECORDS_SNAP_SHOT_JSON: &str = r#"{
    "data": {
        "snapshot": {
            "meta": {
                "views": [
                    {
                        "id": "viwUFDJoVvY1Q",
                        "name": "Grid view",
                        "rows": [
                            {
                                "recordId": "recykfXMQCq0E"
                            },
                            {
                                "recordId": "recDrmJkJClqY"
                            },
                            {
                                "recordId": "recYrxxUvzXnj"
                            }
                        ],
                        "type": 1,
                        "columns": [
                            {
                                "fieldId": "fldvoJy0VpMV5",
                                "statType": 1
                            },
                            {
                                "fieldId": "fldGYibFa2AyX"
                            },
                            {
                                "fieldId": "fldQbTS4Tu7YX"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldhCpbPF5wgg"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldYTgHxM4Nkx"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldUWoHnuqCbB"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldypjJb300zp"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldMtxjFIJdbm",
                                "statType": 8
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldCBnoSSX0Qk",
                                "statType": 8
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldkCruCJZbq5"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldrxkZjcba2P"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldO3TerxFBoT"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldigwAJUXuvE"
                            }
                        ],
                        "autoSave": false,
                        "frozenColumnCount": 1,
                        "displayHiddenColumnWithinMirror": false
                    }
                ],
                "fieldMap": {
                    "fldCBnoSSX0Qk": {
                        "id": "fldCBnoSSX0Qk",
                        "name": "货币类型",
                        "type": 17,
                        "property": {
                            "symbol": "",
                            "precision": 0,
                            "symbolAlign": 0
                        }
                    },
                    "fldGYibFa2AyX": {
                        "id": "fldGYibFa2AyX",
                        "name": "Options",
                        "type": 4,
                        "property": {
                            "options": []
                        }
                    },
                    "fldMtxjFIJdbm": {
                        "id": "fldMtxjFIJdbm",
                        "name": "数字类型01",
                        "type": 2,
                        "property": {
                            "precision": 0,
                            "symbolAlign": 2
                        }
                    },
                    "fldO3TerxFBoT": {
                        "id": "fldO3TerxFBoT",
                        "name": "url类型",
                        "type": 8,
                        "property": {
                            "isRecogURLFlag": false
                        }
                    },
                    "fldQbTS4Tu7YX": {
                        "id": "fldQbTS4Tu7YX",
                        "name": "Attachments",
                        "type": 6
                    },
                    "fldUWoHnuqCbB": {
                        "id": "fldUWoHnuqCbB",
                        "name": "关联成绩表",
                        "type": 7,
                        "property": {
                            "brotherFieldId": "fldiQVEetmF27",
                            "foreignDatasheetId": "dstNXByeylmqUBLB70"
                        }
                    },
                    "fldYTgHxM4Nkx": {
                        "id": "fldYTgHxM4Nkx",
                        "name": "单选类型",
                        "type": 3,
                        "property": {
                            "options": [
                                {
                                    "id": "optcecFvI3KW7",
                                    "name": "好",
                                    "color": 0
                                },
                                {
                                    "id": "optswCbUzxpqC",
                                    "name": "良",
                                    "color": 1
                                },
                                {
                                    "id": "opt1ASNU4a2jg",
                                    "name": "差",
                                    "color": 2
                                }
                            ]
                        }
                    },
                    "fldhCpbPF5wgg": {
                        "id": "fldhCpbPF5wgg",
                        "name": "成员类型",
                        "type": 13,
                        "property": {
                            "isMulti": true,
                            "unitIds": [
                                "1696426247320952834"
                            ],
                            "subscription": false,
                            "shouldSendMsg": true
                        }
                    },
                    "fldigwAJUXuvE": {
                        "id": "fldigwAJUXuvE",
                        "name": "附件类型",
                        "type": 6,
                        "property": null
                    },
                    "fldkCruCJZbq5": {
                        "id": "fldkCruCJZbq5",
                        "name": "百分比类型",
                        "type": 18,
                        "property": {
                            "precision": 0
                        }
                    },
                    "fldrxkZjcba2P": {
                        "id": "fldrxkZjcba2P",
                        "name": "日期类型",
                        "type": 5,
                        "property": {
                            "autoFill": false,
                            "dateFormat": 0,
                            "timeFormat": 1,
                            "includeTime": false
                        }
                    },
                    "fldvoJy0VpMV5": {
                        "id": "fldvoJy0VpMV5",
                        "name": "Title",
                        "type": 19,
                        "property": {
                            "defaultValue": ""
                        }
                    },
                    "fldypjJb300zp": {
                        "id": "fldypjJb300zp",
                        "name": "多选类型",
                        "type": 4,
                        "property": {
                            "options": [
                                {
                                    "id": "opt3PIgcGz7Mf",
                                    "name": "好",
                                    "color": 0
                                },
                                {
                                    "id": "optxHHBOJ1Ot5",
                                    "name": "良",
                                    "color": 1
                                },
                                {
                                    "id": "optBSB4eKgy30",
                                    "name": "差",
                                    "color": 2
                                }
                            ]
                        }
                    }
                }
            },
            "recordMap": {
                "recDrmJkJClqY": {
                    "id": "recDrmJkJClqY",
                    "data": {},
                    "createdAt": 1694596574000,
                    "updatedAt": 1694596574000,
                    "revisionHistory": [
                        0
                    ],
                    "recordMeta": {
                        "createdAt": 1694596574399,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3"
                    },
                    "commentCount": 0
                },
                "recykfXMQCq0E": {
                    "id": "recykfXMQCq0E",
                    "data": {
                        "fldCBnoSSX0Qk": 3,
                        "fldMtxjFIJdbm": 40,
                        "fldO3TerxFBoT": [
                            {
                                "text": "www.google.com",
                                "type": 2,
                                "title": "www.google.com"
                            }
                        ],
                        "fldYTgHxM4Nkx": "opt1ASNU4a2jg",
                        "fldhCpbPF5wgg": [
                            "1696426247320952834"
                        ],
                        "fldkCruCJZbq5": 0.15,
                        "fldrxkZjcba2P": 1694534400000,
                        "fldypjJb300zp": [
                            "opt3PIgcGz7Mf",
                            "optxHHBOJ1Ot5",
                            "optBSB4eKgy30"
                        ],
                        "fldigwAJUXuvE": [
                            {
                                "id": "atcywoE8o3e52",
                                "mimeType": "image/png",
                                "name": "207.png",
                                "size": 237738,
                                "token": "space/2023/09/13/da349d0487af429f9a46d933ca71e5ee",
                                "bucket": "QNY1",
                                "width": 1660,
                                "height": 1006
                            }
                        ],
                        "fldvoJy0VpMV5": [
                            {
                                "type": 1,
                                "text": "100"
                            }
                        ]
                    },
                    "createdAt": 1694596574000,
                    "updatedAt": 1694599151000,
                    "revisionHistory": [
                        0,
                        2,
                        5,
                        10,
                        11,
                        12,
                        13,
                        14,
                        15,
                        16,
                        19,
                        21,
                        25,
                        27,
                        29,
                        30,
                        36,
                        37,
                        38,
                        39,
                        40,
                        45,
                        46,
                        47
                    ],
                    "recordMeta": {
                        "createdAt": 1694596574399,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3",
                        "fieldUpdatedMap": {
                            "fldCBnoSSX0Qk": {
                                "at": 1694597041112,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldMtxjFIJdbm": {
                                "at": 1694597015737,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldO3TerxFBoT": {
                                "at": 1694598126845,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldYTgHxM4Nkx": {
                                "at": 1694596974816,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldhCpbPF5wgg": {
                                "at": 1694596645429,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldigwAJUXuvE": {
                                "at": 1694599482476,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldkCruCJZbq5": {
                                "at": 1694597092614,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldrxkZjcba2P": {
                                "at": 1694597118273,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldvoJy0VpMV5": {
                                "at": 1694599853439,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldypjJb300zp": {
                                "at": 1694596943166,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            }
                        }
                    },
                    "commentCount": 0
                },
                "recYrxxUvzXnj": {
                    "id": "recYrxxUvzXnj",
                    "data": {},
                    "createdAt": 1694596574000,
                    "updatedAt": 1694596574000,
                    "revisionHistory": [
                        0
                    ],
                    "recordMeta": {
                        "createdAt": 1694596574399,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3"
                    },
                    "commentCount": 0
                },
                "recXbVAZBKdHk": {
                    "id": "recXbVAZBKdHk",
                    "data": {},
                    "createdAt": 1694598199000,
                    "updatedAt": 1694598199000,
                    "revisionHistory": [
                        41
                    ],
                    "recordMeta": {
                        "createdAt": 1694598199854,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3"
                    },
                    "commentCount": 0
                },
                "recQiHOf24b0C": {
                    "id": "recQiHOf24b0C",
                    "data": {},
                    "createdAt": 1694599174000,
                    "updatedAt": 1694599174000,
                    "revisionHistory": [
                        48
                    ],
                    "recordMeta": {
                        "createdAt": 1694599174838,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3"
                    },
                    "commentCount": 0
                },
                "recysamnhwxrq": {
                    "id": "recysamnhwxrq",
                    "data": {},
                    "createdAt": 1694599402000,
                    "updatedAt": 1694599402000,
                    "revisionHistory": [
                        51
                    ],
                    "recordMeta": {
                        "createdAt": 1694599402949,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3"
                    },
                    "commentCount": 0
                },
                "rec6Mrut2gbcU": {
                    "id": "rec6Mrut2gbcU",
                    "data": {},
                    "createdAt": 1694599826000,
                    "updatedAt": 1694599826000,
                    "revisionHistory": [
                        58
                    ],
                    "recordMeta": {
                        "createdAt": 1694599826845,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3"
                    },
                    "commentCount": 0
                }
            },
            "datasheetId": "dstiaeZJYXq9G2Rt62"
        }
    }
}
"#;