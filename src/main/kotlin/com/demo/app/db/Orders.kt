package com.demo.app.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Orders : LongIdTable("orders") {
    val description = varchar("description", 120)
    val priceInPence = long("price_in_pence")
    val completedStatus = bool("completed_status").default(false)
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
//    val userIdFk = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)

}