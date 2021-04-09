import { v4 as uuid } from "uuid";
import Cookies from "./cookies";

const COOKIE = "sharedid";
//const PUBLIC_KEY = "https://id.sharedid.org/keys/sharedid.pub";
const PUBLIC_KEY = "https://id-qa.sharedid.org/keys/sharedid.pub";

export default function sharedIDCookie (uid, url = PUBLIC_KEY) {
	// check the cookie for any key of "sharedid", if it exists, exit out
	if (Cookies.get(COOKIE)) {
		return;
	}

	// if no uid is passed in, create a UUID
	if (!uid) {
		uid = uuid();
	}

	// using the public key, encrypt and set the cookie
	return fetch(url)
		.then(response => response.text())
		.then(pubKey => Cookies.encrypt(COOKIE, uid, pubKey))
		.catch(err => { 
			throw err;
		});
};
