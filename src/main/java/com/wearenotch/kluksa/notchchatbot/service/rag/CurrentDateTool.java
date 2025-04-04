package com.wearenotch.kluksa.notchchatbot.service.rag;

import java.util.function.Supplier;

public class CurrentDateTool implements Supplier<CurrentDateTool.Response> {

    public record Response(String date) {}
    public Response get() {
        return new Response(java.time.LocalDate.now().toString());
    }
}
