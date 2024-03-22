package com.example.todolist.modules

import com.example.todolist.api.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@TestInstallIn(components = [SingletonComponent::class], replaces = [ApiModule::class])
@Module
class TestApiModule
{
    @Singleton
    @Provides
    fun provideMockWebServer(): MockWebServer{
        return MockWebServer()
    }

    @Singleton
    @Provides
    fun provideRetrofit(mockWebServer: MockWebServer): Retrofit
    {
        return Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideApi(retrofit: Retrofit): Api
    {
        return retrofit.create(Api::class.java)
    }
}