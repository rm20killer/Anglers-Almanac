package dev.rm20.anglersalmanac.MinigameManager.SkillCheck;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.sun.source.util.Plugin;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import javax.annotation.Nonnull;
import java.util.Random;

public class LinearSkillPage extends InteractiveCustomUIPage<DialEventData> {

    private float zoneX = 0.0f;
    private final float zoneWidth = 60.0f;
    private float currentMarkerX = 0.0f;
    private final float speed = 300.0f;
    private final float maxBarWidth = 600.0f;
    private boolean movingRight = true;

    public LinearSkillPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, DialEventData.CODEC);
    }

    @Override
    public void build(Ref<EntityStore> ref, UICommandBuilder uiCommandBuilder,
                      UIEventBuilder events, Store<EntityStore> store) {

        uiCommandBuilder.append("Pages/Minigames/SkillCheck2.ui");

        // Randomize position and cast to int
        // 0 to (600 - 60)
        this.zoneX = new Random().nextInt(540);
        //AnglersAlmanac.LOGGER.atInfo().log(String.valueOf((int) zoneX));
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(60));
        anchor.setWidth(Value.of(60));
        anchor.setLeft(Value.of((int) zoneX));
        uiCommandBuilder.setObject("#GreenZone.Anchor", anchor);
    }

    public void tick(float dt) {
        if (movingRight) {
            currentMarkerX += speed * dt;
            if (currentMarkerX >= (600 - 10)) {
                movingRight = false;
            }
        } else {
            currentMarkerX -= speed * dt;
            if (currentMarkerX <= 0) {
                movingRight = true;
            }
        }

        int uiPos = (int) currentMarkerX;

        UICommandBuilder move = new UICommandBuilder();
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(60));
        anchor.setWidth(Value.of(60));
        anchor.setLeft(Value.of(uiPos));
        move.setObject("#Marker.Anchor", anchor);
        sendUpdate(move,false);
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, DialEventData data) {
        if ("hit".equals(data.action)) {
            float hitX = data.position;

            // Hit detection logic
            if (hitX >= zoneX && hitX <= (zoneX + zoneWidth)) {
                // Success!
                close();
            } else {
                // Fail!
                close();
            }
        }
    }
}