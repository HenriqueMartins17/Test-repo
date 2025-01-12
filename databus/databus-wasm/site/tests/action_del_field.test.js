const {action_delete_field} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForDelFieldAction} = require('./data/action_del_field_mock');
it('test del field in the end', () => {
    const result_base = [
      {
          "n": "LD",
          "p": [
              "meta",
              "views",
              0,
              "columns",
              3
          ],
          "ld": {
              "hidden": false,
              "fieldId": "fldZ5s95v7tE6"
          }
      },
      {
          "n": "OD",
          "p": [
              "meta",
              "fieldMap",
              "fldZ5s95v7tE6"
          ],
          "od": {
              "id": "fldZ5s95v7tE6",
              "name": "多行文本",
              "type": 1,
            //   "property": null rust这边null不显示了
          }
      }
    ];

    const snapshot = MockDataForDelFieldAction;
    const payload = {
      fieldId: 'fldZ5s95v7tE6',
      datasheetId: 'dstckW8kzoZiUDMR76',
      viewId: 'viwuTxAXZp9fR'
    };
    const result = action_delete_field(snapshot, payload);
    // console.log(111);
    // console.log(result);

    expect(result).toEqual(result_base);
});

