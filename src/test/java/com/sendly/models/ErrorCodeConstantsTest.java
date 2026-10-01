package com.sendly.models;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The error code classes list the codes the API answers with.
 */
class ErrorCodeConstantsTest {

    private static Set<String> codesOf(Class<?> type) throws IllegalAccessException {
        Set<String> codes = new HashSet<>();
        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && field.getType() == String.class) {
                codes.add((String) field.get(null));
            }
        }
        return codes;
    }

    @Test
    void testCallErrorCode_listsFromNumberNotSupported() throws Exception {
        assertTrue(codesOf(CallErrorCode.class).contains("from_number_not_supported"));
    }

    @Test
    void testErrorCodeClasses_listTheApiKeyCheckRefusals() throws Exception {
        for (Class<?> type : new Class<?>[] {CallErrorCode.class, RcsErrorCode.class}) {
            Set<String> codes = codesOf(type);
            assertTrue(codes.contains("too_many_failed_key_attempts"), type.getSimpleName());
            assertTrue(codes.contains("too_many_concurrent_verifications"), type.getSimpleName());
        }
    }
}
