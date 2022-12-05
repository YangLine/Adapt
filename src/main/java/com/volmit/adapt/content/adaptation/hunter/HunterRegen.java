/*------------------------------------------------------------------------------
 -   Adapt is a Skill/Integration plugin  for Minecraft Bukkit Servers
 -   Copyright (c) 2022 Arcane Arts (Volmit Software)
 -
 -   This program is free software: you can redistribute it and/or modify
 -   it under the terms of the GNU General Public License as published by
 -   the Free Software Foundation, either version 3 of the License, or
 -   (at your option) any later version.
 -
 -   This program is distributed in the hope that it will be useful,
 -   but WITHOUT ANY WARRANTY; without even the implied warranty of
 -   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -   GNU General Public License for more details.
 -
 -   You should have received a copy of the GNU General Public License
 -   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

package com.volmit.adapt.content.adaptation.hunter;

import com.volmit.adapt.api.adaptation.SimpleAdaptation;
import com.volmit.adapt.util.C;
import com.volmit.adapt.util.Element;
import com.volmit.adapt.util.Localizer;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class HunterRegen extends SimpleAdaptation<HunterRegen.Config> {
    public HunterRegen() {
        super("hunter-regen");
        registerConfiguration(Config.class);
        setDescription(Localizer.dLocalize("hunter", "regen", "description"));
        setDisplayName(Localizer.dLocalize("hunter", "regen", "name"));
        setIcon(Material.AXOLOTL_BUCKET);
        setBaseCost(getConfig().baseCost);
        setMaxLevel(getConfig().maxLevel);
        setInitialCost(getConfig().initialCost);
        setCostFactor(getConfig().costFactor);
        setInterval(9744);
    }

    @Override
    public void addStats(int level, Element v) {
        v.addLore(C.GRAY + Localizer.dLocalize("hunter", "regen", "lore1"));
        v.addLore(C.GREEN + "+ " + level + C.GRAY + Localizer.dLocalize("hunter", "regen", "lore2"));
        v.addLore(C.RED + "- " + 5 + level + C.GRAY + Localizer.dLocalize("hunter", "regen", "lore3"));
        v.addLore(C.GRAY + "* " + level + C.GRAY + " " + Localizer.dLocalize("hunter", "regen", "lore4"));
        v.addLore(C.GRAY + "* " + level + C.GRAY + " " + Localizer.dLocalize("hunter", "regen", "lore5"));
        v.addLore(C.GRAY + "- " + level + C.RED + " " + Localizer.dLocalize("hunter", "penalty", "lore1"));

    }


    @EventHandler
    public void on(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getEntity() instanceof org.bukkit.entity.Player p && !e.getCause().equals(EntityDamageEvent.DamageCause.STARVATION) && hasAdaptation(p)) {
            if (!getConfig().useConsumable) {

                if (p.getFoodLevel() == 0) {
                    addPotionStacks(p, PotionEffectType.POISON, 2 + getLevel(p), 300, true);

                } else {
                    addPotionStacks(p, PotionEffectType.HUNGER, 10 + getLevel(p), 100, true);
                    addPotionStacks(p, PotionEffectType.REGENERATION, getLevel(p), 5 + getLevel(p), false);
                }
            } else {
                if (getConfig().consumable != null && Material.getMaterial(getConfig().consumable) != null) {
                    Material mat = Material.getMaterial(getConfig().consumable);
                    if (mat != null &&p.getInventory().contains(mat)) {
                        p.getInventory().removeItem(new ItemStack(mat, 1));
                        addPotionStacks(p, PotionEffectType.REGENERATION, getLevel(p), 5 + getLevel(p), false);
                    } else {
                        addPotionStacks(p, PotionEffectType.POISON, 2 + getLevel(p), 300, true);
                    }
                }
            }
        }
    }

    @Override
    public void onTick() {

    }

    @Override
    public boolean isEnabled() {
        return getConfig().enabled;
    }

    @Override
    public boolean isPermanent() {
        return getConfig().permanent;
    }

    @NoArgsConstructor
    protected static class Config {
        boolean permanent = false;
        boolean enabled = true;
        boolean useConsumable = false;
        String consumable = "ROTTEN_FLESH";
        int baseCost = 4;
        int maxLevel = 5;
        int initialCost = 8;
        double costFactor = 0.4;
    }
}
