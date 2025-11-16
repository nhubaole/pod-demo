package com.buildlab.common.store

interface ContentStore {
    fun save(content: String)
    fun retrieve(): String
}