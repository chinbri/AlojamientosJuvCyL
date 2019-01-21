package com.example.chin.domain.usecase.address

import com.example.chin.data.gateways.address.AddressLocalGateway
import com.example.chin.domain.usecase.GetAddressListUseCase
import com.example.chin.domain.usecase.UseCaseResponse
import kotlinx.coroutines.Job
import javax.inject.Inject

class GetAddressListUseCaseImpl @Inject constructor(
    override val job: Job,
    private val addressLocalGateway: AddressLocalGateway
) : GetAddressListUseCase {

    override suspend fun run(input: Unit): UseCaseResponse<List<String>> {

        addressLocalGateway.deleteOldAddresses()

        return UseCaseResponse(output = addressLocalGateway.getAddresses().map {
            it.address
        })

    }

}