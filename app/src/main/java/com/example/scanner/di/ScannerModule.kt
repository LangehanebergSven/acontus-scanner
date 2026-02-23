package com.example.scanner.di

import com.example.scanner.scanner.BarcodeScanner
import com.example.scanner.scanner.KeyenceBarcodeScanner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScannerModule {

    @Binds
    @Singleton
    abstract fun bindBarcodeScanner(
        keyenceBarcodeScanner: KeyenceBarcodeScanner
    ): BarcodeScanner
}
