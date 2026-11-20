package com.yuskil.subscriptionmanager

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

internal enum class Cycle(val label: String, val paymentsPerYear: Int) {
    Monthly("월간", 12), Quarterly("분기", 4), Yearly("연간", 1),
    ThirtyDays("30일", 0), Unknown("확인 필요", 0)
}

internal data class SubscriptionFields(
    val name: String,
    val price: Long,
    val cycle: Cycle,
    val date: LocalDate,
    val category: String,
    val memo: String,
    val active: Boolean,
    @ColumnInfo(defaultValue = "''") val plan: String = "",
    @ColumnInfo(defaultValue = "''") val channel: String = "",
    @ColumnInfo(defaultValue = "''") val creator: String = "",
    @ColumnInfo(defaultValue = "'KRW'") val currency: String = "KRW",
    @ColumnInfo(defaultValue = "'Auto'") val renewal: Renewal = Renewal.Auto,
    @ColumnInfo(defaultValue = "0") val reminder: Boolean = false,
    val purchaseDate: LocalDate? = null
)

@Entity(tableName = "subscriptions")
internal data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Embedded val fields: SubscriptionFields,
    val createdAtMillis: Long
)

internal class BillingDateConverter {
    @TypeConverter
    fun toEpochDay(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun fromEpochDay(day: Long): LocalDate = LocalDate.ofEpochDay(day)
}

@Dao
internal interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY date, id")
    fun observeAll(): Flow<List<Subscription>>

    @Insert
    suspend fun insert(subscription: Subscription): Long

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun find(id: Long): Subscription?

    @Update
    suspend fun update(subscription: Subscription): Int
}

@Database(entities = [Subscription::class], version = 2, exportSchema = false)
@TypeConverters(BillingDateConverter::class)
internal abstract class SubscriptionDatabase : RoomDatabase() {
    abstract fun subscriptions(): SubscriptionDao

    companion object {
        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN plan TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN channel TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN creator TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN currency TEXT NOT NULL DEFAULT 'KRW'")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN renewal TEXT NOT NULL DEFAULT 'Auto'")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN reminder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN purchaseDate INTEGER")
            }
        }

        @Volatile
        private var instance: SubscriptionDatabase? = null

        fun get(context: Context): SubscriptionDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                SubscriptionDatabase::class.java,
                "subscriptions.db"
            ).addMigrations(migration1To2).build().also { instance = it }
        }
    }
}
