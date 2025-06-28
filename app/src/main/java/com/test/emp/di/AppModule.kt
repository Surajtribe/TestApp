package com.test.emp.di

import android.content.Context
import com.test.emp.data.repository.RandomTextRepository
import com.test.emp.presentation.viewmodel.RandomTextViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideRandomTextRepository(
        @ApplicationContext context: Context
    ): RandomTextRepository = RandomTextRepository(context.contentResolver)

    @Provides
    fun provideRandomTextViewModelFactory(
        repository: RandomTextRepository
    ): RandomTextViewModel = RandomTextViewModel(repository)

}