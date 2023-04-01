package com.example.demo;

import java.lang.reflect.Field;

public class TestsUtils {


    public static void injectObjects(Object target, String field, Object toInject){

        boolean privateField = false;

        try {
            Field targetField = target.getClass().getDeclaredField(field);
            if (!targetField.isAccessible()){
                targetField.setAccessible(true);
                privateField = true;
            }
            targetField.set(target, toInject);
            if (privateField){
                targetField.setAccessible(false);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
