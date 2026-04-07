package com.wallstreet.domain.model

data class Strategy(

    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val isCustom: Boolean = false,
    val createAt: Long? = null,

    )
