const {action_del_view} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForDelViewAction} = require('./data/action_del_view_mock');
it('test del view in the end', () => {
    const result_base =
    {
        "n": "LD",
        "p": [
            "meta",
            "views",
            0
        ],
        "ld": {
            "id": "viw0qzZyOwofg",
            "name": "维格视图 2",
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
            "type": 1,
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
            "rowHeightLevel": 1,
            "frozenColumnCount": 1,
            "displayHiddenColumnWithinMirror": false
        }
    };

    const snapshot = MockDataForDelViewAction;
    const payload = {
        viewId: "viw0qzZyOwofg",
    };
    const result = action_del_view(snapshot, payload);
    // console.log(111);
    // console.log(result);

    expect(result).toEqual(result_base);
});

