# SharedID Explainer

The SharedID.org namespace has been utilized to set a standard third-party cookie.  This cookie pool is fully deployed across a large, global browser footprint.  It has an opt-out and is compliant with IAB TCF consent strings when present.  The cookie writing semantic for SharedID runs from an AWS instance.

The intention behind creating this new cookie space is the following:

* Create a scaled third-party cookie space
* Donate this cookie space to Prebid.org
* Allow Prebid.org to own and administrate this domain and cookie pool, making it fully transparent and open source
* This also necessitates that Prebid.org develop a Privacy Policy, which is in process
* Give publishers, exchanges, DSPs, DMPs, etc the freedom to sync with the SharedID.org namespace so they may have synchronize this cookie to their own namespaces
* Develop a Prebid User ID Module sub-module so that SharedID cookie values can be written or accessed from the header, and in first-party storage
* In agreement with Conversant, transfer ownership of the PubCommon ID to Prebid.org; tether all 3rd-party SharedID cookies to first-party PubCommon identifiers via the SharedID User ID module
* Develop scaled coverage of this first-and-third party paired identifier, in anticipation of the third-party version eventually going away.

## Module Requirements

1.
1.
