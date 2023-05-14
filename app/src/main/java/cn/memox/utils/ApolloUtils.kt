package cn.memox.utils

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloRequest
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.interceptor.ApolloInterceptor
import com.apollographql.apollo3.interceptor.ApolloInterceptorChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

fun apollo(): ApolloClient {
    return ApolloClient.Builder()
        .serverUrl(kv.getString("remote_url") ?: "https://memox.usfl.cn/query")
        .addInterceptor(authInterceptor())
        .build()
}

fun authInterceptor(): ApolloInterceptor {
    return object : ApolloInterceptor {
        override fun <D : Operation.Data> intercept(
            request: ApolloRequest<D>,
            chain: ApolloInterceptorChain
        ): Flow<ApolloResponse<D>> {
            val token = kv.token ?: return chain.proceed(request)
            val newRequest = request.newBuilder()
                .addHttpHeader("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        }
    }
}


fun <T : Operation.Data> Flow<ApolloResponse<T>>.onSuccess(onSuccess: (T) -> Unit): Flow<ApolloResponse<T>> {
    return onEach {
        println("dataIt:${it.data}")
        val data = it.data
        if (data != null) {
            onSuccess(data)
        }
    }
}

fun <T : Operation.Data> Flow<ApolloResponse<T>>.onError(onError: (List<Error>) -> Unit): Flow<ApolloResponse<T>> {
    return onEach {
        println("errorsIt:${it.errors}")
        val errors = it.errors
        if (errors != null) {
            onError(errors)
        }
    }
}

fun <T : Operation.Data> Flow<ApolloResponse<T>>.defaultErrorHandler(handler: (String) -> Unit): Flow<ApolloResponse<T>> {
    return onError { errors ->
        handler(errors.joinToString(separator = "\n") { it.message })
    }
}