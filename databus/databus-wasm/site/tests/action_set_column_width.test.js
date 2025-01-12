const {action_set_column_width} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForAction} = require('./data/action_set_column_width_mock');
it('test setColumnWidth2Action', () => {
    const result_base =
    {
        "n": "OR",
        "p": [
            "meta",
            "views",
            0,
            "columns",
            0,
            "width"
        ],
        // "od": undefined,
        "od": null,
        "oi": 266
    }

    const snapshot = MockDataForAction;
    const payload = {
        viewId: "viwtAblNKK8Le",
        fieldId: "fld484MhkgYl9",
        width: 266
    };
    const result = action_set_column_width(snapshot, payload);

    expect(result).toEqual(result_base);
});

