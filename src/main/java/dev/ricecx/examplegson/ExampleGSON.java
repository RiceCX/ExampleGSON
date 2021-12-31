package dev.ricecx.examplegson;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public final class ExampleGSON extends JavaPlugin implements Listener, CommandExecutor {

    private CheckpointFile checkpointFile;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdir())
            getLogger().warning("Failed to create data folder");

        checkpointFile = new CheckpointFile(new File(getDataFolder() + "/checkpoints.json"));

        getServer().getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(getCommand("checkpoints")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length <= 0) return false;

        if (sender instanceof Player player) {
            switch (args[0]) {
                case "add":
                    checkpointFile.addCheckpoint(CheckpointFile.CheckpointData.fromBukkit(player.getLocation()));
                    player.sendMessage("Checkpoint added");
                    break;
                case "remove":
                    checkpointFile.removeCheckpoint(player.getLocation());
                    player.sendMessage("Checkpoint removed");
                    break;
                case "list":
                    player.sendMessage("Checkpoints:");
                    for (CheckpointFile.CheckpointData checkpoint : checkpointFile.getCachedCheckpoints()) {
                        player.sendMessage(checkpoint.toString());
                    }
                    break;
                case "save":
                    checkpointFile.saveCheckpoints();
                    player.sendMessage("Checkpoints saved");
                    break;
                case "reload":
                    checkpointFile.reloadCheckpoints();
                    player.sendMessage("Checkpoints reloaded");
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onDisable() {
    }

}
