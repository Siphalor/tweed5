# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [unreleased]

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
