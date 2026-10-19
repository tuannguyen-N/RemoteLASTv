package com.tuannguyen.remotelastv.data.tv

import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand

object AppCatalog {
    private val targets: Map<KnownApp, Map<TelevisionBrand, List<String>>> = mapOf(
        KnownApp.NETFLIX to mapOf(
            TelevisionBrand.LG to listOf("netflix"),
            TelevisionBrand.SAMSUNG to listOf("11101200001", "3201907018807"),
            TelevisionBrand.ANDROID_TV to listOf("com.netflix.ninja"),
        ),

        KnownApp.YOUTUBE to mapOf(
            TelevisionBrand.LG to listOf("youtube.leanback.v4"),
            TelevisionBrand.SAMSUNG to listOf("111299001912"),
            TelevisionBrand.ANDROID_TV to listOf("com.google.android.youtube.tv"),
        ),

        KnownApp.PRIME_VIDEO to mapOf(
            TelevisionBrand.LG to listOf("amazon"),
            TelevisionBrand.SAMSUNG to listOf(
                "3201512006785",
                "3201910019365",
            ),
            TelevisionBrand.ANDROID_TV to listOf(
                "com.amazon.amazonvideo.livingroom",
            ),
        ),

        KnownApp.DISNEY_PLUS to mapOf(
            TelevisionBrand.LG to listOf("com.disney.disneyplus-prod"),
            TelevisionBrand.SAMSUNG to listOf("3201901017640"),
            TelevisionBrand.ANDROID_TV to listOf("com.disney.disneyplus"),
        ),

        KnownApp.APPLE_TV to mapOf(
            TelevisionBrand.LG to listOf("com.apple.appletv"),
            TelevisionBrand.SAMSUNG to listOf("3201807016597"),
            TelevisionBrand.ANDROID_TV to listOf(
                "com.apple.atve.androidtv.appletv",
            ),
        ),

        KnownApp.SPOTIFY to mapOf(
            TelevisionBrand.LG to listOf("spotify-beehive"),
            TelevisionBrand.SAMSUNG to listOf("3201606009684"),
            TelevisionBrand.ANDROID_TV to listOf("com.spotify.tv.android"),
        ),

        KnownApp.PLEX to mapOf(
            TelevisionBrand.LG to listOf("cdp-30"),
            TelevisionBrand.SAMSUNG to listOf("3201512006963"),
            TelevisionBrand.ANDROID_TV to listOf("com.plexapp.android"),
        ),

        KnownApp.FPT_PLAY to mapOf(
            TelevisionBrand.LG to listOf("com.fpt.fptplay"),
//            TelevisionBrand.ANDROID_TV to listOf("net.fptplay.ottbox"),
            // Samsung: cần xác minh trên TV thật.
        ),

        KnownApp.VTV_GO to mapOf(
            TelevisionBrand.LG to listOf("com.vtvgotv.app"),
//            TelevisionBrand.ANDROID_TV to listOf("vn.vtv.vtvgo"),
            // Samsung: cần xác minh trên TV thật.
        ),
        KnownApp.HBO_MAX to mapOf(
            // LG / Samsung: cần xác minh app ID trên TV thật.
            TelevisionBrand.ANDROID_TV to listOf("com.wbd.stream"),
        ),

        KnownApp.PARAMOUNT_PLUS to mapOf(
            // LG / Samsung: cần xác minh app ID trên TV thật.
            // Package Android TV có trang Google Play đối chiếu:
            // https://play.google.com/store/apps/details?id=com.cbs.ott
            TelevisionBrand.ANDROID_TV to listOf("com.cbs.ott"),
        ),
    )

    fun targetsOf(app: KnownApp, brand: TelevisionBrand): List<String> =
        targets[app]?.get(brand).orEmpty()

    fun identify(id: String, brand: TelevisionBrand): KnownApp? =
        targets.entries.firstOrNull { (_, byBrand) -> id in byBrand[brand].orEmpty() }?.key
}