const assert = require('node:assert');
const json0 = require('ot-json0/lib/json0');

const test_obj = {
  "a": "This ia a stirng",
  "b": 3,
  "c": ["This is c array", 1, 2, 999],
  "d": {
    "d1": 1,
    "d2": "d2 string"
  }
}
// console.log(json0);

const ops = [
  [{"p": ["c", 0], "li": 888}, {"p": ["c", 0], "ld": 8888}, {"p": ["c", 0], "li": 0}],
  [], // list delete, 8888 useless
  [],
]
function measureExecutionTime(func, iterations) {
  const startTime = new Date();
  for (let i = 0; i < iterations; i++) {
      func();
  }
  const endTime = new Date();
  return endTime - startTime;
}

const iterations = [1000, 2000, 10000, 20000, 30000];
iterations.forEach(iteration => {
  const executionTime = measureExecutionTime(()=>{
    for (const op of ops) {
      json0.apply(test_obj, op)
    }
  }, iteration);
  console.log(`Iterations: ${iteration}, js: ${executionTime} ms`);
})
// assert.strictEqual(test_obj.c.length, 5);
// assert.strictEqual(test_obj.c[0], 0);



