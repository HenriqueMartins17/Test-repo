const { json0_apply, json0_apply_str } = require('../pkg/nodejs/databus_wasm');
const assert = require('assert');
function measureExecutionTime(func, iterations) {
    const startTime = new Date();
    for (let i = 0; i < iterations; i++) {
        func();
    }
    const endTime = new Date();
    return endTime - startTime;
}

const snapshot_str = `{
    "meta": {
        "views": [],
        "fieldMap": {}
    },
    "recordMap": {
        "recnMv9BvatQh": {
            "id": "recnMv9BvatQh",
            "data": {}
        },
        "recnMv9BvatQh1": {
            "id": "recnMv9BvatQh",
            "data": {}
        },
        "recnMv9BvatQh2": {
            "id": "recnMv9BvatQh",
            "data": {}
        },
        "recnMv9BvatQh3": {
            "id": "recnMv9BvatQh",
            "data": {}
        },
        "recnMv9BvatQh4": {
            "id": "recnMv9BvatQh",
            "data": {}
        }
    },
    "datasheetId": "dst9zyUXiLDYjowMvz"
}`;
const snapshot = JSON.parse(snapshot_str);

const op_str = `[{
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
}]`
const op = JSON.parse(op_str);

const resultFuncWASM = () => {
    const result = json0_apply_str(snapshot_str, op_str);
};

const iterations = [1000, 2000, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000];

const {type} = require('./node_modules/ot-json0')
const resultFuncJS = () => {
    const result = type.apply(snapshot, op);
};

assert(resultFuncWASM() === resultFuncJS());
console.log("consistency test passed...")

console.log('now start json0 benchmark...');


iterations.forEach(iteration => {
    const executionTime = measureExecutionTime(resultFuncWASM, iteration);
    const executionTime2 = measureExecutionTime(resultFuncJS, iteration);
    console.log(`Iterations: ${iteration}, wasm: ${executionTime} ms, js: ${executionTime2} ms`);

});
