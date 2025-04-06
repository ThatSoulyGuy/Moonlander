package com.thatsoulyguy.moonlander.mod;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class AccessorHelper {

    public static <T> @NotNull FieldAccessor<T> createFieldAccessor(@NotNull Class<?> targetClass, @NotNull String fieldName, @NotNull Class<T> fieldType)
    {
        try
        {
            Field field = targetClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            return new FieldAccessor<>()
            {
                @Override
                public T get(Object instance)
                {
                    try
                    {
                        return fieldType.cast(field.get(instance));
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void set(Object instance, T value)
                {
                    try
                    {
                        field.set(instance, value);
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Field '" + fieldName + "' not found in " + targetClass.getName(), e);
        }
    }
}