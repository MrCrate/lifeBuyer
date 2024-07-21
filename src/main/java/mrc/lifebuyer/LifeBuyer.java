//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package mrc.lifebuyer;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import mrc.lifebuyer.manager.DataManager;
import mrc.lifebuyer.shop.Shop;
import mrc.lifebuyer.shop.ShopItem;
import mrc.lifebuyer.shop.ShopMenuHolder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class LifeBuyer extends JavaPlugin implements Listener {
    private DataManager dataManager;
    private Economy economy;
    private Map<String, Shop> shops;
    private Map<UUID, ShopMenuHolder> playerHolders;
    private static long CLICK_COOLDOWN_LEFT = 1000L;
    private static long CLICK_COOLDOWN_RIGHT = 1000L;
    private final Map<UUID, Long> lastClickLeftTime = new ConcurrentHashMap();
    private final Map<UUID, Long> lastClickRightTime = new ConcurrentHashMap();

    public LifeBuyer() {
    }

    public void onEnable() {
        this.saveDefaultConfig();
        String databasePath = this.getDataFolder() + File.separator + "data";
        this.dataManager = new DataManager(databasePath);

        try {
            this.dataManager.connect();
        } catch (SQLException var3) {
            SQLException e = var3;
            e.printStackTrace();
        }

        this.shops = new HashMap();
        this.playerHolders = new HashMap();
        CLICK_COOLDOWN_LEFT = (long)this.getConfig().getInt("cooldowns.left");
        CLICK_COOLDOWN_RIGHT = (long)this.getConfig().getInt("cooldowns.right");
        this.EconomyManager();
        this.loadShops();
        this.getCommand("sbuyer").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed by players.");
                return true;
            } else if (args.length == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.usage")));
                return true;
            } else {
                String shopName = args[0].toLowerCase();
                if (!this.shops.containsKey(shopName)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.no-shop")));
                    return true;
                } else {
                    Player player = (Player)sender;

                    try {
                        this.openShopMenu(player, shopName);
                        return true;
                    } catch (SQLException var8) {
                        SQLException e = var8;
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        this.getCommand("sixbuyer").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("sixbuyer.admin")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.permission")));
                return false;
            } else if (args[0].equalsIgnoreCase("reload")) {
                this.reloadConfig();
                sender.sendMessage("Конфигурация перезагружена");
                return true;
            } else if (args.length < 4 && !args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.admin-cmd")));
                return true;
            } else if (args.length == 2) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.admin-cmd")));
                return true;
            } else {
                String subCommand = args[0];
                String shopName = args[2].toLowerCase();
                Player player = sender.getServer().getPlayer(args[1]);
                int amount = Integer.parseInt(args[3]);
                if (!this.shops.containsKey(shopName)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.no-shop")));
                    return true;
                } else {
                    SQLException e;
                    if (subCommand.equalsIgnoreCase("setlevel")) {
                        if (amount < 1 || this.getMaxKey(this.getConfig()) <= amount) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.error-lvl")));
                            return false;
                        }

                        try {
                            this.dataManager.setPlayerLevel(player.getUniqueId(), shopName, amount);
                        } catch (SQLException var11) {
                            e = var11;
                            throw new RuntimeException(e);
                        }

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.give-lvl").replace("%player%", player.getName()).replace("%stack", amount + "")));
                    } else if (subCommand.equalsIgnoreCase("setstack")) {
                        if (amount < 1) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.error-count")));
                            return false;
                        }

                        try {
                            this.dataManager.setPlayerCount(player.getUniqueId(), shopName, amount);
                        } catch (SQLException var10) {
                            e = var10;
                            throw new RuntimeException(e);
                        }

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.give-stack").replace("%lvl%", amount + "").replace("%player%", player.getName())));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.admin-cmd")));
                    }

                    return true;
                }
            }
        });
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        try {
            this.dataManager.disconnect();
        } catch (SQLException var2) {
            SQLException e = var2;
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws SQLException {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof ShopMenuHolder) {
            event.setCancelled(true);
            Player player = (Player)event.getWhoClicked();
            int slot = event.getRawSlot();
            ShopMenuHolder holder = (ShopMenuHolder)inventory.getHolder();
            String shopName = holder.getShopName();
            Shop shop = (Shop)this.shops.get(shopName);
            if (shop == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("commands.no-shop")));
            } else {
                ClickType clickType = event.getClick();
                ShopItem shopItem;
                long currentTime;
                long cooldown;
                if (clickType == ClickType.LEFT) {
                    if (slot >= 0 && slot < inventory.getSize()) {
                        shopItem = shop.getItemBySlot(slot);
                        if (shopItem != null) {
                            currentTime = System.currentTimeMillis();
                            cooldown = (Long)this.lastClickLeftTime.getOrDefault(player.getUniqueId(), 0L);
                            if (currentTime - cooldown >= CLICK_COOLDOWN_LEFT) {
                                this.sellItem(player, shopItem, shopName, holder);
                                this.lastClickLeftTime.put(player.getUniqueId(), currentTime);
                            } else if (this.getConfig().getBoolean("messages.cooldowns.enable")) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.cooldowns.left")));
                            }
                        }

                        ItemStack clickedItem = event.getCurrentItem();
                        Material levelUpMaterial = Material.valueOf(this.getConfig().getString("settings.level-up.material"));
                        if (clickedItem != null && clickedItem.getType() == levelUpMaterial) {
                            this.LevelUPCheck(player, shopName);
                        }
                    }
                } else if (clickType == ClickType.RIGHT && slot >= 0 && slot < inventory.getSize()) {
                    shopItem = shop.getItemBySlot(slot);
                    if (shopItem != null) {
                        currentTime = System.currentTimeMillis();
                        cooldown = (Long)this.lastClickRightTime.getOrDefault(player.getUniqueId(), 0L);
                        if (currentTime - cooldown >= CLICK_COOLDOWN_RIGHT) {
                            holder.changeSellAmount(player);
                            Sound sound = Sound.valueOf(this.getConfig().getString("sounds.select.name"));
                            float volume = (float)this.getConfig().getDouble("sounds.select.volume");
                            float pitch = (float)this.getConfig().getDouble("sounds.select.pitch");
                            player.playSound(player.getLocation(), sound, volume, pitch);
                            this.lastClickRightTime.put(player.getUniqueId(), currentTime);
                        } else if (this.getConfig().getBoolean("messages.cooldowns.enable")) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.cooldowns.right")));
                        }
                    }
                }

                this.updateLevelUpItem(player, shopName, holder, inventory);
            }
        }
    }

    private void updateLevelUpItem(Player player, String shopName, ShopMenuHolder holder, Inventory inventory) throws SQLException {
        FileConfiguration config = this.getConfig();
        ConfigurationSection levelSettingsSection = config.getConfigurationSection("level-settings");
        Set<String> keys = levelSettingsSection.getKeys(false);
        int maxKey = keys.stream().mapToInt(Integer::parseInt).max().orElse(0);
        int playerLevel = this.dataManager.getPlayerLevel(player.getUniqueId(), shopName);
        int playerCount = this.dataManager.getPlayerCount(player.getUniqueId(), shopName);
        int playerStacks = config.getInt("level-settings." + playerLevel + ".stacks");
        double playerFactor = config.getDouble("level-settings." + playerLevel + ".factor");
        String base = playerStacks >= config.getInt("level-settings." + maxKey + ".stacks") ? "∞" : String.valueOf(playerStacks);
        String levelUpName = ChatColor.translateAlternateColorCodes('&', config.getString("settings.level-up.name"));
        List<String> levelUpLore = new ArrayList();
        Iterator var17 = config.getStringList("settings.level-up.lore").iterator();

        while(var17.hasNext()) {
            String line = (String)var17.next();
            String formattedLine = ChatColor.translateAlternateColorCodes('&', line);
            formattedLine = formattedLine.replace("%level%", String.valueOf(playerLevel));
            formattedLine = formattedLine.replace("%count%", String.valueOf(playerCount / 64));
            formattedLine = formattedLine.replace("%stack-lvl%", base);
            formattedLine = formattedLine.replace("%factor%", String.valueOf(playerFactor));
            levelUpLore.add(formattedLine);
        }

        ItemStack levelUpItem = new ItemStack(Material.valueOf(config.getString("settings.level-up.material")));
        ItemMeta levelUpItemMeta = levelUpItem.getItemMeta();
        levelUpItemMeta.setDisplayName(levelUpName);
        levelUpItemMeta.setLore(levelUpLore);
        levelUpItem.setItemMeta(levelUpItemMeta);
        int slot = config.getInt("shops." + shopName + ".level-up.slot");
        player.getOpenInventory().setItem(slot, levelUpItem);
        Shop shop = (Shop)this.shops.get(shopName);
        int maxStack = config.getInt("level-settings." + playerLevel + ".max-stack");
        Iterator var22 = shop.getItems().iterator();

        while(var22.hasNext()) {
            ShopItem shopItem = (ShopItem)var22.next();
            Material material = shopItem.getMaterial();
            int playerItemCount = this.dataManager.getPlayerMaterial(player.getUniqueId(), shopName, material.toString());
            int stat = playerItemCount / 64;
            int percent = (int)((double)stat / (double)maxStack * 100.0);
            if (percent > 100) {
                percent = 100;
            }

            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', shopItem.getName()));
            List<String> lore = new ArrayList();
            Iterator var31 = this.getConfig().getStringList("settings.lore-items.text").iterator();

            String line;
            while(var31.hasNext()) {
                String lines = (String)var31.next();
                line = ChatColor.translateAlternateColorCodes('&', lines);
                if (line.contains("%sell%")) {
                    double sellPrice = (double)shopItem.getSellPrice() * this.getConfig().getDouble("level-settings." + playerLevel + ".factor");
                    line = line.replace("%sell%", String.valueOf((int)sellPrice));
                }

                String color_1 = holder.getSellAmount() == 1 ? ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-selected")) : ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-unselected"));
                String color_16 = holder.getSellAmount() == 16 ? ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-selected")) : ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-unselected"));
                String color_64 = holder.getSellAmount() == 64 ? ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-selected")) : ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-unselected"));
                String color_ALL = holder.getSellAmount() == 128 ? ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-selected")) : ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-unselected"));
                String color_1n = holder.getSellAmount() == 1 ? ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-selected")) : ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-unselected"));
                String color_16n = holder.getSellAmount() == 16 ? ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-selected")) : ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-unselected"));
                String color_64n = holder.getSellAmount() == 64 ? ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-selected")) : ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-unselected"));
                String color_ALLn = holder.getSellAmount() == 128 ? ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-selected")) : ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-unselected"));
                line = line.replace("%color_1%", color_1).replace("%color_n1%", color_1n);
                line = line.replace("%color_16%", color_16).replace("%color_n16%", color_16n);
                line = line.replace("%color_64%", color_64).replace("%color_n64%", color_64n);
                line = line.replace("%color_ALL%", color_ALL).replace("%color_nALL%", color_ALLn);
                lore.add(line);
            }

            List<String> additionalLore = shopItem.isBoostEnabled() ? this.getConfig().getStringList("settings.item-boost.item-boosted") : this.getConfig().getStringList("settings.item-boost.item-no-boosted");
            Iterator var46 = additionalLore.iterator();

            while(var46.hasNext()) {
                line = (String)var46.next();
                line = line.replace("%stack%", String.valueOf(maxStack)).replace("%procent%", String.valueOf(percent)).replace("%boost%", String.valueOf(shopItem.getBooster()));
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(shopItem.getSlot(), itemStack);
        }

    }

    private void openShopMenu(Player player, String shopName) throws SQLException {
        Shop shop = (Shop)this.shops.get(shopName);
        if (shop == null) {
            player.sendMessage("Invalid shop.");
        } else {
            int slots = shop.getSlots();
            String title = ChatColor.translateAlternateColorCodes('&', shop.getTitle());
            FileConfiguration config = this.getConfig();
            Inventory inventory = Bukkit.createInventory(new ShopMenuHolder(config, shopName), slots, title);
            int playerLevel = this.dataManager.getPlayerLevel(player.getUniqueId(), shopName);
            int playerCount = this.dataManager.getPlayerCount(player.getUniqueId(), shopName);
            int maxStack = this.getConfig().getInt("level-settings." + playerLevel + ".max-stack");
            double factor = config.getDouble("level-settings." + playerLevel + ".factor");
            boolean isMaxStack = this.getConfig().getInt("level-settings." + playerLevel + ".stacks") >= this.getConfig().getInt("level-settings." + this.getMaxKey(config) + ".stacks");
            String base = isMaxStack ? "∞" : String.valueOf(this.getConfig().getInt("level-settings." + playerLevel + ".stacks"));
            String levelUpName = ChatColor.translateAlternateColorCodes('&', config.getString("settings.level-up.name"));
            List<String> levelUpLore = new ArrayList();
            Iterator var17 = config.getStringList("settings.level-up.lore").iterator();

            String modeName;
            while(var17.hasNext()) {
                modeName = (String)var17.next();
                String formattedLine = ChatColor.translateAlternateColorCodes('&', modeName);
                formattedLine = formattedLine.replace("%level%", String.valueOf(playerLevel));
                formattedLine = formattedLine.replace("%count%", String.valueOf(playerCount / 64));
                formattedLine = formattedLine.replace("%stack-lvl%", base);
                formattedLine = formattedLine.replace("%factor%", String.valueOf(factor));
                levelUpLore.add(formattedLine);
            }

            ItemStack levelUpItem = this.createItemStack(config, "settings.level-up", levelUpName, levelUpLore);
            inventory.setItem(config.getInt("shops." + shopName + ".level-up.slot"), levelUpItem);
            modeName = ChatColor.translateAlternateColorCodes('&', config.getString("settings.mode.name"));
            List<String> modeLore = new ArrayList();
            Iterator var20 = config.getStringList("settings.mode.lore").iterator();

            String decorationName;
            while(var20.hasNext()) {
                String line = (String)var20.next();
                decorationName = ChatColor.translateAlternateColorCodes('&', line);
                modeLore.add(decorationName);
            }

            ItemStack modeItem = this.createItemStack(config, "settings.mode", modeName, modeLore);
            inventory.setItem(config.getInt("shops." + shopName + ".mode.slot"), modeItem);
            ConfigurationSection decorationSection = config.getConfigurationSection("shops." + shopName + ".decoration");
            if (decorationSection != null) {
                decorationName = ChatColor.translateAlternateColorCodes('&', decorationSection.getString("name"));
                Material decorationMaterial = Material.valueOf(decorationSection.getString("material"));
                List<Integer> decorationSlots = decorationSection.getIntegerList("slots");
                Iterator var25 = decorationSlots.iterator();

                while(var25.hasNext()) {
                    int decorationSlot = (Integer)var25.next();
                    ItemStack decorationItem = new ItemStack(decorationMaterial);
                    ItemMeta decorationItemMeta = decorationItem.getItemMeta();
                    decorationItemMeta.setDisplayName(decorationName);
                    decorationItem.setItemMeta(decorationItemMeta);
                    inventory.setItem(decorationSlot, decorationItem);
                }
            }

            Iterator var38 = shop.getItems().iterator();

            while(var38.hasNext()) {
                ShopItem shopItem = (ShopItem)var38.next();
                ItemStack itemStack = new ItemStack(shopItem.getMaterial());
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', shopItem.getName()));
                List<String> lore = new ArrayList();
                Iterator var43 = config.getStringList("settings.lore-items.text").iterator();

                String line;
                while(var43.hasNext()) {
                    String lines = (String)var43.next();
                    String formattedLine = ChatColor.translateAlternateColorCodes('&', lines);
                    if (formattedLine.contains("%sell%")) {
                        double sellPrice = (double)shopItem.getSellPrice() * factor;
                        formattedLine = formattedLine.replace("%sell%", String.valueOf((int)sellPrice));
                    }

                    String color_1 = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-selected"));
                    String color_16 = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-unselected"));
                    line = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-selected"));
                    String color_16n = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("settings.lore-items.color-numb-unselected"));
                    formattedLine = formattedLine.replace("%color_1%", color_1).replace("%color_n1%", line);
                    formattedLine = formattedLine.replace("%color_16%", color_16).replace("%color_n16%", color_16n);
                    formattedLine = formattedLine.replace("%color_64%", color_16).replace("%color_n64%", color_16n);
                    formattedLine = formattedLine.replace("%color_ALL%", color_16).replace("%color_nALL%", color_16n);
                    lore.add(formattedLine);
                }

                int playerItemCount = this.dataManager.getPlayerMaterial(player.getUniqueId(), shopName, shopItem.getMaterial().toString());
                int stat = playerItemCount / 64;
                int percent = (int)((double)stat / (double)maxStack * 100.0);
                if (percent > 100) {
                    percent = 100;
                }

                List noBoostLore;
                Iterator var50;
                if (shopItem.isBoostEnabled()) {
                    noBoostLore = config.getStringList("settings.item-boost.item-boosted");
                    var50 = noBoostLore.iterator();

                    while(var50.hasNext()) {
                        line = (String)var50.next();
                        line = line.replace("%stack%", String.valueOf(maxStack)).replace("%procent%", String.valueOf(percent)).replace("%boost%", String.valueOf(shopItem.getBooster()));
                        lore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                } else {
                    noBoostLore = config.getStringList("settings.item-boost.item-no-boosted");
                    var50 = noBoostLore.iterator();

                    while(var50.hasNext()) {
                        line = (String)var50.next();
                        line = line.replace("%stack%", String.valueOf(maxStack)).replace("%procent%", String.valueOf(percent));
                        lore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                }

                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(shopItem.getSlot(), itemStack);
            }

            player.openInventory(inventory);
            this.playerHolders.put(player.getUniqueId(), (ShopMenuHolder)inventory.getHolder());
        }
    }

    private ItemStack createItemStack(FileConfiguration config, String path, String displayName, List<String> lore) {
        ItemStack itemStack = new ItemStack(Material.valueOf(config.getString(path + ".material")));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private int getMaxKey(FileConfiguration config) {
        ConfigurationSection levelSettingsSection = config.getConfigurationSection("level-settings");
        Set<String> keys = levelSettingsSection.getKeys(false);
        return keys.stream().mapToInt(Integer::parseInt).max().orElse(0);
    }

    private void sellItem(Player player, ShopItem shopItem, String shopName, ShopMenuHolder holder) throws SQLException {
        UUID playerId = player.getUniqueId();
        int sellPrice = shopItem.getSellPrice();
        int sellAmount = holder.getSellAmount();
        int level = this.dataManager.getPlayerLevel(playerId, shopName);
        int boosted = shopItem.getBooster();
        double factor = this.getConfig().getDouble("level-settings." + level + ".factor");
        int itemCount = this.getPlayerItemCount(player, shopItem.getMaterial());
        int temCount;
        String itemssell;
        switch (sellAmount) {
            case 1:
                itemssell = "1";
                temCount = 1;
                break;
            case 16:
                itemssell = "16";
                temCount = 16;
                break;
            case 64:
                itemssell = "64";
                temCount = 64;
                break;
            case 128:
                itemssell = String.valueOf(itemCount);
                temCount = itemCount;
                break;
            default:
                itemssell = "1";
                temCount = 1;
        }

        if (itemCount >= temCount && itemCount != 0) {
            int boostered = shopItem.isBoostEnabled() ? temCount * boosted : temCount;
            int countin = this.dataManager.getPlayerCount(playerId, shopName) + boostered;
            int moneygive = sellPrice * temCount;
            double fianlmoney = (double)moneygive * factor;
            String sellMessage = this.getConfig().getString("messages.sell").replace("%itemname%", shopItem.getName()).replace("%amount%", itemssell).replace("%money%", String.valueOf((int)fianlmoney));
            player.playSound(player.getLocation(), Sound.valueOf(this.getConfig().getString("sounds.sell.name")), (float)this.getConfig().getDouble("sounds.sell.volume"), (float)this.getConfig().getDouble("sounds.sell.pitch"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', sellMessage));
            this.giveMoney(player, fianlmoney);
            int materials = this.dataManager.getPlayerMaterial(playerId, shopName, shopItem.getMaterial().toString()) + temCount;
            if (this.dataManager.getPlayerMaterial(playerId, shopName, shopItem.getMaterial().toString()) / 64 < this.getConfig().getInt("level-settings." + level + ".max-stack")) {
                this.dataManager.setPlayerMaterial(player.getUniqueId(), shopName, shopItem.getMaterial().toString(), materials);
                this.dataManager.setPlayerCount(playerId, shopName, countin);
            }

            this.takeItemsFromPlayerInventory(player, shopItem.getMaterial(), temCount);
        } else {
            player.playSound(player.getLocation(), Sound.valueOf(this.getConfig().getString("sounds.error.name")), (float)this.getConfig().getDouble("sounds.error.volume"), (float)this.getConfig().getDouble("sounds.error.pitch"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.not-items")));
        }
    }


    public void takeItemsFromPlayerInventory(Player player, Material material, int amount) {
        PlayerInventory inventory = player.getInventory();
        int remainingAmount = amount;

        for(int slot = 0; slot < inventory.getSize(); ++slot) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && itemStack.getType() == material) {
                int itemAmount = itemStack.getAmount();
                if (itemAmount >= remainingAmount) {
                    itemStack.setAmount(itemAmount - remainingAmount);
                    break;
                }

                itemStack.setAmount(0);
                remainingAmount -= itemAmount;
            }
        }

        player.updateInventory();
    }

    private void LevelUPCheck(Player player, String shopName) throws SQLException {
        FileConfiguration config = this.getConfig();
        ConfigurationSection levelSettingsSection = config.getConfigurationSection("level-settings");
        Set<String> keys = levelSettingsSection.getKeys(false);
        int maxKey = keys.stream().mapToInt(Integer::parseInt).max().orElse(0);
        int count = this.dataManager.getPlayerCount(player.getUniqueId(), shopName);
        int level = this.dataManager.getPlayerLevel(player.getUniqueId(), shopName);
        String upLevelMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.up-level"));
        String maxLevelMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.max-level"));
        String noLevelMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.no-level"));
        String upLevelSoundName = config.getString("sounds.up-level.name");
        float upLevelSoundVolume = (float)config.getDouble("sounds.up-level.volume");
        float upLevelSoundPitch = (float)config.getDouble("sounds.up-level.pitch");
        String maxLevelSoundName = config.getString("sounds.max-level.name");
        float maxLevelSoundVolume = (float)config.getDouble("sounds.max-level.volume");
        float maxLevelSoundPitch = (float)config.getDouble("sounds.max-level.pitch");
        String noLevelSoundName = config.getString("sounds.no-level.name");
        float noLevelSoundVolume = (float)config.getDouble("sounds.no-level.volume");
        float noLevelSoundPitch = (float)config.getDouble("sounds.no-level.pitch");
        if (count / 64 >= config.getInt("level-settings." + level + ".stacks") && level < maxKey) {
            player.sendMessage(upLevelMessage);
            this.dataManager.setPlayerCount(player.getUniqueId(), shopName, count - config.getInt("level-settings." + level + ".stacks") * 64);
            player.playSound(player.getLocation(), Sound.valueOf(upLevelSoundName), upLevelSoundVolume, upLevelSoundPitch);
            this.dataManager.setPlayerLevel(player.getUniqueId(), shopName, level + 1);
            this.dataManager.clearPlayerData(player.getUniqueId(), shopName);
        } else if (level == maxKey) {
            player.playSound(player.getLocation(), Sound.valueOf(maxLevelSoundName), maxLevelSoundVolume, maxLevelSoundPitch);
            player.sendMessage(maxLevelMessage);
        } else {
            player.playSound(player.getLocation(), Sound.valueOf(noLevelSoundName), noLevelSoundVolume, noLevelSoundPitch);
            player.sendMessage(noLevelMessage);
        }

    }

    private int getPlayerItemCount(Player player, Material material) {
        int count = 0;
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] var6 = contents;
        int var7 = contents.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            ItemStack item = var6[var8];
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }

        return count;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof ShopMenuHolder) {
            Player player = (Player)event.getPlayer();
            this.playerHolders.remove(player.getUniqueId());
        }

    }

    private void loadShops() {
        this.getConfig().getConfigurationSection("shops").getKeys(false).forEach((shopKey) -> {
            String shopTitle = this.getConfig().getString("shops." + shopKey + ".title");
            int shopSlots = this.getConfig().getInt("shops." + shopKey + ".slots");
            Shop shop = new Shop(shopTitle, shopSlots);
            this.getConfig().getConfigurationSection("shops." + shopKey + ".items").getKeys(false).forEach((itemKey) -> {
                String itemName = this.getConfig().getString("shops." + shopKey + ".items." + itemKey + ".name");
                int itemSlot = this.getConfig().getInt("shops." + shopKey + ".items." + itemKey + ".slot");
                Material itemMaterial = Material.valueOf(this.getConfig().getString("shops." + shopKey + ".items." + itemKey + ".material"));
                int itemSellPrice = this.getConfig().getInt("shops." + shopKey + ".items." + itemKey + ".sell");
                boolean boostEnabled = this.getConfig().getBoolean("shops." + shopKey + ".items." + itemKey + ".boost.enable");
                int booster = this.getConfig().getInt("shops." + shopKey + ".items." + itemKey + ".boost.boster");
                shop.addItem(new ShopItem(itemName, itemSlot, itemMaterial, itemSellPrice, boostEnabled, booster));
            });
            this.shops.put(shopKey, shop);
        });
    }

    private void EconomyManager() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.economy = (Economy)rsp.getProvider();
        } else {
            System.err.println(ChatColor.RED + "[Vault] У вас не установлен плагин Vault.");
        }

    }

    private void giveMoney(Player player, double amount) {
        this.economy.depositPlayer(player, amount);
    }
}
