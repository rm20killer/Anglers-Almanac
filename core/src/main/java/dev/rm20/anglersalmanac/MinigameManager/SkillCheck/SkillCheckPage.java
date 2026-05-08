package dev.rm20.anglersalmanac.MinigameManager.SkillCheck;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class SkillCheckPage extends InteractiveCustomUIPage<DialEventData> {

    // Difficulty
    private final float currentRPM = 40.0f;
    private final float zoneCenter = 180.0f;
    // Behavior
    private final float zoneWidth = 20.0f;
    // Stamina
    private int requiredHits = 3;

    public SkillCheckPage(PlayerRef playerRef) {

        super(playerRef, CustomPageLifetime.CanDismiss, DialEventData.CODEC);
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, DialEventData data) {
        if ("attempt".equals(data.action)) {
            float clickPos = data.rotation % 360;
            float diff = Math.abs(clickPos - zoneCenter);

            if (diff <= (zoneWidth / 2)) {
                handleSuccess();
            } else {
                handleFailure();
            }
        }
    }

    private void handleSuccess() {
        requiredHits--;
        if (requiredHits <= 0) {
            // Logic for catching the fish/unlocking goes here
            close();
        } else {
            UICommandBuilder update = new UICommandBuilder();
            //update.set("#GoalCounter.Text", "Required: " + requiredHits);
            // Flash the zone green
            sendUpdate(update);
        }
    }

    private void handleFailure() {
        // Reset progress or add a penalty
        close();
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Minigames/SkillCheck.ui");
    }
}