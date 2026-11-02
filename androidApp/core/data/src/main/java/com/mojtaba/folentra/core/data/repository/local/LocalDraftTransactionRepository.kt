package com.mojtaba.folentra.core.data.repository.local

import com.mojtaba.folentra.core.data.model.DraftTransaction
import com.mojtaba.folentra.core.data.repository.DraftTransactionRepository
import com.mojtaba.folentra.core.database.dao.DraftTransactionDao
import com.mojtaba.folentra.core.database.model.DraftTransactionEntity
import com.mojtaba.folentra.core.database.model.TransactionEntity
import com.mojtaba.folentra.shared.domain.transaction.TransactionFormState
import com.mojtaba.folentra.shared.domain.transaction.TransactionFormValidation
import com.mojtaba.folentra.shared.domain.transaction.TransactionType
import kotlinx.coroutines.flow.map

class LocalDraftTransactionRepository(
    private val dao: DraftTransactionDao,
    private val now: () -> Long = System::currentTimeMillis,
) : DraftTransactionRepository {
    override fun observePending() = dao.observePending().map { rows -> rows.map { it.toModel() } }

    override suspend fun save(draft: DraftTransaction) {
        require(draft.id.isNotBlank() && draft.source.isNotBlank())
        require(draft.amountInput.length <= 100 && draft.currencyCode.length <= 20)
        require(draft.merchant.length <= TransactionFormValidation.MERCHANT_MAX_LENGTH)
        require(draft.note.length <= TransactionFormValidation.NOTE_MAX_LENGTH)
        dao.savePending(draft.toEntity().copy(updatedAt = now()))
    }

    override suspend fun confirm(draft: DraftTransaction): String {
        dao.getById(draft.id)?.confirmedTransactionId?.let { return it }
        require(draft.type in listOf("income", "expense"))
        val timestamp = now()
        val input = requireNotNull(TransactionFormValidation.validate(TransactionFormState(
            amountInput = draft.amountInput, currencyCode = draft.currencyCode,
            transactionType = if (draft.type == "income") TransactionType.INCOME else TransactionType.EXPENSE,
            occurredAt = draft.occurredAt, categoryId = draft.categoryId,
            merchant = draft.merchant, note = draft.note,
        ), timestamp).validatedInput) { "Review the required fields" }
        return dao.confirm(draft.toEntity().copy(updatedAt = timestamp), TransactionEntity(
            id = "draft:${draft.id}",
            amountMinor = if (input.type == TransactionType.EXPENSE) -input.amountMinor else input.amountMinor,
            currencyCode = input.currencyCode, type = draft.type, occurredAt = input.occurredAt,
            categoryId = input.categoryId, merchant = input.merchant, note = input.note,
            source = draft.source, createdAt = timestamp, updatedAt = timestamp,
        ))
    }

    override suspend fun discard(id: String) { dao.discard(id) }
}

private fun DraftTransaction.toEntity() = DraftTransactionEntity(id, amountInput, currencyCode, type,
    occurredAt, categoryId, merchant, note, source, createdAt, updatedAt, confirmedTransactionId)
private fun DraftTransactionEntity.toModel() = DraftTransaction(id, amountInput, currencyCode, type,
    occurredAt, categoryId, merchant, note, source, createdAt, updatedAt, confirmedTransactionId)
