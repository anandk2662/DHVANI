package com.example.dhvani.di

import android.content.Context
import com.example.dhvani.ml.HandModelInference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun provideHandModelInference(@ApplicationContext context: Context): HandModelInference {
        return HandModelInference(context)
    }
}
