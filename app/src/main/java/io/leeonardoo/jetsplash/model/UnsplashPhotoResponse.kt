package io.leeonardoo.jetsplash.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnsplashPhoto(

	@Json(name = "id")
	val id: String,

    @Json(name = "color")
    val color: String,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "description")
    val description: String?,

    @Json(name = "urls")
    val urls: Urls,

    @Json(name = "alt_description")
    val altDescription: String?,

    @Json(name = "links")
    val links: Links,

    @Json(name = "views")
    val views: Int,

    @Json(name = "height")
    val height: Int,

    @Json(name = "width")
    val width: Int,

    @Json(name = "blur_hash")
    val blurHash: String?,

    @Json(name = "user")
    val user: User,

)

@JsonClass(generateAdapter = true)
data class Urls(

    @Json(name = "small")
    val small: String,

    @Json(name = "small_s3")
    val smallS3: String,

    @Json(name = "thumb")
    val thumb: String,

    @Json(name = "raw")
    val raw: String,

    @Json(name = "regular")
    val regular: String,

    @Json(name = "full")
    val full: String
)

@JsonClass(generateAdapter = true)
data class ProfileImage(

    @Json(name = "small")
    val small: String,

    @Json(name = "large")
    val large: String,

    @Json(name = "medium")
    val medium: String
)

@JsonClass(generateAdapter = true)
data class Links(

    @Json(name = "portfolio")
    val portfolio: String?,

    @Json(name = "photos")
    val photos: String?,

    @Json(name = "download")
    val download: String?,

    @Json(name = "download_location")
    val downloadLocation: String?
)

@JsonClass(generateAdapter = true)
data class User(

    @Json(name = "total_photos")
    val totalPhotos: Int,

    @Json(name = "twitter_username")
    val twitterUsername: String?,

    @Json(name = "bio")
    val bio: String?,

    @Json(name = "total_likes")
    val totalLikes: Int,

    @Json(name = "portfolio_url")
    val portfolioUrl: String?,

    @Json(name = "profile_image")
    val profileImage: ProfileImage?,

    @Json(name = "name")
    val name: String,

    @Json(name = "links")
    val links: Links,

    @Json(name = "total_collections")
    val totalCollections: Int,

    @Json(name = "id")
    val id: String,

    @Json(name = "first_name")
    val firstName: String,

    @Json(name = "instagram_username")
    val instagramUsername: String?,

    @Json(name = "username")
    val username: String
)
