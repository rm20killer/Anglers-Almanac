package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Rotation3fc;
import org.joml.*;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.Math;
import java.util.UUID;

/// A helper class for position and rotation calculations.
public class TransformUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void applyBillboard(Ref<EntityStore> looker, Ref<EntityStore>  lookAtTarget, Vector3f finalRotationAdjustment, ComponentAccessor<EntityStore> store){
        Vector3f newRotation;

        if(looker==null){
            LOGGER.atWarning().log("Failed to apply billboard. (Missing entity Ref on looker)");
            return;
        }

        if(lookAtTarget == null){
            LOGGER.atWarning().log("Failed to apply billboard. (Missing entity Ref on lookAtTarget)");
            return;
        }


        Vector3d lookerPos = store.getComponent(looker, TransformComponent.getComponentType()).getPosition();
        Vector3d targetPos = store.getComponent(lookAtTarget, TransformComponent.getComponentType()).getPosition();
        newRotation = billboard(lookerPos, targetPos, finalRotationAdjustment, store);

        if(store.getComponent(looker, HeadRotation.getComponentType()) != null){
            store.getComponent(looker, HeadRotation.getComponentType()).setRotation(toRotation3fc(newRotation));
        }
        store.getComponent(looker, TransformComponent.getComponentType()).setRotation(toRotation3f(newRotation));

    }

    public static void applyBillboard(UUID lookerID, Vector3d lookerPos, Vector3d lookAtTargetPos, Vector3f finalRotationAdjustment, ComponentAccessor<EntityStore> store){
        Vector3f newRotation = new Vector3f();

        Ref<EntityStore> lookerRef = store.getExternalData().getRefFromUUID(lookerID);
        if(lookerRef == null){
            LOGGER.atWarning().log("Failed to apply billboard. (Missing entity Ref)");
            return;
        }
        newRotation = billboard(lookerPos, lookAtTargetPos, finalRotationAdjustment, store);
        if(store.getComponent(lookerRef, HeadRotation.getComponentType()) != null){
            store.getComponent(lookerRef, HeadRotation.getComponentType()).setRotation(toRotation3fc(newRotation));
        }
        store.getComponent(lookerRef, TransformComponent.getComponentType()).setRotation(toRotation3f(newRotation));

    }
    public static void applyBillboardYOnly(UUID lookerID, Vector3d lookerPos, Vector3d lookAtTargetPos, Vector3f finalRotationAdjustment, ComponentAccessor<EntityStore> store){
        Vector3f newRotation = new Vector3f();

        Ref<EntityStore> lookerRef = store.getExternalData().getRefFromUUID(lookerID);
        if(lookerRef == null){
            LOGGER.atWarning().log("Failed to apply billboard. (Missing entity Ref)");
            return;
        }

        newRotation = billboard(lookerPos, lookAtTargetPos, finalRotationAdjustment, store);
        newRotation = new Vector3f(0, newRotation.y, 0);

        if(store.getComponent(lookerRef, HeadRotation.getComponentType()) != null){
            store.getComponent(lookerRef, HeadRotation.getComponentType()).setRotation(toRotation3fc(newRotation));
        }
        store.getComponent(lookerRef, TransformComponent.getComponentType()).setRotation(toRotation3f(newRotation));

    }


    public static Vector3f billboard(Vector3d lookerPos, Vector3d lookAtTargetPos, Vector3f finalRotationAdjustment, ComponentAccessor<EntityStore> store){
        Vector3d directionToPlayer = new Vector3d(lookAtTargetPos).sub(lookerPos).normalize();
        float yaw = (float) Math.atan2(directionToPlayer.x, directionToPlayer.z);
        float pitch = (float) Math.atan2(-directionToPlayer.y, Math.sqrt(directionToPlayer.x * directionToPlayer.x + directionToPlayer.z * directionToPlayer.z));
        Vector3f fishRotation = new Vector3f(pitch, yaw, 0.0f);
        fishRotation = fishRotation.add(finalRotationAdjustment);
        return fishRotation;
    }

    public static Vector3d moveBetween(Vector3d startPos, Vector3d targetPos, float progress){
        Vector3d direction = new Vector3d(targetPos).sub(startPos).normalize();
        double distance = startPos.distance(targetPos);
        return direction.mul(distance * progress);
    }

    public static Vector3d moveTowards(Vector3d startPos, Vector3d targetPos, double amount){
        if(amount > startPos.distance(targetPos)) return targetPos; // Prevent overshooting.
        Vector3d direction = new Vector3d(targetPos).sub(startPos).normalize();
        return direction.mul(amount);
    }

    public static Vector3f moveTowards(Vector3f startPos, Vector3f targetPos, float amount){
        if(amount > startPos.distance(targetPos)) return targetPos; // Prevent overshooting.
        Vector3f direction = new Vector3f(targetPos).sub(startPos).normalize();
        return direction.mul(amount);
    }

    public static double moveTowards(double startPos, double targetPos, double amount){
        double vector = targetPos - startPos;
        double direction = Math.signum(vector);
        double result = direction * amount;
        if(Math.abs(result) > Math.abs(targetPos)) result = targetPos;
        return result;
    }

    public static Vector3d moveAwayFrom(Vector3d startPos, Vector3d targetPos, double amount){
        return moveTowards(startPos, targetPos, amount).mul(-1);
    }

    public static boolean isInFluid(UUID entityId, World world){
        Ref<EntityStore> ref = world.getEntityRef(entityId);
        if(ref == null) return false;
        Store<EntityStore> store = world.getEntityStore().getStore();
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if(transform == null) return false;
        Vector3i posI = new Vector3i();
        Vector3d posD = transform.getPosition();
        posI.set((int)posD.x, (int)posD.y, (int)posD.z);
        return isInFluid(posI, world);
    }

    public static boolean isInFluid(Vector3i pos, World world){
        int occupiedBlockId = world.getFluidId(pos.x, pos.y, pos.z);
        // Is any fluid.
        return occupiedBlockId != 0;
    }

    public static float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    public static Rotation3fc toRotation3fc(Vector3f eulerAngles) {
        return new Rotation3f(
                eulerAngles.x, // pitch
                eulerAngles.y, // yaw
                eulerAngles.z  // roll
        );
    }

    public static Rotation3f toRotation3f(Vector3f eulerAngles) {
        return new Rotation3f(
                eulerAngles.x, // pitch
                eulerAngles.y, // yaw
                eulerAngles.z  // roll
        );
    }
}
