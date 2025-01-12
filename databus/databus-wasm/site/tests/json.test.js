const {add_tn, json0_seri, json0_inverse} = require('../../pkg/nodejs/databus_wasm');
it('adds 1 + 2 to equal 3', () => {
    expect(add_tn(1, 2)).toBe(3);
});

test('test json0 inverse operation json', () => {
    const opArray = [{
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
    }];
    const result = json0_inverse(opArray);

    expect(result).toEqual([
        {
            p: ['recordMap', 'recnMv9BvatQh', 'data', 'fldGzWrhZdznD'],
            od: [
                {
                    type: 1,
                    text: 'asdasd',
                },
            ],
        },
    ]);
});

test('test json0 operation serialize', () => {
    const result = json0_seri();

    expect(result).toEqual({
        age: 30,
        is_student: false,
        name: "Alice",
    });
});