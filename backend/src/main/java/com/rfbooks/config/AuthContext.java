package com.rfbooks.config;

public class AuthContext {

    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();

    private static final String DEFAULT_USER_ID = "resort-admin";

    public static void setCurrentUserId(String userId) {
        currentUserId.set(userId);
    }

    public static String getCurrentUserId() {
        String userId = currentUserId.get();
        return (userId != null) ? userId : DEFAULT_USER_ID;
    }

    public static void clear() {
        currentUserId.remove();
    }
}
