package com.thatsoulyguy.moonlander.ui;

public class Property
{
    private String Key;
    private String Value;

    public Property() { }

    public Property(String key, String value)
    {
        this.Key = key;
        this.Value = value;
    }

    public String getKey()
    {
        return Key;
    }

    public void setKey(String key)
    {
        this.Key = key;
    }

    public String getValue()
    {
        return Value;
    }

    public void setValue(String value)
    {
        this.Value = value;
    }
}