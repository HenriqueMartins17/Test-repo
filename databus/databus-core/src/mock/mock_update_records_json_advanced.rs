#[cfg(test)]
pub const UPDATE_RECORDS_SNAP_SHOT_JSON_ADVANCED: &str = r#"{
    "data": {
        "snapshot": {
            "meta": {
                "views": [
                    {
                        "id": "viwsRqraWn04W",
                        "name": "Grid view",
                        "rows": [
                            {
                                "recordId": "recsoU03ufryC"
                            }
                        ],
                        "type": 1,
                        "columns": [
                            {
                                "fieldId": "fld67xFxFWiQY",
                                "statType": 1
                            },
                            {
                                "fieldId": "fld3mpdpHHzhr"
                            },
                            {
                                "fieldId": "fldHwV4gtdz0x"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fld93XJmtvEvG"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldXtL8di9Ml3"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldAxNNXb0W9n"
                            },
                            {
                                "hidden": false,
                                "fieldId": "flddqAb59nlcP"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldR6jXxnPlHG"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldWIs42zvH0R",
                                "statType": 8
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldfumjqhCzHD",
                                "statType": 8
                            },
                            {
                                "hidden": false,
                                "fieldId": "fld1jYCl4UVNx"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldVF9rtyYutu"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldsTUeKuBZdl"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldEqKudex3TZ"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fld0sz3kYikhm"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldaU54R9oOT4"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldvzcSClJ6bi"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldLtgIQQSuEz"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldjRiWUGW5nh"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldAbEedFa96E"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fld5q3e4zu3e4"
                            },
                            {
                                "hidden": false,
                                "fieldId": "fldaZiYMYLZuD"
                            }
                        ],
                        "autoSave": false,
                        "frozenColumnCount": 1,
                        "displayHiddenColumnWithinMirror": false
                    }
                ],
                "fieldMap": {
                    "fld0sz3kYikhm": {
                        "id": "fld0sz3kYikhm",
                        "name": "计算平均值",
                        "type": 16,
                        "property": {
                            "expression": "AVERAGE({fldVF9rtyYutu},{fldsTUeKuBZdl})",
                            "formatting": {
                                "symbol": "$",
                                "precision": 0,
                                "formatType": 2
                            },
                            "datasheetId": "dstRUJJUjbiX0YE8MZ"
                        }
                    },
                    "fld1jYCl4UVNx": {
                        "id": "fld1jYCl4UVNx",
                        "name": "成绩表",
                        "type": 7,
                        "property": {
                            "brotherFieldId": "fldfDJTCQBFGN",
                            "foreignDatasheetId": "dstkaaiWsiHWTyZqbe"
                        }
                    },
                    "fld3mpdpHHzhr": {
                        "id": "fld3mpdpHHzhr",
                        "name": "Options",
                        "type": 4,
                        "property": {
                            "options": []
                        }
                    },
                    "fld5q3e4zu3e4": {
                        "id": "fld5q3e4zu3e4",
                        "name": "what",
                        "type": 25,
                        "property": {
                            "showAll": false,
                            "linkedFields": [
                                {
                                    "id": "fldzxDJYF61DU",
                                    "name": "Title",
                                    "type": 19
                                },
                                {
                                    "id": "fldX9rQyN4Nqh",
                                    "name": "t1",
                                    "type": 1
                                }
                            ],
                            "linkedViewId": "viwbj7namJ1li",
                            "fullLinkedFields": [
                                {
                                    "id": "fldzxDJYF61DU",
                                    "name": "Title",
                                    "type": 19
                                },
                                {
                                    "id": "fldX9rQyN4Nqh",
                                    "name": "t1",
                                    "type": 1
                                },
                                {
                                    "id": "fldez5vRjQX55",
                                    "name": "Options",
                                    "type": 4
                                }
                            ],
                            "linkedDatasheetId": "dstNXByeylmqUBLB70"
                        }
                    },
                    "fld67xFxFWiQY": {
                        "id": "fld67xFxFWiQY",
                        "name": "Title",
                        "type": 19,
                        "property": {
                            "defaultValue": ""
                        }
                    },
                    "fld93XJmtvEvG": {
                        "id": "fld93XJmtvEvG",
                        "name": "关联班级表",
                        "type": 7,
                        "property": {
                            "brotherFieldId": "fldSc5cHSdLP1",
                            "foreignDatasheetId": "dstfqo9mKoqjJvmkfw"
                        }
                    },
                    "fldAbEedFa96E": {
                        "id": "fldAbEedFa96E",
                        "name": "修改人类型",
                        "type": 24,
                        "property": {
                            "uuids": [
                                "353619ba95d5403caf003879b3ae20e3"
                            ],
                            "collectType": 0,
                            "datasheetId": "dstRUJJUjbiX0YE8MZ",
                            "fieldIdCollection": []
                        }
                    },
                    "fldAxNNXb0W9n": {
                        "id": "fldAxNNXb0W9n",
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
                    "fldEqKudex3TZ": {
                        "id": "fldEqKudex3TZ",
                        "name": "计算",
                        "type": 16,
                        "property": {
                            "expression": "SUM({fldVF9rtyYutu},{fldsTUeKuBZdl})",
                            "formatting": {
                                "symbol": "$",
                                "precision": 0,
                                "formatType": 2
                            },
                            "datasheetId": "dstRUJJUjbiX0YE8MZ"
                        }
                    },
                    "fldHwV4gtdz0x": {
                        "id": "fldHwV4gtdz0x",
                        "name": "Attachments",
                        "type": 6
                    },
                    "fldLtgIQQSuEz": {
                        "id": "fldLtgIQQSuEz",
                        "name": "更新时间类型",
                        "type": 22,
                        "property": {
                            "dateFormat": 0,
                            "timeFormat": 0,
                            "collectType": 0,
                            "datasheetId": "dstRUJJUjbiX0YE8MZ",
                            "includeTime": true,
                            "fieldIdCollection": []
                        }
                    },
                    "fldR6jXxnPlHG": {
                        "id": "fldR6jXxnPlHG",
                        "name": "数字类型01货币类型",
                        "type": 16,
                        "property": {
                            "expression": "SUM({fldWIs42zvH0R},{fldfumjqhCzHD})",
                            "formatting": {
                                "symbol": "$",
                                "precision": 2,
                                "formatType": 2
                            },
                            "datasheetId": "dstRUJJUjbiX0YE8MZ"
                        }
                    },
                    "fldVF9rtyYutu": {
                        "id": "fldVF9rtyYutu",
                        "name": "引用成绩表中语文成绩",
                        "type": 14,
                        "property": {
                            "formatting": {
                                "symbol": "$",
                                "precision": 0,
                                "formatType": 2
                            },
                            "datasheetId": "dstRUJJUjbiX0YE8MZ",
                            "relatedLinkFieldId": "fld1jYCl4UVNx",
                            "lookUpTargetFieldId": "fldqKySSQ2IKc"
                        }
                    },
                    "fldWIs42zvH0R": {
                        "id": "fldWIs42zvH0R",
                        "name": "数字类型01",
                        "type": 2,
                        "property": {
                            "precision": 3,
                            "commaStyle": ",",
                            "symbolAlign": 2
                        }
                    },
                    "fldXtL8di9Ml3": {
                        "id": "fldXtL8di9Ml3",
                        "name": "引用班级表数据",
                        "type": 14,
                        "property": {
                            "formatting": {
                                "symbol": "$",
                                "precision": 0,
                                "formatType": 2
                            },
                            "datasheetId": "dstRUJJUjbiX0YE8MZ",
                            "relatedLinkFieldId": "fld93XJmtvEvG",
                            "lookUpTargetFieldId": "fldKtHoSl68Ma"
                        }
                    },
                    "fldaU54R9oOT4": {
                        "id": "fldaU54R9oOT4",
                        "name": "自增数字类型",
                        "type": 20,
                        "property": {
                            "nextId": 6,
                            "viewIdx": 0,
                            "datasheetId": "dstRUJJUjbiX0YE8MZ"
                        }
                    },
                    "fldaZiYMYLZuD": {
                        "id": "fldaZiYMYLZuD",
                        "name": "单项关联类型",
                        "type": 26,
                        "property": {
                            "foreignDatasheetId": "dstkaaiWsiHWTyZqbe"
                        }
                    },
                    "flddqAb59nlcP": {
                        "id": "flddqAb59nlcP",
                        "name": "智能公式",
                        "type": 16,
                        "property": {
                            "expression": "{fldAxNNXb0W9n}",
                            "datasheetId": "dstRUJJUjbiX0YE8MZ"
                        }
                    },
                    "fldfumjqhCzHD": {
                        "id": "fldfumjqhCzHD",
                        "name": "货币类型",
                        "type": 17,
                        "property": {
                            "symbol": "¥",
                            "precision": 2,
                            "symbolAlign": 0
                        }
                    },
                    "fldjRiWUGW5nh": {
                        "id": "fldjRiWUGW5nh",
                        "name": "创建人类型",
                        "type": 23,
                        "property": {
                            "uuids": [
                                "353619ba95d5403caf003879b3ae20e3"
                            ],
                            "datasheetId": "dstRUJJUjbiX0YE8MZ",
                            "subscription": false
                        }
                    },
                    "fldsTUeKuBZdl": {
                        "id": "fldsTUeKuBZdl",
                        "name": "引用成绩表中数学成绩",
                        "type": 14,
                        "property": {
                            "formatting": {
                                "symbol": "$",
                                "precision": 0,
                                "formatType": 2
                            },
                            "datasheetId": "dstRUJJUjbiX0YE8MZ",
                            "relatedLinkFieldId": "fld1jYCl4UVNx",
                            "lookUpTargetFieldId": "fldYWiqjBW3VL"
                        }
                    },
                    "fldvzcSClJ6bi": {
                        "id": "fldvzcSClJ6bi",
                        "name": "创建时间类型",
                        "type": 21,
                        "property": {
                            "dateFormat": 0,
                            "timeFormat": 0,
                            "datasheetId": "dstRUJJUjbiX0YE8MZ",
                            "includeTime": true
                        }
                    }
                }
            },
            "recordMap": {
                "recsoU03ufryC": {
                    "id": "recsoU03ufryC",
                    "data": {
                        "fld1jYCl4UVNx": [
                            "recKRKW9ZbMiL"
                        ],
                        "fld5q3e4zu3e4": [
                            {
                                "text": "100/202",
                                "type": 1
                            }
                        ],
                        "fld93XJmtvEvG": [
                            "recLKnKbY2X2G"
                        ],
                        "fldAxNNXb0W9n": [
                            "1696426247320952834"
                        ],
                        "fldWIs42zvH0R": 40,
                        "fldaZiYMYLZuD": [
                            "recKRKW9ZbMiL"
                        ],
                        "fldfumjqhCzHD": 3
                    },
                    "createdAt": 1695711739000,
                    "updatedAt": 1695728136000,
                    "revisionHistory": [
                        0,
                        3,
                        7,
                        13,
                        17,
                        24,
                        29,
                        37,
                        42,
                        43,
                        44,
                        47,
                        52
                    ],
                    "recordMeta": {
                        "createdAt": 1695711739149,
                        "createdBy": "353619ba95d5403caf003879b3ae20e3",
                        "fieldUpdatedMap": {
                            "fld1jYCl4UVNx": {
                                "at": 1695713814090,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fld5q3e4zu3e4": {
                                "at": 1695723763329,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fld93XJmtvEvG": {
                                "at": 1695712700849,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldAxNNXb0W9n": {
                                "at": 1695712864270,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldWIs42zvH0R": {
                                "at": 1695713062326,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldaU54R9oOT4": {
                                "autoNumber": 1
                            },
                            "fldaZiYMYLZuD": {
                                "at": 1695728136429,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            },
                            "fldfumjqhCzHD": {
                                "at": 1695713121748,
                                "by": "353619ba95d5403caf003879b3ae20e3"
                            }
                        }
                    },
                    "commentCount": 0
                }
            },
            "datasheetId": "dstRUJJUjbiX0YE8MZ"
        }
    }
}
"#;