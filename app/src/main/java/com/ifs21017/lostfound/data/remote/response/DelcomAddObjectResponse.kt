package com.ifs21017.lostfound.data.remote.response

import com.google.gson.annotations.SerializedName

data class DelcomAddObjectResponse(

	@field:SerializedName("data")
	val data: DataAddObjectResponse,

	@field:SerializedName("success")
	val success: Boolean,

	@field:SerializedName("message")
	val message: String
)

data class DataAddObjectResponse(

	@field:SerializedName("lost_found_id")
	val lostFoundId: Int
)
