package dev.rm20.anglersalmanac.Commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Registration.CommandInfo;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CommandInfo(
        name = "cleanup",
        description = "Purges all tension bar minigame visual widgets from the loaded world chunks"
)
public class PurgeWidgetsCommand extends AbstractPlayerCommand {

    private static final Set<String> TARGET_MODEL_IDS = Set.of(
            "AA_TensionBar_Mid0",
            "AA_TensionBar_Mid1",
            "AA_TensionBar_Mid2",
            "AA_TensionBar_End",
            "SSF_MinigameFrame",
            "SSF_FishIcon",
            "SSF_SwordIcon",
            "AA_FishCatchZone"
    );

    public PurgeWidgetsCommand(@Nonnull String name, @Nonnull String description) {
        super(name, description);
        requirePermission("AnglersAlmanac.admin");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (!playerRef.hasPermission("AnglersAlmanac.admin")) {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.noPerms"));
            return;
        }

        List<Ref<EntityStore>> toDespawn = new ArrayList<>();

        // Iterate through all loaded entities in the world store
        store.forEachChunk((chunk, commandBuffer) -> {
            for (int i = 0; i < chunk.size(); i++) {
                Ref<EntityStore> entityRef = chunk.getReferenceTo(i);
                if (entityRef == null || !entityRef.isValid()) continue;

                ModelComponent modelComponent = chunk.getComponent(i, ModelComponent.getComponentType());
                if (modelComponent != null && modelComponent.getModel() != null) {
                    String modelAssetId = modelComponent.getModel().getModelAssetId();
                    if (modelAssetId != null && TARGET_MODEL_IDS.contains(modelAssetId)) {
                        toDespawn.add(entityRef);
                    }
                }
            }
        });

        int count = toDespawn.size();

        // Safely execute entity removal on the world thread
        world.execute(() -> {
            for (Ref<EntityStore> targetRef : toDespawn) {
                if (targetRef.isValid()) {
                    try {
                        store.removeEntity(targetRef, RemoveReason.REMOVE);
                    } catch (Exception e) {
                        AnglersAlmanac.LOGGER.atWarning().withCause(e).log("Failed to purge orphan widget entity: " + targetRef);
                    }
                }
            }
        });

        AnglersAlmanac.LOGGER.atInfo().log("Purged %d tension bar widgets from world %s", count, world.getName());
        commandContext.sendMessage(Message.raw("Purged " + count + " minigame widget entities from loaded chunks."));
    }
}