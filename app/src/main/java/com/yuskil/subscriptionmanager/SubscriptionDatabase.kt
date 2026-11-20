package com.yuskil.subscriptionmanager

import android.content.Context
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
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

internal enum class Cycle(val label: String, val paymentsPerYear: Int) {
    Monthly("월간", 12), Quarterly("분기", 4), Yearly("연간", 1)
}

internal data class SubscriptionFields(
    val name: String,
    val price: Long,
    val cycle: Cycle,
    val date: LocalDate,
    val category: String,
    val memo: String,
    val active: Boolean
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

    @Query(
        """
        UPDATE subscriptions
        SET name = :name, price = :price, cycle = :cycle, date = :date,
            category = :category, memo = :memo, active = :active
        WHERE id = :id
        """
    )
    suspend fun update(
        id: Long,
        name: String,
        price: Long,
        cycle: Cycle,
        date: LocalDate,
        category: String,
        memo: String,
        active: Boolean
    ): Int
}

@Database(entities = [Subscription::class], version = 1, exportSchema = false)
@TypeConverters(BillingDateConverter::class)
internal abstract class SubscriptionDatabase : RoomDatabase() {
    abstract fun subscriptions(): SubscriptionDao

    companion object {
        @Volatile
        private var instance: SubscriptionDatabase? = null

        fun get(context: Context): SubscriptionDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                SubscriptionDatabase::class.java,
                "subscriptions.db"
            ).build().also { instance = it }
        }
    }
}
