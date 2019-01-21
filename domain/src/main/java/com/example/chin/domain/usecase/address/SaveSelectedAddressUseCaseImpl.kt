package com.example.chin.domain.usecase.address

import com.example.chin.data.entities.address.AddressModel
import com.example.chin.data.gateways.address.AddressLocalGateway
import com.example.chin.domain.usecase.SaveSelectedAddressUseCase
import com.example.chin.domain.usecase.UseCaseResponse
import kotlinx.coroutines.Job
import java.util.*
import javax.inject.Inject

class SaveSelectedAddressUseCaseImpl @Inject constructor(
    override val job: Job,
    private val addressLocalGateway: AddressLocalGateway
) : SaveSelectedAddressUseCase {

    override suspend fun run(input: String): UseCaseResponse<Unit> {

        if(addressLocalGateway.getAddress(input) != null){

            addressLocalGateway.updateAddress(
                AddressModel(input, Date())
            )

        }else{

            addressLocalGateway.insertAddress(
                AddressModel(input, Date())
            )

        }

        return UseCaseResponse(output = Unit)

    }

}