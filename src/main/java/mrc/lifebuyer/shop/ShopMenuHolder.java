package mrc.lifebuyer.shop;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ShopMenuHolder implements InventoryHolder {
    private final FileConfiguration config;
    private final String shopName;
    private int sellAmount;
    private final int[] sellAmounts = new int[] { 1, 16, 64, 128 };
    private int sellAmountIndex = 0;

    public ShopMenuHolder(FileConfiguration config, String shopName) {
        this.config = config;
        this.shopName = shopName;
        this.sellAmount = this.sellAmounts[this.sellAmountIndex];
    }

    public String getShopName() {
        return this.shopName;
    }

    public int getSellAmount() {
        return this.sellAmount;
    }

    public void nextSellAmount() {
        this.sellAmountIndex = (this.sellAmountIndex + 1) % this.sellAmounts.length;
        this.sellAmount = this.sellAmounts[this.sellAmountIndex];
    }

    public void changeSellAmount(Player player) {
        nextSellAmount();
        String select = "1";
        if (this.sellAmount == 1) {
            select = "1";
        } else if (this.sellAmount == 16) {
            select = "16";
        } else if (this.sellAmount == 64) {
            select = "64";
        } else if (this.sellAmount == 128) {
            select = "128";
        }
        String message = ChatColor.translateAlternateColorCodes('&', this.config.getString("messages.select").replace("%count%", select));
        player.sendMessage(message);
    }

    public Inventory getInventory() {
        return null;
    }
}
