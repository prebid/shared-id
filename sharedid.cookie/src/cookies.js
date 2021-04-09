import { JSEncrypt } from "jsencrypt";

export const getCookie = key => document.cookie
	.split('; ')
	.find(row => row.startsWith(`${key}=`));

export const setCookie = (key, value) => document.cookie = `${key}=${value}`;

export const encryptCookie = (key, value, pubKey) => {
	const encryption = new JSEncrypt();
	encryption.setPublicKey(pubKey);
	const encryptedID = encryption.encrypt(value);
	setCookie(key, encryptedID);
	return encryptedID;
};

export default {
	get: getCookie,
	set: setCookie,
	encrypt: encryptCookie,
};
