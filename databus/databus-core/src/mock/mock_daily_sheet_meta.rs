#[cfg(test)]
pub const NAME_LIST_SHEET_META: &str  = r#"
{
    "views": [
        {
            "id": "viw2eDPsHvn4K",
            "name": " 全部信息",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": false,
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fld2QSzuK3LS6"
                    }
                ],
                "keepSort": true
            },
            "filterInfo": {
                "conditions": [
                    {
                        "value": null,
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdt3xKDDuDd9O"
                    },
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtBSBlyLdnxi"
                    },
                    {
                        "value": null,
                        "fieldId": "fldok6Y3X2uTC",
                        "operator": "is",
                        "fieldType": 16,
                        "conditionId": "cdtG7a0EfCQ98"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viw7V7O6iS5Tm",
            "name": "[OD]在职",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "sortInfo": {
                "rules": [
                    {
                        "desc": false,
                        "fieldId": "fldrmuo3feVp3"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldXl1LwYDQrx"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdtmVFKe8uwWl"
                    },
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isNotEmpty",
                        "fieldType": 13,
                        "conditionId": "cdt342sqrfV9q"
                    },
                    {
                        "value": [
                            "optqDLV19Crwd"
                        ],
                        "fieldId": "fldSurxao1u2C",
                        "operator": "is",
                        "fieldType": 3,
                        "conditionId": "cdt8eCiuSLKOm"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwHCLKGUUryt",
            "name": "[OD]在职统计",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "sortInfo": {
                "rules": [
                    {
                        "desc": false,
                        "fieldId": "fld9dtBA8JMz1"
                    }
                ],
                "keepSort": true
            },
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdtmVFKe8uwWl"
                    },
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isNotEmpty",
                        "fieldType": 13,
                        "conditionId": "cdt342sqrfV9q"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwsadeXx6hf2",
            "name": "[OD]待入职视图",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldy8pYZSesf5"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fldrmuo3feVp3",
                        "operator": "isNotEmpty",
                        "fieldType": 19,
                        "conditionId": "cdtzwfFLyJ2ut"
                    },
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdt0W0mYsIdM7"
                    },
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isEmpty",
                        "fieldType": 13,
                        "conditionId": "cdt6RNVvtGXfl"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwLJZe1dQ1U2",
            "name": "base地视图",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fld9dtBA8JMz1"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldy8pYZSesf5"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isNotEmpty",
                        "fieldType": 13,
                        "conditionId": "cdtmVFKe8uwWl"
                    },
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdt342sqrfV9q"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwnbYTDjeaIX",
            "name": "[OD]招聘",
            "rows": [],
            "type": 1,
            "columns": [],
            "sortInfo": {
                "rules": [
                    {
                        "desc": false,
                        "fieldId": "fldrmuo3feVp3"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldrQiSm9FG11"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isNotEmpty",
                        "fieldType": 13,
                        "conditionId": "cdtmVFKe8uwWl"
                    },
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdt342sqrfV9q"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwNUQeEd8Fkr",
            "name": "[OD]离职",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fld2QSzuK3LS6"
                    }
                ],
                "keepSort": true
            },
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isNotEmpty",
                        "fieldType": 5,
                        "conditionId": "cdt85b5Ww49zP"
                    },
                    {
                        "value": true,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtopnlcupv4M"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viw2LTtXrABPK",
            "name": "[OD]我的福利费额度",
            "rows": [],
            "type": 1,
            "columns": [],
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldXl1LwYDQrx"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtIXGVr9JyfK"
                    },
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isNotEmpty",
                        "fieldType": 13,
                        "conditionId": "cdtmVFKe8uwWl"
                    },
                    {
                        "value": [
                            "optmalzhd41ZV"
                        ],
                        "fieldId": "fldy8pYZSesf5",
                        "operator": "isNot",
                        "fieldType": 3,
                        "conditionId": "cdtxBxv5jbcca"
                    },
                    {
                        "value": [
                            "Self"
                        ],
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "contains",
                        "fieldType": 13,
                        "conditionId": "cdtyTsLN3BEtt"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwP5n1mBwXHA",
            "name": "[OD]福利费登记",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "filterInfo": {
                "conditions": [
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtIXGVr9JyfK"
                    },
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isNotEmpty",
                        "fieldType": 13,
                        "conditionId": "cdtmVFKe8uwWl"
                    },
                    {
                        "value": [
                            "optmalzhd41ZV"
                        ],
                        "fieldId": "fldy8pYZSesf5",
                        "operator": "isNot",
                        "fieldType": 3,
                        "conditionId": "cdtxBxv5jbcca"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 2,
            "frozenColumnCount": 1
        },
        {
            "id": "viw9SGJkRDkUn",
            "name": "巫师训练营",
            "rows": [],
            "type": 1,
            "columns": [],
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldXl1LwYDQrx"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtIXGVr9JyfK"
                    },
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isNotEmpty",
                        "fieldType": 13,
                        "conditionId": "cdt68FHx2DOI3"
                    },
                    {
                        "value": [
                            "optqDLV19Crwd"
                        ],
                        "fieldId": "fldSurxao1u2C",
                        "operator": "is",
                        "fieldType": 3,
                        "conditionId": "cdt77EzZW23ZR"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwV5Qwlzm1MK",
            "name": "勾选视图",
            "rows": [],
            "type": 1,
            "columns": [],
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldy8pYZSesf5"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtKxelHuDyj8"
                    },
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdtmip9Uphuo1"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwSQbBmTBJtL",
            "name": "深圳",
            "rows": [],
            "type": 1,
            "columns": [],
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldXl1LwYDQrx"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "optIHmSU8qs5V"
                        ],
                        "fieldId": "fldy8pYZSesf5",
                        "operator": "is",
                        "fieldType": 3,
                        "conditionId": "cdtMmCryFEZu0"
                    },
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtWiqesUOtM9"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwLFN7Lv9KB5",
            "name": "珠海",
            "rows": [],
            "type": 1,
            "columns": [],
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldrRaoIS0ayX"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "opttaxblJGxOs"
                        ],
                        "fieldId": "fldy8pYZSesf5",
                        "operator": "is",
                        "fieldType": 3,
                        "conditionId": "cdtnrYjsNIavI"
                    },
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtQYLG4h9rb5"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwS2igsOnqCw",
            "name": "产品部",
            "rows": [],
            "type": 1,
            "columns": [],
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldXl1LwYDQrx"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtsi4ruscfzu"
                    },
                    {
                        "value": [
                            "optdPrEJAEGaL"
                        ],
                        "fieldId": "fldXl1LwYDQrx",
                        "operator": "is",
                        "fieldType": 3,
                        "conditionId": "cdtyBstC8L5Ys"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwAsibHNVhKX",
            "name": "运营组",
            "rows": [],
            "type": 1,
            "columns": [],
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldXl1LwYDQrx"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "optxCQFgqucmh"
                        ],
                        "fieldId": "fldXl1LwYDQrx",
                        "operator": "is",
                        "fieldType": 3,
                        "conditionId": "cdtr7OZ69KBel"
                    },
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtEbG4EK6j3s"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwZ0uzYicZzj",
            "name": "【勿动】机器人专用",
            "rows": [],
            "type": 1,
            "columns": [],
            "filterInfo": {
                "conditions": [
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtuqtwazw4Ls"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwUGicDGxsPs",
            "name": "All",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "sortInfo": {
                "rules": [
                    {
                        "desc": false,
                        "fieldId": "fldm9iVuL9hWd"
                    }
                ],
                "keepSort": false
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwUMOQOjuzoK",
            "name": "江门",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "optmalzhd41ZV"
                        ],
                        "fieldId": "fldy8pYZSesf5",
                        "operator": "is",
                        "fieldType": 3,
                        "conditionId": "cdtdl654sKhbl"
                    },
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdtUqbcigrTPI"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwICjKJD6wEx",
            "name": "鸭鸭币",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fld2QSzuK3LS6"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldy8pYZSesf5"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": null,
                        "fieldId": "fldrmuo3feVp3",
                        "operator": "is",
                        "fieldType": 19,
                        "conditionId": "cdtuYIwtZHldR"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwlx1iHk9KTf",
            "name": "HB",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "sortInfo": {
                "rules": [
                    {
                        "desc": false,
                        "fieldId": "fldok6Y3X2uTC"
                    }
                ],
                "keepSort": true
            },
            "filterInfo": {
                "conditions": [
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtOOkHi5Hfd5"
                    },
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdtXG1WYQR20c"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwKk5xtKoMeW",
            "name": "周年",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "sortInfo": {
                "rules": [
                    {
                        "desc": false,
                        "fieldId": "fld9dtBA8JMz1"
                    }
                ],
                "keepSort": true
            },
            "filterInfo": {
                "conditions": [
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtOOkHi5Hfd5"
                    },
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdtXG1WYQR20c"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwcNbPtD00YR",
            "name": "入职天数",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": false,
            "sortInfo": {
                "rules": [
                    {
                        "desc": false,
                        "fieldId": "fldx0PKuNeY5i"
                    }
                ],
                "keepSort": true
            },
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fld2di1GhLLBD",
                        "operator": "isNotEmpty",
                        "fieldType": 13,
                        "conditionId": "cdtXG1WYQR20c"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwOvq4ukZmH4",
            "name": "入职率统计",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldyMteIs873q"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "ThisYear"
                        ],
                        "fieldId": "fld9dtBA8JMz1",
                        "operator": "is",
                        "fieldType": 5,
                        "conditionId": "cdth1Z786B0BZ"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwiKSO9oNw1W",
            "name": "斯蒂亚",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fld2QSzuK3LS6"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "optmalzhd41ZV"
                        ],
                        "fieldId": "fldy8pYZSesf5",
                        "operator": "is",
                        "fieldType": 3,
                        "conditionId": "cdt0jy5IaZeXk"
                    },
                    {
                        "value": [
                            "李洁玲"
                        ],
                        "fieldId": "fldrmuo3feVp3",
                        "operator": "is",
                        "fieldType": 19,
                        "conditionId": "cdtUkLGO0Iwri"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwsM4u3KgMce",
            "name": "五险一金缴纳核对",
            "rows": [],
            "type": 1,
            "columns": [],
            "autoSave": true,
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldy8pYZSesf5"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fld2QSzuK3LS6",
                        "operator": "isEmpty",
                        "fieldType": 5,
                        "conditionId": "cdtM3KiKWkJxm"
                    },
                    {
                        "value": false,
                        "fieldId": "fldyMteIs873q",
                        "operator": "is",
                        "fieldType": 11,
                        "conditionId": "cdtnIQgfCAum4"
                    },
                    {
                        "value": [
                            "optp9RlqdQ8Hl"
                        ],
                        "fieldId": "fldSurxao1u2C",
                        "operator": "isNot",
                        "fieldType": 3,
                        "conditionId": "cdtGS8rRsvLGe"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        }
    ],
    "fieldMap": {
        "fld062H5aclMM": {
            "id": "fld062H5aclMM",
            "name": "发送入职欢迎语",
            "type": 11,
            "property": {
                "icon": "white_check_mark"
            }
        },
        "fld0dqGSgUFec": {
            "id": "fld0dqGSgUFec",
            "name": "【汇总】异常情况的提示页",
            "type": 7,
            "property": {
                "brotherFieldId": "fld1a9cTBEVWa",
                "foreignDatasheetId": "dstEBM0KgMD331XV0U"
            }
        },
        "fld1IFISCFgFw": {
            "id": "fld1IFISCFgFw",
            "name": "行政负责",
            "type": 13,
            "property": {
                "isMulti": false,
                "unitIds": [
                    "1473123573069488129",
                    "1465886601457737730",
                    "1348891603735490562",
                    "eef91dfe57c342f586dd59eead8412c1",
                    "1513711453331079169"
                ],
                "shouldSendMsg": false
            }
        },
        "fld1U1gfUwHhd": {
            "id": "fld1U1gfUwHhd",
            "name": "借用设备管理表",
            "type": 7,
            "property": {
                "brotherFieldId": "fldW7lLyauSlb",
                "foreignDatasheetId": "dst1CVHHtnN2DrSf0S"
            }
        },
        "fld22W9ljKb5f": {
            "id": "fld22W9ljKb5f",
            "name": "入职时间 2",
            "type": 1
        },
        "fld2QSzuK3LS6": {
            "id": "fld2QSzuK3LS6",
            "name": "离职时间",
            "type": 5,
            "property": {
                "autoFill": false,
                "dateFormat": 0,
                "timeFormat": 1,
                "includeTime": false
            }
        },
        "fld2di1GhLLBD": {
            "id": "fld2di1GhLLBD",
            "name": "成员",
            "type": 13,
            "property": {
                "isMulti": false,
                "unitIds": [
                    "1263684016908349442",
                    "1252071950792060929",
                    "1236159940691759106",
                    "1279970638760640513",
                    "1236159981951127554",
                    "1236159919934148610",
                    "1245317610827067394",
                    "1280057449658105857",
                    "1236159955090804739",
                    "1247423607436918786",
                    "1248128772280324097",
                    "1236159961759748098",
                    "1236159965190688770",
                    "1236159978704736258",
                    "1236159975479316482",
                    "1256090359603449857",
                    "1236159934022815746",
                    "1236159923541250050",
                    "1236159947884990467",
                    "1236155491608956930",
                    "1267292467797618690",
                    "1247860899280756738",
                    "1272717987410919426",
                    "1248563803955593218",
                    "1236159968508383235",
                    "1236159971893186562",
                    "1236181428773851139",
                    "1301010949063540737",
                    "1318036931975581697",
                    "1324558167132975105",
                    "1315936463289638914",
                    "1326005292914622466",
                    "1330758041065287682",
                    "1330758041065287681",
                    "1331429553208356866",
                    "1331429553208356867",
                    "1331800231031799809",
                    "1346750792415907841",
                    "1332194584943042561",
                    "1348525696215621634",
                    "1348522362969866242",
                    "1348891603735490562",
                    "1351786599057256450",
                    "1363690341559554049",
                    "1362246978201309185",
                    "1366236508953956353",
                    "1364460426635444226",
                    "1368760460757925890",
                    "1368761134405091329",
                    "1369852753562374145",
                    "1369838990566166529",
                    "1373819346295971841",
                    "1373821934982651905",
                    "1379285570496053249",
                    "1380191073824935938",
                    "1372022802228420610",
                    "1383977346358362114",
                    "1366587136543793154",
                    "1390153833960116225",
                    "1386522136551415810",
                    "1383972817844764673",
                    "1372387976995119106",
                    "1390154922704965633",
                    "1380500004381720577",
                    "1280696426361307138",
                    "1290901978183032833",
                    "1392303816675229698",
                    "1394182869368479745",
                    "1394853948814438402",
                    "1396673950263361537",
                    "1397098613377572865",
                    "1397369375711797250",
                    "1399570202138824705",
                    "1401786588671705089",
                    "1401811436676513794",
                    "1406817490964582401",
                    "1410432016485756929",
                    "1404987732391280642",
                    "1416952184298610689",
                    "1416956861987758081",
                    "1416954212206845953",
                    "1415553251672166401",
                    "1415495288819388417",
                    "1419500240272486401",
                    "1419471008104312834",
                    "1419471008104312833",
                    "1419513420465958914",
                    "1406823153103015937",
                    "1414490062198992898",
                    "1424599223142375425",
                    "1424573596616024066",
                    "1425701941690863618",
                    "1424575895723761666",
                    "1429717221578563585",
                    "1429981234974728194",
                    "1432914956977647618",
                    "1437258325627023362",
                    "1437258581364752385",
                    "1437296496513449985",
                    "1435130648898871297",
                    "1438692797070934018",
                    "1438692797070934019",
                    "1440520652471046145",
                    "1441346693648924674",
                    "1442322538503458817",
                    "1446319193942900738",
                    "1446319296346832898",
                    "1452471307472281601",
                    "1455016686812925953",
                    "1455014195291492353",
                    "1455016363419504642",
                    "1455014553795432450",
                    "1458266708664729601",
                    "1460079077529788417",
                    "1462627050492919809",
                    "1462626442666967042",
                    "1463341117127708673",
                    "1465881570658525185",
                    "1465886601457737730",
                    "1470229918950928386",
                    "1470228601345871874",
                    "1473123573069488129",
                    "1475651419560148994",
                    "1476082516016082946",
                    "1478202310904180737",
                    "1478194584467730433",
                    "1478202310904180739",
                    "1478202310904180738",
                    "1465513160016584706",
                    "1461255571421696002",
                    "1465513160016584712",
                    "1471762125258821633",
                    "1476441967847784449",
                    "1465513160016584708",
                    "1465513160016584709",
                    "1465513160016584711",
                    "1465513160016584707",
                    "1483259072447569921",
                    "1490884740456435713",
                    "1491242419907788805",
                    "1491242419907788804",
                    "1491242419907788803",
                    "1491315303300792322",
                    "1493414215430443009",
                    "1491242419907788802",
                    "1496668821385707521",
                    "1498110505840873475",
                    "1498110505840873474",
                    "1498110505840873473",
                    "1498118291198906369",
                    "1501025101229117442",
                    "1501025047760130050",
                    "1465612319981629442",
                    "1503558817115738114",
                    "1504282050267762689",
                    "1506103261919571969",
                    "1508630963513380865",
                    "1506900054450237442",
                    "1508655223883616258",
                    "1513711453331079169",
                    "1513711453331079170",
                    "1514525957665329153",
                    "1515973857632927746",
                    "1518790349065871361",
                    "1522050489295892481",
                    "1524937874233024514",
                    "1528922630518657026",
                    "1528922630518657027",
                    "1528922385999122433",
                    "1528922630518657025",
                    "1529285159690866690",
                    "1531101953302110209",
                    "d9d62c578aae4a42be684f9e37a43dc2",
                    "1535091394758152194",
                    "1536180543279198210",
                    "1539072834341613570",
                    "1541970819050057730",
                    "1542696089951338497",
                    "1542696089951338498",
                    "1543804180065140738",
                    "1544142307192782849",
                    "1544505805460860929",
                    "1546683534196998145",
                    "1546686620751757314",
                    "1546321142954500097",
                    "1547062557893697538",
                    "1547416989492887554",
                    "1549942905099710466",
                    "1549232675800350722",
                    "50855429a837492284dbb285d77896ca",
                    "1554012878767112193",
                    "50cbab6cb65e49518831145ea7379ca7",
                    "1559727097413672961",
                    "1562268144583675906",
                    "1562258693298425858",
                    "1574586528331800578",
                    "eef91dfe57c342f586dd59eead8412c1",
                    "1630759817378496513",
                    "1633292550214717442",
                    "1636176651195625473",
                    "1670614323794595841",
                    "1670974667829067778",
                    "1675700060695617537",
                    "1678658030024867842",
                    "1680764105450643457",
                    "1683304294001729538",
                    "1688384533843058693",
                    "1688384533843058690",
                    "1688384533843058691",
                    "1688384533843058692",
                    "1688384533843058689",
                    "1689438502983999489",
                    "1695976862359830529",
                    "1697438699532369921",
                    "1699967259781087233",
                    "1699975235078176769"
                ],
                "shouldSendMsg": false
            }
        },
        "fld30jb2Zo8Ax": {
            "id": "fld30jb2Zo8Ax",
            "name": "课程跟进表",
            "type": 1
        },
        "fld3Je6j6yG4r": {
            "id": "fld3Je6j6yG4r",
            "name": "办公设备",
            "type": 4,
            "property": {
                "options": [
                    {
                        "id": "optOor5mLUpF6",
                        "name": "电脑",
                        "color": 0
                    },
                    {
                        "id": "optPKAZrqXoQ9",
                        "name": "显示器",
                        "color": 1
                    },
                    {
                        "id": "opt9Egl55srLe",
                        "name": "拓展坞",
                        "color": 2
                    },
                    {
                        "id": "optTQS5FYKwcZ",
                        "name": "HDMI数据线",
                        "color": 3
                    }
                ]
            }
        },
        "fld3PG3xCacY1": {
            "id": "fld3PG3xCacY1",
            "name": "210604答疑会内容的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fldMT50kog9M6",
                "foreignDatasheetId": "dsthFksg7gi6lwTSAd"
            }
        },
        "fld3p1q045Ujh": {
            "id": "fld3p1q045Ujh",
            "name": "部门信息",
            "type": 1
        },
        "fld4Dby0MIcRt": {
            "id": "fld4Dby0MIcRt",
            "name": "体验问题记录 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldO19AbZveIV",
                "foreignDatasheetId": "dstge3s0Dq60joX3dA"
            }
        },
        "fld4OX1x2n53y": {
            "id": "fld4OX1x2n53y",
            "name": "团建费额度 3",
            "type": 1
        },
        "fld4Q1zyy82L1": {
            "id": "fld4Q1zyy82L1",
            "name": "2 资材申领汇总",
            "type": 7,
            "property": {
                "brotherFieldId": "fldW3QybeTMmI",
                "foreignDatasheetId": "dstva4Xn3Jg7tvDAFd"
            }
        },
        "fld5BQVwMc1Rk": {
            "id": "fld5BQVwMc1Rk",
            "name": "关键绩效 Key Results的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fldAkxNFoxIIu",
                "foreignDatasheetId": "dstVnNR9p4bZlSfqFr"
            }
        },
        "fld5mdDKY7EnU": {
            "id": "fld5mdDKY7EnU",
            "name": "生日礼品库",
            "type": 7,
            "property": {
                "brotherFieldId": "fld2bPkfebVIH",
                "foreignDatasheetId": "dstQed6tV5ipCjgy0G"
            }
        },
        "fld5nd9lTs9vM": {
            "id": "fld5nd9lTs9vM",
            "name": "新建维格表",
            "type": 7,
            "property": {
                "brotherFieldId": "fldfxg5C2GrD1",
                "foreignDatasheetId": "dstzcCJcu3JVEkzPQ4"
            }
        },
        "fld6SJNixqMxY": {
            "id": "fld6SJNixqMxY",
            "name": "客户名单",
            "type": 7,
            "property": {
                "brotherFieldId": "fld4kOOVKyuwI",
                "foreignDatasheetId": "dstcLkfD6fNhSsSxkQ"
            }
        },
        "fld6dsOhFbuI8": {
            "id": "fld6dsOhFbuI8",
            "name": "在职证明的副本 2",
            "type": 1
        },
        "fld7bag5zkriQ": {
            "id": "fld7bag5zkriQ",
            "name": "考评状态",
            "type": 11,
            "property": {
                "icon": "white_check_mark"
            }
        },
        "fld8a5yFHc88A": {
            "id": "fld8a5yFHc88A",
            "name": "星球管理局（综合档案） 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldA3lc6riogl",
                "foreignDatasheetId": "dst05rU7b1S4NApAmp"
            }
        },
        "fld8ckTJ6CNoB": {
            "id": "fld8ckTJ6CNoB",
            "name": "每天工作记录-Grid view 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldZW6uAgNAKJ",
                "foreignDatasheetId": "dstEFSii62bHFFDWbT"
            }
        },
        "fld9Gbq9NT41v": {
            "id": "fld9Gbq9NT41v",
            "name": "招聘需求汇总表（迭代版）",
            "type": 7,
            "property": {
                "brotherFieldId": "fldYc0pmjI0JZ",
                "foreignDatasheetId": "dstmzJLTmMHlZm5kVj"
            }
        },
        "fld9L65891gHK": {
            "id": "fld9L65891gHK",
            "name": "招聘任务",
            "type": 7,
            "property": {
                "brotherFieldId": "fldig4zIIoTEQ",
                "foreignDatasheetId": "dstmzJLTmMHlZm5kVj"
            }
        },
        "fld9YGBc1ZnDb": {
            "id": "fld9YGBc1ZnDb",
            "name": "员工工牌记录表",
            "type": 1
        },
        "fld9dtBA8JMz1": {
            "id": "fld9dtBA8JMz1",
            "desc": "",
            "name": "入职时间",
            "type": 5,
            "property": {
                "autoFill": false,
                "dateFormat": 0,
                "timeFormat": 1,
                "includeTime": false
            }
        },
        "fld9eQ2avduRF": {
            "id": "fld9eQ2avduRF",
            "name": "元宵谜题",
            "type": 7,
            "property": {
                "brotherFieldId": "fldfjlJV1RB56",
                "foreignDatasheetId": "dstbfBRuquSHEmZ2v3"
            }
        },
        "fld9jPWAHJXV8": {
            "id": "fld9jPWAHJXV8",
            "name": "🧐设计走查表",
            "type": 7,
            "property": {
                "brotherFieldId": "fldt6EjtkurJ5",
                "foreignDatasheetId": "dstK57xFcHCc7AuXp7"
            }
        },
        "fld9oPnS2mcYr": {
            "id": "fld9oPnS2mcYr",
            "name": "个人用空间站权益升级需求",
            "type": 7,
            "property": {
                "brotherFieldId": "fldCra9n4vUkC",
                "foreignDatasheetId": "dst2z8UL7R8coeZmLf"
            }
        },
        "fld9zdURxemPo": {
            "id": "fld9zdURxemPo",
            "name": "参会成员",
            "type": 7,
            "property": {
                "brotherFieldId": "fldLunpy2AoGY",
                "foreignDatasheetId": "dstkH2EehvWl1Mug9Y"
            }
        },
        "fldAUJDr90xPZ": {
            "id": "fldAUJDr90xPZ",
            "name": "CMS任务管理",
            "type": 7,
            "property": {
                "brotherFieldId": "fldCkVxk31csq",
                "foreignDatasheetId": "dst9T44hgdeEmmRuUE"
            }
        },
        "fldB52dNEOdEA": {
            "id": "fldB52dNEOdEA",
            "name": "内部礼盒信息收集表",
            "type": 7,
            "property": {
                "brotherFieldId": "fldOrcYnDbngk",
                "foreignDatasheetId": "dsty3M5DHXfJgGyUAF"
            }
        },
        "fldBstcGjFZe8": {
            "id": "fldBstcGjFZe8",
            "name": "深圳网络问题收集统计",
            "type": 7,
            "property": {
                "brotherFieldId": "fldXRhdineilQ",
                "foreignDatasheetId": "dst6LHW1d149G4GUF4"
            }
        },
        "fldCOyJ7huM8S": {
            "id": "fldCOyJ7huM8S",
            "name": "任务分解（airtable归档）",
            "type": 7,
            "property": {
                "brotherFieldId": "fldsGCHQgJmXo",
                "foreignDatasheetId": "dst7pJUp3Se1NqWAwd"
            }
        },
        "fldCl0mIincWW": {
            "id": "fldCl0mIincWW",
            "name": "招聘负责",
            "type": 13,
            "property": {
                "isMulti": false,
                "unitIds": [
                    "1348891603735490562",
                    "1473123573069488129",
                    "1513711453331079169",
                    "eef91dfe57c342f586dd59eead8412c1"
                ],
                "shouldSendMsg": false
            }
        },
        "fldDYWOepmaF7": {
            "id": "fldDYWOepmaF7",
            "name": "资产明细",
            "type": 7,
            "property": {
                "brotherFieldId": "fldpCs9W7b6XN",
                "foreignDatasheetId": "dstPBGl36uW9UTh3jZ"
            }
        },
        "fldDloxIUlxF7": {
            "id": "fldDloxIUlxF7",
            "name": "服务端专用测试用例",
            "type": 7,
            "property": {
                "brotherFieldId": "fldcL6VbcDyuQ",
                "foreignDatasheetId": "dstgq222auuCKhVMzf"
            }
        },
        "fldDrcAUWqGND": {
            "id": "fldDrcAUWqGND",
            "name": "内部礼盒信息收集表的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fldOrcYnDbngk",
                "foreignDatasheetId": "dstNmFcW2F8BxFG3Uc"
            }
        },
        "fldE7hYw4ZgGV": {
            "id": "fldE7hYw4ZgGV",
            "name": "入驻星球流程-管理局 2",
            "type": 1
        },
        "fldEDplF8RiCU": {
            "id": "fldEDplF8RiCU",
            "name": "候选人列表（修葺中）",
            "type": 1
        },
        "fldEVJkEBiQhY": {
            "id": "fldEVJkEBiQhY",
            "name": "离职员工信息",
            "type": 7,
            "property": {
                "brotherFieldId": "fldHDL4bBwaxm",
                "foreignDatasheetId": "dstKE2fWjhT3kMnaeM"
            }
        },
        "fldEa6BgEzYPC": {
            "id": "fldEa6BgEzYPC",
            "name": "创建日期",
            "type": 21,
            "property": {
                "dateFormat": 0,
                "timeFormat": 1,
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "includeTime": false
            }
        },
        "fldFqvJLshJmL": {
            "id": "fldFqvJLshJmL",
            "name": "维格派特别奖",
            "type": 7,
            "property": {
                "brotherFieldId": "fldmorcYt3URk",
                "foreignDatasheetId": "dstecJYQgHJa8WHDbz"
            }
        },
        "fldHDz9c81hai": {
            "id": "fldHDz9c81hai",
            "name": "入驻星球流程-管理局",
            "type": 7,
            "property": {
                "brotherFieldId": "fldDXjwlP7KTJ",
                "foreignDatasheetId": "dst05rU7b1S4NApAmp"
            }
        },
        "fldHSP95pUZey": {
            "id": "fldHSP95pUZey",
            "name": "工牌需求",
            "type": 7,
            "property": {
                "limitToView": "viwm5Xvig85eN",
                "brotherFieldId": "fldWnezn0Pucr",
                "foreignDatasheetId": "dstTSf8NgfgHt3iav8"
            }
        },
        "fldHTqWGktGYZ": {
            "id": "fldHTqWGktGYZ",
            "name": "人才盘点基础数据表",
            "type": 1,
            "property": null
        },
        "fldHrQAw7sSYc": {
            "id": "fldHrQAw7sSYc",
            "name": "配置标准",
            "type": 14,
            "property": {
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "relatedLinkFieldId": "fldwTh5HOd1al",
                "lookUpTargetFieldId": "fldwweTeIahCT"
            }
        },
        "fldI2imKMCfi6": {
            "id": "fldI2imKMCfi6",
            "desc": "一般为你的直系上司或直系上司钦点的同学，TA会给你介绍公司的业务，带领你熟悉日常的工作内容，过程中帮助你找到自己在团队中的定位",
            "name": "入职导师",
            "type": 13,
            "property": {
                "isMulti": true,
                "unitIds": [
                    "1419513420465958914",
                    "1247860899280756738",
                    "1236159919934148610",
                    "1272717987410919426",
                    "1236155491608956930",
                    "1348891603735490562",
                    "1236159968508383235",
                    "1236159965190688770",
                    "1397369375711797250",
                    "1301010949063540737",
                    "1236159961759748098",
                    "1396673950263361537",
                    "1248128772280324097",
                    "1236159981951127554",
                    "1236159971893186562",
                    "1315936463289638914",
                    "1419471008104312833",
                    "1366587136543793154",
                    "1236159978704736258",
                    "1435130648898871297",
                    "1236159940691759106",
                    "1455016686812925953",
                    "1410432016485756929",
                    "1236159947884990467",
                    "1386522136551415810",
                    "1256090359603449857",
                    "1446319193942900738",
                    "1390154922704965633",
                    "1414490062198992898",
                    "1267292467797618690",
                    "1348522362969866242",
                    "1404987732391280642",
                    "1324558167132975105",
                    "1437258581364752385",
                    "eef91dfe57c342f586dd59eead8412c1",
                    "1491242419907788803",
                    "1245317610827067394",
                    "1406817490964582401",
                    "1416956861987758081",
                    "1399570202138824705",
                    "1406823153103015937"
                ],
                "shouldSendMsg": true
            }
        },
        "fldI9UL9xlqxc": {
            "id": "fldI9UL9xlqxc",
            "name": "来~吐个槽（提交人）",
            "type": 7,
            "property": {
                "brotherFieldId": "fld4VOk18m5Wo",
                "foreignDatasheetId": "dstxMwTVU1CGwMZkhs"
            }
        },
        "fldIFS9bXPQkw": {
            "id": "fldIFS9bXPQkw",
            "name": "本周PE客服",
            "type": 16,
            "property": {
                "expression": "IF(AND(DATETIME_DIFF({fldfTS1TMleKQ}, today(),'d')> -7, DATETIME_DIFF({fldfTS1TMleKQ}, today(),'d')<=0, {fldfTS1TMleKQ} != blank()), true(), false())",
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldIrOHCyje6M": {
            "id": "fldIrOHCyje6M",
            "name": "入职周年",
            "type": 16,
            "property": {
                "expression": "TONOW({fld9dtBA8JMz1}，“y”)",
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "callNowValue": "2022-08-24T03:33:04.910Z"
            }
        },
        "fldJTjeWlKLBq": {
            "id": "fldJTjeWlKLBq",
            "name": "入职天数",
            "type": 16,
            "property": {
                "expression": "DATETIME_DIFF(DATESTR({fld2QSzuK3LS6}),DATESTR({fld9dtBA8JMz1}),\"d\")+1",
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldKDgg6XpMLB": {
            "id": "fldKDgg6XpMLB",
            "name": "转正主管评价",
            "type": 7,
            "property": {
                "brotherFieldId": "fldUnbgKBxCKo",
                "foreignDatasheetId": "dst0v6iiipQvXpzTrH"
            }
        },
        "fldKeRkCywgg5": {
            "id": "fldKeRkCywgg5",
            "name": "候选人",
            "type": 1,
            "property": null
        },
        "fldKj5zzbPvNj": {
            "id": "fldKj5zzbPvNj",
            "name": "礼包统计",
            "type": 7,
            "property": {
                "brotherFieldId": "fldcz3Q6dnm60",
                "foreignDatasheetId": "dstbyQTuTXCLqqMRhu"
            }
        },
        "fldKrZHYe9KiC": {
            "id": "fldKrZHYe9KiC",
            "name": "需求变更日志 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldFJIGwlMjTx",
                "foreignDatasheetId": "dste5H4GEeCAx18a1M"
            }
        },
        "fldKzVewMNWBv": {
            "id": "fldKzVewMNWBv",
            "name": "修改人 2",
            "type": 24,
            "property": {
                "uuids": [
                    "40e05e0e8c144fc1891f305b0e286678",
                    "e9da9e818ef841acafae4a09b33fe510",
                    "56732bec236c4af3a86e1f8d0f4eafea",
                    "d9d62c578aae4a42be684f9e37a43dc2",
                    "c7186ca7cffa4ee8a470290d8f93487f",
                    "9769074644aa41188506baeea86f2090",
                    "50855429a837492284dbb285d77896ca",
                    "23a5f52cd1ee4e6abdc8a77e9b73cdec",
                    "ed624281c5a4496ab6e8647bc93778c5",
                    "0b9d9562b0d245dd8294b6838953fb05",
                    "d07c75be237c46d2aaddfae2b2ff826c",
                    "50cbab6cb65e49518831145ea7379ca7",
                    "d8c2112dea4f487390984953253e8198",
                    "b7aac62d932749f1af1f9cba1efe92c2",
                    "16b01b70f15948929b6c6a6ec9207da6",
                    "adb90c2ee1f34caeb6b75d5da578961d",
                    "9166bea35d79456994b99956dbfabcb9",
                    "c04f56663d0c48d5aea1f7952d2c2d7b",
                    "1b56b12cd69043a9ac4175bd9b9a1c65",
                    "7abcf0d82a7443c5aaac355d6b4cdd2f",
                    "aa3e6af7041c4907ba03889acc0b0cd1",
                    "51092e31f9c145b59c5ad48af1791ac6",
                    "938fd14da9f243f785e57ad961f35bc6",
                    "33004fa41e6647428b5642a0ced4258c",
                    "a114e7dac07f41bca2ac02c121a9bae2",
                    "d732c0da5479443ea89043d8d4e5877d",
                    "23a5f52cd1ee4e6abdc8a77e9b73cdeb",
                    "e3501cfc02004ab08b5548d032da654a",
                    "f5f141c56086423bb7919a2060f52fe8",
                    "c420f951cc224993aa3f310376296d86",
                    "5b8686771f1e48a7b94da20eaf280d4d",
                    "4c8699668d504523b086787ce94387c7",
                    "e65d9f6b59654d00a48ad0b53e14f8d5",
                    "eeb620a54e2248c69c25de68e6eb668c",
                    "3e2f7d835958472ab43a623f15dec64f",
                    "c47a715d13944ae99f0927a30ea32d1b",
                    "27fdde1dfb6143019c38455cfe8111c1",
                    "5f935c8c5ffb4475ba91fb8125aa16fd",
                    "e1036b3b5b704b41a01b1a2e84bbd1e7",
                    "42eb6f658f924cea811752bf2d571cee",
                    "869dd39aca9a426db28fb242407df25a",
                    "b20a693c428c4d0cb807d8b3b2c99f18",
                    "6667f5487f4c4b178b1fd4901f1f8f3a",
                    "aef5a6605ea341279c811a724c1f7d26",
                    "d610b4fe86c14b18a521dee9ca70c94a",
                    "62bda04135074b78be76fd4a8cfd01f2",
                    "88ebe5c2489e4eb8bcd55fb454ff32ea",
                    "335acbbbe938436588af6bced6a0599a",
                    "0bce025e57a34d9aa711ed79906950d5",
                    "714e4c52cbe14916bba7441b9f3272cf",
                    "f585a1a6d253449d94e080a58aef3ff8",
                    "3be66402587e4647bde42f4782cb5656",
                    "cbfd06ca491e4042b7c2035a0ee608ae",
                    "92481000ece4453b8210ecdd009b30aa",
                    "4adb71fba75e4468855a13fc930ba9b6",
                    "06bf53cb580b4ad09042e1f0ac769c5d",
                    "b3a6d217ad694717913ed730bb786e1b",
                    "e37d580871cb4e31975c07cb65c94f88",
                    "0e13a79312354ee8a33941deceb815bc",
                    "b41ca32b24cd43398fbdab9395dd03e0",
                    "49189866e85349a2a2f652500cf804ba",
                    "4ae7d0eb0a824496b13a692489190d55",
                    "db745334b28940a0a6d90b503d1ffd3b",
                    "687759764929455d8cba1f8576cb33e5",
                    "c532004636e54c7580aff340ed3a4544",
                    "81129cb05275419fad66de83aec23fa3",
                    "a2cbc7f85b8146d1af426a35962d7287",
                    "2902598ce9c84e09b2d1c1d2252b6ced",
                    "dccfb205937e41e7a43da2e20164b3f8",
                    "b12efa00b20342c5be0a6277dda2fe83",
                    "113f836068e74a71b7c7133ba2c40785",
                    "09d19fd689d14381a6081e370d409329",
                    "ef7b19c9fc004ec38b7457f6b78024c4",
                    "1b67f95ff96441c4afd83ffdde14b2b5",
                    "b5ebf378a5284838b4b153fecc3cf299",
                    "64cd375e55f747c89f185644f29f6628",
                    "7e78825636d94299a8278362d6a5b526",
                    "2b4f27bb617d4c4ea9420f056f9fead5",
                    "d9173074608d4c878204fd94b38c20d5",
                    "abb6b0f8b412486da052e8b36b775243",
                    "5d02b5c8303e499f83dbc6ab5716ab8f",
                    "a3dcc9d0401a45118f6e247e01ec150b",
                    "934e8afb905c4d25983a6a74404ec88d",
                    "4ef4fd57ccd34fa7b626a1614c091441",
                    "07681b9f05434029945c663c406c1caf",
                    "bbcbf7223e0e41db87d454542afed7f4",
                    "c9939674d7d14eda83dd065aa08be009",
                    "232d6afb8a304011803754c066301458",
                    "d0602d9d87414c29a3bffb2fa9e80814",
                    "a83ec20f15c9459893d133c2c369eff6",
                    "d193606f64d342909e0a93696af00dd1",
                    "cd25a5d644d64e5b87cac6fbd6bce7bc",
                    "72e1442435e043899bce153ff166c516",
                    "a98da11c03364ea49da876d4182f66a0",
                    "a5b8b044d6b04b0abb27ef315aecf94e",
                    "b8557184c6b44676a04ff2c691221b91",
                    "f6c562fdb30f4ea196321f8d41113f25",
                    "7d6b87fcd4674ceb8feed06e8a114d93",
                    "d2aa673f873f45439128bd5e52835477",
                    "adc2a9cdd80f44778ac6e32f4e42e8e2",
                    "76a73ad9c85a4e5290b4cfb4a1191d37",
                    "2dc7f9021ee54e6e87d4b05051eeac93",
                    "76111f93d7a441e1bfc621d700e3c1d2",
                    "b957324791a14054986f9c44854464e4",
                    "a0ccc0a1c3be4faeb3c8c324459e57bf",
                    "036d0e2ac1cc4374b844a6458525fef2",
                    "74418ea3b23b4e06b8f30293e1fb3045",
                    "dd95efceb7d14809a83792a7d70b50dd",
                    "bf298308635c42f79a338013bda92b98",
                    "51c8394434044a0bb4d05f6741444caa",
                    "d240a6c915ea41249722bff53b0af2c1",
                    "697362ceb9684cf19e722d84c74dc8a7",
                    "c367d0d6f00347f2a020e7cf6e8ebc01",
                    "888e483c713c4aaeadd2ab22bae903f5",
                    "17e07cf00d6d447fb2225e9db547f8c8",
                    "7c9d449df3fd41bbb02c1de075ce4782",
                    "c31130492ba24f76877615138ae93f93",
                    "09ca3f33c11a4980881fef5082c71b4a",
                    "def28f269bf349cda7258607bbb8f9ee",
                    "ece039d976ef4f25914ee9bbaaaae47a",
                    "bed6e84b6fec4d82889f059c18fa07f4",
                    "e8b2a91f20db4a168b4007c5a92f6e81",
                    "dfb67951515946f3992e41ce911479cb",
                    "a91f0313fcc748baa2424f813db2392b",
                    "1e1bb55e093b4a86babfc67372effa66",
                    "1ef3dbdd698e4557acb15e50d5ef7852",
                    "6117483ce3e341cfa5c8fe6d0e02cd46",
                    "c95a4e599d8b424e842b6731e941ccf9",
                    "edcee07cf0fd4f0e837a296a77f9bd94",
                    "8a766617f21041279e1dd85664b76be6",
                    "489e6f58daa34040a041eb72403f3fc1",
                    "0c4565496bb3439dac52a3ef15944d26",
                    "e0e07b0cf26142d0b2d43ff61cea5493",
                    "bf5c019052a842e7afec2367a94d39e0",
                    "bcf86bc2497a4361939437f257a91df1",
                    "fecf59caf1234267a50dc406454c2da0",
                    "1345c2d50ff44e2a8c7fa8b3e9a57525",
                    "3c64f2254bbb47fc91b1638121560926",
                    "15bfdd1e5aef45d8bca07f681b5d9232",
                    "806784677c814c9db93972c023f7f4d4",
                    "96e24eef131840d795cf2570eb49a3ed",
                    "110639147e82437aba1ccde50f05c3ff",
                    "eef91dfe57c342f586dd59eead8412c1",
                    "1a3ad2705d8840e89295f5c8017c0cdf",
                    "1bee932968574cf6919902273e5ae46c",
                    "a8f27b8ed27f459cb4512158c2052a6f",
                    "5bf58d1f2dc2495093744d49beb34ee2",
                    "411e0a7b9cfc4d2aa637e4f391f11b1d",
                    "f891ba9bd43d47089dca81a2163f8047",
                    "b89a6129d7aa4ce984cf8f34f475d384",
                    "f338683905004610b6006e2a0b8eccc8",
                    "3abacc827b73493d9e01818ffca4ca2c",
                    "71b468470b9e46df9d50c9840bcd331d",
                    "f95a1585ad194809aea2f209f1aee26f",
                    "cf3189d22b234b00ba37b566a74572e0",
                    "a244dc4ee9bc41ce8a9e7a8f9ec61ea6",
                    "01cf8a2164bd4017bf2ea926d1814005",
                    "2b80290611344714b8a3bd39fcb4a098",
                    "ecb9982b2edd4079ab77dde456b7bb58",
                    "955dca1609ce4610a0d4ccceec21b7c8",
                    "00d3451ab21f4648861c2f2558252603",
                    "a5e5b003fc9b40b08b5d9cfa52f9f26d",
                    "38575d399ae74f8bbc2a1d9d51adf357",
                    "8980d25da42947f995451386fc291834",
                    "e42e6e891baa42ccaa1e0f316d7925d3",
                    "254cef9eeef54e8ea85de49391947ca9",
                    "5dee6a81f6fe408f934312a12984e462",
                    "c9863e875fe64331a9e6e6ff61ba407c",
                    "13afd5b930f644b69e79de4f9643887a",
                    "12859838940c483c8341649a14b950e5",
                    "75e1bb975ca64210a4b3944ee87fa280",
                    "176384fe0cfd4b03afe6e574eccf562a",
                    "45d4037c64ac442d88d45c971a9565c3",
                    "15699ee1c08a40298a61e913599074a7"
                ],
                "collectType": 1,
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "fieldIdCollection": [
                    "fldjXbS0TABeG"
                ]
            }
        },
        "fldLRMpm0bSUW": {
            "id": "fldLRMpm0bSUW",
            "name": "待解决清单",
            "type": 7,
            "property": {
                "brotherFieldId": "fldMT50kog9M6",
                "foreignDatasheetId": "dstJb6FfTGa085UNwc"
            }
        },
        "fldLaSSxk1Lgz": {
            "id": "fldLaSSxk1Lgz",
            "name": "多行文本",
            "type": 16,
            "property": {
                "expression": "DATETIME_PARSE(year(today())&'-'&MID(DATETIME_FORMAT({fldV1GmxiFH26}),6,5))",
                "formatting": {
                    "autoFill": false,
                    "dateFormat": 0,
                    "timeFormat": 1,
                    "includeTime": false
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldLbCb2bdz6P": {
            "id": "fldLbCb2bdz6P",
            "name": "离职访谈纪录",
            "type": 1
        },
        "fldLthrA9Ec89": {
            "id": "fldLthrA9Ec89",
            "name": "中文头衔",
            "type": 19,
            "property": {
                "defaultValue": ""
            }
        },
        "fldM4VVJ9bSmG": {
            "id": "fldM4VVJ9bSmG",
            "name": "会议记录-默认视图",
            "type": 7,
            "property": {
                "brotherFieldId": "flda50S1quL68",
                "foreignDatasheetId": "dstlnZyoJ5tPWbbPFC"
            }
        },
        "fldMM6tiehdSj": {
            "id": "fldMM6tiehdSj",
            "name": "需求变更日志",
            "type": 7,
            "property": {
                "brotherFieldId": "fld0AQXAzMAGZ",
                "foreignDatasheetId": "dste5H4GEeCAx18a1M"
            }
        },
        "fldMhdhwLhY4m": {
            "id": "fldMhdhwLhY4m",
            "name": "在职证明的副本",
            "type": 1
        },
        "fldNBGC0X40mY": {
            "id": "fldNBGC0X40mY",
            "name": "CMS任务管理 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldfP775V15tp",
                "foreignDatasheetId": "dst9T44hgdeEmmRuUE"
            }
        },
        "fldNTXW9wpT13": {
            "id": "fldNTXW9wpT13",
            "name": "CMS 任务管理 copy 2",
            "type": 1,
            "property": null
        },
        "fldNpslKG4WN1": {
            "id": "fldNpslKG4WN1",
            "name": "产研站立会主持人",
            "type": 7,
            "property": {
                "brotherFieldId": "flddoKqKYzjQy",
                "foreignDatasheetId": "dst2QDtugyLKKdKWkA"
            }
        },
        "fldNrNnxgjFQL": {
            "id": "fldNrNnxgjFQL",
            "name": "团建费额度 2",
            "type": 1
        },
        "fldOHpImXNlO0": {
            "id": "fldOHpImXNlO0",
            "name": "旅会需求申请管理表",
            "type": 7,
            "property": {
                "brotherFieldId": "fldJuxK1822gv",
                "foreignDatasheetId": "dstsTSooYqrTwPm76Y"
            }
        },
        "fldOJ61tpGQmZ": {
            "id": "fldOJ61tpGQmZ",
            "name": "手机号2",
            "type": 10
        },
        "fldP3MyDg3GF5": {
            "id": "fldP3MyDg3GF5",
            "name": "任务表 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldds9UStXAin",
                "foreignDatasheetId": "dstyLBkU95PUV7sg6o"
            }
        },
        "fldP6XCTZ6TiQ": {
            "id": "fldP6XCTZ6TiQ",
            "name": "新员工入职任务表的副本",
            "type": 1
        },
        "fldQNizblM1MK": {
            "id": "fldQNizblM1MK",
            "name": "2 设备采购入库登记",
            "type": 7,
            "property": {
                "brotherFieldId": "fldQANw8xLEDw",
                "foreignDatasheetId": "dstJRwrrNzdnMU6bkn"
            }
        },
        "fldRGo1NTqiom": {
            "id": "fldRGo1NTqiom",
            "name": "修改时间",
            "type": 22,
            "property": {
                "dateFormat": 0,
                "timeFormat": 1,
                "collectType": 1,
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "includeTime": false,
                "fieldIdCollection": [
                    "fldjXbS0TABeG"
                ]
            }
        },
        "fldRYdI2LfLKm": {
            "id": "fldRYdI2LfLKm",
            "name": "入职办理",
            "type": 13,
            "property": {
                "isMulti": false,
                "unitIds": [
                    "1473123573069488129",
                    "1348891603735490562",
                    "eef91dfe57c342f586dd59eead8412c1",
                    "1513711453331079169"
                ],
                "shouldSendMsg": false
            }
        },
        "fldSaD7HWd0nd": {
            "id": "fldSaD7HWd0nd",
            "name": "花名",
            "type": 19,
            "property": {
                "defaultValue": ""
            }
        },
        "fldSlA71NW5UI": {
            "id": "fldSlA71NW5UI",
            "name": "选手名单",
            "type": 7,
            "property": {
                "brotherFieldId": "fld3WuiHggo0g",
                "foreignDatasheetId": "dstVpYhJXBYgC3rMU9"
            }
        },
        "fldSurxao1u2C": {
            "id": "fldSurxao1u2C",
            "name": "在职类型",
            "type": 3,
            "property": {
                "options": [
                    {
                        "id": "optqDLV19Crwd",
                        "name": "全职",
                        "color": 0
                    },
                    {
                        "id": "optp9RlqdQ8Hl",
                        "name": "实习",
                        "color": 3
                    },
                    {
                        "id": "opt99o6D7p0sT",
                        "name": "兼职",
                        "color": 1
                    },
                    {
                        "id": "opt1UIiYdACZ2",
                        "name": "待入职",
                        "color": 2
                    }
                ]
            }
        },
        "fldTEwRYhAvIE": {
            "id": "fldTEwRYhAvIE",
            "name": "名片头衔",
            "type": 1
        },
        "fldTTDY0vXyXe": {
            "id": "fldTTDY0vXyXe",
            "name": "礼物名单",
            "type": 1
        },
        "fldTgm6k2grf6": {
            "id": "fldTgm6k2grf6",
            "name": "团建费信息登记 3",
            "type": 7,
            "property": {
                "brotherFieldId": "fldMy10Izq797",
                "foreignDatasheetId": "dstZmmaSEhNo8TNEPC"
            }
        },
        "fldUy2EGFbZ39": {
            "id": "fldUy2EGFbZ39",
            "name": "HB推送",
            "type": 16,
            "property": {
                "expression": "IF(MID({fldx0PKuNeY5i}，1，99)=MID({fldVhkqIyHzci}，1，99)，“发”，“”)",
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldV1GmxiFH26": {
            "id": "fldV1GmxiFH26",
            "name": "生日",
            "type": 5,
            "property": {
                "autoFill": false,
                "dateFormat": 0,
                "timeFormat": 1,
                "includeTime": false
            }
        },
        "fldVhNDBdswJp": {
            "id": "fldVhNDBdswJp",
            "name": "渠道沟通",
            "type": 7,
            "property": {
                "brotherFieldId": "fldmsFTTK7mG5",
                "foreignDatasheetId": "dstrqG4J8m9HhoZwzP"
            }
        },
        "fldVhkqIyHzci": {
            "id": "fldVhkqIyHzci",
            "name": "today",
            "type": 16,
            "property": {
                "expression": "TODAY()",
                "formatting": {
                    "autoFill": false,
                    "dateFormat": 4,
                    "timeFormat": 1,
                    "includeTime": false
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "callToDayValue": "2022-08-24T00:00:00.000Z"
            }
        },
        "fldW2jLDvfbzE": {
            "id": "fldW2jLDvfbzE",
            "name": "每周工作内容的副本",
            "type": 1
        },
        "fldWVuuAAFRl6": {
            "id": "fldWVuuAAFRl6",
            "desc": "理论上，是工位同桌。\n在你通读Handbook的过程中，TA能帮你答疑解惑，帮助你更好地理解课程内容，让你更快地融入到团队中；除了工作，生活中的疑惑也可以趁此机会咨询TA哦（如公司的社团活动，北上深珠的美食美景，知无不言言无不尽~）",
            "name": "生活导师",
            "type": 13,
            "property": {
                "isMulti": false,
                "unitIds": [
                    "1419471008104312833",
                    "1372022802228420610",
                    "1348891603735490562",
                    "1301010949063540737",
                    "1248128772280324097",
                    "1429981234974728194",
                    "1416956861987758081",
                    "1236159965190688770",
                    "1397369375711797250",
                    "1236159919934148610",
                    "1410432016485756929",
                    "1435130648898871297",
                    "1348522362969866242",
                    "1247860899280756738",
                    "1236159940691759106",
                    "1455016686812925953",
                    "1236159947884990467",
                    "1256090359603449857",
                    "1455014195291492353",
                    "1236155491608956930",
                    "1475651419560148994",
                    "1432914956977647618",
                    "1386522136551415810",
                    "1473123573069488129",
                    "1437296496513449985",
                    "1315936463289638914",
                    "1462626442666967042",
                    "1437258581364752385",
                    "1366236508953956353",
                    "1414490062198992898",
                    "1465886601457737730",
                    "1267292467797618690",
                    "1331800231031799809",
                    "1404987732391280642",
                    "1245317610827067394",
                    "1236159968508383235",
                    "1272717987410919426",
                    "eef91dfe57c342f586dd59eead8412c1",
                    "1462627050492919809",
                    "1324558167132975105",
                    "1406817490964582401",
                    "1406823153103015937"
                ],
                "shouldSendMsg": true
            }
        },
        "fldXXpVzFS9aU": {
            "id": "fldXXpVzFS9aU",
            "name": "SDR转正通知提醒",
            "type": 7,
            "property": {
                "brotherFieldId": "fldQM4jrWljb2",
                "foreignDatasheetId": "dstR8Xw5fe818hny08"
            }
        },
        "fldXl1LwYDQrx": {
            "id": "fldXl1LwYDQrx",
            "name": "一级团队",
            "type": 3,
            "property": {
                "options": [
                    {
                        "id": "optxCQFgqucmh",
                        "name": "市场运营 Marketing",
                        "color": 12
                    },
                    {
                        "id": "optdPrEJAEGaL",
                        "name": "产品工程 Product",
                        "color": 11
                    },
                    {
                        "id": "optC8N8i0doHv",
                        "name": "技术研发 Engineering",
                        "color": 10
                    },
                    {
                        "id": "optKRPzrSeRJB",
                        "name": "商业工程 Business",
                        "color": 7
                    },
                    {
                        "id": "optJVKQQwIAEz",
                        "name": "CEO Office",
                        "color": 14
                    },
                    {
                        "id": "optqOn0vb7Nbv",
                        "name": "斯蒂亚 SDR",
                        "color": 19
                    },
                    {
                        "id": "opt7FnBnn9lz4",
                        "name": "vika维格",
                        "color": 30
                    },
                    {
                        "id": "optlgUA5nChWx",
                        "name": "生态渠道 Partners",
                        "color": 45
                    },
                    {
                        "id": "opt44Hd51q7cH",
                        "name": "客户运营 Customers",
                        "color": 0
                    },
                    {
                        "id": "optjY81CuFnee",
                        "name": "产品设计 Design",
                        "color": 1
                    }
                ]
            }
        },
        "fldXq10EG6v1K": {
            "id": "fldXq10EG6v1K",
            "name": "转正前产出收集",
            "type": 7,
            "property": {
                "brotherFieldId": "fldflp0k8vSBP",
                "foreignDatasheetId": "dstoPQ8Ui07Crx5VSJ"
            }
        },
        "fldXrcbtG9lzy": {
            "id": "fldXrcbtG9lzy",
            "name": "周年推送",
            "type": 16,
            "property": {
                "expression": "IF({fldIrOHCyje6M}>0,IF(MID({fldzvFmkCGz3q}，1，99)=MID({fldVhkqIyHzci}，1，99)，“发”,\"\"),\"\")",
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldYE7YHh54WN": {
            "id": "fldYE7YHh54WN",
            "name": "旅会需求申请表",
            "type": 7,
            "property": {
                "brotherFieldId": "fldbnKAh5RcMO",
                "foreignDatasheetId": "dstsTSooYqrTwPm76Y"
            }
        },
        "fldYO4YFeYnTF": {
            "id": "fldYO4YFeYnTF",
            "name": "2022招聘任务-在职",
            "type": 7,
            "property": {
                "brotherFieldId": "fldqU2hC4VKqT",
                "foreignDatasheetId": "dstj5i0sprFVLbPQwa"
            }
        },
        "fldYxYeiCk0D7": {
            "id": "fldYxYeiCk0D7",
            "name": "维格内部网站导航",
            "type": 1
        },
        "fldZVx3Xg1oVF": {
            "id": "fldZVx3Xg1oVF",
            "name": "2022q1",
            "type": 7,
            "property": {
                "brotherFieldId": "fldSAhsZzu50Q",
                "limitSingleRecord": true,
                "foreignDatasheetId": "dstBwgbPbTceN3kvVQ"
            }
        },
        "fldZZdQm2HeQe": {
            "id": "fldZZdQm2HeQe",
            "name": "身份证号码",
            "type": 19,
            "property": {}
        },
        "fldaSTKG4TbvK": {
            "id": "fldaSTKG4TbvK",
            "name": "体验问题记录",
            "type": 7,
            "property": {
                "brotherFieldId": "flds221ldi6HV",
                "foreignDatasheetId": "dstge3s0Dq60joX3dA"
            }
        },
        "fldaZewu3pmug": {
            "id": "fldaZewu3pmug",
            "name": "排班汇总",
            "type": 7,
            "property": {
                "brotherFieldId": "fldKsWVX8SVCw",
                "foreignDatasheetId": "dstGBVuAxAo9MM97xt"
            }
        },
        "fldaqap3HrRrG": {
            "id": "fldaqap3HrRrG",
            "name": "会议记录 Meetings的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fldWxl5GBd97P",
                "foreignDatasheetId": "dstaKqvPqT7lPD0JET"
            }
        },
        "fldbPX5YAe5Eq": {
            "id": "fldbPX5YAe5Eq",
            "name": "每周工作内容-Grid view",
            "type": 7,
            "property": {
                "brotherFieldId": "fldAKnYlbS6S8",
                "foreignDatasheetId": "dstREkLbplLoQaRJfT"
            }
        },
        "fldboXeLCRfYs": {
            "id": "fldboXeLCRfYs",
            "name": "南山办新冠疫苗接种统计",
            "type": 7,
            "property": {
                "brotherFieldId": "fld50qoZooHha",
                "foreignDatasheetId": "dstA8eo0xaEXhZj0R9"
            }
        },
        "fldcCg6253HuN": {
            "id": "fldcCg6253HuN",
            "name": "目标 Objectives",
            "type": 7,
            "property": {
                "brotherFieldId": "fldXkzfK49hyl",
                "foreignDatasheetId": "dstQ4jZJZwWKzUSMFW"
            }
        },
        "fldcHkZmNeAvZ": {
            "id": "fldcHkZmNeAvZ",
            "name": "T恤尺码统计表 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldIPHCRyn4RN",
                "foreignDatasheetId": "dstBhVePSyiiMMAvBQ"
            }
        },
        "fldcdV7AjdqqU": {
            "id": "fldcdV7AjdqqU",
            "name": "2022团建费",
            "type": 7,
            "property": {
                "brotherFieldId": "fldtIHxXXqhnI",
                "foreignDatasheetId": "dstWHiGnjrEbbKiiKu"
            }
        },
        "fldd9llodOA7W": {
            "id": "fldd9llodOA7W",
            "name": "固定资产登记+盘点",
            "type": 7,
            "property": {
                "brotherFieldId": "fldJyhRzMLV7o",
                "foreignDatasheetId": "dstzNgJCnYPUMh4EuR"
            }
        },
        "flddBBly2kwKV": {
            "id": "flddBBly2kwKV",
            "name": "星球管理局（综合档案）的副本 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldDXjwlP7KTJ",
                "foreignDatasheetId": "dstoabiaWHZa6DCMNw"
            }
        },
        "flddEBG6hUj63": {
            "id": "flddEBG6hUj63",
            "name": "生日礼品选择处",
            "type": 1
        },
        "flddjODREThNG": {
            "id": "flddjODREThNG",
            "name": "自备电脑",
            "type": 3,
            "property": {
                "options": [
                    {
                        "id": "optMz6fjYosAU",
                        "name": "🔴 是",
                        "color": 0
                    },
                    {
                        "id": "opthrmJRqxMBn",
                        "name": "🟣 否",
                        "color": 1
                    }
                ]
            }
        },
        "flde9RVlT9nDH": {
            "id": "flde9RVlT9nDH",
            "name": "潜在用户收集",
            "type": 7,
            "property": {
                "brotherFieldId": "fldwveuk3i3q6",
                "foreignDatasheetId": "dst50Tr7BRnuWBqSHr"
            }
        },
        "fldekiqWrT52u": {
            "id": "fldekiqWrT52u",
            "name": "修改人",
            "type": 24,
            "property": {
                "uuids": [
                    "40e05e0e8c144fc1891f305b0e286678",
                    "e9da9e818ef841acafae4a09b33fe510",
                    "56732bec236c4af3a86e1f8d0f4eafea",
                    "50855429a837492284dbb285d77896ca",
                    "16b01b70f15948929b6c6a6ec9207da6",
                    "1b56b12cd69043a9ac4175bd9b9a1c65",
                    "aa3e6af7041c4907ba03889acc0b0cd1",
                    "7abcf0d82a7443c5aaac355d6b4cdd2f",
                    "51092e31f9c145b59c5ad48af1791ac6",
                    "23a5f52cd1ee4e6abdc8a77e9b73cdeb",
                    "f5f141c56086423bb7919a2060f52fe8",
                    "b7aac62d932749f1af1f9cba1efe92c2",
                    "c420f951cc224993aa3f310376296d86",
                    "4c8699668d504523b086787ce94387c7",
                    "e65d9f6b59654d00a48ad0b53e14f8d5",
                    "9166bea35d79456994b99956dbfabcb9",
                    "23a5f52cd1ee4e6abdc8a77e9b73cdec",
                    "eeb620a54e2248c69c25de68e6eb668c",
                    "3e2f7d835958472ab43a623f15dec64f",
                    "5b8686771f1e48a7b94da20eaf280d4d",
                    "869dd39aca9a426db28fb242407df25a",
                    "0b9d9562b0d245dd8294b6838953fb05",
                    "09d19fd689d14381a6081e370d409329",
                    "b12efa00b20342c5be0a6277dda2fe83",
                    "0bce025e57a34d9aa711ed79906950d5",
                    "714e4c52cbe14916bba7441b9f3272cf",
                    "f585a1a6d253449d94e080a58aef3ff8",
                    "06bf53cb580b4ad09042e1f0ac769c5d",
                    "4adb71fba75e4468855a13fc930ba9b6",
                    "b3a6d217ad694717913ed730bb786e1b",
                    "b41ca32b24cd43398fbdab9395dd03e0",
                    "d732c0da5479443ea89043d8d4e5877d",
                    "81129cb05275419fad66de83aec23fa3",
                    "a2cbc7f85b8146d1af426a35962d7287",
                    "2902598ce9c84e09b2d1c1d2252b6ced",
                    "113f836068e74a71b7c7133ba2c40785",
                    "ef7b19c9fc004ec38b7457f6b78024c4",
                    "2b4f27bb617d4c4ea9420f056f9fead5",
                    "c532004636e54c7580aff340ed3a4544",
                    "6bddd702ec134563860578b0171c3073",
                    "d0602d9d87414c29a3bffb2fa9e80814",
                    "a5b8b044d6b04b0abb27ef315aecf94e",
                    "f6c562fdb30f4ea196321f8d41113f25",
                    "0e13a79312354ee8a33941deceb815bc",
                    "76a73ad9c85a4e5290b4cfb4a1191d37",
                    "76111f93d7a441e1bfc621d700e3c1d2",
                    "6667f5487f4c4b178b1fd4901f1f8f3a",
                    "dd95efceb7d14809a83792a7d70b50dd",
                    "bf298308635c42f79a338013bda92b98",
                    "b5ebf378a5284838b4b153fecc3cf299",
                    "697362ceb9684cf19e722d84c74dc8a7",
                    "7c9d449df3fd41bbb02c1de075ce4782",
                    "09ca3f33c11a4980881fef5082c71b4a",
                    "ece039d976ef4f25914ee9bbaaaae47a",
                    "dfb67951515946f3992e41ce911479cb",
                    "6117483ce3e341cfa5c8fe6d0e02cd46",
                    "adb90c2ee1f34caeb6b75d5da578961d",
                    "e37d580871cb4e31975c07cb65c94f88",
                    "b8557184c6b44676a04ff2c691221b91",
                    "33004fa41e6647428b5642a0ced4258c",
                    "92481000ece4453b8210ecdd009b30aa",
                    "27fdde1dfb6143019c38455cfe8111c1",
                    "edcee07cf0fd4f0e837a296a77f9bd94",
                    "adc2a9cdd80f44778ac6e32f4e42e8e2",
                    "a0ccc0a1c3be4faeb3c8c324459e57bf",
                    "d610b4fe86c14b18a521dee9ca70c94a",
                    "a3dcc9d0401a45118f6e247e01ec150b",
                    "c7186ca7cffa4ee8a470290d8f93487f",
                    "c95a4e599d8b424e842b6731e941ccf9",
                    "51c8394434044a0bb4d05f6741444caa",
                    "7d6b87fcd4674ceb8feed06e8a114d93",
                    "d2aa673f873f45439128bd5e52835477",
                    "cbfd06ca491e4042b7c2035a0ee608ae",
                    "aef5a6605ea341279c811a724c1f7d26",
                    "1b67f95ff96441c4afd83ffdde14b2b5",
                    "2dc7f9021ee54e6e87d4b05051eeac93",
                    "db745334b28940a0a6d90b503d1ffd3b",
                    "c31130492ba24f76877615138ae93f93",
                    "e3501cfc02004ab08b5548d032da654a",
                    "cd25a5d644d64e5b87cac6fbd6bce7bc",
                    "687759764929455d8cba1f8576cb33e5",
                    "b957324791a14054986f9c44854464e4",
                    "17e07cf00d6d447fb2225e9db547f8c8",
                    "934e8afb905c4d25983a6a74404ec88d",
                    "1e1bb55e093b4a86babfc67372effa66",
                    "74418ea3b23b4e06b8f30293e1fb3045",
                    "d193606f64d342909e0a93696af00dd1",
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    "72e1442435e043899bce153ff166c516",
                    "d07c75be237c46d2aaddfae2b2ff826c",
                    "62bda04135074b78be76fd4a8cfd01f2",
                    "a91f0313fcc748baa2424f813db2392b",
                    "232d6afb8a304011803754c066301458",
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    "c47a715d13944ae99f0927a30ea32d1b",
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    "888e483c713c4aaeadd2ab22bae903f5",
                    "64cd375e55f747c89f185644f29f6628",
                    "dccfb205937e41e7a43da2e20164b3f8",
                    "4ae7d0eb0a824496b13a692489190d55",
                    "a114e7dac07f41bca2ac02c121a9bae2",
                    "d9173074608d4c878204fd94b38c20d5",
                    "42eb6f658f924cea811752bf2d571cee",
                    "49189866e85349a2a2f652500cf804ba",
                    "c367d0d6f00347f2a020e7cf6e8ebc01",
                    "a83ec20f15c9459893d133c2c369eff6",
                    "5f935c8c5ffb4475ba91fb8125aa16fd",
                    "e8b2a91f20db4a168b4007c5a92f6e81",
                    "bed6e84b6fec4d82889f059c18fa07f4",
                    "d240a6c915ea41249722bff53b0af2c1",
                    "9769074644aa41188506baeea86f2090",
                    "7e78825636d94299a8278362d6a5b526",
                    "ed624281c5a4496ab6e8647bc93778c5",
                    "07681b9f05434029945c663c406c1caf",
                    "d9d62c578aae4a42be684f9e37a43dc2",
                    "50cbab6cb65e49518831145ea7379ca7",
                    "c04f56663d0c48d5aea1f7952d2c2d7b",
                    "a98da11c03364ea49da876d4182f66a0",
                    "c9939674d7d14eda83dd065aa08be009",
                    "88ebe5c2489e4eb8bcd55fb454ff32ea",
                    "e1036b3b5b704b41a01b1a2e84bbd1e7",
                    "bbcbf7223e0e41db87d454542afed7f4",
                    "335acbbbe938436588af6bced6a0599a",
                    "d8c2112dea4f487390984953253e8198",
                    "def28f269bf349cda7258607bbb8f9ee",
                    "3be66402587e4647bde42f4782cb5656",
                    "5d02b5c8303e499f83dbc6ab5716ab8f",
                    "938fd14da9f243f785e57ad961f35bc6",
                    "b20a693c428c4d0cb807d8b3b2c99f18",
                    "1ef3dbdd698e4557acb15e50d5ef7852",
                    "4ef4fd57ccd34fa7b626a1614c091441",
                    "abb6b0f8b412486da052e8b36b775243",
                    "bf5c019052a842e7afec2367a94d39e0",
                    "036d0e2ac1cc4374b844a6458525fef2",
                    "8a766617f21041279e1dd85664b76be6",
                    "489e6f58daa34040a041eb72403f3fc1",
                    "0c4565496bb3439dac52a3ef15944d26",
                    "e0e07b0cf26142d0b2d43ff61cea5493",
                    "bcf86bc2497a4361939437f257a91df1",
                    "fecf59caf1234267a50dc406454c2da0",
                    "1345c2d50ff44e2a8c7fa8b3e9a57525",
                    "3c64f2254bbb47fc91b1638121560926",
                    "15bfdd1e5aef45d8bca07f681b5d9232",
                    "806784677c814c9db93972c023f7f4d4",
                    "96e24eef131840d795cf2570eb49a3ed",
                    "110639147e82437aba1ccde50f05c3ff",
                    "eef91dfe57c342f586dd59eead8412c1",
                    "1a3ad2705d8840e89295f5c8017c0cdf",
                    "1bee932968574cf6919902273e5ae46c",
                    "a8f27b8ed27f459cb4512158c2052a6f",
                    "5bf58d1f2dc2495093744d49beb34ee2",
                    "411e0a7b9cfc4d2aa637e4f391f11b1d",
                    "f891ba9bd43d47089dca81a2163f8047",
                    "b89a6129d7aa4ce984cf8f34f475d384",
                    "f338683905004610b6006e2a0b8eccc8",
                    "3abacc827b73493d9e01818ffca4ca2c",
                    "71b468470b9e46df9d50c9840bcd331d",
                    "f95a1585ad194809aea2f209f1aee26f",
                    "cf3189d22b234b00ba37b566a74572e0",
                    "a244dc4ee9bc41ce8a9e7a8f9ec61ea6",
                    "01cf8a2164bd4017bf2ea926d1814005",
                    "2b80290611344714b8a3bd39fcb4a098",
                    "ecb9982b2edd4079ab77dde456b7bb58",
                    "955dca1609ce4610a0d4ccceec21b7c8",
                    "00d3451ab21f4648861c2f2558252603",
                    "a5e5b003fc9b40b08b5d9cfa52f9f26d",
                    "38575d399ae74f8bbc2a1d9d51adf357",
                    "8980d25da42947f995451386fc291834",
                    "e42e6e891baa42ccaa1e0f316d7925d3",
                    "254cef9eeef54e8ea85de49391947ca9",
                    "5dee6a81f6fe408f934312a12984e462",
                    "c9863e875fe64331a9e6e6ff61ba407c",
                    "13afd5b930f644b69e79de4f9643887a",
                    "12859838940c483c8341649a14b950e5",
                    "75e1bb975ca64210a4b3944ee87fa280",
                    "176384fe0cfd4b03afe6e574eccf562a",
                    "45d4037c64ac442d88d45c971a9565c3",
                    "15699ee1c08a40298a61e913599074a7"
                ],
                "collectType": 0,
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "fieldIdCollection": []
            }
        },
        "fldevML8MNXBo": {
            "id": "fldevML8MNXBo",
            "name": "英文头衔",
            "type": 19,
            "property": {
                "defaultValue": ""
            }
        },
        "fldfHAO94EM17": {
            "id": "fldfHAO94EM17",
            "name": "团建费信息登记",
            "type": 1
        },
        "fldfTS1TMleKQ": {
            "id": "fldfTS1TMleKQ",
            "name": "Robot - 本周PE客服updateTime",
            "type": 5,
            "property": {
                "autoFill": false,
                "dateFormat": 1,
                "timeFormat": 0,
                "includeTime": false
            }
        },
        "fldfeOyphY95i": {
            "id": "fldfeOyphY95i",
            "name": "社群用户反馈",
            "type": 7,
            "property": {
                "brotherFieldId": "fldLzlm3fZDEx",
                "foreignDatasheetId": "dstWKX36l4PxPS1KJ7"
            }
        },
        "fldg6ERkxgsEU": {
            "id": "fldg6ERkxgsEU",
            "name": "星球管理局（综合档案）的副本",
            "type": 1
        },
        "fldgOjAyn8zSr": {
            "id": "fldgOjAyn8zSr",
            "name": "团建费信息登记的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fld04D3zuNatf",
                "foreignDatasheetId": "dstWHiGnjrEbbKiiKu"
            }
        },
        "fldgWVIDtBRGc": {
            "id": "fldgWVIDtBRGc",
            "name": "每天工作记录-Grid view",
            "type": 7,
            "property": {
                "brotherFieldId": "fldZW6uAgNAKJ",
                "foreignDatasheetId": "dstEFSii62bHFFDWbT"
            }
        },
        "fldgt7GvJLjzA": {
            "id": "fldgt7GvJLjzA",
            "name": "2022个人团建费额度",
            "type": 16,
            "property": {
                "expression": "COUNTA({fldqdsUD85vWo})*100",
                "formatting": {
                    "symbol": "¥",
                    "precision": 2,
                    "formatType": 17
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldh6hMxCwvh7": {
            "id": "fldh6hMxCwvh7",
            "name": "种子用户维护",
            "type": 7,
            "property": {
                "brotherFieldId": "fldVu9qhjVWkK",
                "foreignDatasheetId": "dst50Tr7BRnuWBqSHr"
            }
        },
        "fldhGBtkgS10N": {
            "id": "fldhGBtkgS10N",
            "name": "2022个人当月剩余可用额度",
            "type": 16,
            "property": {
                "expression": "{fldgt7GvJLjzA}-COUNTIF({fldu9u5JfJMaZ}，TRIM({fld2di1GhLLBD}))*100",
                "formatting": {
                    "symbol": "¥",
                    "precision": 2,
                    "formatType": 17
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldhkEuyo8ALe": {
            "id": "fldhkEuyo8ALe",
            "name": "司龄",
            "type": 16,
            "property": {
                "expression": "DATETIME_DIFF(TODAY(),{fld9dtBA8JMz1})/365",
                "formatting": {
                    "symbol": "¥",
                    "precision": 1,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "callToDayValue": "2022-12-06T00:00:00.000Z"
            }
        },
        "fldhy67A8vntP": {
            "id": "fldhy67A8vntP",
            "name": "本次旅会意见和建议调查",
            "type": 1
        },
        "fldi5uLPTMUe3": {
            "id": "fldi5uLPTMUe3",
            "name": "有维青年",
            "type": 7,
            "property": {
                "brotherFieldId": "fldhkii8zUMq8",
                "foreignDatasheetId": "dstlygUmd3lxP1nxKj"
            }
        },
        "fldiCp6KeJqnj": {
            "id": "fldiCp6KeJqnj",
            "name": "CMS 任务管理 copy",
            "type": 1,
            "property": null
        },
        "fldikWZQZGaUs": {
            "id": "fldikWZQZGaUs",
            "name": "转正名单管理",
            "type": 7,
            "property": {
                "brotherFieldId": "fldJI81RB3ZgM",
                "foreignDatasheetId": "dstAaQSrsmeg5TAJ4Z"
            }
        },
        "fldin1yTnqLgT": {
            "id": "fldin1yTnqLgT",
            "name": "三级团队",
            "type": 3,
            "property": {
                "options": [
                    {
                        "id": "opt2HvuQrRsbc",
                        "name": "华南区域 South China",
                        "color": 5
                    },
                    {
                        "id": "optUfiOe9pHWV",
                        "name": "华北区域 North China",
                        "color": 5
                    },
                    {
                        "id": "optnAXAvgFEZO",
                        "name": "华东区域 East China",
                        "color": 5
                    },
                    {
                        "id": "opttAJtOmbRKv",
                        "name": "销售运营 SOM",
                        "color": 2
                    },
                    {
                        "id": "optEqJxukbLqG",
                        "name": "客户成功 CSM",
                        "color": 2
                    },
                    {
                        "id": "optBG19stCGbk",
                        "name": "网络销售 ISR",
                        "color": 2
                    }
                ]
            }
        },
        "fldjHHDHE0ccz": {
            "id": "fldjHHDHE0ccz",
            "name": "邮箱",
            "type": 9
        },
        "fldjLlH9EDuOL": {
            "id": "fldjLlH9EDuOL",
            "name": "come on~吐个槽（处理人）",
            "type": 7,
            "property": {
                "brotherFieldId": "fld7cCLPn9Qv0",
                "foreignDatasheetId": "dstxMwTVU1CGwMZkhs"
            }
        },
        "fldjPgWVjkSZg": {
            "id": "fldjPgWVjkSZg",
            "name": "生日礼品库的副本",
            "type": 1
        },
        "fldjXbS0TABeG": {
            "id": "fldjXbS0TABeG",
            "name": "巫师类型",
            "type": 4,
            "property": {
                "options": [
                    {
                        "id": "opt1ZN6qkbZYe",
                        "name": "技术型巫师",
                        "color": 0
                    },
                    {
                        "id": "optE0wjg9r2NA",
                        "name": "数据型巫师",
                        "color": 1
                    },
                    {
                        "id": "optaO07Oi1MFs",
                        "name": "业务型巫师",
                        "color": 2
                    },
                    {
                        "id": "optc1KYsfopKD",
                        "name": "麻瓜",
                        "color": 3
                    }
                ]
            }
        },
        "fldja5v1N7Mjt": {
            "id": "fldja5v1N7Mjt",
            "name": "工牌",
            "type": 14,
            "property": {
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "relatedLinkFieldId": "fldHSP95pUZey",
                "lookUpTargetFieldId": "fldQZSJyqUoVf"
            }
        },
        "fldk1q1w0uCb1": {
            "id": "fldk1q1w0uCb1",
            "name": "days",
            "type": 16,
            "property": {
                "expression": "TONOW({fldV1GmxiFH26}，“d”)",
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "callNowValue": "2022-08-24T03:33:04.910Z"
            }
        },
        "fldk8wMkVPGD6": {
            "id": "fldk8wMkVPGD6",
            "name": "团建费信息登记 2",
            "type": 1
        },
        "fldkXXNxR7qg0": {
            "id": "fldkXXNxR7qg0",
            "name": "设备采购进度",
            "type": 1
        },
        "fldkeKMUyVcP6": {
            "id": "fldkeKMUyVcP6",
            "name": "报销记录",
            "type": 7,
            "property": {
                "brotherFieldId": "fldM4Kmclta4c",
                "foreignDatasheetId": "dstKuG9gmhKe89y1Yw"
            }
        },
        "fldkmVPyliZ6D": {
            "id": "fldkmVPyliZ6D",
            "name": "办公规划",
            "type": 7,
            "property": {
                "brotherFieldId": "fldHV1xFFFHNR",
                "foreignDatasheetId": "dst2rXzYs5nsizUsvW"
            }
        },
        "fldlk3lUskZAD": {
            "id": "fldlk3lUskZAD",
            "name": "业务侧面试官资源池",
            "type": 7,
            "property": {
                "brotherFieldId": "fld6gwzqmGrJO",
                "foreignDatasheetId": "dstmcnSeN3qhP1PykU"
            }
        },
        "fldm7PDKGBeCn": {
            "id": "fldm7PDKGBeCn",
            "name": "会议讨论 Meetings",
            "type": 7,
            "property": {
                "brotherFieldId": "fldWxl5GBd97P",
                "foreignDatasheetId": "dst7CxEjHHmeSSo4QB"
            }
        },
        "fldm9iVuL9hWd": {
            "id": "fldm9iVuL9hWd",
            "name": "转正时间",
            "type": 16,
            "property": {
                "expression": "DATEADD({fld9dtBA8JMz1},6,\"months\")",
                "formatting": {
                    "autoFill": false,
                    "dateFormat": 0,
                    "timeFormat": 1,
                    "includeTime": false
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldmGL3XYhhZ3": {
            "id": "fldmGL3XYhhZ3",
            "name": "议题",
            "type": 7,
            "property": {
                "brotherFieldId": "fldl0c3mPMxQu",
                "foreignDatasheetId": "dstMlW8WgprLVkFU32"
            }
        },
        "fldmSX2w5JiaB": {
            "id": "fldmSX2w5JiaB",
            "name": "关键绩效 Key Results",
            "type": 7,
            "property": {
                "brotherFieldId": "fldAkxNFoxIIu",
                "foreignDatasheetId": "dstM3y48UekTzKprlW"
            }
        },
        "fldmfEI3Cmj8i": {
            "id": "fldmfEI3Cmj8i",
            "name": "新建维格表 4",
            "type": 7,
            "property": {
                "brotherFieldId": "fldBEMcSRCPXD",
                "foreignDatasheetId": "dstVLifjUvDCcSkHEX"
            }
        },
        "fldmkq4qCoMiz": {
            "id": "fldmkq4qCoMiz",
            "name": "有维青年的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fldhkii8zUMq8",
                "foreignDatasheetId": "dstBsMt7J46Uszqbjx"
            }
        },
        "fldmvWjg6pxfQ": {
            "id": "fldmvWjg6pxfQ",
            "name": "入职架构",
            "type": 7,
            "property": {
                "brotherFieldId": "fldFBriSNKpBC",
                "foreignDatasheetId": "dstX2rGGL4qw5Mvlqr"
            }
        },
        "fldoBzna0BOzQ": {
            "id": "fldoBzna0BOzQ",
            "name": "3 资产过程管理汇总的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fldW3QybeTMmI",
                "foreignDatasheetId": "dst8GpDcryKR7rJUUl"
            }
        },
        "fldoX0C59j7qV": {
            "id": "fldoX0C59j7qV",
            "name": "考评时间",
            "type": 5,
            "property": {
                "autoFill": false,
                "dateFormat": 0,
                "timeFormat": 1,
                "includeTime": false
            }
        },
        "fldoXJMoTPSyW": {
            "id": "fldoXJMoTPSyW",
            "name": "在职证明的副本 3",
            "type": 1
        },
        "fldojn3h0lqUn": {
            "id": "fldojn3h0lqUn",
            "name": "任务表",
            "type": 7,
            "property": {
                "brotherFieldId": "fldthuByCRUuP",
                "foreignDatasheetId": "dstyLBkU95PUV7sg6o"
            }
        },
        "fldok6Y3X2uTC": {
            "id": "fldok6Y3X2uTC",
            "name": "生日月份",
            "type": 16,
            "property": {
                "expression": "MONTH({fldV1GmxiFH26})",
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldoqBEHgROpS": {
            "id": "fldoqBEHgROpS",
            "name": "在职证明",
            "type": 1
        },
        "fldp7VZmrRf2j": {
            "id": "fldp7VZmrRf2j",
            "name": "团建费额度",
            "type": 7,
            "property": {
                "brotherFieldId": "fld04D3zuNatf",
                "foreignDatasheetId": "dstZmmaSEhNo8TNEPC"
            }
        },
        "fldq1yfB55C5F": {
            "id": "fldq1yfB55C5F",
            "name": "神奇关联",
            "type": 7,
            "property": {
                "foreignDatasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldqARerS1ewv": {
            "id": "fldqARerS1ewv",
            "name": "办公室门禁卡信息登记",
            "type": 7,
            "property": {
                "brotherFieldId": "fld1zVXYYvrxx",
                "foreignDatasheetId": "dstcADEq5hA1wGyg2R"
            }
        },
        "fldqGs7UE9xAP": {
            "id": "fldqGs7UE9xAP",
            "name": "需求书单",
            "type": 7,
            "property": {
                "brotherFieldId": "fldR2gOVe37Ao",
                "foreignDatasheetId": "dstbMmYXBzfLs9CBhE"
            }
        },
        "fldqISZeOfLPG": {
            "id": "fldqISZeOfLPG",
            "name": "OD任务推送",
            "type": 3,
            "property": {
                "options": [
                    {
                        "id": "optyxZvnJaJ1x",
                        "name": "是",
                        "color": 0
                    }
                ]
            }
        },
        "fldqdRfCF0v5O": {
            "id": "fldqdRfCF0v5O",
            "name": "资产在册的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fldlEq5ytsiBG",
                "foreignDatasheetId": "dstd732A9PStJlVEWq"
            }
        },
        "fldqdsUD85vWo": {
            "id": "fldqdsUD85vWo",
            "name": "2022团建费额度",
            "type": 1
        },
        "fldqhzpRrAlHv": {
            "id": "fldqhzpRrAlHv",
            "name": "关键绩效 Key Results 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldG33SeQOhtc",
                "foreignDatasheetId": "dstM3y48UekTzKprlW"
            }
        },
        "fldqke85qyo5p": {
            "id": "fldqke85qyo5p",
            "name": "years",
            "type": 16,
            "property": {
                "expression": "TONOW({fldV1GmxiFH26}，“y”)",
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "callNowValue": "2022-08-24T03:33:04.910Z"
            }
        },
        "fldrQiSm9FG11": {
            "id": "fldrQiSm9FG11",
            "name": "二级团队",
            "type": 3,
            "property": {
                "options": [
                    {
                        "id": "optuGnWo0XaQd",
                        "name": "内容市场  Content Marketing",
                        "color": 2
                    },
                    {
                        "id": "optwO2A8tcsij",
                        "name": "市场策划 GTM Growth",
                        "color": 2
                    },
                    {
                        "id": "optf66eADq1qY",
                        "name": "品牌视觉 Graphic Design",
                        "color": 2
                    },
                    {
                        "id": "optch5EaN4Uod",
                        "name": "财务运营 Finance",
                        "color": 2
                    },
                    {
                        "id": "optdVjPJEAPWN",
                        "name": "客户运营 Customers",
                        "color": 2
                    },
                    {
                        "id": "optZPjOdJQWkG",
                        "name": "客户开发 MDR",
                        "color": 2
                    },
                    {
                        "id": "opt1zQGCTwb73",
                        "name": "平台产品 Product Platform",
                        "color": 1
                    },
                    {
                        "id": "optaTS2JvBS9v",
                        "name": "设计产品 Product Design",
                        "color": 1
                    },
                    {
                        "id": "optZ33tsKMLpV",
                        "name": "用户产品 Product Community",
                        "color": 1
                    },
                    {
                        "id": "optabFhNE4bi8",
                        "name": "前端工程 UX Engineering",
                        "color": 0
                    },
                    {
                        "id": "optlptuixxlY2",
                        "name": "云端工程 Cloud Engineering",
                        "color": 0
                    },
                    {
                        "id": "opt1p8EfQPLzD",
                        "name": "运维架构 Infra",
                        "color": 0
                    },
                    {
                        "id": "opto6JKhFJLKF",
                        "name": "质量工程 Quality Engineering",
                        "color": 0
                    },
                    {
                        "id": "optQKnkYMBP6t",
                        "name": "移动端工程 Mobile Engineering",
                        "color": 0
                    },
                    {
                        "id": "optbUoZXUvMNH",
                        "name": "云平台架构 DevOps",
                        "color": 6
                    },
                    {
                        "id": "opt9QXQeGXL5I",
                        "name": "大客户交付 KA Delivery",
                        "color": 6
                    },
                    {
                        "id": "optIRacmrcI5g",
                        "name": "应用研发 Labs Engineering",
                        "color": 6
                    },
                    {
                        "id": "optqNuFkz1ZQ2",
                        "name": "大客户咨询 KA Consulting",
                        "color": 6
                    },
                    {
                        "id": "optw18mRAaGSo",
                        "name": "人事运营 Talent",
                        "color": 4
                    },
                    {
                        "id": "optg61RAcNZ78",
                        "name": "综合管理 PMO",
                        "color": 4
                    },
                    {
                        "id": "opt9pIIBBuiWP",
                        "name": "战略参谋组 Strategy",
                        "color": 4
                    },
                    {
                        "id": "optnte8q35NA9",
                        "name": "招聘开发 JDR",
                        "color": 4
                    },
                    {
                        "id": "opt2HvuQrRsbc",
                        "name": "华南区域 South China",
                        "color": 30
                    },
                    {
                        "id": "optUfiOe9pHWV",
                        "name": "华北区域 North China",
                        "color": 30
                    },
                    {
                        "id": "optnAXAvgFEZO",
                        "name": "华东区域 East China",
                        "color": 30
                    },
                    {
                        "id": "opt984xxEXaDy",
                        "name": "管理部",
                        "color": 9
                    },
                    {
                        "id": "optcIHTblMKP5",
                        "name": "渠道运营 Channel Sales",
                        "color": 25
                    },
                    {
                        "id": "opt2MdjZEewcZ",
                        "name": "生态运营 Partner Marketing",
                        "color": 25
                    },
                    {
                        "id": "opttAJtOmbRKv",
                        "name": "销售运营 SOM",
                        "color": 8
                    },
                    {
                        "id": "optEqJxukbLqG",
                        "name": "客户成功 CSM",
                        "color": 9
                    },
                    {
                        "id": "optBG19stCGbk",
                        "name": "网络销售 ISR",
                        "color": 8
                    },
                    {
                        "id": "optEnDOvgnLB1",
                        "name": "体验设计 UX Design",
                        "color": 1
                    }
                ]
            }
        },
        "fldrRaoIS0ayX": {
            "id": "fldrRaoIS0ayX",
            "name": "性别",
            "type": 3,
            "property": {
                "options": [
                    {
                        "id": "opt8CaYwXKETc",
                        "name": "男",
                        "color": 0
                    },
                    {
                        "id": "optLSWU6ufbuy",
                        "name": "女",
                        "color": 1
                    }
                ]
            }
        },
        "fldrghNXdj9ul": {
            "id": "fldrghNXdj9ul",
            "name": "新建维格表 5",
            "type": 1
        },
        "fldrmuo3feVp3": {
            "id": "fldrmuo3feVp3",
            "name": "姓名",
            "type": 19,
            "property": {
                "defaultValue": ""
            }
        },
        "flds6z36SNzzx": {
            "id": "flds6z36SNzzx",
            "name": "部门信息 2",
            "type": 1
        },
        "fldsf7m78EmUd": {
            "id": "fldsf7m78EmUd",
            "name": "启动会&培训名单 210724",
            "type": 7,
            "property": {
                "brotherFieldId": "fldz2jDbZWayp",
                "foreignDatasheetId": "dstlsuDK10tR9SS4fH"
            }
        },
        "fldssUH3kUuPv": {
            "id": "fldssUH3kUuPv",
            "name": "寿星名单",
            "type": 1
        },
        "fldteieyblnGL": {
            "id": "fldteieyblnGL",
            "name": "months",
            "type": 16,
            "property": {
                "expression": "TONOW({fldV1GmxiFH26}，“M”)",
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "callNowValue": "2022-08-24T03:33:04.910Z"
            }
        },
        "fldtmYFQOF4Yn": {
            "id": "fldtmYFQOF4Yn",
            "name": "生日邮件",
            "type": 7,
            "property": {
                "brotherFieldId": "fldiBCL3d6esW",
                "foreignDatasheetId": "dstgaVbr0aofijLBYL"
            }
        },
        "fldtzRoK4of91": {
            "id": "fldtzRoK4of91",
            "name": "电脑设备采购登记表",
            "type": 7,
            "property": {
                "brotherFieldId": "fldhjZZQtJkpX",
                "foreignDatasheetId": "dstQeDzRfdkqn10Pw5"
            }
        },
        "fldu9u5JfJMaZ": {
            "id": "fldu9u5JfJMaZ",
            "name": "当月已消费",
            "type": 14,
            "property": {
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "relatedLinkFieldId": "fldqdsUD85vWo",
                "lookUpTargetFieldId": "fldcbTqmdkU5W"
            }
        },
        "flduaapiBEu3h": {
            "id": "flduaapiBEu3h",
            "name": "场景熟悉度考察",
            "type": 7,
            "property": {
                "brotherFieldId": "fldASWbXaPed1",
                "foreignDatasheetId": "dstsT5KFiN74N1rp6p"
            }
        },
        "fldv0A62pG6Va": {
            "id": "fldv0A62pG6Va",
            "name": "资产在册",
            "type": 7,
            "property": {
                "brotherFieldId": "fldlEq5ytsiBG",
                "foreignDatasheetId": "dstxAYLYN1VyUetMou"
            }
        },
        "fldvCK9Zc9rih": {
            "id": "fldvCK9Zc9rih",
            "name": "离职访谈纪录 2",
            "type": 7,
            "property": {
                "brotherFieldId": "fldOEJppAW8MW",
                "foreignDatasheetId": "dstwl4ghB4xpm96FPp"
            }
        },
        "fldvXeuR5znlv": {
            "id": "fldvXeuR5znlv",
            "name": "手机号1",
            "type": 10
        },
        "fldvpygX7nb4p": {
            "id": "fldvpygX7nb4p",
            "name": "OD需求池",
            "type": 7,
            "property": {
                "brotherFieldId": "fldIZVgPHW0lU",
                "foreignDatasheetId": "dstHliXj8Bp4RfNpQJ"
            }
        },
        "fldw0b8TBzguI": {
            "id": "fldw0b8TBzguI",
            "name": "新建维格表 3",
            "type": 7,
            "property": {
                "brotherFieldId": "fldqFOG1qElAM",
                "foreignDatasheetId": "dsth0deurs8VQMWb7z"
            }
        },
        "fldwG00NrX8zr": {
            "id": "fldwG00NrX8zr",
            "name": "weeks",
            "type": 16,
            "property": {
                "expression": "TONOW({fldV1GmxiFH26}，“w”)",
                "formatting": {
                    "symbol": "¥",
                    "precision": 0,
                    "formatType": 2
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC",
                "callNowValue": "2022-08-24T03:33:04.910Z"
            }
        },
        "fldwTh5HOd1al": {
            "id": "fldwTh5HOd1al",
            "name": "设备配置标准",
            "type": 7,
            "property": {
                "limitToView": "viwWrzREBYdg8",
                "brotherFieldId": "fldTphn13anXE",
                "foreignDatasheetId": "dsth8SGZtmiugeSVba"
            }
        },
        "fldweEFwrumLJ": {
            "id": "fldweEFwrumLJ",
            "name": "新建维格表 2",
            "type": 1
        },
        "fldwgPyNT6gtL": {
            "id": "fldwgPyNT6gtL",
            "name": "2022招聘任务-待入职",
            "type": 7,
            "property": {
                "brotherFieldId": "fldXM4CnXhLCk",
                "foreignDatasheetId": "dstj5i0sprFVLbPQwa"
            }
        },
        "fldx0PKuNeY5i": {
            "id": "fldx0PKuNeY5i",
            "name": "生日日期",
            "type": 16,
            "property": {
                "expression": "{fldV1GmxiFH26}",
                "formatting": {
                    "autoFill": false,
                    "dateFormat": 4,
                    "timeFormat": 1,
                    "includeTime": false
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        },
        "fldxl55KmOdRs": {
            "id": "fldxl55KmOdRs",
            "name": "2022招聘任务-离职",
            "type": 7,
            "property": {
                "brotherFieldId": "fldLi2I6f2gOW",
                "foreignDatasheetId": "dstj5i0sprFVLbPQwa"
            }
        },
        "fldxnhRrGwEgT": {
            "id": "fldxnhRrGwEgT",
            "name": "2023年内部礼盒信息收集表的副本",
            "type": 7,
            "property": {
                "brotherFieldId": "fldOrcYnDbngk",
                "foreignDatasheetId": "dstFrBX9jxhhDmzD2l"
            }
        },
        "fldy8pYZSesf5": {
            "id": "fldy8pYZSesf5",
            "name": "办公地点",
            "type": 3,
            "property": {
                "options": [
                    {
                        "id": "optIHmSU8qs5V",
                        "name": "深圳南山",
                        "color": 0
                    },
                    {
                        "id": "opt5yKDtlSWd8",
                        "name": "深圳福田",
                        "color": 1
                    },
                    {
                        "id": "opttaxblJGxOs",
                        "name": "珠海",
                        "color": 2
                    },
                    {
                        "id": "optRf1BvbSMLX",
                        "name": "北京",
                        "color": 3
                    },
                    {
                        "id": "optL71TJyCqlx",
                        "name": "上海",
                        "color": 4
                    },
                    {
                        "id": "optmalzhd41ZV",
                        "name": "江门",
                        "color": 5
                    },
                    {
                        "id": "optGwWUGmLJzy",
                        "name": "西安",
                        "color": 6
                    },
                    {
                        "id": "opt9iRIDASeDY",
                        "name": "武汉",
                        "color": 7
                    },
                    {
                        "id": "opt5mVSl14AAJ",
                        "name": "成都",
                        "color": 8
                    },
                    {
                        "id": "optddYvQsktO4",
                        "name": "郑州",
                        "color": 9
                    },
                    {
                        "id": "optEyFNYPcKTy",
                        "name": "淄博",
                        "color": 0
                    },
                    {
                        "id": "optees6d3HjC2",
                        "name": "杭州",
                        "color": 1
                    },
                    {
                        "id": "optqWxxTUjIVE",
                        "name": "海南东方",
                        "color": 2
                    },
                    {
                        "id": "opt3TGhB6m6J7",
                        "name": "福建福州",
                        "color": 3
                    }
                ]
            }
        },
        "fldyMteIs873q": {
            "id": "fldyMteIs873q",
            "name": "✈️ 未入职",
            "type": 11,
            "property": {
                "icon": "white_check_mark"
            }
        },
        "fldygTxC5LvD9": {
            "id": "fldygTxC5LvD9",
            "name": "采购计划",
            "type": 7,
            "property": {
                "brotherFieldId": "fldynsc1Vdpqs",
                "foreignDatasheetId": "dstSL2uBKznvJMbmQG"
            }
        },
        "fldzZKRHYmnsn": {
            "id": "fldzZKRHYmnsn",
            "name": "星球管理局（综合档案）",
            "type": 1
        },
        "fldzvFmkCGz3q": {
            "id": "fldzvFmkCGz3q",
            "name": "周年日期",
            "type": 16,
            "property": {
                "expression": "{fld9dtBA8JMz1}",
                "formatting": {
                    "autoFill": false,
                    "dateFormat": 4,
                    "timeFormat": 1,
                    "includeTime": false
                },
                "datasheetId": "dst8vlYfcUxjEK6oEC"
            }
        }
    },
    "widgetPanels": [
        {
            "id": "wpl1Vdtxq4DlR",
            "widgets": [
                {
                    "id": "wdtC8Nmi4v8wexFU6F",
                    "height": 11
                },
                {
                    "id": "wdtoNfNzyqmWqCLoxN",
                    "height": 6.2
                },
                {
                    "id": "wdtHGpGwRjX29AhS03",
                    "height": 6.2
                },
                {
                    "id": "wdtWfMd44LqxjEviPs",
                    "height": 6.2
                },
                {
                    "id": "wdtPc8kviYd75yCDsg",
                    "height": 6.2
                }
            ]
        }
    ],
    "archivedRecordIds": []
}
"#;
#[cfg(test)]
pub const DAILY_SHEET_META: &str  = r#"
{
    "views": [
        {
            "id": "viwNfFVaoRhDz",
            "name": "产研全局",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 162,
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 168,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "autoSave": false,
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fldcIuJdFq5AN"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "desc": false,
                    "fieldId": "fldGPXWXSjDMz"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "产品设计"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtX48vjUoVun"
                    },
                    {
                        "value": [
                            "CEO"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtuNMvBOpLvb"
                    },
                    {
                        "value": [
                            "Front"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdto6J8o66nau"
                    },
                    {
                        "value": [
                            "Backend"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdt7dkPzi4ucz"
                    },
                    {
                        "value": [
                            "产品运营&用户运营"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtLS9RYDZn1w"
                    },
                    {
                        "value": [
                            "平台架构"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdt3RlWUKpmf9"
                    },
                    {
                        "value": [
                            "商业产品"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtPbgd5hnpay"
                    },
                    {
                        "value": [
                            "产品策划"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtc02YxDz7SJ"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwLDmj5lO1yC",
            "name": "今日视图",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 139,
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 84,
                    "hidden": true,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 192,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 168,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "width": 289,
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": false,
                    "fieldId": "fldlb8DabKkAK"
                }
            ],
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldGPXWXSjDMz"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "Today"
                        ],
                        "fieldId": "fldcIuJdFq5AN",
                        "operator": "is",
                        "fieldType": 5,
                        "conditionId": "cdtvGufApcnUA"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 4,
            "frozenColumnCount": 1
        },
        {
            "id": "viwkZ694MsA4Z",
            "name": "我的晨会速记",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 200,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fldcIuJdFq5AN"
                    }
                ],
                "keepSort": true
            },
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "Self"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "is",
                        "fieldType": 23,
                        "conditionId": "cdt2M4RRWTV6B"
                    }
                ],
                "conjunction": "and"
            },
            "frozenColumnCount": 1
        },
        {
            "id": "viwCfigCiTBda",
            "name": "产研-Cloud(Backend)",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 245,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "hidden": true,
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "backend"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtbn0uHQmjmn"
                    },
                    {
                        "value": [
                            "Infra"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtqW7ulSVHHI"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwM4O9cVJ8nz",
            "name": "质量工程组",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 139,
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 128,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "8980d25da42947f995451386fc291834",
                            "abb6b0f8b412486da052e8b36b775243",
                            "c47a715d13944ae99f0927a30ea32d1b"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdtA6MYj5r9SD"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1,
            "displayHiddenColumnWithinMirror": true
        },
        {
            "id": "viwszTDUdjnzc",
            "name": "CEO Office",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "hidden": null,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "hidden": null,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "hidden": null,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "hidden": null,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": null,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "hidden": null,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "hidden": null,
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "hidden": null,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "hidden": null,
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "hidden": null,
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": null,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": null,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": null,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fldcIuJdFq5AN"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "CEO"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtuYzlyoa3Jy"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 3,
            "frozenColumnCount": 2
        },
        {
            "id": "viwfKYOdXr1wb",
            "name": "产品设计团队",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 154,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "hidden": true,
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "产品设计 Product Design"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtVXTQTMzxct"
                    },
                    {
                        "value": [
                            "Today"
                        ],
                        "fieldId": "fldcIuJdFq5AN",
                        "operator": "is",
                        "fieldType": 5,
                        "conditionId": "cdtLnp4LI3tcI"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwoXikrB7I8g",
            "name": "市场运营",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 221,
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 121,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 170,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "hidden": true,
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "市场运营"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtfOEiXqhyP8"
                    },
                    {
                        "value": [
                            "e9da9e818ef841acafae4a09b33fe510",
                            "0e13a79312354ee8a33941deceb815bc"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdt0kv1fA0iys"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwrWq9snE8ve",
            "name": "网络销售（ISR）",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "hidden": null,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "hidden": true,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 143,
                    "hidden": null,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 117,
                    "hidden": null,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 482,
                    "hidden": null,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "width": 335,
                    "hidden": null,
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "hidden": null,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "hidden": null,
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "hidden": null,
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": null,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": null,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": null,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "8980d25da42947f995451386fc291834",
                            "955dca1609ce4610a0d4ccceec21b7c8",
                            "a5e5b003fc9b40b08b5d9cfa52f9f26d"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdt5579Lx2PR7"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 4
        },
        {
            "id": "viwVscgo6zRGG",
            "name": "市场开发（MDR ）",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 139,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 129,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "width": 156,
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "hidden": true,
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "市场开发（MDR）"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "is",
                        "fieldType": 19,
                        "conditionId": "cdtyWdfObCkRu"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwoEl9UhMZX5",
            "name": "客户运营",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 154,
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 120,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": false,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fldGPXWXSjDMz"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "desc": true,
                    "fieldId": "fldGPXWXSjDMz"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "客户运营"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdt0kvQiOPvwH"
                    },
                    {
                        "value": [
                            "网络销售（ISR）"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtrdJ0bBF2jN"
                    },
                    {
                        "value": [
                            "b7aac62d932749f1af1f9cba1efe92c2",
                            "e9da9e818ef841acafae4a09b33fe510"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdtK2Aq41lFQQ"
                    },
                    {
                        "value": [
                            "大客户"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtidfpgZdg9B"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 4,
            "frozenColumnCount": 1
        },
        {
            "id": "viwT0E8kZbJoU",
            "name": "大客户咨询 Consulting",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 155,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 91,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 155,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 115,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "width": 140,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 210,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": false,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": false,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "autoSave": true,
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "大客户咨询 Consulting"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "is",
                        "fieldType": 19,
                        "conditionId": "cdtOSxq0BpQ5k"
                    },
                    {
                        "value": [
                            "6667f5487f4c4b178b1fd4901f1f8f3a",
                            "a3dcc9d0401a45118f6e247e01ec150b",
                            "934e8afb905c4d25983a6a74404ec88d"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdt6x5vqnhpH0"
                    },
                    {
                        "value": [
                            "ADR"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdttb90LErnDe"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 3,
            "frozenColumnCount": 1
        },
        {
            "id": "viwpXDgCaPyuS",
            "name": "产研-Frontend",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 139,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "hidden": true,
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "c47a715d13944ae99f0927a30ea32d1b",
                            "a5b8b044d6b04b0abb27ef315aecf94e",
                            "81129cb05275419fad66de83aec23fa3",
                            "aa3e6af7041c4907ba03889acc0b0cd1",
                            "76111f93d7a441e1bfc621d700e3c1d2",
                            "1a3ad2705d8840e89295f5c8017c0cdf",
                            "255111a777ae4196a3b37f80e3e537c5",
                            "4bd87b8e26464a27880d40dd4ad23054",
                            "5352eb38c92d4a03985c2ca5b4c9dd82",
                            "0c0da8f832d04969b19bedfdfa89829f",
                            "e9da9e818ef841acafae4a09b33fe510"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdtTAva2OkmX4"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viw47uvqcl5Av",
            "name": "All",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 142,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 225,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": false,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": false,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fldThzKPG3ZrM"
                    }
                ],
                "keepSort": true
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwpZGW5wj8em",
            "name": "清单",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 134,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "width": 141,
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": false,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": false,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "frozenColumnCount": 1
        },
        {
            "id": "viwbdJXSrOMIo",
            "name": "产研全局的副本",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 162,
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 84,
                    "hidden": true,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "hidden": true,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 168,
                    "hidden": true,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "hidden": true,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "hidden": true,
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "hidden": true,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "hidden": true,
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "hidden": true,
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "autoSave": false,
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fldcIuJdFq5AN"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": false,
                    "fieldId": "fldGPXWXSjDMz"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "产品设计"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtX48vjUoVun"
                    },
                    {
                        "value": [
                            "CEO"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtuNMvBOpLvb"
                    },
                    {
                        "value": [
                            "Front"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdto6J8o66nau"
                    },
                    {
                        "value": [
                            "Cloud"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdt7dkPzi4ucz"
                    },
                    {
                        "value": [
                            "Infra"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtbVHghSGpAq"
                    },
                    {
                        "value": [
                            "产品运营&用户运营"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtLS9RYDZn1w"
                    },
                    {
                        "value": [
                            "平台架构"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdt3RlWUKpmf9"
                    },
                    {
                        "value": [
                            "商业产品"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtPbgd5hnpay"
                    },
                    {
                        "value": [
                            "产品策划"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtc02YxDz7SJ"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwuMj3aHLEyn",
            "name": "商业工程",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 210,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 168,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "desc": false,
                    "fieldId": "fldGPXWXSjDMz"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "商业产品&产品市场 Product Marketing"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtGYCIw0sqXM"
                    },
                    {
                        "value": [
                            "平台架构"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtBvQj81WhPo"
                    },
                    {
                        "value": [
                            "渠道"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdt6cyhio1nK3"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwyKauiT8ctN",
            "name": "财务运营",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 135,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 122,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 98,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "width": 90,
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 378,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "width": 301,
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "财务"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "contains",
                        "fieldType": 19,
                        "conditionId": "cdtPTAmbpn1YM"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwj9RU2LML8j",
            "name": "空的",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 139,
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 192,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 168,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "width": 289,
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": false,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": false,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "fieldId": "fldv2ZjbAtjX6",
                        "operator": "isEmpty",
                        "fieldType": 1,
                        "conditionId": "cdtfaTYvKg0Rg"
                    },
                    {
                        "fieldId": "fldDkKtueN1LA",
                        "operator": "isEmpty",
                        "fieldType": 1,
                        "conditionId": "cdtYbq7q28lDN"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwDnwBibIvZu",
            "name": "产品策划&交互设计",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "fieldId": "fldCg7b279U1p"
                },
                {
                    "width": 122,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "width": 138,
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "width": 119,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "width": 132,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": false,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": false,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "Today"
                        ],
                        "fieldId": "fldcIuJdFq5AN",
                        "operator": "is",
                        "fieldType": 5,
                        "conditionId": "cdtr9mRy4XxHN"
                    },
                    {
                        "value": [
                            "aa3e6af7041c4907ba03889acc0b0cd1",
                            "06bf53cb580b4ad09042e1f0ac769c5d",
                            "4c8699668d504523b086787ce94387c7",
                            "d0602d9d87414c29a3bffb2fa9e80814",
                            "bed6e84b6fec4d82889f059c18fa07f4",
                            "b8557184c6b44676a04ff2c691221b91",
                            "c367d0d6f00347f2a020e7cf6e8ebc01",
                            "33004fa41e6647428b5642a0ced4258c",
                            "56732bec236c4af3a86e1f8d0f4eafea",
                            "4adb71fba75e4468855a13fc930ba9b6",
                            "ece039d976ef4f25914ee9bbaaaae47a",
                            "27fdde1dfb6143019c38455cfe8111c1",
                            "2b80290611344714b8a3bd39fcb4a098",
                            "ecb9982b2edd4079ab77dde456b7bb58",
                            "00d3451ab21f4648861c2f2558252603",
                            "ef7b19c9fc004ec38b7457f6b78024c4",
                            "7d6b87fcd4674ceb8feed06e8a114d93",
                            "0e13a79312354ee8a33941deceb815bc",
                            "e9da9e818ef841acafae4a09b33fe510",
                            "15699ee1c08a40298a61e913599074a7"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdt5i688fsZnY"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwWLFLdELwcu",
            "name": "生态渠道部",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 210,
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 134,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 103,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "hidden": true,
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "hidden": true,
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "hidden": true,
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": true,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "sortInfo": {
                "rules": [
                    {
                        "desc": true,
                        "fieldId": "fldcIuJdFq5AN"
                    }
                ],
                "keepSort": true
            },
            "groupInfo": [
                {
                    "desc": true,
                    "fieldId": "fldcIuJdFq5AN"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "生态渠道部"
                        ],
                        "fieldId": "fldGPXWXSjDMz",
                        "operator": "is",
                        "fieldType": 19,
                        "conditionId": "cdt2rOAkpECWe"
                    },
                    {
                        "value": [
                            "23a5f52cd1ee4e6abdc8a77e9b73cdec",
                            "b7aac62d932749f1af1f9cba1efe92c2",
                            "110639147e82437aba1ccde50f05c3ff",
                            "ed624281c5a4496ab6e8647bc93778c5"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdtkokSxWHDPf"
                    }
                ],
                "conjunction": "or"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1
        },
        {
            "id": "viwIgjTsE9f0j",
            "name": "维格视图",
            "rows": [],
            "type": 1,
            "columns": [
                {
                    "width": 139,
                    "fieldId": "fldCg7b279U1p",
                    "statType": 1
                },
                {
                    "width": 84,
                    "fieldId": "fldt8wjGzqYtI"
                },
                {
                    "width": 192,
                    "fieldId": "fldGPXWXSjDMz"
                },
                {
                    "width": 168,
                    "fieldId": "fldcIuJdFq5AN"
                },
                {
                    "fieldId": "fldIAVKXphPwy"
                },
                {
                    "width": 264,
                    "fieldId": "fldv2ZjbAtjX6"
                },
                {
                    "width": 289,
                    "fieldId": "fldDkKtueN1LA"
                },
                {
                    "width": 183,
                    "fieldId": "fldXgeStG7rtl"
                },
                {
                    "fieldId": "fldcbixWZPvhi"
                },
                {
                    "fieldId": "fldThzKPG3ZrM"
                },
                {
                    "fieldId": "fldzHORJNNOih"
                },
                {
                    "fieldId": "fldlb8DabKkAK"
                },
                {
                    "hidden": false,
                    "fieldId": "fldBIYbuuSEeP"
                }
            ],
            "filterInfo": {
                "conditions": [
                    {
                        "value": [
                            "33004fa41e6647428b5642a0ced4258c",
                            "4c8699668d504523b086787ce94387c7",
                            "56732bec236c4af3a86e1f8d0f4eafea",
                            "4adb71fba75e4468855a13fc930ba9b6",
                            "abb6b0f8b412486da052e8b36b775243",
                            "c367d0d6f00347f2a020e7cf6e8ebc01",
                            "12859838940c483c8341649a14b950e5",
                            "b89a6129d7aa4ce984cf8f34f475d384",
                            "0e13a79312354ee8a33941deceb815bc"
                        ],
                        "fieldId": "fldcbixWZPvhi",
                        "operator": "contains",
                        "fieldType": 23,
                        "conditionId": "cdtAPhGNNE1in"
                    },
                    {
                        "value": [
                            "Today"
                        ],
                        "fieldId": "fldThzKPG3ZrM",
                        "operator": "is",
                        "fieldType": 21,
                        "conditionId": "cdtxij9Ts43v1"
                    }
                ],
                "conjunction": "and"
            },
            "rowHeightLevel": 1,
            "frozenColumnCount": 1,
            "displayHiddenColumnWithinMirror": false
        }
    ],
    "fieldMap": {
        "fldBIYbuuSEeP": {
            "id": "fldBIYbuuSEeP",
            "name": "附件",
            "type": 6,
            "property": null
        },
        "fldCg7b279U1p": {
            "id": "fldCg7b279U1p",
            "desc": "",
            "name": "标题",
            "type": 16,
            "property": {
                "expression": "IF({fldzHORJNNOih}=BLANK(),{fldcIuJdFq5AN}&“-”&{fldcbixWZPvhi},{fldcIuJdFq5AN}&“-”&{fldzHORJNNOih})",
                "datasheetId": "dstrmVd9p6ZPMYXbXc"
            }
        },
        "fldDkKtueN1LA": {
            "id": "fldDkKtueN1LA",
            "desc": "格式是清单体：\n1. ....\n2. ....\n\n或\n\n- .....\n- .....",
            "name": "今日计划完成什么？",
            "type": 1,
            "required": true
        },
        "fldGPXWXSjDMz": {
            "id": "fldGPXWXSjDMz",
            "name": "所属团队",
            "type": 19,
            "property": {},
            "required": true
        },
        "fldIAVKXphPwy": {
            "id": "fldIAVKXphPwy",
            "name": "当天精力占比",
            "type": 18,
            "property": {
                "precision": 0
            }
        },
        "fldThzKPG3ZrM": {
            "id": "fldThzKPG3ZrM",
            "name": "创建时间",
            "type": 21,
            "property": {
                "dateFormat": 0,
                "timeFormat": 0,
                "datasheetId": "dstrmVd9p6ZPMYXbXc",
                "includeTime": true
            }
        },
        "fldXgeStG7rtl": {
            "id": "fldXgeStG7rtl",
            "desc": "工作正在面临什么挑战想跟大家同步的？ （一直都没挑战难道是工作太简单了？）\n有什么想要跟大家同步的信息？或需要大家团队协作的？",
            "name": "面临有什么挑战？需要请求支援的？",
            "type": 1
        },
        "fldcIuJdFq5AN": {
            "id": "fldcIuJdFq5AN",
            "desc": "",
            "name": "晨会日期",
            "type": 5,
            "property": {
                "autoFill": true,
                "dateFormat": 0,
                "timeFormat": 0,
                "includeTime": false
            },
            "required": true
        },
        "fldcbixWZPvhi": {
            "id": "fldcbixWZPvhi",
            "name": "创建人",
            "type": 23,
            "property": {
                "uuids": [
                    "e9da9e818ef841acafae4a09b33fe510",
                    "50855429a837492284dbb285d77896ca",
                    "49189866e85349a2a2f652500cf804ba",
                    "3be66402587e4647bde42f4782cb5656",
                    "50cbab6cb65e49518831145ea7379ca7",
                    "d9d62c578aae4a42be684f9e37a43dc2",
                    "9769074644aa41188506baeea86f2090",
                    "bf5c019052a842e7afec2367a94d39e0",
                    "27fdde1dfb6143019c38455cfe8111c1",
                    "b7aac62d932749f1af1f9cba1efe92c2",
                    "b957324791a14054986f9c44854464e4",
                    "a0ccc0a1c3be4faeb3c8c324459e57bf",
                    "b41ca32b24cd43398fbdab9395dd03e0",
                    "e65d9f6b59654d00a48ad0b53e14f8d5",
                    "def28f269bf349cda7258607bbb8f9ee",
                    "8a766617f21041279e1dd85664b76be6",
                    "7e78825636d94299a8278362d6a5b526",
                    "e8b2a91f20db4a168b4007c5a92f6e81",
                    "232d6afb8a304011803754c066301458",
                    "b3a6d217ad694717913ed730bb786e1b",
                    "adc2a9cdd80f44778ac6e32f4e42e8e2",
                    "113f836068e74a71b7c7133ba2c40785",
                    "869dd39aca9a426db28fb242407df25a",
                    "cd25a5d644d64e5b87cac6fbd6bce7bc",
                    "7d6b87fcd4674ceb8feed06e8a114d93",
                    "934e8afb905c4d25983a6a74404ec88d",
                    "16b01b70f15948929b6c6a6ec9207da6",
                    "6667f5487f4c4b178b1fd4901f1f8f3a",
                    "c420f951cc224993aa3f310376296d86",
                    "0e13a79312354ee8a33941deceb815bc",
                    "c04f56663d0c48d5aea1f7952d2c2d7b",
                    "a3dcc9d0401a45118f6e247e01ec150b",
                    "eeb620a54e2248c69c25de68e6eb668c",
                    "92481000ece4453b8210ecdd009b30aa",
                    "e37d580871cb4e31975c07cb65c94f88",
                    "5f935c8c5ffb4475ba91fb8125aa16fd",
                    "ef7b19c9fc004ec38b7457f6b78024c4",
                    "aef5a6605ea341279c811a724c1f7d26",
                    "74418ea3b23b4e06b8f30293e1fb3045",
                    "c31130492ba24f76877615138ae93f93",
                    "b12efa00b20342c5be0a6277dda2fe83",
                    "edcee07cf0fd4f0e837a296a77f9bd94",
                    "d9173074608d4c878204fd94b38c20d5",
                    "72e1442435e043899bce153ff166c516",
                    "b5ebf378a5284838b4b153fecc3cf299",
                    "1b67f95ff96441c4afd83ffdde14b2b5",
                    "697362ceb9684cf19e722d84c74dc8a7",
                    "09d19fd689d14381a6081e370d409329",
                    "fecf59caf1234267a50dc406454c2da0",
                    "5d02b5c8303e499f83dbc6ab5716ab8f",
                    "b20a693c428c4d0cb807d8b3b2c99f18",
                    "4ef4fd57ccd34fa7b626a1614c091441",
                    "489e6f58daa34040a041eb72403f3fc1",
                    "938fd14da9f243f785e57ad961f35bc6",
                    "3e2f7d835958472ab43a623f15dec64f",
                    "955dca1609ce4610a0d4ccceec21b7c8",
                    "2dc7f9021ee54e6e87d4b05051eeac93",
                    "06bf53cb580b4ad09042e1f0ac769c5d",
                    "c532004636e54c7580aff340ed3a4544",
                    "aa3e6af7041c4907ba03889acc0b0cd1",
                    "07681b9f05434029945c663c406c1caf",
                    "bcf86bc2497a4361939437f257a91df1",
                    "c367d0d6f00347f2a020e7cf6e8ebc01",
                    "81129cb05275419fad66de83aec23fa3",
                    "33004fa41e6647428b5642a0ced4258c",
                    "d0602d9d87414c29a3bffb2fa9e80814",
                    "bbcbf7223e0e41db87d454542afed7f4",
                    "23a5f52cd1ee4e6abdc8a77e9b73cdeb",
                    "4c8699668d504523b086787ce94387c7",
                    "17e07cf00d6d447fb2225e9db547f8c8",
                    "dd95efceb7d14809a83792a7d70b50dd",
                    "9166bea35d79456994b99956dbfabcb9",
                    "ece039d976ef4f25914ee9bbaaaae47a",
                    "806784677c814c9db93972c023f7f4d4",
                    "76111f93d7a441e1bfc621d700e3c1d2",
                    "4ae7d0eb0a824496b13a692489190d55",
                    "bed6e84b6fec4d82889f059c18fa07f4",
                    "56732bec236c4af3a86e1f8d0f4eafea",
                    "88ebe5c2489e4eb8bcd55fb454ff32ea",
                    "5b8686771f1e48a7b94da20eaf280d4d",
                    "4adb71fba75e4468855a13fc930ba9b6",
                    "a5b8b044d6b04b0abb27ef315aecf94e",
                    "d240a6c915ea41249722bff53b0af2c1",
                    "b8557184c6b44676a04ff2c691221b91",
                    "1ef3dbdd698e4557acb15e50d5ef7852",
                    "d2aa673f873f45439128bd5e52835477",
                    "c47a715d13944ae99f0927a30ea32d1b",
                    "dccfb205937e41e7a43da2e20164b3f8",
                    "6117483ce3e341cfa5c8fe6d0e02cd46",
                    "abb6b0f8b412486da052e8b36b775243",
                    "8f5a563cad7c45ceaf314770a8826840",
                    "f5f141c56086423bb7919a2060f52fe8",
                    "bf298308635c42f79a338013bda92b98",
                    "e0e07b0cf26142d0b2d43ff61cea5493",
                    "809fe43cf6a747d7b206e33748792940",
                    "42eb6f658f924cea811752bf2d571cee",
                    "ed624281c5a4496ab6e8647bc93778c5",
                    "e4ef619bca514d64993528432d0eace8",
                    "0c4565496bb3439dac52a3ef15944d26",
                    "62bda04135074b78be76fd4a8cfd01f2",
                    "888e483c713c4aaeadd2ab22bae903f5",
                    "cf60398ae9f74777acbdddfa84cc2b4d",
                    "81a56ba5748a44299550afabc396f714",
                    "3c64f2254bbb47fc91b1638121560926",
                    "2e1f4ff70d7840fbb4b683263c42c251",
                    "33dfa2a3abb145c28416d8baedf0bf26",
                    "142393c2acd44f398864bdfe32707826",
                    "38575d399ae74f8bbc2a1d9d51adf357",
                    "110639147e82437aba1ccde50f05c3ff",
                    "adb90c2ee1f34caeb6b75d5da578961d",
                    "335acbbbe938436588af6bced6a0599a",
                    "15bfdd1e5aef45d8bca07f681b5d9232",
                    "12859838940c483c8341649a14b950e5",
                    "a91f0313fcc748baa2424f813db2392b",
                    "23a5f52cd1ee4e6abdc8a77e9b73cdec",
                    "1345c2d50ff44e2a8c7fa8b3e9a57525",
                    "464ef0dc8995468ba98c5bb53ee7a24e",
                    "8823b323166b4cc186a18197ab0458a5",
                    "1a3ad2705d8840e89295f5c8017c0cdf",
                    "031421e092ac430ab9296da45adfd803",
                    "40bb452436d34ab4a7bb7f575fdf3b9e",
                    "96e24eef131840d795cf2570eb49a3ed",
                    "8980d25da42947f995451386fc291834",
                    "ae69545c05a5447c8054af6f94363ca5",
                    "c0e8be9a0c37446fa41e6a146905541e",
                    "e42e6e891baa42ccaa1e0f316d7925d3",
                    "eef91dfe57c342f586dd59eead8412c1",
                    "2cd0727fe4f14bcc87bfba0cf4e3192f",
                    "b7ab89f1428d4aee99d24586db2b954b",
                    "e49dd7ee806c4b84b74057215c8a3c14",
                    "e96641e7356645dc80ce89fdc0c22bfc",
                    "00d3451ab21f4648861c2f2558252603",
                    "13afd5b930f644b69e79de4f9643887a",
                    "7d1f24f2db124c9bb4ab7a38b9bcf667",
                    "aab914e6db39427caa5f20fc7572e086",
                    "5dee6a81f6fe408f934312a12984e462",
                    "e1e2f6b58352402b96dccfb56509649e",
                    "4353132603204cd28b964364802d1f7b",
                    "1d2b180735364f998c8f82f46b464778",
                    "91cab8f3c0c840a892df3930fc9b6dde",
                    "5f6306e0c28b42febe28a002f84960c8",
                    "c9863e875fe64331a9e6e6ff61ba407c",
                    "a8f27b8ed27f459cb4512158c2052a6f",
                    "f891ba9bd43d47089dca81a2163f8047",
                    "411e0a7b9cfc4d2aa637e4f391f11b1d",
                    "b89a6129d7aa4ce984cf8f34f475d384",
                    "cf3189d22b234b00ba37b566a74572e0",
                    "f95a1585ad194809aea2f209f1aee26f",
                    "f338683905004610b6006e2a0b8eccc8",
                    "a244dc4ee9bc41ce8a9e7a8f9ec61ea6",
                    "2b80290611344714b8a3bd39fcb4a098",
                    "a5e5b003fc9b40b08b5d9cfa52f9f26d",
                    "01cf8a2164bd4017bf2ea926d1814005",
                    "71b468470b9e46df9d50c9840bcd331d",
                    "3abacc827b73493d9e01818ffca4ca2c",
                    "5bf58d1f2dc2495093744d49beb34ee2",
                    "254cef9eeef54e8ea85de49391947ca9",
                    "809be60903c74daf870bdbdc601d7f7b",
                    "75e1bb975ca64210a4b3944ee87fa280",
                    "6e8b3e2b938949aaa84c3a6609a55e2b",
                    "9826625b53af4ea1ba2627683b188575",
                    "176384fe0cfd4b03afe6e574eccf562a",
                    "45d4037c64ac442d88d45c971a9565c3",
                    "b870bc181ad5478dacb4b3aa1229c7d1",
                    "255111a777ae4196a3b37f80e3e537c5",
                    "4bd87b8e26464a27880d40dd4ad23054",
                    "222951e46ad848f7be88011aea47c084",
                    "5352eb38c92d4a03985c2ca5b4c9dd82",
                    "5b168ab4e0a14725ae58f60dda9c2288",
                    "c0233f3a70f04824b4b2e7c538796d30",
                    "082476dc25834402ba979d62804480cd",
                    "93ae987a39104019a8718f318e7b3073",
                    "0c0da8f832d04969b19bedfdfa89829f",
                    "0a6622aa1f40437ab1fa2ed30afc1ee4",
                    "802168e06531478db5eb90cef583249d",
                    "8e298cf6b4e24ba6b08292723ffcf07b",
                    "15699ee1c08a40298a61e913599074a7",
                    "df53cdd4baa0409ab810d6379b44f6d2",
                    "3be4e677d92b4ebbb7d897aed8e49f92"
                ],
                "datasheetId": "dstrmVd9p6ZPMYXbXc"
            }
        },
        "fldlb8DabKkAK": {
            "id": "fldlb8DabKkAK",
            "desc": "",
            "name": "计划人",
            "type": 16,
            "property": {
                "expression": "IF({fldzHORJNNOih}=BLANK(),CONCATENATE({fldcbixWZPvhi}),CONCATENATE({fldzHORJNNOih}))",
                "datasheetId": "dstrmVd9p6ZPMYXbXc"
            }
        },
        "fldt8wjGzqYtI": {
            "id": "fldt8wjGzqYtI",
            "desc": "",
            "name": "周期",
            "type": 16,
            "property": {
                "expression": "year({fldcIuJdFq5AN})&\"-\"&REPT(“0”，2-LEN(WEEKNUM({fldcIuJdFq5AN})))&WEEKNUM({fldcIuJdFq5AN})",
                "datasheetId": "dstrmVd9p6ZPMYXbXc"
            }
        },
        "fldv2ZjbAtjX6": {
            "id": "fldv2ZjbAtjX6",
            "desc": "格式是清单体：（📢：周一写上周总结）\n1. ....\n2. ....\n\n或\n\n- .....\n- .....\n\n--------\n\n# 如果你是市场、运营，AI将会整理数据策略跟进情况，请按这个格式填写\n\n## 数据策略\n>仅供参考写与自己相关的指标，也可以调整成自己的\n\n-organic traffic: 123->321,下一步策略\n-organic keywords: 123->321,下一步策略\n-YouTube ads click rate yesterday: 123->321,下一步策略\n-paid traffic leads: 123->321,下一步策略\n-paid traffic click to leads rate: 123->321,下一步策略\n-backlinks: 123->321,下一步策略\n-authority: 123->321,下一步策略\n-returning users: 数字,下一步策略\n-mailchimp subcribers: 数字,下一步策略\n-rewardful affiliate partners: 数字,下一步策略\n\n## 工作事项\n- 任务1\n- 任务2\n\n-------\n\n## 如果你是销售、商务，AI将会整理客户跟进情况，请按这个格式填写\n\n> 原则基础为，要填够50个客户/代理商/合作伙伴，其中20%/10个为新的：\n\n## 事项：\n- 任务1\n- 任务2\n\n## 新客户：\n- 客户名1：状态, 下一步行动\n- 客户名2：状态, 下一步行动\n\n## 老客户（成交过）：\n- 客户名1：状态, 下一步行动\n- 客户名2：状态, 下一步行动\n\n## 新代理商\n- 代理商1：状态，下一步行动，准备签约\n- 代理商2：状态，下一步行动，准备签约\n\n## 老代理商（已签约）\n- 代理商1：状态，下一步行动\n  - 代理客户：状态\n- 代理商2：状态，下一步行动\n  - 代理客户：状态\n\n## 新合作伙伴：\n- 合作伙伴1：状态, 下一步行动\n- 合作伙伴1：状态, 下一步行动\n\n## 老合作伙伴（有过线索或合作）：\n- 合作伙伴1：状态, 下一步行动\n  - 伙伴客户1: 状态\n- 合作伙伴2：状态, 下一步行动\n  - 伙伴客户2: 状态",
            "name": "昨日完成了什么？",
            "type": 1,
            "required": true
        },
        "fldzHORJNNOih": {
            "id": "fldzHORJNNOih",
            "name": "提交人",
            "type": 13,
            "property": {
                "isMulti": false,
                "unitIds": [
                    "1542696089951338498",
                    "1491242419907788805",
                    "1529285159690866690",
                    "1236159968508383235",
                    "1465513160016584707",
                    "1483259072447569921",
                    "1528922630518657025",
                    "1513711453331079170",
                    "1514525957665329153",
                    "1465513160016584712",
                    "1524937874233024514",
                    "5f935c8c5ffb4475ba91fb8125aa16fd",
                    "b89a6129d7aa4ce984cf8f34f475d384",
                    "1236155491608956930",
                    "9166bea35d79456994b99956dbfabcb9",
                    "1554012878767112193",
                    "254cef9eeef54e8ea85de49391947ca9",
                    "5dee6a81f6fe408f934312a12984e462",
                    "1549232675800350722",
                    "1465513160016584709",
                    "a5e5b003fc9b40b08b5d9cfa52f9f26d",
                    "fecf59caf1234267a50dc406454c2da0",
                    "1267292467797618690",
                    "1236159955090804739",
                    "1695976862359830529",
                    "1236159971893186562",
                    "1397369375711797250",
                    "1415553251672166401",
                    "1697438699532369921",
                    "1460079077529788417",
                    "1455014553795432450",
                    "1399570202138824705"
                ],
                "shouldSendMsg": false
            }
        }
    },
    "widgetPanels": [
        {
            "id": "wpl9NYHzpbs5L",
            "widgets": [
                {
                    "id": "wdt2XwVNMYbP8obAdL",
                    "height": 6.2
                },
                {
                    "id": "wdtdupnEtrWsZs5zFN",
                    "height": 6.2
                },
                {
                    "y": 9007199254740991,
                    "id": "wdthAoTzxmb0JTN6a4",
                    "height": 6.2
                },
                {
                    "y": 9007199254740991,
                    "id": "wdtgqT0BJ8qoWrKold",
                    "height": 6.2
                }
            ]
        }
    ],
    "archivedRecordIds": []
}
"#;