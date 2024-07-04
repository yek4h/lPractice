package net.lyragames.practice.utils;

import lombok.Getter;
import lombok.SneakyThrows;
import net.lyragames.practice.PracticePlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.AsyncCatcher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.UUID;

public class PlayerUtil {
    public static HashSet<UUID> denyMovement = new HashSet<>();

    public static int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Field pingField = entityPlayer.getClass().getDeclaredField("ping");

            return pingField.getInt(entityPlayer);
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reset(Player player) {
        AsyncCatcher.enabled = false;

        player.getActivePotionEffects().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0f);
        player.setFireTicks(0);
        player.setMaximumNoDamageTicks(20);
        player.setNoDamageTicks(20);
        player.setSaturation(20);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setContents(new ItemStack[36]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.updateInventory();
        PracticePlugin.instance.hologramManager.hide(player);
        PracticePlugin.instance.hologramManager.show(player);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            player.showPlayer(onlinePlayer);
            onlinePlayer.showPlayer(player);
        }
    }

    public static void resetSpectator(Player player) {
        AsyncCatcher.enabled = false;

        player.getActivePotionEffects().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0f);
        player.setFireTicks(0);
        player.setMaximumNoDamageTicks(20);
        player.setNoDamageTicks(20);
        player.setSaturation(20);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setContents(new ItemStack[36]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.updateInventory();
        PracticePlugin.instance.hologramManager.hide(player);
        PracticePlugin.instance.hologramManager.show(player);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(onlinePlayer);
            onlinePlayer.hidePlayer(player);
        }
    }

    public static void reset(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        AsyncCatcher.enabled = false;

        player.getActivePotionEffects().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0f);
        player.setFireTicks(0);
        player.setMaximumNoDamageTicks(20);
        player.setNoDamageTicks(20);
        player.setSaturation(20);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setContents(new ItemStack[36]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.updateInventory();
        PracticePlugin.instance.hologramManager.hide(player);
        PracticePlugin.instance.hologramManager.show(player);
    }

    public static UUID lastAttacker(Player player) {
        Object entityPlayer = null;
        try {
            entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Field lastDamagerField = entityPlayer.getClass().getDeclaredField("lastDamager");
            Field uuidField = lastDamagerField.getDeclaringClass().getDeclaredField("uniqueID");

            return (UUID) uuidField.get(entityPlayer);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }

    public static void denyMovement(Player player) {
        /*AsyncCatcher.enabled = false;

        player.setWalkSpeed(0.0F);
        player.setFlySpeed(0.0F);
        player.setFoodLevel(0);
        player.setSprinting(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200));*/

        denyMovement.add(player.getUniqueId());
    }

    public static void allowMovement(Player player) {
        /*AsyncCatcher.enabled = false;

        player.setWalkSpeed(0.2F);
        player.setFlySpeed(0.2F);
        player.setFoodLevel(20);
        player.setSprinting(true);
        player.removePotionEffect(PotionEffectType.JUMP); */
        denyMovement.remove(player.getUniqueId());
    }
}
