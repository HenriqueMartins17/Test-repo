console.log('test-node.js');
const {add_tn, json0_apply} = require('./pkg/nodejs/databus_wasm');
console.log(add_tn(1, 2));

const snapshot_str = `{
        "meta": {
            "views": [],
            "fieldMap": {}
        },
        "recordMap": {
            "recnMv9BvatQh": {
                "id": "recnMv9BvatQh",
                "data": {}
            }
        },
        "datasheetId": "dst9zyUXiLDYjowMvz"
    }`;
const op = `[{
        "n": "OI",
        "p": [
            "recordMap",
            "recnMv9BvatQh",
            "data",
            "fldGzWrhZdznD"
        ],
        "oi": [
            {
                "type": 1,
                "text": "asdasd"
            }
        ]
    }]`;

const result = json0_apply(snapshot_str, op);
console.log(result);