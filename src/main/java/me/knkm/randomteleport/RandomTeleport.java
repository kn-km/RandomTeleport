package me.knkm.randomteleport;

import com.Zrips.CMI.Modules.tp.Teleportations;
import com.Zrips.CMI.events.CMIPlayerTeleportEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RandomTeleport extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCMIPlayerTeleport(CMIPlayerTeleportEvent event) {
        if (event.getType() != Teleportations.TeleportType.randomTp) {
            return;
        }
        if (event.getSafe().getTpCondition() != Teleportations.TpCondition.Good){
            return;
        }
        Location to = event.getTo();
        event.setCancelled(true);
        if (event.isAsynchronous()) {
            loadNearby(to);
            event.getPlayer().teleport(to);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                loadNearby(to);
                Bukkit.getScheduler().runTask(this, () -> {
                    event.getPlayer().teleportAsync(to);
                });
            });
        }
    }

    private void loadNearby(Location location) {
        List<CompletableFuture<Chunk>> asyncChunk = new ArrayList<>();
        for (int i = -4; i < 5; i++) {
            int delx = i * 16;
            for (int j = -4; j < 5; j++) {
                int delz = j * 16;
                Location l1 = new Location(location.getWorld(), location.getX() + delx, location.getY(), location.getZ() + delz);
                CompletableFuture<Chunk> chunk = l1.getWorld().getChunkAtAsync(l1, true);
                asyncChunk.add(chunk);
            }
        }
        while (!check(asyncChunk)) {
            try {
                //wait for async chunk load
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean check(List<CompletableFuture<Chunk>> asyncChunk) {
        boolean flag = true;
        for (CompletableFuture<Chunk> chunk : asyncChunk) {
            if (!chunk.isDone()) {
                flag = false;
            }
        }
        return flag;
    }
}
