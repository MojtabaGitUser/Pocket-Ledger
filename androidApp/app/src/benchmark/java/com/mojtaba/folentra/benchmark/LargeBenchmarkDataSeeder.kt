package com.mojtaba.folentra.benchmark

import com.mojtaba.folentra.core.data.model.TransactionTagLink
import com.mojtaba.folentra.core.data.repository.BudgetRepository
import com.mojtaba.folentra.core.data.repository.CategoryRepository
import com.mojtaba.folentra.core.data.repository.TagRepository
import com.mojtaba.folentra.core.data.repository.TransactionRepository
import com.mojtaba.folentra.core.testing.performance.LargeBenchmarkDataset
import com.mojtaba.folentra.core.testing.performance.LargeBenchmarkSeedData

class LargeBenchmarkDataSeeder(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val tagRepository: TagRepository,
    private val seedData: LargeBenchmarkSeedData = LargeBenchmarkDataset.create(),
) {
    suspend fun seedLargeDataset(): LargeBenchmarkSeedResult {
        categoryRepository.upsertAll(seedData.categories)
        tagRepository.upsertAll(seedData.tags)
        seedData.budgets.chunked(ChunkSize).forEach { budgets ->
            budgetRepository.upsertAll(budgets)
        }
        seedData.transactions.chunked(ChunkSize).forEach { transactions ->
            transactionRepository.upsertAll(transactions)
        }
        seedData.transactionTagLinks.forEach { link ->
            tagRepository.addTagToTransaction(TransactionTagLink(link.transactionId, link.tagId))
        }
        return LargeBenchmarkSeedResult(
            categoriesUpserted = seedData.categories.size,
            budgetsUpserted = seedData.budgets.size,
            tagsUpserted = seedData.tags.size,
            transactionsUpserted = seedData.transactions.size,
            linksUpserted = seedData.transactionTagLinks.size,
        )
    }

    private companion object {
        const val ChunkSize = 500
    }
}

data class LargeBenchmarkSeedResult(
    val categoriesUpserted: Int,
    val budgetsUpserted: Int,
    val tagsUpserted: Int,
    val transactionsUpserted: Int,
    val linksUpserted: Int,
)
