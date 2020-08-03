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

1. Module must set a 1st party cookie in prebid.pub.com domain
  1. SameSite = lax
  1. Exp can be configurable, default should be 28 days
1. Module must call out to sharedid.org/usync so a 3rd party cookie can be set
  1. SameSite = none, secure
  1. Exp can be configurable, default should be 28 days
  1. Resulting uuid string should be the same as the 1st pc
1. Module must send both the 1pc & 3pc to bid adapters
1. SharedId.org/usync
  1. Open source and make available for download on Sharedid.org
    1. TBD on whether we just just make the endpoint and a config to enter redirect urls into or the whole code base available.
  1. As an administrator of /usync, I want /usync to be able to create a mapping between it and the ad tech partners that decide to deploy it.
  1. As an administrator of /usync, I want /usync to call no more than five additional partners per sync so that our syncing footprint does not become overwhelming for publishers.   
