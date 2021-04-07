import {JSEncrypt} from "jsencrypt";

const URL_SHARED_ID_PUBLIC_KEY = "https://sharedid.org/keys/sharedid-public-key";

const HttpClient = function () {
    this.get = function (url, cb) {
        const request = new XMLHttpRequest();
        request.onreadystatechange = function () {
            if (request.readyState === 4 && request.status === 200)
                cb(request.responseText);
        }

        request.open("GET", url, true);
        request.send(null);
    }
}
const client = new HttpClient();

class SharedID {
    constructor() {
        this.encryption = new JSEncrypt();
        this.initialized = false;
    }

    set id(value) {
        if (!this.initialized) {
            client.get(URL_SHARED_ID_PUBLIC_KEY, response => {
                this.encryption.setPublicKey(response);
                document.cookie = `sharedid=${this.encryption.encrypt(value)}`;
            })
        } else {
            document.cookie = `sharedid=${this.encryption.encrypt(value)}`;
        }
    }
}

window.SharedID = SharedID;
