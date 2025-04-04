package com.wearenotch.kluksa.notchchatbot.service.rag;


import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.function.Function;

@Slf4j
public class RevenueTool implements Function<RevenueTool.Request, RevenueTool.Response> {

    public record Request(Integer year) {}
    public record Response(BigDecimal height, Unit unit) {}
    public enum Unit {
        EUR, HRK
    }

    public Response apply(Request request) {
        final BigDecimal revenue = BigDecimal.valueOf(request.year*1000. + Math.random() * 1000000);
        log.info("Year: {}, Revenue: {}", request.year, revenue);
        return new Response(revenue, Unit.EUR);
    }
}
