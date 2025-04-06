package com.thatsoulyguy.moonlander.mod.accessor;

import java.lang.reflect.Method;

public class MethodAccessorHelper {

    @SuppressWarnings("unchecked")
    public static <T> MethodAccessor<T> createMethodAccessor(Class<?> targetClass, String methodName)
    {
        try
        {
            Method targetMethod = null;

            for (Method method : targetClass.getDeclaredMethods())
            {
                if (method.getName().equals(methodName))
                {
                    if (targetMethod != null)
                        throw new IllegalStateException("Multiple methods with name " + methodName + " found in " + targetClass.getName());

                    targetMethod = method;
                }
            }

            if (targetMethod == null)
                throw new NoSuchMethodException("Method " + methodName + " not found in " + targetClass.getName());

            targetMethod.setAccessible(true);

            Method finalTargetMethod = targetMethod;

            return (instance, args) ->
            {
                try
                {
                    return (T) finalTargetMethod.invoke(instance, args);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            };
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}