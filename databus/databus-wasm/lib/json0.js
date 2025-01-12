const databus = require('../pkg/nodejs/databus_wasm');

const databusInstance = new databus.DataBusBridge('http://0.0.0.0:8625', 'http://localhost:3333/nest/v1');

exports.create = function () {
    // (??): If no arguments are passed, return null
    if (arguments.length == 0) {
        return null;
    }

    let stringifiedArgs = Array.from(arguments).map((arg) => JSON.stringify(arg));

    return JSON.parse(databusInstance.json0_create(...stringifiedArgs));
};

exports.apply = function () {
    let stringifiedArgs = Array.from(arguments).map((arg) => JSON.stringify(arg));

    return JSON.parse(databusInstance.json0_apply(...stringifiedArgs));
};

exports.compose = function () {
    let stringifiedArgs = Array.from(arguments).map((arg) => JSON.stringify(arg));

    return JSON.parse(databusInstance.json0_compose(...stringifiedArgs));
};

exports.transform = function () {
    let stringifiedArgs = Array.from(arguments).map((arg) => JSON.stringify(arg));

    return JSON.parse(databusInstance.json0_transform(...stringifiedArgs));
};

exports.transformX = function () {
    let stringifiedArgs = Array.from(arguments).map((arg) => JSON.stringify(arg));

    return JSON.parse(databusInstance.json0_transform_x(...stringifiedArgs));
};

exports.invert = function () {
    let stringifiedArgs = Array.from(arguments).map((arg) => JSON.stringify(arg));

    return JSON.parse(databusInstance.json0_invert(...stringifiedArgs));
};
