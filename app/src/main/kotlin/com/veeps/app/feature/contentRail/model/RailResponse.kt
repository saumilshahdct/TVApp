package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class RailResponse(@SerializedName("data") var railData: ArrayList<RailData> = arrayListOf())