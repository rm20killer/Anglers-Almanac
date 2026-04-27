package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/// A helper class for position and rotation calculations.
public class TransformUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void applyBillboard(Ref<EntityStore> looker, Ref<EntityStore>  lookAtTarget, Vector3f finalRotationAdjustment, ComponentAccessor<EntityStore> store){
        Vector3f newRotation = new Vector3f();

        if(looker == null || lookAtTarget == null){
            LOGGER.atWarning().log("Failed to apply billboard. (Missing entity Ref)");
            return;
        }

        Vector3d lookerPos = store.getComponent(looker, TransformComponent.getComponentType()).getPosition();
        Vector3d targetPos = store.getComponent(lookAtTarget, TransformComponent.getComponentType()).getPosition();
        newRotation = billboard(lookerPos, targetPos, finalRotationAdjustment, store);

        if(store.getComponent(looker, HeadRotation.getComponentType()) != null){
            store.getComponent(looker, HeadRotation.getComponentType()).setRotation(newRotation);
        }
        store.getComponent(looker, TransformComponent.getComponentType()).setRotation(newRotation);

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
            store.getComponent(lookerRef, HeadRotation.getComponentType()).setRotation(newRotation);
        }
        store.getComponent(lookerRef, TransformComponent.getComponentType()).setRotation(newRotation);

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
            store.getComponent(lookerRef, HeadRotation.getComponentType()).setRotation(newRotation);
        }
        store.getComponent(lookerRef, TransformComponent.getComponentType()).setRotation(newRotation);

    }

    public static Vector3f billboard(Vector3d lookerPos, Vector3d lookAtTargetPos, Vector3f finalRotationAdjustment, ComponentAccessor<EntityStore> store){
        Vector3d directionToPlayer = Vector3d.directionTo(lookerPos, lookAtTargetPos);
        Vector3f fishRotation = Vector3f.lookAt(directionToPlayer);
        fishRotation = fishRotation.add(finalRotationAdjustment);
        return fishRotation;
    }

    public static Vector3d moveBetween(Vector3d startPos, Vector3d endPos, float progress){
        Vector3d direction = Vector3d.directionTo(startPos, endPos);
        double distance = startPos.distanceTo(endPos);
        return direction.scale(distance * progress);
    }

    public static Vector3d moveTowards(Vector3d startPos, Vector3d targetPos, double amount){
        if(amount > startPos.distanceTo(targetPos)) return targetPos; // Prevent overshooting.
        Vector3d direction = Vector3d.directionTo(startPos, targetPos);
        return direction.scale(amount);
    }

    public static Vector3f moveTowards(Vector3f startPos, Vector3f targetPos, float amount){
        if(amount > startPos.distanceTo(targetPos)) return targetPos; // Prevent overshooting.
        Vector3f direction = Vector3f.directionTo(startPos, targetPos);
        return direction.scale(amount);
    }

    public static double moveTowards(double startPos, double targetPos, double amount){
        double vector = targetPos - startPos;
        double direction = Math.signum(vector);
        double result = direction * amount;
        if(Math.abs(result) > Math.abs(targetPos)) result = targetPos;
        return result;
    }

    public static Vector3d moveAwayFrom(Vector3d startPos, Vector3d targetPos, double amount){
        return moveTowards(startPos, targetPos, amount).scale(-1);
    }

    public static boolean isInFluid(UUID entityId, World world){
        Ref<EntityStore> ref = world.getEntityRef(entityId);
        if(ref == null) return false;
        Store<EntityStore> store = world.getEntityStore().getStore();
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if(transform == null) return false;
        Vector3i pos = transform.getPosition().clone().toVector3i();
        return isInFluid(pos, world);
    }

    public static boolean isInFluid(Vector3i pos, World world){
        int occupiedBlockId = world.getFluidId(pos.x, pos.y, pos.z);
        // Is any fluid.
        return occupiedBlockId != 0;
    }

    public static float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

}
