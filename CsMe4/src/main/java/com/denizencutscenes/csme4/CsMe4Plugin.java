package com.denizencutscenes.csme4;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.dummy.Dummy;
import com.ticxo.modelengine.api.entity.ActiveModel;
import com.ticxo.modelengine.api.entity.ModeledEntity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class CsMe4Plugin extends JavaPlugin implements CommandExecutor, Listener {
    private final Map<String, InstanceData> instances = new HashMap<>();

    @Override
    public void onEnable() {
        if (getCommand("cs_me4") != null) {
            getCommand("cs_me4").setExecutor(this);
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (InstanceData data : instances.values()) {
            data.dummy().setRemoved(true);
            data.modeledEntity().destroy();
        }
        instances.clear();
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
            case "visibility":
                handleVisibility(sender, args);
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
            sender.sendMessage("Usage: /cs_me4 create <instanceId> <modelId> [viewer]");
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

        applyVisibility(dummy, viewer.getUniqueId());

        instances.put(instanceId, new InstanceData(dummy, modeledEntity, activeModel, viewer.getUniqueId(), false));
        sender.sendMessage("Created ModelEngine instance: " + instanceId);
    }

    private void handleAnimPlay(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /cs_me4 anim_play <instanceId> <animationId> [fadeIn] [fadeOut] [speed] [loop]");
            return;
        }

        InstanceData data = instances.get(args[1]);
        if (data == null) {
            sender.sendMessage("Unknown instance: " + args[1]);
            return;
        }

        String animationId = args[2];
        float fadeIn = 0f;
        float fadeOut = 0f;
        float speed = 1f;
        boolean loop = false;

        if (args.length >= 4) {
            Float parsedFadeIn = parseFloatArg(sender, args[3], "fadeIn");
            if (parsedFadeIn == null) {
                return;
            }
            fadeIn = parsedFadeIn;
        }

        if (args.length >= 5) {
            Float parsedFadeOut = parseFloatArg(sender, args[4], "fadeOut");
            if (parsedFadeOut == null) {
                return;
            }
            fadeOut = parsedFadeOut;
        }

        if (args.length >= 6) {
            Float parsedSpeed = parseFloatArg(sender, args[5], "speed");
            if (parsedSpeed == null) {
                return;
            }
            speed = parsedSpeed;
        }

        if (args.length >= 7) {
            Boolean parsedLoop = parseBooleanArg(sender, args[6], "loop");
            if (parsedLoop == null) {
                return;
            }
            loop = parsedLoop;
        }

        data.activeModel().getAnimationHandler().playAnimation(animationId, fadeIn, fadeOut, speed, loop);
        sender.sendMessage("Playing animation " + animationId + " for " + args[1]);
    }

    private void handleAnimStop(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /cs_me4 anim_stop <instanceId> <animationId> [force]");
            return;
        }

        InstanceData data = instances.get(args[1]);
        if (data == null) {
            sender.sendMessage("Unknown instance: " + args[1]);
            return;
        }

        String animationId = args[2];
        boolean force = false;
        if (args.length >= 4) {
            Boolean parsedForce = parseBooleanArg(sender, args[3], "force");
            if (parsedForce == null) {
                return;
            }
            force = parsedForce;
        }

        if (force) {
            if (!forceStopAnimationIfAvailable(data.activeModel(), animationId)) {
                data.activeModel().getAnimationHandler().stopAnimation(animationId);
            }
        } else {
            data.activeModel().getAnimationHandler().stopAnimation(animationId);
        }
        sender.sendMessage("Stopped animation " + animationId + " for " + args[1]);
    }

    private void handleMove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /cs_me4 move <instanceId> [x y z yaw pitch [world]]");
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
                if (args.length >= 7) {
                    float yaw = Float.parseFloat(args[5]);
                    float pitch = Float.parseFloat(args[6]);
                    targetLocation.setYaw(yaw);
                    targetLocation.setPitch(pitch);
                }
                if (args.length >= 8) {
                    var world = Bukkit.getWorld(args[7]);
                    if (world == null) {
                        sender.sendMessage("Unknown world: " + args[7]);
                        return;
                    }
                    targetLocation.setWorld(world);
                }
            } catch (NumberFormatException ex) {
                sender.sendMessage("Coordinates, yaw, and pitch must be numbers.");
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
        applyVisibility(data.dummy(), data.viewerUuid());
        sender.sendMessage("Moved instance " + args[1]);
    }

    private void handleVisibility(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /cs_me4 visibility <instanceId> <visible|hidden>");
            return;
        }

        InstanceData data = instances.get(args[1]);
        if (data == null) {
            sender.sendMessage("Unknown instance: " + args[1]);
            return;
        }

        String mode = args[2].toLowerCase(Locale.ROOT);
        boolean hidden;
        switch (mode) {
            case "hidden":
            case "hide":
            case "false":
            case "off":
            case "0":
                hidden = true;
                break;
            case "visible":
            case "show":
            case "true":
            case "on":
            case "1":
                hidden = false;
                break;
            default:
                sender.sendMessage("Visibility must be 'visible' or 'hidden'.");
                return;
        }

        data.setForceHidden(hidden);
        applyVisibility(data);
        sender.sendMessage("Visibility for " + args[1] + " set to " + (hidden ? "hidden" : "visible"));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /cs_me4 remove <instanceId>");
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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cleanup_viewer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        cleanup_viewer(event.getPlayer().getUniqueId());
    }

    private void cleanup_viewer(UUID viewerUuid) {
        instances.entrySet().removeIf(entry -> {
            InstanceData data = entry.getValue();
            if (!data.viewerUuid().equals(viewerUuid)) {
                return false;
            }
            data.dummy().setRemoved(true);
            data.modeledEntity().destroy();
            return true;
        });
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

    private void applyVisibility(InstanceData data) {
        Player viewer = Bukkit.getPlayer(data.viewerUuid());
        if (viewer == null) {
            return;
        }
        if (data.forceHidden()) {
            data.dummy().setForceHidden(viewer, true);
            data.dummy().setForceViewing(viewer, false);
        } else {
            data.dummy().setForceHidden(viewer, false);
            data.dummy().setForceViewing(viewer, true);
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("/cs_me4 create <instanceId> <modelId> [viewer]");
        sender.sendMessage("/cs_me4 anim_play <instanceId> <animationId> [fadeIn] [fadeOut] [speed] [loop]");
        sender.sendMessage("/cs_me4 anim_stop <instanceId> <animationId> [force]");
        sender.sendMessage("/cs_me4 move <instanceId> [x y z]");
        sender.sendMessage("/cs_me4 remove <instanceId>");
    }

    private void applyVisibility(Dummy<?> dummy, UUID viewerUuid) {
        Player viewer = Bukkit.getPlayer(viewerUuid);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (viewer != null && player.getUniqueId().equals(viewerUuid)) {
                dummy.setForceViewing(player, true);
            } else {
                dummy.setForceHidden(player, true);
            }
        }
    }

    private record InstanceData(
            Dummy<?> dummy,
            ModeledEntity modeledEntity,
            ActiveModel activeModel,
            UUID viewerUuid
    ) {
    }
}
