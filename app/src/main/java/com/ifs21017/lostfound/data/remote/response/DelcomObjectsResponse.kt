package com.ifs21017.lostfound.data.remote.response

import com.google.gson.annotations.SerializedName

data class DelcomObjectsResponse(

	@field:SerializedName("data")
	val data: DataObjectsResponse,

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("message")
	val message: String
)

data class DataObjectsResponse(

	@field:SerializedName("lost_founds")
	val lostFounds: List<LostFoundsItemResponse>
)

data class LostFoundsItemResponse(

	@field:SerializedName("cover")
	val cover: String?,

	@field:SerializedName("updated_at")
	val updatedAt: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("title")
	val title: String,

	@field:SerializedName("is_completed")
	var isCompleted: Int,

	@field:SerializedName("status")
	val status: String
)
