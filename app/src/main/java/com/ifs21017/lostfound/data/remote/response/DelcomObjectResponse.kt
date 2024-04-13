package com.ifs21017.lostfound.data.remote.response

import com.google.gson.annotations.SerializedName

data class DelcomObjectResponse(

	@field:SerializedName("data")
	val data: DataObjectResponse,

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("message")
	val message: String
)

data class LostFoundObjectResponse(

	@field:SerializedName("cover")
	val cover: Any,

	@field:SerializedName("updated_at")
	val updatedAt: String,

	@field:SerializedName("user_id")
	val userId: Int,

	@field:SerializedName("author")
	val author: Author,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("title")
	val title: String,

	@field:SerializedName("is_completed")
	val isCompleted: Int,

	@field:SerializedName("status")
	val status: String
)

data class DataObjectResponse(

	@field:SerializedName("lost_found")
	val lostFound: LostFoundObjectResponse
)
