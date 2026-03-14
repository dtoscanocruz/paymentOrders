package com.example.hpayments.adapters.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface PaymentOrderMapper {

    com.example.hpayments.ports.dto.SubmitPaymentOrderRequest toPort(com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderRequest api);

    com.example.hpayments.rest.adapter.dto.SubmitPaymentOrderResponse toApi(com.example.hpayments.ports.dto.SubmitPaymentOrderResponse port);

    @Mapping(target = "lastUpdate", source = "lastUpdate", qualifiedByName = "toOffsetDateTime")
    com.example.hpayments.rest.adapter.dto.GetPaymentOrderStatusResponse toApi(com.example.hpayments.ports.dto.GetPaymentOrderStatusResponse port);

    @Named("toOffsetDateTime")
    default OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atOffset(ZoneOffset.UTC);
    }

}
