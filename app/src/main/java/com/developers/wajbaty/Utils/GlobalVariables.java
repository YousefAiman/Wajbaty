package com.developers.wajbaty.Utils;

public class GlobalVariables {

    private static String currentRestaurantId;

    public static String getCurrentRestaurantId() {
        return currentRestaurantId;
    }

    public static void setCurrentRestaurantId(String currentRestaurantId) {
        GlobalVariables.currentRestaurantId = currentRestaurantId;
    }
}
