const {add_tn, action_add_record} = require('../../pkg/nodejs/databus_wasm');
const {MockDataForAddRecordAction} = require('./data/action_add_record_mock');
it('adds 1 + 2 to equal 32', () => {
    expect(add_tn(1, 2)).toBe(3);
});
it('test insert record in the end', () => {
    const result_base = [
        {
            "n": "LI",
            "p": [
                "meta",
                "views",
                0,
                "rows",
                3
            ],
            "li": {
                "recordId": "reclj2P5LfpTF"
            }
        },
        {
            "n": "OI",
            "p": [
                "recordMap",
                "reclj2P5LfpTF"
            ],
            "oi": {
                "id": "reclj2P5LfpTF",
                "data": {},
                "commentCount": 0,
                "comments": [],
                "recordMeta": {}
            }
        }
    ];

    const snapshot = MockDataForAddRecordAction;
    const payload = {viewId: "viwDtemXMuFxz", record: {
        id: "reclj2P5LfpTF",
        data: {},
        commentCount: 0,
        comments: [],
        recordMeta: {},
    }, index: 3};
    const result = action_add_record(snapshot, payload);

    expect(result).toEqual(result_base);
});

it('test insert record in the start', () => {
    const result_base = [
        {
            "n": "LI",
            "p": [
                "meta",
                "views",
                0,
                "rows",
                0
            ],
            "li": {
                "recordId": "recslko04eos1"
            }
        },
        {
            "n": "OI",
            "p": [
                "recordMap",
                "recslko04eos1"
            ],
            "oi": {
                "id": "recslko04eos1",
                "data": {},
                "commentCount": 0,
                "comments": [],
                "recordMeta": {}
            }
        }
    ];

    const snapshot = MockDataForAddRecordAction;
    const payload = {viewId: "viwDtemXMuFxz", record: {
        id: "recslko04eos1",
        data: {},
        commentCount: 0,
        comments: [],
        recordMeta: {},
    }, index: 0};
    const result = action_add_record(snapshot, payload);

    expect(result).toEqual(result_base);
});

it('test insert record in the middle', () => {
    const result_base = [
        {
            "n": "LI",
            "p": [
                "meta",
                "views",
                0,
                "rows",
                2
            ],
            "li": {
                "recordId": "recyYpNFsYmUo"
            }
        },
        {
            "n": "OI",
            "p": [
                "recordMap",
                "recyYpNFsYmUo"
            ],
            "oi": {
                "id": "recyYpNFsYmUo",
                "data": {},
                "commentCount": 0,
                "comments": [],
                "recordMeta": {}
            }
        }
    ];

    const snapshot = MockDataForAddRecordAction;
    const payload = {viewId: "viwDtemXMuFxz", record: {
        id: "recyYpNFsYmUo",
        data: {},
        commentCount: 0,
        comments: [],
        recordMeta: {},
    }, index: 2};
    const result = action_add_record(snapshot, payload);

    expect(result).toEqual(result_base);
});

