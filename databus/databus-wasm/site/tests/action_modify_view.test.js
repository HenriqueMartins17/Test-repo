const {action_modify_view} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForAction} = require('./data/action_modify_view_mock');
it('test modify view in the begin', () => {
    const result_base =
    [
        {
            "n": "OR",
            "p": [
                "meta",
                "views",
                0,
                "name"
            ],
            "od": "Grid view",
            "oi": "modifyView2Action_test"
        }
    ];

    const snapshot = MockDataForAction;
    const payload = {
        viewId: "viwtAblNKK8Le",
        key: "name",
        value: "modifyView2Action_test"
    };
    const result = action_modify_view(snapshot, payload);

    expect(result).toEqual(result_base);
});

