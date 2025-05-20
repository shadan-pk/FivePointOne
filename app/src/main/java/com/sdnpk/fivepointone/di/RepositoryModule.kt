package com.sdnpk.fivepointone.di

//import com.sdnpk.fivepointone.repository.SpeakerRepositoryImpl
import com.sdnpk.fivepointone.repository.SpeakerRepository
import com.sdnpk.fivepointone.repository.SpeakerRepositoryImpl
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
    abstract fun bindSpeakerRepository(
        impl: SpeakerRepositoryImpl
    ): SpeakerRepository
}
