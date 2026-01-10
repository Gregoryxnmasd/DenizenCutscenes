package com.extracraft;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.dummy.Dummy;
import com.ticxo.modelengine.api.entity.ActiveModel;
import com.ticxo.modelengine.api.entity.ModeledEntity;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CsMe4Plugin extends JavaPlugin implements CommandExecutor {
    private final Map<String, InstanceData> instances = new HashMap<>();

    @Override
    public void onEnable() {
        if (getCommand("csme4") != null) {
            getCommand("csme4").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "create":
                handleCreate(sender, args);
                return true;
            case "anim_play":
                handleAnimPlay(sender, args);
                return true;
            case "anim_stop":
                handleAnimStop(sender, args);
                return true;
            case "move":
                handleMove(sender, args);
                return true;
            case "remove":
                handleRemove(sender, args);
                return true;
            default:
                sendUsage(sender);
                return true;
        }
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /csme4 create <instanceId> <modelId> [viewer]");
            return;
        }

        String instanceId = args[1];
        if (instances.containsKey(instanceId)) {
            sender.sendMessage("Instance already exists: " + instanceId);
            return;
        }

        String modelId = args[2];
        Player viewer = resolveViewer(sender, args.length >= 4 ? args[3] : null);
        if (viewer == null) {
            sender.sendMessage("Viewer must be an online player.");
            return;
        }

        Location spawnLocation = viewer.getLocation();
        Dummy<?> dummy = ModelEngineAPI.createDummy(spawnLocation);
        dummy.setDetectingPlayers(false);

        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        modeledEntity.addModel(activeModel);

        dummy.setForceViewing(viewer, true);

        instances.put(instanceId, new InstanceData(dummy, modeledEntity, activeModel, viewer.getUniqueId()));
        sender.sendMessage("Created ModelEngine instance: " + instanceId);
    }

    private void handleAnimPlay(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /csme4 anim_play <instanceId> <animationId>");
            return;
        }

        InstanceData data = instances.get(args[1]);
        if (data == null) {
            sender.sendMessage("Unknown instance: " + args[1]);
            return;
        }

        String animationId = args[2];
        data.activeModel().getAnimationHandler().playAnimation(animationId, 0, 0);
        sender.sendMessage("Playing animation " + animationId + " for " + args[1]);
    }

    private void handleAnimStop(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /csme4 anim_stop <instanceId> <animationId>");
            return;
        }

        InstanceData data = instances.get(args[1]);
        if (data == null) {
            sender.sendMessage("Unknown instance: " + args[1]);
            return;
        }

        String animationId = args[2];
        data.activeModel().getAnimationHandler().stopAnimation(animationId);
        sender.sendMessage("Stopped animation " + animationId + " for " + args[1]);
    }

    private void handleMove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /csme4 move <instanceId> [x y z]");
            return;
        }

        InstanceData data = instances.get(args[1]);
        if (data == null) {
            sender.sendMessage("Unknown instance: " + args[1]);
            return;
        }

        Location targetLocation = null;
        if (args.length >= 5) {
            try {
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                targetLocation = data.dummy().getLocation().clone();
                targetLocation.setX(x);
                targetLocation.setY(y);
                targetLocation.setZ(z);
            } catch (NumberFormatException ex) {
                sender.sendMessage("Coordinates must be numbers.");
                return;
            }
        } else if (sender instanceof Player player) {
            targetLocation = player.getLocation();
        }

        if (targetLocation == null) {
            sender.sendMessage("Provide coordinates or run as a player.");
            return;
        }

        data.dummy().setLocation(targetLocation);
        sender.sendMessage("Moved instance " + args[1]);
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /csme4 remove <instanceId>");
            return;
        }

        InstanceData data = instances.remove(args[1]);
        if (data == null) {
            sender.sendMessage("Unknown instance: " + args[1]);
            return;
        }

        data.dummy().setRemoved(true);
        sender.sendMessage("Removed instance: " + args[1]);
    }

    private Player resolveViewer(CommandSender sender, String nameOrNull) {
        if (nameOrNull != null) {
            return Bukkit.getPlayer(nameOrNull);
        }
        if (sender instanceof Player player) {
            return player;
        }
        return null;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("/csme4 create <instanceId> <modelId> [viewer]");
        sender.sendMessage("/csme4 anim_play <instanceId> <animationId>");
        sender.sendMessage("/csme4 anim_stop <instanceId> <animationId>");
        sender.sendMessage("/csme4 move <instanceId> [x y z]");
        sender.sendMessage("/csme4 remove <instanceId>");
    }

    private record InstanceData(
            Dummy<?> dummy,
            ModeledEntity modeledEntity,
            ActiveModel activeModel,
            UUID viewerUuid
    ) {
    }
}
