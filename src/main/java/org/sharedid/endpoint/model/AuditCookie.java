package org.sharedid.endpoint.model;

import org.sharedid.endpoint.util.HostUtils;
import org.sharedid.endpoint.util.IpUtil;
import org.sharedid.endpoint.util.IpUtilException;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.time.Instant;


public class AuditCookie {
    private static final Logger logger = LoggerFactory.getLogger(AuditCookie.class);

    private String version;
    private String userId;
    private String hostIpAddress;
    private String userIpAddress;       //masked last octet
    private String country;
    private Long renewedTimestampSeconds;       //UTC seconds
    private String referrerDomain;
    private String initiatorType;
    private String initiatorId;
    private boolean hasConsentString;
    private String consentString;

    public AuditCookie() { }

    public AuditCookie(String version,
                       String userId,
                       String hostIpAddress,
                       String userIpAddress,
                       String country,
                       Long renewedTimestampSeconds,
                       String referrerDomain,
                       String initiatorType,
                       String initiatorId,
                       boolean hasConsentString,
                       String consentString) {
        this.version = version;
        this.userId = userId;
        this.hostIpAddress = hostIpAddress;
        this.userIpAddress = userIpAddress;
        this.country = country;
        this.renewedTimestampSeconds = renewedTimestampSeconds;
        this.referrerDomain = referrerDomain;
        this.initiatorType = initiatorType;
        this.initiatorId = initiatorId;
        this.hasConsentString = hasConsentString;
        this.consentString = consentString;
    }

    public static AuditCookie fromCookie(Cookie cookie, String cipherKey) throws AuditCookieDeserializationException {
        AuditCookie auditCookie = new AuditCookie();
        auditCookie.deserialize(cookie.getValue(), cipherKey);
        return auditCookie;
    }

    public String getUserId() {
        return userId;
    }

    public String getConsentString() {
        return consentString;
    }

    public Long getRenewedTimestampSeconds() {
        return renewedTimestampSeconds;
    }

    public void setRenewedTimestampSeconds(Long renewedTimestampSeconds) {
        this.renewedTimestampSeconds = renewedTimestampSeconds;
    }

    public Cookie toCookie(String cookieName, String cipherKey, long ttl) throws AuditCookieSerializationException {
        String cookieValue = serialize(cipherKey);

        Cookie cookie = Cookie.cookie(cookieName, cookieValue);
        cookie.setMaxAge(ttl);

        return cookie;
    }

    public void deserialize(String encryptedCookieString, String cipherKey)
            throws AuditCookieDeserializationException
    {
        String cookieString = blowFishDecrypt(cipherKey, encryptedCookieString);
        deserialize(cookieString);
    }

    public void deserialize(String cookieString)
    {
        String[] groups = cookieString.split("\\|");

        if (groups.length != 4)
        {
            logger.debug("Audit cookie deserialization failed. Cookie string does not have 4 groups.");
            return;
        }

        version = groups[0];
        readGroup2(groups);
        readGroup3(groups);
        readGroup4(groups);
    }

    public String serialize(String cipherKey) throws AuditCookieSerializationException
    {
        String cookieValue = serialize();
        return blowFishEncrypt(cipherKey, cookieValue);
    }

    public String serialize()
    {
        StringBuilder out = new StringBuilder();

        if (version != null)
        {
            out.append(version);
        }

        out.append("|");

        String hostIpAddressGroupValue = null;

        try
        {
            hostIpAddressGroupValue = hostIpAddress != null ? String.valueOf(IpUtil.ipToInt(hostIpAddress)) : null;
        }
        catch (IpUtilException iue)
        {
            logger.debug("Audit cookie host ip address has malformed ip address: {}", hostIpAddress);
        }

        String userIpAddressGroupValue = null;

        try
        {
            userIpAddressGroupValue = userIpAddress != null ? String.valueOf(IpUtil.ipToInt(userIpAddress)) : null;
        }
        catch (IpUtilException iue)
        {
            logger.debug("Audit cookie host ip address has malformed ip address: {}", userIpAddress);
        }

        String[] group2 = {
                userId,
                hostIpAddressGroupValue,
                userIpAddressGroupValue,
                country,
                renewedTimestampSeconds != null ? String.valueOf(renewedTimestampSeconds) : null,
                referrerDomain
        };
        String[] group3 = {initiatorType, initiatorId};
        String[] group4 = {hasConsentString ? "1" : "0", hasConsentString ? consentString : null};

        out.append(joinGroup(group2));
        out.append("|");
        out.append(joinGroup(group3));
        out.append("|");
        out.append(joinGroup(group4));

        return out.toString();
    }

    private String blowFishEncrypt(String cipherKey, String cookieValue) throws AuditCookieSerializationException
    {
        byte[] keyData = cipherKey.getBytes();
        SecretKeySpec keySpec = new SecretKeySpec(keyData, "Blowfish");

        try
        {
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedCookieValue = cipher.doFinal(cookieValue.getBytes());

            return Base64.encodeBase64String(encryptedCookieValue);
        }
        catch (Exception e)
        {
            throw new AuditCookieSerializationException(e);
        }
    }

    private String blowFishDecrypt(String cipherKey, String encryptedCookieValue)
            throws AuditCookieDeserializationException
    {
        byte[] keyData = cipherKey.getBytes();
        SecretKeySpec keySpec = new SecretKeySpec(keyData, "Blowfish");

        try
        {
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decryptedCookieValue = cipher.doFinal(Base64.decodeBase64(encryptedCookieValue));
            return new String(decryptedCookieValue);
        }
        catch (Exception e)
        {
            throw new AuditCookieDeserializationException(e);
        }
    }

    private void readGroup2(String[] groups)
    {
        //read group 2
        String[] group2 = groups[1].split("\\^");

        //khaos cookie
        if (group2.length > 0)
        {
            userId = group2[0];
        }

        //host ip address
        if (group2.length > 1)
        {

            try
            {
                int ip = Integer.parseInt(group2[1]);
                hostIpAddress = IpUtil.intToIp(ip);
            }
            catch (NumberFormatException nfe)
            {
                logger.debug("Audit cookie host ip address was malformed. {}", group2[1]);
            }
        }

        //user ip address
        if (group2.length > 2)
        {
            try
            {
                int ip = Integer.parseInt(group2[2]);

                //mask the last octet
                ip = ip & 0xFFFFFF00;

                userIpAddress = IpUtil.intToIp(ip);
            }
            catch (NumberFormatException nfe)
            {
                logger.debug("Audit cookie user ip address was malformed. {}", group2[2]);
            }
        }

        //country
        if (group2.length > 3)
        {
            country = group2[3];
        }

        if (group2.length > 4)
        {
            try
            {
                renewedTimestampSeconds = Long.parseLong(group2[4]);
            }
            catch (NumberFormatException nfe)
            {
                logger.debug("Audit cookie renewed timestamp was malformed. {}", group2[4]);
            }
        }

        if (group2.length > 5)
        {
            referrerDomain = group2[5];
        }
    }

    private void readGroup3(String[] groups)
    {
        String[] group3 = groups[2].split("\\^");

        if (group3.length > 0)
        {
            initiatorType = group3[0];
        }

        if (group3.length > 1)
        {
            initiatorId = group3[1];
        }
    }

    private void readGroup4(String[] groups)
    {
        String[] group4 = groups[3].split("\\^");

        if (group4.length > 0)
        {
            try
            {
                hasConsentString = Integer.parseInt(group4[0]) == 1;
            }
            catch (NumberFormatException nfe)
            {
                logger.debug("Audit cookie has consent string value was malformed: {}", hasConsentString);
            }
        }

        if (group4.length > 1 && hasConsentString)
        {
            consentString = group4[1].substring(0, Math.min(100, group4[1].length()));      //truncated to 100 characters
        }
    }

    /*
        Joins a group with ^ as delimiter. Any trailing null values in the group with
        not be put into the string. Further, any null values in the middle will
        be placed with empty values, e.g., "a,null,b,c" -> "a^^b^c" and "a,b,c,null" -> "a^b^c"
     */
    private String joinGroup(String[] group)
    {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < group.length; i++)
        {
            String value = group[i];

            if (value != null)
            {
                out.append(value);
            }

            //apply delimiter except for last value
            if (i < group.length - 1)
            {
                out.append("^");
            }
        }

        //remove any trailing empty values

        String s = out.toString();

        while (s.endsWith("^"))
        {
            s = s.substring(0, s.length() - 1);
        }

        return s;
    }

    public static class Factory {
        public AuditCookie build(RoutingContext routingContext,
                                 String userId,
                                 String geoQuery,
                                 String gdprConsentString) {
            String version = "1";

            HttpServerRequest request = routingContext.request();

            String hostIpAddress = null;
            InetAddress localHost = HostUtils.getLocalhost();
            if (localHost != null) {
                hostIpAddress = localHost.getHostAddress();
            }

            String userIpAddress = HostUtils.getUserAddress(request);

            String countryCode = null;
            if (geoQuery != null) {
                countryCode = geoQuery;
            }

            long renewedTimestampSeconds = Instant.now().toEpochMilli();
            String referrerDomain = request.uri();

            String initiatorId = "";
            String initiatorType = "";

            boolean hasConsentString = StringUtils.isNotEmpty(gdprConsentString);

            return new AuditCookie(
                    version,
                    userId,
                    hostIpAddress,
                    userIpAddress,
                    countryCode,
                    renewedTimestampSeconds,
                    referrerDomain,
                    initiatorType,
                    initiatorId,
                    hasConsentString,
                    gdprConsentString
            );
        }
    }
}
