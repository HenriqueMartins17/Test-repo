const {action_move_view} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForMoveViewAction} = require('./data/action_move_view_mock');
it('test move view in the end', () => {
    const result_base =
    {
        "n": "LM",
        "p": [
            "meta",
            "views",
            1
        ],
        "lm": 0
    }

    const snapshot = MockDataForMoveViewAction;
    const payload = {
        viewId: "viw0qzZyOwofg",
        target: 0
    };
    const result = action_move_view(snapshot, payload);
    // console.log(111);
    // console.log(result);

    expect(result).toEqual(result_base);
});

