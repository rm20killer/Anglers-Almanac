package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;

public class WeightableAnimation extends ModelAsset.Animation{

    public WeightableAnimation(){
        super();
    }

    public WeightableAnimation(ModelAsset.Animation anim){
        super(
                "",
                anim.getAnimation(),
                anim.getSpeed(),
                anim.getBlendingDuration(),
                anim.isLooping(),
                (float) anim.getWeight(),
                anim.toPacket().footstepIntervals,
                anim.getSoundEventId()
        );
    }

    public WeightableAnimation(ModelAsset.Animation anim, float newWeight){
        super(
                "",
                anim.getAnimation(),
                anim.getSpeed(),
                anim.getBlendingDuration(),
                anim.isLooping(),
                newWeight,
                anim.toPacket().footstepIntervals,
                anim.getSoundEventId()
        );
    }

    public void setWeight(float newWeight){
        this.weight = newWeight;
    }
}
