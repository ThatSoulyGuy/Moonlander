package com.thatsoulyguy.moonlander.ui;

public class JsonEntry
{
    private String Name;
    private Property Property;

    public JsonEntry() { }

    public JsonEntry(String name, Property property)
    {
        this.Name = name;
        this.Property = property;
    }

    public String getName()
    {
        return Name;
    }

    public void setName(String name)
    {
        this.Name = name;
    }

    public Property getProperty()
    {
        return Property;
    }

    public void setProperty(Property property)
    {
        this.Property = property;
    }
}