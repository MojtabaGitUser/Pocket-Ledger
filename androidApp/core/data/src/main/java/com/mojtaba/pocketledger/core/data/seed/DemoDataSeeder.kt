package com.mojtaba.pocketledger.core.data.seed

import com.mojtaba.pocketledger.core.data.repository.BudgetRepository
import com.mojtaba.pocketledger.core.data.repository.CategoryRepository
import com.mojtaba.pocketledger.core.data.repository.TagRepository
import com.mojtaba.pocketledger.core.data.repository.TransactionRepository

class DemoDataSeeder(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val tagRepository: TagRepository,
    private val seedData: DemoSeedData = DemoSeedData,
) {
    suspend fun seedDemoData(): DemoSeedResult {
        categoryRepository.upsertAll(seedData.categories)
        tagRepository.upsertAll(seedData.tags)
        transactionRepository.upsertAll(seedData.transactions)
        budgetRepository.upsertAll(seedData.budgets)
        seedData.transactionTagLinks.forEach { link ->
            tagRepository.addTagToTransaction(link)
        }
        return seedData.result
    }

    suspend fun isDemoDataPresent(): Boolean =
        categoryRepository.getById(DemoSeedIds.CategoryGroceries) != null &&
            tagRepository.getById(DemoSeedIds.TagEssential) != null &&
            transactionRepository.getById(DemoSeedIds.TransactionRent) != null &&
            budgetRepository.getById(DemoSeedIds.BudgetGroceries) != null
}
