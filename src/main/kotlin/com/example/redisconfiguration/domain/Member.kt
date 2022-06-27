package com.example.redisconfiguration.domain

import java.util.Objects

class Member private constructor(
    id: Long,
    name: String,
) {

    var id: Long = id
        private set

    var name: String = name
        private set

    override fun hashCode(): Int = Objects.hash(id, name)
    override fun toString(): String = "Member(id=$id, name=$name)"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherMember = (other as? Member) ?: return false
        return this.id == otherMember.id && this.name == otherMember.name
    }

    companion object {
        fun create(id: Long, name: String): Member {
            return Member(id = id, name = name)
        }
    }
}
