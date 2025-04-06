package com.thatsoulyguy.moonlander.mod.patch;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.util.List;

public class CentralAdvice
{
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.This(optional = true) Object self, @Advice.AllArguments Object[] args, @Advice.Origin Method method)
    {
        String origin = method.getDeclaringClass().getName() + "::" + method.getName();

        List<InjectionInvoker> startInvokers = CentralPatchManager.getInjectionInvokers(origin, InjectionPoint.START);

        if (!startInvokers.isEmpty())
        {
            for (InjectionInvoker invoker : startInvokers)
            {
                try
                {
                    invoker.invoke(self, args);
                }
                catch (Exception ex)
                {
                    System.err.println("[CentralAdvice] Exception during onEnter (injection) for " + origin);
                    ex.printStackTrace();
                }
            }
        }

        PatchInvoker patchInvoker = CentralPatchManager.getPatchInvoker(origin);

        if (patchInvoker != null)
        {
            try
            {
                patchInvoker.invoke(self, args);
            }
            catch (Exception ex)
            {
                System.err.println("[CentralAdvice] Exception during onEnter (patch) for " + origin);
                ex.printStackTrace();
            }

            return true;
        }

        return false;
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.This(optional = true) Object self, @Advice.AllArguments Object[] args, @Advice.Origin Method method)
    {
        String origin = method.getDeclaringClass().getName() + "::" + method.getName();

        List<InjectionInvoker> endInvokers = CentralPatchManager.getInjectionInvokers(origin, InjectionPoint.END);

        if (!endInvokers.isEmpty())
        {
            for (InjectionInvoker invoker : endInvokers)
            {
                try
                {
                    invoker.invoke(self, args);
                }
                catch (Exception ex)
                {
                    System.err.println("[CentralAdvice] Exception during onExit for " + origin);
                    ex.printStackTrace();
                }
            }
        }
    }
}