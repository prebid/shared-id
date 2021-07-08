const SOURCE = 'pubcid.org';
const URL = 'https://id.sharedid.org/pubcid';

window.googletag = window.googletag || {cmd: []};
window.googletag.encryptedSignalProviders = window.googletag.encryptedSignalProviders || [];

function collector() {
    return new Promise((resolve, reject) => fetch(URL)
        .then(response => response.json())
        .then(data => document.cookie = SOURCE + '=' + data[SOURCE])
        .then(resolve)
        .catch(reject)
    );
}

googletag.encryptedSignalProviders.push({
    id: 'pubcid.org',
    collectorFunction: collector
});
