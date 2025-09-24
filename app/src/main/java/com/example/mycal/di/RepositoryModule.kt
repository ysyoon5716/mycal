package com.example.mycal.di

import com.example.mycal.data.repository.CalendarRepositoryImpl
import com.example.mycal.data.repository.CalendarSourceRepositoryImpl
import com.example.mycal.domain.repository.CalendarRepository
import com.example.mycal.domain.repository.CalendarSourceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        calendarRepositoryImpl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindCalendarSourceRepository(
        calendarSourceRepositoryImpl: CalendarSourceRepositoryImpl
    ): CalendarSourceRepository
}