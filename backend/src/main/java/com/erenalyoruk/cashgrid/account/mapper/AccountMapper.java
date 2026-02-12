package com.erenalyoruk.cashgrid.account.mapper;

import com.erenalyoruk.cashgrid.account.dto.AccountResponse;
import com.erenalyoruk.cashgrid.account.dto.CreateAccountRequest;
import com.erenalyoruk.cashgrid.account.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "currency", ignore = true)
    Account toEntity(CreateAccountRequest request);

    @Mapping(target = "currency", expression = "java(account.getCurrency().name())")
    AccountResponse toResponse(Account account);
}
