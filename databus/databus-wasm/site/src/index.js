import("./node_modules/databus_wasm/databus_wasm").then((js) => {
    const {add_tn, DataBusBridge} = js
    const n = add_tn(1, 2);
    console.log('n is ', n)
    document.getElementById('c').innerHTML = '' + n;
    const databus = new DataBusBridge("http://0.0.0.0:8625");
    databus.print();
    const printButton = document.getElementById('printButton');
    printButton.addEventListener('click', () => {
        databus.print();
        const dstId = document.getElementById('dstId').value;
        if (!dstId) {
            console.log('dstId is empty')
            return
        }
        databus.get_datasheet_pack(dstId).then((pack) => {
            console.log('pack e is ', pack)
            document.getElementById('e').innerHTML = JSON.stringify(pack, null, 2)
        })
    });
});
