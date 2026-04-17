package com.marujho.freshsnap.di

import com.marujho.freshsnap.BuildConfig
import com.marujho.freshsnap.data.remote.api.GroqApi
import com.marujho.freshsnap.data.remote.api.OpenFoodFactsApi
import com.marujho.freshsnap.data.remote.api.TheMealDbApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    // region OpenFoodFacts

    @Provides
    @Singleton
    @Named("openfoodfacts")
    fun provideOpenFoodFactsRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(@Named("openfoodfacts") retrofit: Retrofit): OpenFoodFactsApi =
        retrofit.create(OpenFoodFactsApi::class.java)

    // endregion

    // region TheMealDB

    @Provides
    @Singleton
    @Named("mealdb")
    fun provideMealDbRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideTheMealDbApi(@Named("mealdb") retrofit: Retrofit): TheMealDbApi =
        retrofit.create(TheMealDbApi::class.java)

    // endregion

    // region Groq AI

    @Provides
    @Singleton
    @Named("groq")
    fun provideGroqOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("groq")
    fun provideGroqRetrofit(
        moshi: Moshi,
        @Named("groq") okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideGroqApi(@Named("groq") retrofit: Retrofit): GroqApi =
        retrofit.create(GroqApi::class.java)

    // endregion
}
