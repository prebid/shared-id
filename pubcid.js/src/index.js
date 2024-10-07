console.warn("Warning: The Shared ID Google Deploy Secure Signals script is deprecated in favor of the Prebid User ID method. Please adjust your GAM Secure Signals settings. This script may or may not function as intended. It may have some unusual side effects. See https://support.google.com/admanager/answer/10488752?hl=en&ref_topic=10728657&sjid=15896989004232235718-NA for additional detail.");

const SOURCE = 'pubcid.org';

window.googletag = window.googletag || {cmd: []};
window.googletag.encryptedSignalProviders = window.googletag.encryptedSignalProviders || [];
window.pbjs =  window.pbjs || [];

function collector() {
    return new Promise((resolve, reject) => {
        const pubCommonUids = pbjs.getUserIdsAsEids().filter(function (eids) {
            return eids && eids.source == SOURCE;
        });
       let pubcidValue = pubCommonUids ? pubCommonUids[0].uids[0].id : generateUUID();
        window.localStorage.setItem(SOURCE, pubcidValue);
        if (pubcidValue) {
            resolve(pubcidValue);
        } else {
            reject("Setting local storage failed");
        }
    });
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
    id: SOURCE,
    collectorFunction: collector
});
