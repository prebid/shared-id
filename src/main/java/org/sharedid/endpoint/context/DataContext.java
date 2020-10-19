package org.sharedid.endpoint.context;

import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.sharedid.endpoint.consent.GdprConsentString;
import org.sharedid.endpoint.model.AuditCookie;

/**
 * Simple wrapper for retrieving type safe data from the RoutingContext data map.
 */
public class DataContext {
    private static final String DATA_DATA_CONTEXT = "data.dataContext";
    private static final String DATA_IS_OPTED_OUT = "data.isOptedOut";
    private static final String DATA_IS_GDPR_PARAM = "data.isGdprParam";
    private static final String DATA_GDPR_CONSENT_PARAM = "data.gdprConsentParam";
    private static final String DATA_US_PRIVACY_PARAM = "data.usPrivacyParam";
    private static final String DATA_REDIRECT_URL_PARAM = "data.redirectUrlParam";
    private static final String DATA_USER_ID = "data.userId";
    private static final String DATA_USER_COOKIE = "data.userCookie";
    private static final String DATA_IS_NEW_USER_ID = "data.isNewUserId";
    private static final String DATA_IS_SYNCED_USER_ID = "data.isSyncedUserId";
    private static final String DATA_VENDOR = "data.vendor";
    private static final String DATA_GDPR_CONSENT_STRING = "data.gdprConsentString";
    private static final String DATA_AUDIT_COOKIE = "data.auditCookie";
    private static final String DATA_GEO_QUERY = "data.geoQuery";
    private static final String DATA_IS_GDPR_COUNTRY = "data.isGdprCountry";

    private RoutingContext routingContext;

    public DataContext(RoutingContext routingContext) {
        this.routingContext = routingContext;
    }

    public static DataContext from(RoutingContext routingContext) {
        DataContext dataContext = (DataContext) routingContext.data().get(DATA_DATA_CONTEXT);

        if (dataContext == null) {
            dataContext = new DataContext(routingContext);
            routingContext.data().put(DATA_DATA_CONTEXT, dataContext);
        }

        return dataContext;
    }

    public Boolean getIsOptedOut() {
        return get(DATA_IS_OPTED_OUT);
    }

    public void setIsOptedOut(boolean isOptedOut) {
        put(DATA_IS_OPTED_OUT, isOptedOut);
    }

    public String isGdprParam() {
        return get(DATA_IS_GDPR_PARAM);
    }

    public void setIsGdprParam(String gdprParameter) {
        put(DATA_IS_GDPR_PARAM, gdprParameter);
    }

    public String getGdprConsentParam() {
        return get(DATA_GDPR_CONSENT_PARAM);
    }

    public void setGdprConsentParam(String gdprConsentParameter) {
        put(DATA_GDPR_CONSENT_PARAM, gdprConsentParameter);
    }

    public String getUsPrivacyParam() {
        return get(DATA_US_PRIVACY_PARAM);
    }

    public void setUsPrivacyParam(String usPrivacyParam) {
        put(DATA_US_PRIVACY_PARAM, usPrivacyParam);
    }

    public String getRedirectUrlParam() {
        return get(DATA_REDIRECT_URL_PARAM);
    }

    public void setRedirectUrlParam(String redirectUrlParam) {
        put(DATA_REDIRECT_URL_PARAM, redirectUrlParam);
    }

    public String getUserId() {
        return get(DATA_USER_ID);
    }

    public void setUserId(String userId) {
        put(DATA_USER_ID, userId);
    }

    public Cookie getUserCookie() {
        return get(DATA_USER_COOKIE);
    }

    public void setUserCookie(Cookie cookie) {
        put(DATA_USER_COOKIE, cookie);
    }

    public Boolean isNewUserId() {
        return get(DATA_IS_NEW_USER_ID);
    }

    public void setIsNewUserId(boolean isNewUserId) {
        put(DATA_IS_NEW_USER_ID, isNewUserId);
    }

    public boolean isSyncedUserId() {
        return get(DATA_IS_SYNCED_USER_ID, false);
    }

    public void setIsSyncedUserId(boolean isSyncedUserId) {
        put(DATA_IS_SYNCED_USER_ID, isSyncedUserId);
    }

    public Integer getVendor() {
        return get(DATA_VENDOR);
    }

    public void setVendor(Integer vendor) {
        put(DATA_VENDOR, vendor);
    }

    public GdprConsentString getGdprConsentString() {
        return get(DATA_GDPR_CONSENT_STRING);
    }

    public void setGdprConsentString(GdprConsentString gdprConsentString) {
        put(DATA_GDPR_CONSENT_STRING, gdprConsentString);
    }

    public AuditCookie getAuditCookie() {
        return get(DATA_AUDIT_COOKIE);
    }

    public void setAuditCookie(AuditCookie auditCookie) {
        put(DATA_AUDIT_COOKIE, auditCookie);
    }

    public String getGeoQuery() {
        return get(DATA_GEO_QUERY);
    }

    public void setGeoQuery(String geoQuery) {
        put(DATA_GEO_QUERY, geoQuery);
    }

    public Boolean getIsGdprCountry() {
        return get(DATA_IS_GDPR_COUNTRY);
    }

    public void setIsGdprCountry(Boolean isGdprCountry) {
        put(DATA_IS_GDPR_COUNTRY, isGdprCountry);
    }

    private <T> T get(String key, T def) {
        return (T) routingContext.data().getOrDefault(key, def);
    }

    private <T> T get(String key) {
        return (T) routingContext.data().get(key);
    }

    private <T> void put(String key, T obj) {
        routingContext.data().put(key, obj);
    }
}
