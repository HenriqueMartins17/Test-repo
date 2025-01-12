

# Dev

> [Back](../README.md)


## Packages Levels

```
└── **databus-wasm**
  └── databus-dao-api (API索取)
  └── databus-core (做关联和OT计算)
    └── json0
  └── databus-shared
└── **databus-server**
  └── databus-dao-db (数据库读写组合)
  └── databus-shared
  └── databus-core (做关联和OT计算)
    └── json0
└── databus-tablebundle
  └── databus-core
    └── json0
  └── databus-shared
```

- DatasheetPackSO extends DatasheetPackPO

## Bindings


Different programming languages use different binding libraries:

- WebAssembly - RustWASM: https://rustwasm.github.io/book/introduction.html
- REST-API - ActixWeb: https://actix.rs/
- [弃用]Python - PyO3: https://pyo3.rs/v0.19.0/
- [弃用]Java - JNI: https://docs.rs/jni/latest/jni/
- [弃用]NodeJS - NAPI: https://napi.rs/

## API Reference

DataBus 要非常注意接口设计，参考这些文档做 API 设计:

- [APITable REST API](https://developers.apitable.com/api/introduction/)
- [APITable Widget SDK](https://developers.apitable.com/widget/introduction/)
- [APITable Scripting SDK](https://developers.apitable.com/script/introduction/)
- [DataBus API Reference](https://apitable.getoutline.com/doc/api-reference-PtqqimrTQx)

DataBus所有接口，会自动发布在这个网址：
https://databus.surge.sh

## Programming Convention

1. Please use the naming convention of `DAO / PO / BO` when creating `structs`.
2. Avoid using `Dynamic Dependency Injection` as Rust is a static compile language.
3. Instead of creating numerous `modules`, it's recommended to use shallow `structs`.
4. Use [Screaming Architecture](https://levelup.gitconnected.com/what-is-screaming-architecture-f7c327af9bb2#:~:text=The%20term%20%E2%80%9Cscreaming%20architecture%E2%80%9D%20is,was%20coined%20by%20Robert%20C.) and [Domain-driven Design](https://dev.to/stevescruz/domain-driven-design-ddd-file-structure-4pja).
5. 通常struct里不会出现JSON Value，而是几乎都是真struct；
6. 几乎不会用到Option<T>，如果有出现Option<T>的场景，请确实是否已经应该新建一个XO，比如你想在NodeSO添加Option<T>，其实是不是应该新建一个NodePO?

## Code Files Best Practice on Rust

- so/
  - mod.rs
  - (other...)
  - types.rs | types/


## Caching

Caching is implemented in our system using a double-double 4-level caching strategy:

1. **Browser-side L1 Cache**: `Browser-side Memory Cache(BMC)`, which is implemented as a LRU (Least Recently Used) `HashMap` in `databus-core+databus-wasm`'s DataBundle struct.
2. **Browser-side L2 Cache**: `Browser-side Persistent Cache(BPC)`, which uses `IndexedDB` as a persistent cache component that resides in the user's browser.
3. **Server-side L1 Cache**: `Server-side Memory Cache(SMC)`, which is implemented as a LRU (Least Recently Used) `HashMap` in `databus-core+databus-server/python/java/nodejs`'s DataBundle struct.
4. **Server-side L2 Cache**: `Server-side Persistent Cache(SPC)`, which uses `Redis` as a persistent cache database with LRU eviction policies.

These caching levels help to optimize the performance and efficiency of our system by reducing the number of requests made to the server and minimizing the response time.

To determine whether the data needs to be updated, you can rely on the `revision` versioning number field in the datasheet.