# SharedID Encrypt

SharedID.org Encrypt

## Installation

```bash
npm install --save sharedid.encrypt
```

## Usage

After including the source code, call the function as follows:

```html
<script src="./sharedid.encrypt.min.js"></script>
<script>
    sharedIDEncrypt("sharedid-value", "http://localhost:9000/sharedid.pub");
    sharedIDEncrypt("sharedid-value");
    sharedIDEncrypt().then(signal => console.log(signal));
/script>
```

Please don't copy/paste this example as its calling it 3 different times to show you the possible options.  Choose the one that best fits your needs and read the API docs below to help further decide based on the parameters of the function.

## API

#### sharedIDencrypt(uid?, url?);

Returns a `Promise` with an encrypted string.

#### uid

Type: `String`

A UID string that will be encrypted and stored as a string. If one is not passed in, an [UUID (version 4)](https://www.ietf.org/rfc/rfc4122.txt) will be created for you.

#### url

Type: `String`

The URL location to the public RSA key for encrypting.  If one is not provided, it will default the Shared ID RSA key.

## How We Version

We use [SemVer](https://semver.org/) for its versioning providing us an opt-in approach to releases. This means we add a version number according to the spec, as you see below. So rather than force developers to consume the latest and greatest, they can choose which version to consume and test any newer ones before upgrading. Please the read the spec as it goes into further detail.

Given a version number **MAJOR.MINOR.PATCH**, increment the:

-   **MAJOR** version when you make incompatible API changes.
-   **MINOR** version when you add functionality in a backward-compatible manner.
-   **PATCH** version when you make backward-compatible bug fixes.

Additional labels for pre-release and build metadata are available as extensions to the **MAJOR.MINOR.PATCH** format.

## License

©2021 Apache 2.0. See [LICENSE](./LICENSE) for specifics.
