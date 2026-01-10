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
import org.bukkit.plugin.java.JavaPlugin;

public final class CsMe4Plugin extends JavaPlugin implements CommandExecutor {
    private final Map<String, InstanceData> instances = new HashMap<>();

    @Override
    public void onEnable() {
        if (getCommand("cs_me4") != null) {
            getCommand("cs_me4").setExecutor(this);
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

        dummy.setForceViewing(viewer, true);

        instances.put(instanceId, new InstanceData(dummy, modeledEntity, activeModel, viewer.getUniqueId()));
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
            sender.sendMessage("Usage: /cs_me4 move <instanceId> [x y z]");
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
        sender.sendMessage("/cs_me4 create <instanceId> <modelId> [viewer]");
        sender.sendMessage("/cs_me4 anim_play <instanceId> <animationId> [fadeIn] [fadeOut] [speed] [loop]");
        sender.sendMessage("/cs_me4 anim_stop <instanceId> <animationId> [force]");
        sender.sendMessage("/cs_me4 move <instanceId> [x y z]");
        sender.sendMessage("/cs_me4 remove <instanceId>");
    }

    private Float parseFloatArg(CommandSender sender, String value, String label) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            sender.sendMessage(label + " must be a number.");
            return null;
        }
    }

    private Boolean parseBooleanArg(CommandSender sender, String value, String label) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        sender.sendMessage(label + " must be true or false.");
        return null;
    }

    private boolean forceStopAnimationIfAvailable(ActiveModel model, String animationId) {
        Object handler = model.getAnimationHandler();
        try {
            Method method = handler.getClass().getMethod("forceStopAnimation", String.class);
            method.invoke(handler, animationId);
            return true;
        } catch (NoSuchMethodException ignored) {
            try {
                Method method = handler.getClass().getMethod("stopAnimation", String.class, boolean.class);
                method.invoke(handler, animationId, true);
                return true;
            } catch (NoSuchMethodException ignoredAgain) {
                return false;
            } catch (IllegalAccessException | InvocationTargetException ex) {
                getLogger().warning("Failed to force stop animation " + animationId + ": " + ex.getMessage());
                return false;
            }
        } catch (IllegalAccessException | InvocationTargetException ex) {
            getLogger().warning("Failed to force stop animation " + animationId + ": " + ex.getMessage());
            return false;
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
