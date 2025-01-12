const { apply, invert, compose, transform, transformX } = require('../lib/json0');

// apply test

let snapshot = { foo: 1, bar: 2 };
let op = [{ p: ['foo'], od: 1, oi: 3 }];
let expected = { foo: 3, bar: 2 };
let result = apply(snapshot, op);

console.log('\n----- apply test -----\n');

console.log('apply result:', result);
console.log('apply expected:', expected);
console.log(JSON.stringify(result) == JSON.stringify(expected));

console.log('\n----- apply test -----\n');

// invert test

// compose test

let compose_snapshot = [{ p: [2], ld: [], li: null }];
let compose_operation = [{ p: [2], ld: null }];
let compose_expected = [{ p: [2], ld: [] }];
let compose_result = compose(compose_snapshot, compose_operation);

console.log('\n----- compose test ----\n');

console.log('compose result:', compose_result);
console.log('compose expected:', compose_expected);
console.log(JSON.stringify(compose_result) == JSON.stringify(compose_expected));

console.log('\n----- compose test ----\n');

// transform test

// transformX test
