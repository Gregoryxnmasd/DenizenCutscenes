package com.extracraft;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
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
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class CsMe4Plugin extends JavaPlugin implements CommandExecutor, Listener {
    private final Map<String, InstanceData> instances = new HashMap<>();

    @Override
    public void onEnable() {
        if (getCommand("csme4") != null) {
            getCommand("csme4").setExecutor(this);
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
            case "spawn":
                handleSpawn(sender, args);
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
            case "cleanup_viewer":
                handleCleanupViewer(sender, args);
                return true;
            default:
                sendUsage(sender);
                return true;
        }
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        if (args.length < 9) {
            sender.sendMessage("Usage: /csme4 spawn <viewerUUID> <cutsceneId> <modelId> <x> <y> <z> <yaw> <pitch>");
            return;
        }

        UUID viewerUuid = parseUuid(sender, args[1], "viewerUUID");
        if (viewerUuid == null) {
            return;
        }

        String cutsceneId = args[2];
        String modelId = args[3];
        Player viewer = Bukkit.getPlayer(viewerUuid);
        if (viewer == null) {
            sender.sendMessage("Viewer must be an online player.");
            return;
        }

        double x;
        double y;
        double z;
        float yaw;
        float pitch;
        try {
            x = Double.parseDouble(args[4]);
            y = Double.parseDouble(args[5]);
            z = Double.parseDouble(args[6]);
            yaw = Float.parseFloat(args[7]);
            pitch = Float.parseFloat(args[8]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Location coordinates and rotation must be numbers.");
            return;
        }

        String key = buildKey(viewerUuid, cutsceneId);
        InstanceData existing = instances.remove(key);
        if (existing != null) {
            existing.dummy().setRemoved(true);
        }

        Location spawnLocation = new Location(viewer.getWorld(), x, y, z, yaw, pitch);
        Dummy<?> dummy = new Dummy<>();
        dummy.setLocation(spawnLocation);
        dummy.registerData();
        dummy.setDetectingPlayers(false);

        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        modeledEntity.addModel(activeModel, true);

        applyVisibility(dummy, viewer.getUniqueId());

        instances.put(key, new InstanceData(dummy, modeledEntity, activeModel, viewer.getUniqueId(), cutsceneId));
        sender.sendMessage("Spawned ModelEngine instance for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
    }

    private void handleAnimPlay(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /csme4 anim_play <viewerUUID> <cutsceneId> <animationId>");
            return;
        }

        UUID viewerUuid = parseUuid(sender, args[1], "viewerUUID");
        if (viewerUuid == null) {
            return;
        }

        String cutsceneId = args[2];
        InstanceData data = instances.get(buildKey(viewerUuid, cutsceneId));
        if (data == null) {
            sender.sendMessage("Missing instance for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
            return;
        }

        String animationId = args[3];
        data.activeModel().getAnimationHandler().playAnimation(animationId, 0.0, 0.0, 0.0, false);
        sender.sendMessage("Playing animation " + animationId + " for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
    }

    private void handleAnimStop(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /csme4 anim_stop <viewerUUID> <cutsceneId> [animationId]");
            return;
        }

        UUID viewerUuid = parseUuid(sender, args[1], "viewerUUID");
        if (viewerUuid == null) {
            return;
        }

        String cutsceneId = args[2];
        InstanceData data = instances.get(buildKey(viewerUuid, cutsceneId));
        if (data == null) {
            sender.sendMessage("Missing instance for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
            return;
        }

        if (args.length >= 4) {
            String animationId = args[3];
            data.activeModel().getAnimationHandler().stopAnimation(animationId);
            sender.sendMessage("Stopped animation " + animationId + " for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
        } else {
            data.activeModel().getAnimationHandler().forceStopAllAnimations();
            sender.sendMessage("Stopped animations for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
        }
    }

    private void handleMove(CommandSender sender, String[] args) {
        if (args.length < 8) {
            sender.sendMessage("Usage: /csme4 move <viewerUUID> <cutsceneId> <x> <y> <z> <yaw> <pitch>");
            return;
        }

        UUID viewerUuid = parseUuid(sender, args[1], "viewerUUID");
        if (viewerUuid == null) {
            return;
        }

        String cutsceneId = args[2];
        InstanceData data = instances.get(buildKey(viewerUuid, cutsceneId));
        if (data == null) {
            sender.sendMessage("Missing instance for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
            return;
        }

        double x;
        double y;
        double z;
        float yaw;
        float pitch;
        try {
            x = Double.parseDouble(args[3]);
            y = Double.parseDouble(args[4]);
            z = Double.parseDouble(args[5]);
            yaw = Float.parseFloat(args[6]);
            pitch = Float.parseFloat(args[7]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Location coordinates and rotation must be numbers.");
            return;
        }

        Location targetLocation = data.dummy().getLocation().clone();
        targetLocation.setX(x);
        targetLocation.setY(y);
        targetLocation.setZ(z);
        targetLocation.setYaw(yaw);
        targetLocation.setPitch(pitch);
        data.dummy().setLocation(targetLocation);
        sender.sendMessage("Moved instance for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
    }

    private void handleVisibility(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /csme4 visibility <viewerUUID> <cutsceneId> <visible|hidden>");
            return;
        }

        UUID viewerUuid = parseUuid(sender, args[1], "viewerUUID");
        if (viewerUuid == null) {
            return;
        }

        String cutsceneId = args[2];
        InstanceData data = instances.get(buildKey(viewerUuid, cutsceneId));
        if (data == null) {
            sender.sendMessage("Missing instance for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
            return;
        }

        String mode = args[3].toLowerCase(Locale.ROOT);
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
        sender.sendMessage("Visibility for viewer " + viewerUuid + " in cutscene " + cutsceneId + " set to "
                + (hidden ? "hidden" : "visible") + ".");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /csme4 remove <viewerUUID> <cutsceneId>");
            return;
        }

        UUID viewerUuid = parseUuid(sender, args[1], "viewerUUID");
        if (viewerUuid == null) {
            return;
        }

        String cutsceneId = args[2];
        String key = buildKey(viewerUuid, cutsceneId);
        InstanceData data = instances.remove(key);
        if (data == null) {
            sender.sendMessage("Missing instance for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
            return;
        }

        data.dummy().setRemoved(true);
        sender.sendMessage("Removed instance for viewer " + viewerUuid + " in cutscene " + cutsceneId + ".");
    }

    private void handleCleanupViewer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /csme4 cleanup_viewer <viewerUUID>");
            return;
        }

        UUID viewerUuid = parseUuid(sender, args[1], "viewerUUID");
        if (viewerUuid == null) {
            return;
        }

        int removedCount = 0;
        var iterator = instances.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, InstanceData> entry = iterator.next();
            if (!entry.getValue().viewerUuid().equals(viewerUuid)) {
                continue;
            }
            entry.getValue().dummy().setRemoved(true);
            iterator.remove();
            removedCount++;
        }
        sender.sendMessage("Removed " + removedCount + " instance(s) for viewer " + viewerUuid + ".");
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
        sender.sendMessage("/csme4 spawn <viewerUUID> <cutsceneId> <modelId> <x> <y> <z> <yaw> <pitch>");
        sender.sendMessage("/csme4 anim_play <viewerUUID> <cutsceneId> <animationId>");
        sender.sendMessage("/csme4 anim_stop <viewerUUID> <cutsceneId> [animationId]");
        sender.sendMessage("/csme4 move <viewerUUID> <cutsceneId> <x> <y> <z> <yaw> <pitch>");
        sender.sendMessage("/csme4 visibility <viewerUUID> <cutsceneId> <visible|hidden>");
        sender.sendMessage("/csme4 remove <viewerUUID> <cutsceneId>");
        sender.sendMessage("/csme4 cleanup_viewer <viewerUUID>");
    }

    private String buildKey(UUID viewerUuid, String cutsceneId) {
        return viewerUuid + ":" + cutsceneId.toLowerCase(Locale.ROOT);
    }

    private UUID parseUuid(CommandSender sender, String input, String label) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(label + " must be a valid UUID.");
            return null;
        }
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

    private static final class InstanceData {
        private final Dummy<?> dummy;
        private final ModeledEntity modeledEntity;
        private final ActiveModel activeModel;
        private final UUID viewerUuid;
        private final String cutsceneId;
        private boolean forceHidden;

        private InstanceData(
                Dummy<?> dummy,
                ModeledEntity modeledEntity,
                ActiveModel activeModel,
                UUID viewerUuid,
                String cutsceneId
        ) {
            this.dummy = dummy;
            this.modeledEntity = modeledEntity;
            this.activeModel = activeModel;
            this.viewerUuid = viewerUuid;
            this.cutsceneId = cutsceneId;
        }

        private Dummy<?> dummy() {
            return dummy;
        }

        private ModeledEntity modeledEntity() {
            return modeledEntity;
        }

        private ActiveModel activeModel() {
            return activeModel;
        }

        private UUID viewerUuid() {
            return viewerUuid;
        }

        private String cutsceneId() {
            return cutsceneId;
        }

        private boolean forceHidden() {
            return forceHidden;
        }

        private void setForceHidden(boolean forceHidden) {
            this.forceHidden = forceHidden;
        }
    }
}
