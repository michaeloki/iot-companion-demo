package com.inspirati.iotcompanion.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Source {

    @SerializedName("title")
    @Expose
    var title: String? = null
    @SerializedName("slug")
    @Expose
    var slug: String? = null
    @SerializedName("url")
    @Expose
    var url: String? = null
    @SerializedName("crawl_rate")
    @Expose
    var crawlRate: Int? = null

}