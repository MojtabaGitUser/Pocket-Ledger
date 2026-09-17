package com.mojtaba.folentra.core.data.seed

import com.mojtaba.folentra.core.data.repository.CategoryRepository
import kotlinx.coroutines.flow.first

class DefaultCategorySeeder(
    private val categoryRepository: CategoryRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {
    /**
     * Populates a new or previously affected empty database without changing an
     * existing category catalogue. This makes repeated startup calls safe and
     * preserves user-restored, renamed, or deactivated categories.
     */
    suspend fun seedIfEmpty(): Int {
        if (categoryRepository.observeAll().first().isNotEmpty()) return 0

        val categories = DefaultCategoryCatalog.categories(currentTimeMillis())
        return categoryRepository.insertAllIfAbsent(categories)
    }
}
