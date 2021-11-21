package com.volmit.adapt.content.adaptation;

import com.volmit.adapt.api.adaptation.SimpleAdaptation;
import com.volmit.adapt.util.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class AgilityWallJump extends SimpleAdaptation {
    private final KMap<Player, Double> airjumps = new KMap<>();

    public AgilityWallJump() {
        super("wall-jump");
        setDescription("Hold shift while mid-air against a wall to wall latch & jump!");
        setIcon(Material.LADDER);
        setBaseCost(2);
        setCostFactor(0.65);
        setMaxLevel(5);
        setInitialCost(8);
        setInterval(50);
    }

    @Override
    public void addStats(int level, Element v) {
        v.addLore(C.GREEN + "+ " + getMaxJumps(level) + C.GRAY + " Max Jumps");
        v.addLore(C.GREEN + "+ " + Form.pc(getJumpHeight(level), 0) + C.GRAY + " Jump Height");
    }

    @EventHandler
    public void on(PlayerQuitEvent e)
    {
        airjumps.remove(e.getPlayer());
    }

    private int getMaxJumps(int level)
    {
        return level + (level / 2);
    }

    private double getJumpHeight(int level)
    {
        return 0.625 + (getLevelPercent(level) * 0.225);
    }

    @EventHandler
    public void on(PlayerMoveEvent e)
    {
        if(airjumps.containsKey(e.getPlayer()))
        {
            if(e.getPlayer().isOnGround() && !e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData().getMaterial().isAir())
            {
                airjumps.remove(e.getPlayer());
            }
        }
    }

    @Override
    public void onTick() {
        for(Player i : Bukkit.getOnlinePlayers())
        {
            int level = getLevel(i);
            if(level <= 0)
            {
                continue;
            }

            Double j = airjumps.get(i);

            if(j != null && j-0.25 >= getMaxJumps(level))
            {
                i.setGravity(true);
                continue;
            }

            if(i.isFlying() || !i.isSneaking() || i.getFallDistance() < 0.3)
            {
                boolean jumped = false;

                if(!i.hasGravity() && i.getFallDistance() > 0.45 && canStick(i))
                {
                    j = j == null ? 0 : j;
                    j++;

                    if(j-0.25 <= getMaxJumps(level))
                    {
                        jumped = true;
                        i.setVelocity(i.getVelocity().setY(getJumpHeight(level)));
                        J.s(() ->                         i.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, i.getLocation(), 7, 0, -1, 0, 0.075));
                    }

                    airjumps.put(i, j);
                }

                if(!jumped && !i.hasGravity())
                {
                    i.setGravity(true);
                    i.getLocation().getWorld().playSound(i.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 0.439f);
                }
                continue;
            }

            if(canStick(i))
            {
                if(i.hasGravity())
                {
                    i.getLocation().getWorld().playSound(i.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 0.89f);
                    i.getLocation().getWorld().playSound(i.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1f, 1.39f);
                }

                i.setGravity(false);
                Vector c = i.getVelocity();
                i.setVelocity(i.getVelocity().setY((c.getY() * 0.35)-0.0025));
                Double vv = airjumps.get(i);
                vv = vv == null ? 0 : vv;
                vv+=0.0127;
                airjumps.put(i, vv);
            }

            if(!canStick(i) && !i.hasGravity())
            {
                i.setGravity(true);
            }
        }
    }

    private boolean canStick(Player p)
    {
        Block[] pb = new Block[] {
                p.getLocation().getBlock().getRelative(BlockFace.NORTH),
                p.getLocation().getBlock().getRelative(BlockFace.SOUTH),
                p.getLocation().getBlock().getRelative(BlockFace.EAST),
                p.getLocation().getBlock().getRelative(BlockFace.WEST),
                p.getLocation().getBlock().getRelative(BlockFace.NORTH_EAST),
                p.getLocation().getBlock().getRelative(BlockFace.SOUTH_EAST),
                p.getLocation().getBlock().getRelative(BlockFace.NORTH_WEST),
                p.getLocation().getBlock().getRelative(BlockFace.SOUTH_WEST),
                p.getLocation().getBlock().getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP),
                p.getLocation().getBlock().getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP),
                p.getLocation().getBlock().getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP),
                p.getLocation().getBlock().getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP),
                p.getLocation().getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.UP),
                p.getLocation().getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP),
                p.getLocation().getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.UP),
                p.getLocation().getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.UP),
        };

        for(Block i : pb)
        {
            if(i.getBlockData().getMaterial().isSolid())
            {
                Vector velocity = p.getVelocity();
                Vector shift = p.getLocation().subtract(i.getLocation().clone().add(0.5, 0.5, 0.5)).toVector();
                velocity.setX(velocity.getX() - (shift.getX() / 16));
                velocity.setZ(velocity.getZ() - (shift.getZ() / 16));
                p.setVelocity(velocity);

                return true;
            }
        }

        return false;
    }
}
