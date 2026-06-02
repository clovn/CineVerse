package com.cineverse.core.database

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DatabaseDriverFactory): CineVerseDatabase {
    val driver = driverFactory.createDriver()
    return CineVerseDatabase(driver)
}
