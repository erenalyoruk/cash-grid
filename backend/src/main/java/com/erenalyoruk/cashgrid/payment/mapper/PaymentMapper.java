package com.erenalyoruk.cashgrid.payment.mapper;

import com.erenalyoruk.cashgrid.payment.dto.PaymentResponse;
import com.erenalyoruk.cashgrid.payment.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "sourceIban", source = "sourceAccount.iban")
    @Mapping(target = "targetIban", source = "targetAccount.iban")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdByUsername", source = "createdBy.username")
    @Mapping(
            target = "approvedByUsername",
            expression =
                    "java(payment.getApprovedBy() != null ? payment.getApprovedBy().getUsername() :"
                            + " null)")
    PaymentResponse toResponse(Payment payment);
}
