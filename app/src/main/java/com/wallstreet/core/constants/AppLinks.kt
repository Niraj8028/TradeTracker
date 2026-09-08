package com.wallstreet.core.constants

/**
 * Canonical URLs for the public site. The legal pages live there rather than
 * being duplicated in the app, so a wording change ships without a release and
 * the two copies cannot drift apart.
 */
object AppLinks {
    const val SITE = "https://tradecoachofficial.vercel.app"

    const val PRIVACY_POLICY = "$SITE/policies/privacy-policy/"
    const val TERMS_OF_USE = "$SITE/policies/terms-of-use/"
    const val CHILD_SAFETY = "$SITE/policies/child-safety-standards/"
    const val SUPPORT = "$SITE/get-in-touch/"
}
