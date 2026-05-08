package dev.rm20.anglersalmanac.Components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.common.OggVorbisInfoCache;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEventLayer;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.*;

public class AudioPlayerComponent implements Component<EntityStore> {
    public static ComponentType<EntityStore, AudioPlayerComponent> COMPONENT_TYPE;
    private List<String> sounds = new ArrayList<>();
    private HashMap<String, Long> soundDurations = new HashMap<>(); // Duration of each sound as nanoseconds
    public long playNextTime = 0;
    public long lastPlayTime = 0;
    public int lastSoundIndex = 0;
    public long allowedOverlap = 0; // (30000000 is an okay value for removing loop silence = 30ms). Audio overlap allowed in nanoseconds.
    public boolean autoplay = false;
    public boolean autoplayAsRandom = false;
    public UUID selfUUID;
    public Ref<EntityStore> playerRef;

    public AudioPlayerComponent(){

    }

    public AudioPlayerComponent(Ref<EntityStore> player){
        playerRef = player;
    }


    public void addSound(String soundEventId){
        if(sounds.contains(soundEventId)) return; // Already contains sound, don't duplicate.
        sounds.add(soundEventId);
        soundDurations.put(soundEventId, readDurationOf(soundEventId));
    }

    public void addSounds(String[] soundEventIds){
        for(String sound : soundEventIds){
            addSound(sound);
        }
    }

    public void removeSound(String soundEventId){
        sounds.remove(soundEventId);
        soundDurations.remove(soundEventId);
    }

    public void removeSounds(String[] soundEventIds){
        for(String sound : soundEventIds){
            removeSound(sound);
        }
    }

    public boolean hasSound(String soundEventId){
        return sounds.contains(soundEventId);
    }

    private long readDurationOf(String soundEventId){
        if(!sounds.contains(soundEventId)) return 0;
        // TODO search through all files in all layers to find the longest duration to return.
        var asset = SoundEvent.getAssetMap().getAsset(soundEventId);
        if(asset == null){
            AnglersAlmanac.LOGGER.atSevere().log("No sound file matching name %s found!", soundEventId);
        }

        // Get duration from file info.
        SoundEventLayer layer = asset.getLayers()[0];
        double durationSeconds = OggVorbisInfoCache.getNow(layer.getFiles()[0]).duration;

        // Convert to nanoseconds (without losing precision)
        BigDecimal secDecimal = BigDecimal.valueOf(durationSeconds);
        BigDecimal nanoMultiplier = new BigDecimal("1000000000");

        return secDecimal.multiply(nanoMultiplier).longValue();
    }

    public long getDurationOf(String soundEventId){
        if(!sounds.contains(soundEventId)) return 0;
        return soundDurations.get(soundEventId);
    }

    public boolean isCurrentlyPlaying(){
        //TimeUnit.SECONDS.convert(System.nanoTime() - lastCastOrReelTime, TimeUnit.NANOSECONDS) >= cooldownTime;
        return System.nanoTime() < playNextTime - allowedOverlap;
    }

    public void playRandomSound(Vector3d pos, @Nonnull ComponentAccessor<EntityStore> componentAccessor){
        String soundId = sounds.get(new Random().nextInt(sounds.size()));
        playSound(soundId, pos, componentAccessor);
    }
    public void playRandomSound(@Nonnull ComponentAccessor<EntityStore> componentAccessor){
        String soundId = sounds.get(new Random().nextInt(sounds.size()));
        playSound(soundId, componentAccessor);
    }

    public void playSound(String soundId, Vector3d pos, @Nonnull ComponentAccessor<EntityStore> componentAccessor){
        if(playerRef==null)
        {
            SoundUtil.playSoundEvent3d(SoundEvent.getAssetMap().getIndex(soundId), SoundCategory.UI, pos, componentAccessor);
        }
        else {
            SoundUtil.playSoundEvent3dToPlayer(playerRef,SoundEvent.getAssetMap().getIndex(soundId), SoundCategory.UI, pos, componentAccessor);
        }
        playNextTime = System.nanoTime() + getDurationOf(soundId);
        lastPlayTime = System.nanoTime();
        lastSoundIndex = sounds.indexOf(soundId);
    }
    public void playSound(String soundId, @Nonnull ComponentAccessor<EntityStore> componentAccessor){
        playSound(soundId, getAudioPlayerPos(componentAccessor), componentAccessor);

    }

    /// Can be run in a system tick. Will only play the next sound if the current sound has concluded.
    public void doLoopAll(boolean pickRandom, Vector3d pos, @Nonnull ComponentAccessor<EntityStore> componentAccessor){
        if(isCurrentlyPlaying()) {
            //AnglersAlmanac.LOGGER.atInfo().log("Sound already playing; skipping");
            return;
        }
        if(pickRandom){
            playRandomSound(pos, componentAccessor);
        }else{
            // TODO Test this function with pickRandom false.
            // Pick next sound or overflow.
            int nextSoundIndex = lastSoundIndex+1;
            if(lastSoundIndex >= sounds.size()){ nextSoundIndex = 0; }
            playSound(sounds.get(nextSoundIndex), pos, componentAccessor);
        }
    }
    public void doLoopAll(boolean pickRandom, @Nonnull ComponentAccessor<EntityStore> componentAccessor){
        doLoopAll(pickRandom, getAudioPlayerPos(componentAccessor), componentAccessor);
    }

    public void doLoopSingle(String soundEventId, Vector3d pos, @Nonnull ComponentAccessor<EntityStore> componentAccessor){
        if(isCurrentlyPlaying()) return;
        playSound(soundEventId, pos, componentAccessor);
    }
    public void doLoopSingle(String soundEventId, @Nonnull ComponentAccessor<EntityStore> componentAccessor){
        if(isCurrentlyPlaying()) return;
        playSound(soundEventId, getAudioPlayerPos(componentAccessor) , componentAccessor);
    }

    private Vector3d getAudioPlayerPos(@Nonnull ComponentAccessor<EntityStore> componentAccessor){
        if(selfUUID == null) return Vector3d.ZERO;
        TransformComponent transform = componentAccessor.getComponent(componentAccessor.getExternalData().getRefFromUUID(selfUUID), TransformComponent.getComponentType());
        if(transform == null) return Vector3d.ZERO;
        return transform.getPosition().clone();
    }


    public AudioPlayerComponent spawnNewAudioPlayerEntity(@Nonnull ComponentAccessor<EntityStore> componentAccessor){
        return spawnNewAudioPlayerEntity(Vector3d.ZERO, componentAccessor);
    }
    public static AudioPlayerComponent spawnNewAudioPlayerEntity(Vector3d spawnPos, @Nonnull ComponentAccessor<EntityStore> componentAccessor){
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        UUID uuid = UUIDUtil.generateVersion3UUID();
        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(uuid));
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(spawnPos, Vector3f.ZERO));

        AudioPlayerComponent apc = new AudioPlayerComponent();
        apc.selfUUID = uuid;
        holder.addComponent(AudioPlayerComponent.getComponentType(), apc);

        componentAccessor.getExternalData().getWorld().execute(() -> {
            componentAccessor.addEntity(holder, AddReason.SPAWN);
        });

        return apc;
    }

    public static AudioPlayerComponent spawnNewAudioPlayerEntity(Vector3d spawnPos, @Nonnull ComponentAccessor<EntityStore> componentAccessor, Ref<EntityStore> playerRef){
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        UUID uuid = UUIDUtil.generateVersion3UUID();
        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(uuid));
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(spawnPos, Vector3f.ZERO));

        AudioPlayerComponent apc = new AudioPlayerComponent(playerRef);
        apc.selfUUID = uuid;
        holder.addComponent(AudioPlayerComponent.getComponentType(), apc);

        componentAccessor.getExternalData().getWorld().execute(() -> {
            componentAccessor.addEntity(holder, AddReason.SPAWN);
        });

        return apc;
    }




    @Override
    public @Nullable Component<EntityStore> clone() {
        AudioPlayerComponent ac = new AudioPlayerComponent();
        ac.lastSoundIndex = this.lastSoundIndex;
        ac.playNextTime = this.playNextTime;
        ac.lastPlayTime = this.lastPlayTime;
        ac.sounds = this.sounds;
        ac.soundDurations = this.soundDurations;
        return null;
    }

    public static ComponentType<EntityStore, AudioPlayerComponent> getComponentType() {
        return COMPONENT_TYPE;
    }
}
