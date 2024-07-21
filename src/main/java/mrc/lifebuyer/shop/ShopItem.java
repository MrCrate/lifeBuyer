package mrc.lifebuyer.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopItem {
    private final String name;
    private final int slot;
    private final Material material;
    private final int sellPrice;
    private final boolean boostEnabled;
    private final int booster;

    public ShopItem(String name, int slot, Material material, int sellPrice, boolean boostEnabled, int booster) {
        this.name = name;
        this.slot = slot;
        this.material = material;
        this.sellPrice = sellPrice;
        this.boostEnabled = boostEnabled;
        this.booster = booster;
    }

    public boolean isBoostEnabled() {
        return this.boostEnabled;
    }

    public int getBooster() {
        return this.booster;
    }

    public String getName() {
        return this.name;
    }

    public int getSlot() {
        return this.slot;
    }

    public Material getMaterial() {
        return this.material;
    }

    public int getSellPrice() {
        return this.sellPrice;
    }

    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(this.name);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
}
