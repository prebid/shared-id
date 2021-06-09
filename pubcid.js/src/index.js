const SOURCE = 'pubcid.org';
const URL = 'https://id.sharedid.org/pubcid';

window.googletag = window.googletag || {cmd: []};
window.googletag.encryptedSignalSource = window.googletag.encryptedSignalSource || {};
googletag.encryptedSignalSource['pubcid.org'] = () => {
    return new Promise((resolve, reject) =>
        fetch(URL)
            .then(response => response.json())
            .then(data => document.cookie = SOURCE + '=' + data[SOURCE])
            .then(resolve)
            .catch(reject)
    );
};
