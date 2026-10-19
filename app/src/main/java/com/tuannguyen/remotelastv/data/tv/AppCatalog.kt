package com.tuannguyen.remotelastv.data.tv

import com.tuannguyen.remotelastv.domain.model.KnownApp
import com.tuannguyen.remotelastv.domain.model.TelevisionBrand

object AppCatalog {
    private val targets: Map<KnownApp, Map<TelevisionBrand, List<String>>> = mapOf(
        KnownApp.NETFLIX to mapOf(
            TelevisionBrand.LG to listOf("netflix"),                                  // verified (log)
            TelevisionBrand.SAMSUNG to listOf("11101200001", "3201907018807"),
            TelevisionBrand.ANDROID_TV to listOf("https://www.netflix.com/title"),
        ),
        KnownApp.YOUTUBE to mapOf(
            TelevisionBrand.LG to listOf("youtube.leanback.v4"),                      // verified (log)
            TelevisionBrand.SAMSUNG to listOf("111299001912"),
            TelevisionBrand.ANDROID_TV to listOf("https://www.youtube.com"),
        ),
        KnownApp.PRIME_VIDEO to mapOf(
            TelevisionBrand.LG to listOf("amazon"),
            TelevisionBrand.SAMSUNG to listOf(
                "3201512006785",
                "3201910019365"
            ),      // id bạn đã có
            TelevisionBrand.ANDROID_TV to listOf("https://app.primevideo.com"),
        ),
        KnownApp.DISNEY_PLUS to mapOf(
            TelevisionBrand.LG to listOf("com.disney.disneyplus-prod"),
            TelevisionBrand.SAMSUNG to listOf("3201901017640"),
            TelevisionBrand.ANDROID_TV to listOf("https://www.disneyplus.com"),
        ),
        KnownApp.APPLE_TV to mapOf(
            TelevisionBrand.LG to listOf("com.apple.appletv"),                        // verified (log)
            TelevisionBrand.SAMSUNG to listOf("3201807016597"),
            TelevisionBrand.ANDROID_TV to listOf("https://tv.apple.com"),
        ),
        KnownApp.SPOTIFY to mapOf(
            TelevisionBrand.LG to listOf("spotify-beehive"),
            TelevisionBrand.SAMSUNG to listOf("3201606009684"),
            TelevisionBrand.ANDROID_TV to listOf("https://open.spotify.com"),
        ),
        KnownApp.PLEX to mapOf(
            TelevisionBrand.LG to listOf("cdp-30"),
            TelevisionBrand.SAMSUNG to listOf("3201512006963"),
            TelevisionBrand.ANDROID_TV to listOf("https://app.plex.tv"),
        ),
        KnownApp.FPT_PLAY to mapOf(
            TelevisionBrand.LG to listOf("com.fpt.fptplay"),
            // SAMSUNG, ANDROID_TV: chưa có id đáng tin, cần lấy từ máy thật
        ),
        KnownApp.VTV_GO to mapOf(
            TelevisionBrand.LG to listOf("com.vtvgotv.app"),
            // SAMSUNG, ANDROID_TV: chưa có id đáng tin
        ),
        KnownApp.GALAXY_PLAY to mapOf(
            // Dịch vụ của Samsung nên không có trên LG.
            // SAMSUNG: chưa có id đáng tin; ANDROID_TV: chưa rõ
        ),
    )

    fun targetsOf(app: KnownApp, brand: TelevisionBrand): List<String> =
        targets[app]?.get(brand).orEmpty()

    fun identify(id: String, brand: TelevisionBrand): KnownApp? =
        targets.entries.firstOrNull { (_, byBrand) -> id in byBrand[brand].orEmpty() }?.key
}