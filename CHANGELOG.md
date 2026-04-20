# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- `networking`: Added module for Minecraft networking.
- `coat-bridge`: Added experimental text mapper based on Tweed Serde.

### Changed
- **Breaking**: Added a context type and method paramter to all `Middleware`s.
- **Breaking**@`core`: Refactored type hierarchy and methods of `StructuredConfigEntry`.
- **Breaking**@`core`: Refactored the interface of `ConfigEntryValueVisitor` concerning structured entries.
- **Breaking**@`serde-*`: Repackaged all classes to bear `tweed5.serde` instead of `tweed5.data` in their packages.
- **Breaking**@`serde-extension`: Changed data reading and error handling to use a result class instead of exceptions.  
  The result class also allows returning an empty result for better differentiation from `null` values.
- **Breaking**@`serde-extension`: Removed `ReadWriteExtension#readerChain` and `ReadWriteExtension#writerChain`.  
  Instead, the newly introduced `readSubEntry` and `writeSubEntry` methods are provided on the respective contexts.
- `weaver-pojo-serde-extension`: Slightly changed the `SerdePojoReaderWriterSpec`
  to be more closely aligned with Java's identifier rules.
- `attributes-extension`: The `AttributesReadWriteFilterExtension` now correctly skips non-matching compound entries
  instead of returning `null` for them.
- `serde-hjson`: `inlineCommentType` on `HjsonWriter.Options` now correctly works builder-style.

## [0.7.2] - 2026-03-29

### Changed
- `minecraft`: Added support for Minecraft 26.1.
  This includes some required changes in the build process. So this release is published for all versions of Minecraft.

## [0.7.1] - 2026-02-08

### Changed
- Relicensed to MPL-2.0.

## [0.7.0] - 2025-12-19

### Changed
- **Breaking**: Inverted the order in which middlewares are applied.

### Fixed
- `default-extensions`: Fixed `ReadFallbackExtension` being applied too late.

## [0.6.0] - 2025-12-14

### Added
- `weaver-pojo`: Added an interface `TweedPojoWeaver` as the official way to weave POJOs.

### Fixed
- `construct`: Add error context to certain exceptions arising in `TweedConstructFactory`.

### Changed
- `weaver-pojo`: Renamed and refactored the internal weaver classes (especially `TweedPojoWeaverBootstrapper`).
