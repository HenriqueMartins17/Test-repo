#[cfg(test)]
pub const MOCK_DATASHEET_MAP_JSON: &str = r#"{
    "dst1": {
        "snapshot": {
            "meta": {
                "fieldMap": {
                    "fld1": {
                        "id": "fld1",
                        "name": "field 1",
                        "type": 1,
                        "property": null
                    },
                    "fld2": {
                        "id": "fld2",
                        "name": "field 2",
                        "type": 4,
                        "property": {
                            "options": [
                                {
                                    "id": "opt1",
                                    "name": "option 1",
                                    "color": 0
                                },
                                {
                                    "id": "opt2",
                                    "name": "option 2",
                                    "color": 1
                                },
                                {
                                    "id": "opt3",
                                    "name": "option 3",
                                    "color": 2
                                }
                            ],
                            "defaultValue": [
                                "opt2",
                                "opt1"
                            ]
                        }
                    }
                },
                "views": [
                    {
                        "id": "viw1",
                        "type": 1,
                        "columns": [
                            {
                                "fieldId": "fld1"
                            },
                            {
                                "fieldId": "fld2"
                            }
                        ],
                        "frozenColumnCount": 1,
                        "name": "view 1",
                        "rows": [
                            {
                                "recordId": "rec1"
                            },
                            {
                                "recordId": "rec2"
                            },
                            {
                                "recordId": "rec3"
                            },
                            {
                                "recordId": "rec4"
                            },
                            {
                                "recordId": "rec5"
                            }
                        ]
                    },
                    {
                        "id": "viw2",
                        "type": 1,
                        "columns": [
                            {
                                "fieldId": "fld1"
                            },
                            {
                                "fieldId": "fld2",
                                "hidden": true
                            }
                        ],
                        "frozenColumnCount": 1,
                        "name": "view 2",
                        "rows": [
                            {
                                "recordId": "rec2"
                            },
                            {
                                "recordId": "rec3"
                            },
                            {
                                "recordId": "rec5"
                            },
                            {
                                "recordId": "rec1"
                            },
                            {
                                "recordId": "rec4"
                            }
                        ]
                    },
                    {
                        "id": "viw3",
                        "type": 1,
                        "columns": [
                            {
                                "fieldId": "fld1"
                            },
                            {
                                "fieldId": "fld2"
                            }
                        ],
                        "frozenColumnCount": 1,
                        "name": "view 3",
                        "rows": [
                            {
                                "recordId": "rec3"
                            },
                            {
                                "recordId": "rec1"
                            },
                            {
                                "recordId": "rec2"
                            },
                            {
                                "recordId": "rec6"
                            },
                            {
                                "recordId": "rec4"
                            }
                        ]
                    }
                ]
            },
            "recordMap": {
                "rec1": {
                    "id": "rec1",
                    "data": {
                        "fld1": [
                            {
                                "type": 1,
                                "text": "text 1"
                            }
                        ],
                        "fld2": [
                            "opt2",
                            "opt1"
                        ]
                    },
                    "commentCount": 0
                },
                "rec2": {
                    "id": "rec2",
                    "data": {
                        "fld1": [
                            {
                                "type": 1,
                                "text": "text 2"
                            }
                        ],
                        "fld2": [
                            "opt1"
                        ]
                    },
                    "commentCount": 0
                },
                "rec3": {
                    "id": "rec3",
                    "data": {
                        "fld1": [
                            {
                                "type": 1,
                                "text": "text 3"
                            }
                        ],
                        "fld2": []
                    },
                    "commentCount": 1,
                    "comments": [
                        {
                            "revision": 7,
                            "createdAt": 1669886283547,
                            "commentId": "cmt1001",
                            "unitId": "100004",
                            "commentMsg": {
                                "type": "dfs",
                                "content": "foo",
                                "html": "foo"
                            }
                        }
                    ]
                },
                "rec4": {
                    "id": "rec4",
                    "data": {
                        "fld1": [
                            {
                                "type": 1,
                                "text": "text 4"
                            }
                        ],
                        "fld2": [
                            "opt3",
                            "opt2",
                            "opt1"
                        ]
                    },
                    "commentCount": 0
                },
                "rec5": {
                    "id": "rec5",
                    "data": {
                        "fld1": [
                            {
                                "type": 1,
                                "text": "text 5"
                            }
                        ],
                        "fld2": [
                            "opt3"
                        ]
                    },
                    "commentCount": 0
                }
            },
            "datasheetId": "dst1"
        },
        "datasheet": {
            "id": "dst1",
            "name": "datasheet 1",
            "description": "this is datasheet 1",
            "parentId": "",
            "icon": "",
            "nodeShared": false,
            "nodePermitSet": false,
            "spaceId": "spc1",
            "role": "",
            "permissions": {},
            "revision": 12
        },
        "fieldPermissionMap": {
            "fld1": {
                "role": "editor",
                "setting": {
                    "formSheetAccessible": true
                },
                "permission": {
                    "editable": true,
                    "readable": true
                },
                "manageable": true
            },
            "fld2": {
                "role": "editor",
                "setting": {
                    "formSheetAccessible": true
                },
                "permission": {
                    "editable": true,
                    "readable": true
                },
                "manageable": true
            }
        }
    }
}
"#;

#[cfg(test)]
pub const MOCK_DATASHEET_MAP_COMMAND_MANAGER_JSON: &str = r#"{
    "dst1": {
        "snapshot": {
          "meta": {
            "fieldMap": {
              "fld1": {
                "id": "fld1",
                "name": "field 1",
                "type": 1,
                "property": null
              },
              "fld2": {
                "id": "fld2",
                "name": "field 2",
                "type": 4,
                "property": {
                  "options": [
                    {
                      "id": "opt1",
                      "name": "option 1",
                      "color": 0
                    },
                    {
                      "id": "opt2",
                      "name": "option 2",
                      "color": 1
                    },
                    {
                      "id": "opt3",
                      "name": "option 3",
                      "color": 2
                    }
                  ],
                  "defaultValue": [
                    "opt2",
                    "opt1"
                  ]
                }
              },
              "fld3": {
                "id": "fld3",
                "name": "Field 3",
                "type": 13,
                "property": {
                  "isMulti": true,
                  "shouldSendMsg": false,
                  "unitIds": [
                    "100000",
                    "100001",
                    "100002"
                  ]
                }
              }
            },
            "views": [
              {
                "id": "viw1",
                "type": 1,
                "columns": [
                  {
                    "fieldId": "fld1"
                  },
                  {
                    "fieldId": "fld2"
                  }
                ],
                "frozenColumnCount": 1,
                "name": "view 1",
                "rows": [
                  {
                    "recordId": "rec1"
                  },
                  {
                    "recordId": "rec2"
                  },
                  {
                    "recordId": "rec3"
                  }
                ]
              },
              {
                "id": "viw2",
                "type": 1,
                "columns": [
                  {
                    "fieldId": "fld1"
                  },
                  {
                    "fieldId": "fld2",
                    "hidden": true
                  }
                ],
                "frozenColumnCount": 1,
                "name": "view 1",
                "rows": [
                  {
                    "recordId": "rec2"
                  },
                  {
                    "recordId": "rec3"
                  },
                  {
                    "recordId": "rec1"
                  }
                ]
              }
            ]
          },
          "recordMap": {
            "rec1": {
              "id": "rec1",
              "data": {
                "fld1": [
                  {
                    "type": 1,
                    "text": "text 1"
                  }
                ],
                "fld2": [
                  "opt2",
                  "opt1"
                ]
              },
              "commentCount": 0
            },
            "rec2": {
              "id": "rec2",
              "data": {
                "fld1": [
                  {
                    "type": 1,
                    "text": "text 2"
                  }
                ],
                "fld2": [
                  "opt1"
                ]
              },
              "commentCount": 0
            },
            "rec3": {
              "id": "rec3",
              "data": {
                "fld1": [
                  {
                    "type": 1,
                    "text": "text 3"
                  }
                ],
                "fld2": []
              },
              "commentCount": 0
            }
          },
          "datasheetId": "dst1"
        },
        "datasheet": {
          "id": "dst1",
          "name": "datasheet 1",
          "description": "this is datasheet 1",
          "parentId": "",
          "icon": "",
          "nodeShared": false,
          "nodePermitSet": false,
          "spaceId": "spc1",
          "role": "",
          "permissions": {},
          "revision": 1
        }
      },
      "dst2": {
        "snapshot": {
          "meta": {
            "fieldMap": {
              "fld2-1": {
                "id": "fld2-1",
                "name": "Field 1",
                "type": 1,
                "property": null
              },
              "fld2-2": {
                "id": "fld2-2",
                "name": "Field 2",
                "type": 7,
                "property": {
                  "foreignDatasheetId": "dst3",
                  "brotherFieldId": "fld3-2"
                }
              }
            },
            "views": [
              {
                "id": "viw1",
                "type": 1,
                "columns": [
                  {
                    "fieldId": "fld2-1"
                  },
                  {
                    "fieldId": "fld2-2"
                  }
                ],
                "frozenColumnCount": 1,
                "name": "view 1",
                "rows": [
                  {
                    "recordId": "rec2-1"
                  },
                  {
                    "recordId": "rec2-2"
                  }
                ]
              }
            ]
          },
          "recordMap": {
            "rec2-1": {
              "id": "rec2-1",
              "data": {
                "fld2-1": [
                  {
                    "type": 1,
                    "text": "text 1"
                  }
                ],
                "fld2-2": []
              },
              "commentCount": 0
            },
            "rec2-2": {
              "id": "rec2-2",
              "data": {
                "fld2-1": [
                  {
                    "type": 1,
                    "text": "text 2"
                  }
                ],
                "fld2-2": [
                  "rec3-1"
                ]
              },
              "commentCount": 0
            }
          },
          "datasheetId": "dst2"
        },
        "datasheet": {
          "id": "dst2",
          "name": "datasheet 2",
          "description": "this is datasheet 2",
          "parentId": "",
          "icon": "",
          "nodeShared": false,
          "nodePermitSet": false,
          "spaceId": "spc1",
          "role": "",
          "permissions": {},
          "revision": 2
        }
      },
      "dst3": {
        "snapshot": {
          "meta": {
            "fieldMap": {
              "fld3-2": {
                "id": "fld3-2",
                "name": "3 my field 2",
                "type": 7,
                "property": {
                  "foreignDatasheetId": "dst2",
                  "brotherFieldId": "fld2-2"
                }
              },
              "fld3-1": {
                "id": "fld3-1",
                "name": "3-Field 1",
                "type": 2,
                "property": {
                  "precision": 1
                }
              }
            },
            "views": [
              {
                "id": "viw1",
                "type": 1,
                "columns": [
                  {
                    "fieldId": "fld3-1"
                  },
                  {
                    "fieldId": "fld3-2"
                  }
                ],
                "frozenColumnCount": 1,
                "name": "view 1",
                "rows": [
                  {
                    "recordId": "rec3-1"
                  },
                  {
                    "recordId": "rec3-2"
                  }
                ]
              }
            ]
          },
          "recordMap": {
            "rec3-1": {
              "id": "rec3-1",
              "data": {
                "fld3-1": [
                  {
                    "type": 1,
                    "text": "text 1"
                  }
                ],
                "fld3-2": [
                  "rec2-2"
                ]
              },
              "commentCount": 0
            },
            "rec3-2": {
              "id": "rec3-2",
              "data": {
                "fld3-1": [
                  {
                    "type": 1,
                    "text": "text 2"
                  }
                ],
                "fld3-2": []
              },
              "commentCount": 0
            }
          },
          "datasheetId": "dst3"
        },
        "datasheet": {
          "id": "dst3",
          "name": "datasheet 3",
          "description": "this is datasheet 3",
          "parentId": "",
          "icon": "",
          "nodeShared": false,
          "nodePermitSet": false,
          "spaceId": "spc1",
          "role": "",
          "permissions": {},
          "revision": 3
        }
      }
}
"#;
