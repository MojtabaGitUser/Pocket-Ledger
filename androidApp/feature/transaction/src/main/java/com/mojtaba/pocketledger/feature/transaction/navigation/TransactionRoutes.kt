package com.mojtaba.pocketledger.feature.transaction.navigation

import android.net.Uri

object TransactionRoutes {
    const val ListRoute = "transactions/list"
    const val TransactionIdArg = "transactionId"
    const val DetailRoutePattern = "transactions/detail/{$TransactionIdArg}"
    const val EditRoutePattern = "transactions/edit/{$TransactionIdArg}"

    fun detailRoute(transactionId: String): String =
        "transactions/detail/${Uri.encode(transactionId)}"

    fun editRoute(transactionId: String): String =
        "transactions/edit/${Uri.encode(transactionId)}"
}
