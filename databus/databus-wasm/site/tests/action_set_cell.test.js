const {add_tn, action_set_cell_in_node_js, action_set_cell,json0_seri, json0_inverse} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForAction} = require('./data/action_mock');
it('adds 1 + 2 to equal 32', () => {
    expect(add_tn(1, 2)).toBe(3);
});
it('test record map have no data', () => {
    const snapshot = MockDataForAction;
    const payload = {recordId: "x", fieldId: "x", value: null};
    const result = action_set_cell(snapshot, payload);

    expect(result === null).toBe(true);
});


it('test no diff to update', () => {
    const snapshot = MockDataForAction;
    const payload = {recordId: "reclx3H5CZbZP", fieldId: "fldmHjmSjZxVn", value: [
            {
                "text": "说的是",
                "type": 1
            }
        ]};
    const result = action_set_cell(snapshot, payload);

    expect(result === null).toBe(true);

    payload.fieldId = "xxx";
    payload.value = null;
    const result2 = action_set_cell(snapshot, payload);
    expect(result2 === null).toBe(true);
});

it('test not modify', () => {
    const result_base = {
        "n": "OD",
        "od": [{"text": "说的是", "type": 1}],
        "p": ["recordMap", "reclx3H5CZbZP", "data", "fldmHjmSjZxVn"]
    };
    const snapshot = MockDataForAction;
    const payload = {recordId: "reclx3H5CZbZP", fieldId: "fldmHjmSjZxVn", value: null};
    const result = action_set_cell(snapshot, payload);

    expect(result !== null).toBe(true);
    expect(result).toEqual(result_base);

    payload.value = [];
    const result2 = action_set_cell(snapshot, payload);
    expect(result2 !== null).toBe(true);
    expect(result2).toEqual(result_base);
});
it('test add data', () => {
    const result_base = {
        "n": "OI",
        "oi": ["reciZgdFWE4eC"],
        "p": ["recordMap", "recthZXaIAaOW", "data", "fldpYxbYNp5L4"]
    };
    const snapshot = MockDataForAction;
    const payload = {recordId: "recthZXaIAaOW", fieldId: "fldpYxbYNp5L4", value: ["reciZgdFWE4eC"]};
    const result = action_set_cell(snapshot, payload);

    expect(result !== null).toBe(true);
    expect(result).toEqual(result_base);

});

it('test replace data', () => {
    const result_base = {"n":"OR","od":[{"text":"说的是","type":1}],"oi":[{"text":"说的是你吗","type":1}],
        "p": ["recordMap", "reclx3H5CZbZP", "data", "fldmHjmSjZxVn"]};
    const snapshot = MockDataForAction;
    const payload = {recordId: "reclx3H5CZbZP", fieldId: "fldmHjmSjZxVn", value: [
            {
                "text": "说的是你吗",
                "type": 1
            }
        ]};
    const result = action_set_cell(snapshot, payload);

    expect(result !== null).toBe(true);
    expect(result).toEqual(result_base);

});
