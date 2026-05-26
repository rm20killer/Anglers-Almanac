package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.ambience.AmbiencePlugin;
import com.hypixel.hytale.builtin.ambience.components.AmbientEmitterComponent;
import com.hypixel.hytale.component.*;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateSoundEvents;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoundUtils {

    public static void changeSoundAssetVolume(PlayerRef playerRef, String soundId, float newVolume) {

        try {
            IndexedLookupTableAssetMap<String, SoundEvent> assetMap = SoundEvent.getAssetMap();
            SoundEvent originalSound = assetMap.getAsset(soundId);
            if (originalSound == null) {
                AnglersAlmanac.LOGGER.atWarning().log("SoundID not found: " + soundId);
                return;
            }

            com.hypixel.hytale.protocol.SoundEvent modifiedSound = originalSound.toPacket();
            modifiedSound.volume = newVolume;
            int soundIndex = assetMap.getIndex(soundId);
            Map<Integer, com.hypixel.hytale.protocol.SoundEvent> updates = new HashMap();
            updates.put(soundIndex, modifiedSound);
            UpdateSoundEvents packet = new UpdateSoundEvents(UpdateType.AddOrUpdate, assetMap.getNextIndex(), updates);
            playerRef.getPacketHandler().write(packet);
            //AnglersAlmanac.LOGGER.atInfo().log("Changing sound asset volume to %s", newVolume);
        } catch (Exception e) {
            AnglersAlmanac.LOGGER.atWarning().log("Changing sound volume failed: " + e.getMessage());
        }
    }

    public static UUID createNewSoundEntity(String soundEventAssetId, Vector3d spawnPos, @Nonnull Store<EntityStore> store){
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        UUID uuid = UUIDUtil.generateVersion3UUID();
        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(uuid));
        int netId = store.getExternalData().takeNextNetworkId();
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(netId));



        AmbientEmitterComponent emitterComponent = new AmbientEmitterComponent();
        emitterComponent.setSoundEventId(soundEventAssetId);
        holder.addComponent(AmbientEmitterComponent.getComponentType(), emitterComponent);
        TransformComponent transform = new TransformComponent();
        transform.setPosition(spawnPos);
        holder.addComponent(TransformComponent.getComponentType(), transform);
        holder.addComponent(Nameplate.getComponentType(), new Nameplate(soundEventAssetId));
        holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
        Model model = AmbiencePlugin.get().getAmbientEmitterModel();
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));



        store.getExternalData().getWorld().execute(() -> {
            store.addEntity(holder, AddReason.SPAWN);
            //SoundUtil.playSoundEventEntity(SoundEvent.getAssetMap().getIndex(soundEventAssetId), netId, store);

            //SoundUtil.playSoundEvent3d(store.getExternalData().getRefFromUUID(uuid), SoundEvent.getAssetMap().getIndex(soundEventAssetId),transform.getPosition().clone() ,store);
        });

        return uuid;
    }
}
