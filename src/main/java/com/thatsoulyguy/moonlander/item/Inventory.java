package com.thatsoulyguy.moonlander.item;

import com.thatsoulyguy.moonlander.util.DoubleConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Inventory implements Serializable
{
    public byte currentlySelectedSlotIndex = 0;

    private final @NotNull Short[][] slots = new Short[4][9];
    private final @NotNull Byte[][] slotCounts = new Byte[4][9];

    private transient List<DoubleConsumer<Vector2i, SlotData>> onSlotChangedCallbackList;

    public Inventory()
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                slots[x][y] = ItemRegistry.ITEM_AIR.getId();
                slotCounts[x][y] = 0;
            }
        }
    }

    public void initialize()
    {
        onSlotChangedCallbackList = new ArrayList<>();
    }

    public void registerOnSlotChangedCallback(@NotNull DoubleConsumer<Vector2i, SlotData> callback)
    {
        onSlotChangedCallbackList.add(callback);
    }

    public void addItem(short item, byte count)
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                if (slots[x][y] == item && slotCounts[x][y] <= 63)
                {
                    setSlot(new Vector2i(x, y), new SlotData(slots[x][y], (byte) (slotCounts[x][y] + count)));

                    return;
                }
                else if (slots[x][y] == ItemRegistry.ITEM_AIR.getId())
                {
                    setSlot(new Vector2i(x, y), new SlotData(item, count));

                    return;
                }
            }
        }
    }

    public void refreshAll()
    {
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                final Vector2i position = new Vector2i(x, y);

                onSlotChangedCallbackList.forEach(callback -> callback.run(position, getSlot(position)));
            }
        }
    }

    public @NotNull SlotData getCurrentlySelectedSlot()
    {
        return getSlot(new Vector2i(0, currentlySelectedSlotIndex));
    }

    public void setSlot(@NotNull Vector2i position, @NotNull SlotData data)
    {
        if (slots[position.x][position.y] == data.id() && slotCounts[position.x][position.y] == data.count())
            return;

        onSlotChangedCallbackList.forEach(callback -> callback.run(position, data));

        if (data.count() <= 0)
            slots[position.x][position.y] = ItemRegistry.ITEM_AIR.getId();
        else
            slots[position.x][position.y] = data.id();

        slotCounts[position.x][position.y] = data.count();
    }

    public @NotNull SlotData getSlot(@NotNull Vector2i position)
    {
        return new SlotData(slots[position.x][position.y], slotCounts[position.x][position.y]);
    }

    public record SlotData(short id, byte count) { }
}