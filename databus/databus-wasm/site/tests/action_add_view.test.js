const {action_add_view} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForAddViewAction} = require('./data/action_add_view_mock');
it('test add view in the end', () => {
    const result_base =
    {
        "n": "LI",
        "p": [
            "meta",
            "views",
            1
        ],
        "li": {
            "id": "viw0qzZyOwofg",
            "name": "维格视图 2",
            "type": 1,
            "rowHeightLevel": 1,
            "columns": [
                {
                    "fieldId": "fldDwqb8JScnD",
                    "statType": 1
                },
                {
                    "fieldId": "fldJ0f2U6C7E1"
                },
                {
                    "fieldId": "fldBMmUiHGU6C"
                }
            ],
            "rows": [
                {
                    "recordId": "recKf2HlpY0eH"
                },
                {
                    "recordId": "rec1Fx3Jiy4bD"
                },
                {
                    "recordId": "recnSX92TwjDm"
                }
            ],
            "frozenColumnCount": 1,
            "displayHiddenColumnWithinMirror": false
        }
    };

    const snapshot = MockDataForAddViewAction;
    const view = {
        "id": "viw0qzZyOwofg",
        "name": "维格视图 2",
        "type": 1,
        "rowHeightLevel": 1,
        "columns": [
            {
                "fieldId": "fldDwqb8JScnD"
            },
            {
                "fieldId": "fldJ0f2U6C7E1"
            },
            {
                "fieldId": "fldBMmUiHGU6C"
            }
        ],
        "rows": [
            {
                "recordId": "recKf2HlpY0eH"
            },
            {
                "recordId": "rec1Fx3Jiy4bD"
            },
            {
                "recordId": "recnSX92TwjDm"
            }
        ],
        "frozenColumnCount": 1,
        "displayHiddenColumnWithinMirror": false
    };
    const startIndex = 1;
    const payload = {
        startIndex,
        view
    };
    const result = action_add_view(snapshot, payload);
    // console.log(111);
    // console.log(result);

    expect(result).toEqual(result_base);
});

