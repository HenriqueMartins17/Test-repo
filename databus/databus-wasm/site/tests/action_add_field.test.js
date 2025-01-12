const {action_add_field} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForAddFieldAction} = require('./data/action_add_field_mock');
it('test add field in the end', () => {
    const result_base = [
        {
            n: 'LI',
            p: [ 'meta', 'views', 0, 'columns', 3 ],
            li: { fieldId: 'fldFlJeRUPUb2', hidden: false }
        },
        {
            n: 'OI',
            p: [ 'meta', 'fieldMap', 'fldFlJeRUPUb2' ],
            oi: { id: 'fldFlJeRUPUb2', name: 'test', type: 1, property: null }
        }
    ];

    const snapshot = MockDataForAddFieldAction;
    const options = {
        data: {
          id: 'fldFlJeRUPUb2',
          name: 'test',
          type: 1,
          property: null
        },
        viewId: 'viwuTxAXZp9fR',
        index: 3,
        fieldId: undefined,
        offset: undefined,
        hiddenColumn: undefined,
      };
      const field = {
        id: 'fldFlJeRUPUb2',
        name: 'test',
        type: 1,
        property: null
      };
      const payload = {
        viewId: options && options.viewId,
        index: options && options.index,
        fieldId: options && options.fieldId,
        offset: options && options.offset,
        hiddenColumn: options && options.hiddenColumn,
        field,
      };
    const result = action_add_field(snapshot, payload);

    expect(result).toEqual(result_base);
});

