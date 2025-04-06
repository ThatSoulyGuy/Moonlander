package com.thatsoulyguy.moonlander.mod;

import net.bytebuddy.asm.Advice;

public class PatchAdvice
{
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean enter(@Advice.This Object self, @Advice.AllArguments Object[] args, @Advice.Origin("#t::#m") String origin)
    {
        PatchInvoker invoker = PatchFunctionManager.getInvoker(origin);

        if (invoker != null)
        {
            invoker.invoke(self, args);

            return true;
        }

        return false;
    }
}