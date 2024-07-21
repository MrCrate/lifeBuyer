package mrc.lifebuyer.shop;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class Shop {
    private final String title;
    private final int slots;
    private final List<ShopItem> items;
    private final List<ItemStack> itemStacks;

    public Shop(String title, int slots) {
        this.title = title;
        this.slots = slots;
        this.items = new ArrayList<>();
        this.itemStacks = new ArrayList<>();
    }

    public String getTitle() {
        return this.title;
    }

    public int getSlots() {
        return this.slots;
    }

    public void addItem(ShopItem item) {
        this.items.add(item);
        ItemStack itemStack = item.getItemStack();
        if (itemStack != null) {
            this.itemStacks.add(itemStack);
        }
    }

    public ShopItem getItemBySlot(int slot) {
        for (ShopItem item : this.items) {
            if (item.getSlot() == slot) {
                return item;
            }
        }
        return null;
    }

    public List<ShopItem> getItems() {
        return this.items;
    }
}
