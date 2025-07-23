package com.demo.app.db

import org.jetbrains.exposed.dao.id.LongIdTable

object Users : LongIdTable("users") {
    val firstName = varchar("first_name", length = 50)
    val lastName = varchar("last_name", length = 50)
    val email = varchar("email", length = 120)
    val firstLineOfAddress = varchar("first_line_of_address", length = 50)
    val secondLineOfAddress = varchar("second_line_of_address", length = 50).nullable()
    val town = varchar("town", length = 50)
    val postCode = varchar("post_code", length = 10)

}