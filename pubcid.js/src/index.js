const SOURCE = 'pubcid.org';

window.googletag = window.googletag || {cmd: []};
window.googletag.encryptedSignalProviders = window.googletag.encryptedSignalProviders || [];

function collector() {
    let value = getCookie();
    if(value){
        return;
    }
    value = generateUUID();
    document.cookie = SOURCE + '=' + value;
}

/**
 * Returns the cookie
 * @returns {string}
 */
function getCookie() {
    let name = SOURCE + "=";
    let ca = document.cookie.split(';');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

/**
 * Returns a random v4 UUID of the form xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx,
 * where each x is replaced with a random hexadecimal digit from 0 to f,
 * and y is replaced with a random hexadecimal digit from 8 to b.
 * https://gist.github.com/jed/982883 via node-uuid
 */
function generateUUID(placeholder) {
    return placeholder
        ? (placeholder ^ getRandomData() >> placeholder / 4).toString(16)
        : ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, generateUUID);
}

/**
 * Returns random data using the Crypto API if available and Math.random if not
 * Method is from https://gist.github.com/jed/982883 like generateUUID, direct link https://gist.github.com/jed/982883#gistcomment-45104
 */
function getRandomData() {
    if (window && window.crypto && window.crypto.getRandomValues) {
        return crypto.getRandomValues(new Uint8Array(1))[0] % 16;
    } else {
        return Math.random() * 16;
    }
}

googletag.encryptedSignalProviders.push({
    id: 'pubcid.org',
    collectorFunction: collector
});

