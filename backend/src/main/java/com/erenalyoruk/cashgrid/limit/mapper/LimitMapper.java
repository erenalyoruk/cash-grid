package com.erenalyoruk.cashgrid.limit.mapper;

import com.erenalyoruk.cashgrid.limit.dto.LimitResponse;
import com.erenalyoruk.cashgrid.limit.model.Limit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LimitMapper {

    @Mapping(target = "role", expression = "java(limit.getRole().name())")
    LimitResponse toResponse(Limit limit);
}
