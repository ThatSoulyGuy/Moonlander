package com.thatsoulyguy.moonlander.mod.patch;


import com.thatsoulyguy.moonlander.annotation.Static;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Static
public class CentralPatchManager
{
    private static final List<Class<?>> patchFunctionClasses = new ArrayList<>();
    private static final List<Class<?>> injectionFunctionClasses = new ArrayList<>();

    private static final Map<String, PatchInvoker> patchInvokers = new ConcurrentHashMap<>();

    private static final Map<String, Map<InjectionPoint, List<InjectionInvoker>>> injectionInvokers = new ConcurrentHashMap<>();

    private CentralPatchManager() { }

    public static void registerPatchClass(@NotNull Class<?> patchClass)
    {
        patchFunctionClasses.add(patchClass);
    }

    public static void registerInjectionClass(@NotNull Class<?> patchClass)
    {
        injectionFunctionClasses.add(patchClass);
    }

    public static PatchInvoker getPatchInvoker(@NotNull String targetSpec)
    {
        return patchInvokers.get(targetSpec);
    }

    public static List<InjectionInvoker> getInjectionInvokers(@NotNull String targetSpec, @NotNull InjectionPoint injectionPoint)
    {
        Map<InjectionPoint, List<InjectionInvoker>> map = injectionInvokers.get(targetSpec);

        if (map == null)
            return List.of();

        return map.getOrDefault(injectionPoint, List.of());
    }

    public static void applyAllPatches()
    {
        for (Class<?> patchClass : patchFunctionClasses)
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

                        System.out.println("Registered patch function for " + targetSpec + " from " + patchClass.getName());
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("Failed to process patch functions for " + patchClass.getName());
                e.printStackTrace();
            }
        }

        for (Class<?> patchClass : injectionFunctionClasses)
        {
            try
            {
                Object patchInstance = patchClass.getDeclaredConstructor().newInstance();

                for (Method injectMethod : patchClass.getDeclaredMethods())
                {
                    if (injectMethod.isAnnotationPresent(InjectFunction.class))
                    {
                        InjectFunction annotation = injectMethod.getAnnotation(InjectFunction.class);

                        String targetSpec = annotation.value();
                        InjectionPoint injectionPoint = annotation.injectionPoint();

                        InjectionInvoker invoker = (self, args) ->
                        {
                            Object[] newArgs = new Object[args.length + 1];

                            newArgs[0] = self;

                            System.arraycopy(args, 0, newArgs, 1, args.length);

                            try
                            {
                                injectMethod.invoke(patchInstance, newArgs);
                            }
                            catch (Exception e)
                            {
                                throw new RuntimeException(e);
                            }
                        };

                        injectionInvokers
                                .computeIfAbsent(targetSpec, k -> new ConcurrentHashMap<>())
                                .computeIfAbsent(injectionPoint, k -> new ArrayList<>())
                                .add(invoker);

                        System.out.println("Registered injection for " + targetSpec + ":" + injectionPoint.name() + " from " + patchClass.getName());
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("Failed to process injection patches for " + patchClass.getName());
                e.printStackTrace();
            }
        }

        Map<Class<?>, Set<String>> targetsByClass = new HashMap<>();

        Set<String> allTargetSpecs = new HashSet<>();

        allTargetSpecs.addAll(patchInvokers.keySet());
        allTargetSpecs.addAll(injectionInvokers.keySet());

        for (String spec : allTargetSpecs)
        {
            String[] parts = spec.split("::");

            if (parts.length != 2)
                continue;

            try
            {
                Class<?> clazz = Class.forName(parts[0]);
                targetsByClass.computeIfAbsent(clazz, k -> new HashSet<>()).add(spec);
            }
            catch (ClassNotFoundException e)
            {
                System.err.println("Target class not found for spec: " + spec);
                e.printStackTrace();
            }
        }

        for (Map.Entry<Class<?>, Set<String>> entry : targetsByClass.entrySet())
        {
            Class<?> targetClass = entry.getKey();
            Set<String> specs = entry.getValue();

            Set<String> methodNames = new HashSet<>();

            for (String spec : specs)
            {
                String[] parts = spec.split("::");

                if (parts.length == 2)
                    methodNames.add(parts[1]);
            }
            try
            {
                new ByteBuddy()
                        .rebase(targetClass)
                        .visit(Advice.to(CentralAdvice.class).on(ElementMatchers.namedOneOf(methodNames.toArray(new String[0]))))
                        .make()
                        .load(targetClass.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());

                System.out.println("Applied central instrumentation to class " + targetClass.getName() + " for methods: " + methodNames);
            }
            catch (Exception e)
            {
                System.err.println("Failed to instrument class " + targetClass.getName());
                e.printStackTrace();
            }
        }
    }

    public static void uninitialize()
    {
        patchInvokers.clear();
        injectionInvokers.clear();
        patchFunctionClasses.clear();
        injectionFunctionClasses.clear();
    }
}