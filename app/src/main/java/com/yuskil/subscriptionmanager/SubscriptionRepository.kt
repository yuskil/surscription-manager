package com.yuskil.subscriptionmanager

internal class SubscriptionRepository(private val dao: SubscriptionDao) {
    val subscriptions = dao.observeAll()

    suspend fun save(id: Long?, fields: SubscriptionFields): Long? {
        if (id == null) {
            return dao.insert(Subscription(fields = fields, createdAtMillis = System.currentTimeMillis()))
        }
        val existing = dao.find(id) ?: return null
        val updated = dao.update(existing.copy(fields = fields))
        return id.takeIf { updated == 1 }
    }
}
