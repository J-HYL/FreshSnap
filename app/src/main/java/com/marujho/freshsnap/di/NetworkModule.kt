package com.marujho.freshsnap.di

import com.marujho.freshsnap.data.remote.api.OpenFoodFactsApi
import com.marujho.freshsnap.data.remote.api.TheMealDBApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenFoodFactsRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TheMealDBRetrofit




@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    @OpenFoodFactsRetrofit
    fun provideOpenFoodFactsRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    @TheMealDBRetrofit
    fun provideTheMealDBRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(@OpenFoodFactsRetrofit retrofit: Retrofit): OpenFoodFactsApi =
        retrofit.create(OpenFoodFactsApi::class.java)

    @Provides
    @Singleton
    fun provideTheMealDBApi(@TheMealDBRetrofit retrofit: Retrofit): TheMealDBApi =
        retrofit.create(TheMealDBApi::class.java)

}