import {JSEncrypt} from "jsencrypt";

window.sharedIdCookie = function (value, url = "https://id.sharedid.org/keys/sharedid.pub") {
    fetch(url)
        .then(response => response.text())
        .then(key => {
            const encryption = new JSEncrypt();
            encryption.setPublicKey(key);
            document.cookie = `sharedid=${encryption.encrypt(value)}`;
        })
        .catch(err => {
            throw err;
        });
};
