# SharedID Cookie

SharedID.org Cookie

## Installation

```bash
npm install --save sharedid.cookie
```

## Usage

After including the source code, call the function as follows:

```javascript
window.sharedIdCookie("some-value", "://example.com/sharedid.pub");
```

The first parameter is the `value` of the cookie that will encrypted and is required.  The second parameter is the `url` for where the Shared ID public key is hosted.  By default it points to the correct location, but in the event you need to override it, this is how you would do it.

## How We Version

We use [SemVer](https://semver.org/) for its versioning providing us an opt-in approach to releases. This means we add a version number according to the spec, as you see below. So rather than force developers to consume the latest and greatest, they can choose which version to consume and test any newer ones before upgrading. Please the read the spec as it goes into further detail.

Given a version number **MAJOR.MINOR.PATCH**, increment the:

-   **MAJOR** version when you make incompatible API changes.
-   **MINOR** version when you add functionality in a backward-compatible manner.
-   **PATCH** version when you make backward-compatible bug fixes.

Additional labels for pre-release and build metadata are available as extensions to the **MAJOR.MINOR.PATCH** format.

## License

©2021 Apache 2.0. See [LICENSE](./LICENSE) for specifics.
