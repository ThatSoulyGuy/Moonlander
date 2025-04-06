package com.thatsoulyguy.moonlander.mod.accessor;

import com.thatsoulyguy.moonlander.annotation.Static;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Static
public class PatchMethodAccessorManager
{
    private static final List<Class<?>> patchClasses = new ArrayList<>();

    private PatchMethodAccessorManager() { }

    public static void register(@NotNull Class<?> patchClass)
    {
        patchClasses.add(patchClass);
    }

    public static void applyPatches()
    {
        for (Class<?> patchClass : patchClasses)
        {
            for (Field field : patchClass.getDeclaredFields())
            {
                if (field.isAnnotationPresent(MethodReference.class))
                {
                    MethodReference annotation = field.getAnnotation(MethodReference.class);

                    String spec = annotation.value();
                    String[] parts = spec.split("::");

                    if (parts.length != 2)
                        throw new IllegalArgumentException("Invalid method reference spec: " + spec);

                    String targetClassName = parts[0];
                    String targetMethodName = parts[1];

                    try
                    {
                        Class<?> targetClass = patchClass.getClassLoader().loadClass(targetClassName);

                        Type genericType = field.getGenericType();

                        if (genericType instanceof ParameterizedType pt)
                        {
                            Type[] typeArgs = pt.getActualTypeArguments();

                            if (typeArgs.length > 0 && typeArgs[0] instanceof Class)
                                ;
                            else
                                ;
                        }
                        else
                            ;

                        MethodAccessor<?> accessor = MethodAccessorHelper.createMethodAccessor(targetClass, targetMethodName);

                        field.setAccessible(true);
                        field.set(null, accessor);

                        System.out.println("Injected method accessor for " + spec + " into " + patchClass.getName());
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Failed to inject method accessor for field: " + field.getName(), e);
                    }
                }
            }
        }
    }
}