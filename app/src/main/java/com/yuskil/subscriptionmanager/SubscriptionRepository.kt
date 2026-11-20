package com.yuskil.subscriptionmanager

internal class SubscriptionRepository(private val dao: SubscriptionDao) {
    val subscriptions = dao.observeAll()

    suspend fun save(id: Long?, fields: SubscriptionFields): Long? {
        if (id == null) {
            return dao.insert(Subscription(fields = fields, createdAtMillis = System.currentTimeMillis()))
        }
        val updated = dao.update(
            id, fields.name, fields.price, fields.cycle, fields.date,
            fields.category, fields.memo, fields.active
        )
        return id.takeIf { updated == 1 }
    }
}
