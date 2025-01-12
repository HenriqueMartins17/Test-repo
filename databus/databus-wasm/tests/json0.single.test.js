const { apply, invert, compose, transform, transformX } = require('../lib/json0');

describe('JSON0 library', () => {
    it('should apply operations correctly', () => {
        let snapshot = { foo: 1, bar: 2 };
        let op = [{ p: ['foo'], od: 1, oi: 3 }];
        let expected = { foo: 3, bar: 2 };
        let result = apply(snapshot, op);

        expect(result).toEqual(expected);
    });
});
