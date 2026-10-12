package dev.rm20.anglersalmanac.Utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3d;

public class WaterUtils {

    /**
     * Checks if a given position is in water.
     */
    public static boolean isInWater(World world, Vector3d pos) {
        if (world == null || pos == null) return false;
        return isInWater(world, (int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
    }

    /**
     * Checks if given block coordinates are in water.
     */
    public static boolean isInWater(World world, int x, int y, int z) {
        if (world == null) return false;
        Fluid fluid = getFluidId(x, y, z, world);
        if (fluid == null) return false;
        return fluid.getId().toLowerCase().contains("water");
    }

    /**
     * Retrieves the Fluid at the given block coordinates.
     */
    public static Fluid getFluidId(int x, int y, int z, World world) {
        if (world == null) return null;
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int sectionY = ChunkUtil.indexSection(y);
        int chunkZ = ChunkUtil.chunkCoordinate(z);
        int blockIndex = ChunkUtil.indexBlock(x, y, z);
        Ref<ChunkStore> sectionRef = world.getChunkStore().getChunkSectionReference(chunkX, sectionY, chunkZ);
        if (sectionRef == null || !sectionRef.isValid()) return null;
        FluidSection fluidSection = world.getChunkStore().getStore().getComponent(sectionRef, FluidSection.getComponentType());
        if (fluidSection == null) return null;
        return fluidSection.getFluid(blockIndex);
    }

    /**
     * Checks if a solid block exists at the given coordinates.
     */
    public static boolean isSolidBlock(World world, int x, int y, int z) {
        if (world == null) return false;
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int sectionY = ChunkUtil.indexSection(y);
        int chunkZ = ChunkUtil.chunkCoordinate(z);
        Ref<ChunkStore> sectionRef = world.getChunkStore().getChunkSectionReference(chunkX, sectionY, chunkZ);
        if (sectionRef == null || !sectionRef.isValid()) return false;
        BlockSection blockSection = world.getChunkStore().getStore().getComponent(sectionRef, BlockSection.getComponentType());
        if (blockSection == null) return false;
        int blockIndex = ChunkUtil.indexBlock(x, y, z);
        int blockId = blockSection.get(blockIndex);
        return blockId != BlockType.EMPTY_ID;
    }

    /**
     * Checks if (x, z) is a valid open water surface around refY.
     * Returns the water surface Y if open water, or null if solid block, land, or no water.
     */
    public static Double getWaterSurfaceAt(World world, double x, double z, double refY) {
        if (world == null) return null;
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        int baseY = (int) Math.floor(refY);

        // Search vertical blocks around the reference height
        for (int y = baseY + 2; y >= baseY - 2; y--) {
            // Must have water at this block
            if (!isInWater(world, blockX, y, blockZ)) {
                continue;
            }
            // Must NOT have a solid block at this block
            if (isSolidBlock(world, blockX, y, blockZ)) {
                continue;
            }
            // Block directly above MUST be air / open (not water, not solid)
            if (isInWater(world, blockX, y + 1, blockZ)) {
                continue;
            }
            if (isSolidBlock(world, blockX, y + 1, blockZ)) {
                // Obstructed above
                return null;
            }

            // Valid water surface found
            return (double) y + 1.0;
        }

        return null;
    }
}
