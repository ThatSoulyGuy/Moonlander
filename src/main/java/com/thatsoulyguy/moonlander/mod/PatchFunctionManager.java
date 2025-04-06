package com.thatsoulyguy.moonlander.mod;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PatchFunctionManager
{
    private static final List<Class<?>> patchClasses = new ArrayList<>();

    private static final Map<String, PatchInvoker> patchInvokers = new ConcurrentHashMap<>();

    public static void register(@NotNull Class<?> object)
    {
        patchClasses.add(object);
    }

    public static PatchInvoker getInvoker(String targetSpec)
    {
        return patchInvokers.get(targetSpec);
    }

    public static void applyPatches()
    {
        for (Class<?> patchClass : patchClasses)
        {
            try
            {
                Object patchInstance = patchClass.getDeclaredConstructor().newInstance();

                for (Method patchMethod : patchClass.getDeclaredMethods())
                {
                    if (patchMethod.isAnnotationPresent(PatchFunction.class))
                    {
                        PatchFunction annotation = patchMethod.getAnnotation(PatchFunction.class);

                        String targetSpec = annotation.value();
                        String[] parts = targetSpec.split("::");

                        if (parts.length != 2)
                        {
                            System.err.println("Invalid patch spec: " + targetSpec);
                            continue;
                        }

                        String targetClassName = parts[0];
                        String targetMethodName = parts[1];

                        Class<?> targetClass = patchClass.getClassLoader().loadClass(targetClassName);

                        Method targetMethod = null;

                        Class<?>[] patchParams = patchMethod.getParameterTypes();

                        for (Method method : targetClass.getDeclaredMethods())
                        {
                            if (method.getName().equals(targetMethodName))
                            {
                                Class<?>[] targetParams = method.getParameterTypes();

                                if (patchParams.length == targetParams.length + 1)
                                {
                                    boolean match = true;

                                    for (int i = 0; i < targetParams.length; i++)
                                    {
                                        if (!targetParams[i].equals(patchParams[i + 1]))
                                        {
                                            match = false;
                                            break;
                                        }
                                    }

                                    if (match)
                                    {
                                        targetMethod = method;
                                        break;
                                    }
                                }
                            }
                        }

                        if (targetMethod == null)
                        {
                            System.err.println("Could not find matching target method for patch: " + targetSpec);
                            continue;
                        }

                        if (Modifier.isAbstract(targetMethod.getModifiers()))
                        {
                            System.err.println("Cannot patch abstract method: " + targetSpec);
                            continue;
                        }

                        PatchInvoker invoker = (self, args) ->
                        {
                            Object[] newArgs = new Object[args.length + 1];

                            newArgs[0] = self;

                            System.arraycopy(args, 0, newArgs, 1, args.length);

                            try
                            {
                                patchMethod.invoke(patchInstance, newArgs);
                            }
                            catch (Exception ex)
                            {
                                throw new RuntimeException(ex);
                            }
                        };

                        patchInvokers.put(targetSpec, invoker);

                        new ByteBuddy()
                                .rebase(targetClass)
                                .visit(Advice.to(PatchAdvice.class)
                                        .on(ElementMatchers.named(targetMethodName)
                                                .and(ElementMatchers.takesArguments(targetMethod.getParameterTypes()))))
                                .make()
                                .load(targetClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

                        System.out.println("Patched " + targetSpec + " successfully.");
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("Failed to apply patch for class " + patchClass.getName());
                e.printStackTrace();
            }
        }
    }
}