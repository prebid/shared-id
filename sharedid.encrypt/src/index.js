import { JSEncrypt } from "jsencrypt";
import { v4 as uuid } from "uuid";

const PUBLIC_KEY = "https://id.sharedid.org/keys/sharedid.pub";

const encrypt = (value, pubKey) => {
	const encryption = new JSEncrypt();
	encryption.setPublicKey(pubKey);
	const encryptedID = encryption.encrypt(value);
	return encryptedID;
};

export default function sharedIDEncrypt (uid, url = PUBLIC_KEY) {
	// if no uid is passed in, create a UUID
	if (!uid) {
		uid = uuid();
	}

	// using the public key, encrypt the UID
	return new Promise((resolve, reject) =>
		fetch(url)
			.then(response => response.text())
			.then(pubKey => encrypt(uid, pubKey))
			.then(resolve)
			.catch(reject)
	);
};
