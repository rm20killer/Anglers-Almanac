package dev.rm20.anglersalmanac.Utils.LineRender;

import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

import javax.annotation.Nonnull;

import static com.hypixel.hytale.server.core.modules.debug.DebugUtils.addLine;

public class FishingLineRender {


    /**
     *
     * @param world
     * @param start
     * @param end
     * @param color
     * @param thickness
     * @param sagFactor
     * @param segments
     * @param time
     * @param flags
     */
    public static void drawFishingLine(
            @Nonnull final World world,
            @Nonnull final Vector3dc start,
            @Nonnull final Vector3dc end,
            @Nonnull final Vector3fc color,
            final double thickness,
            final double sagFactor,
            final int segments,
            final float time,
            final int flags) {

        final double dx = end.x() - start.x();
        final double dy = end.y() - start.y();
        final double dz = end.z() - start.z();
        final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 0.001) return;

        final double midX = (start.x() + end.x()) * 0.5;
        final double midY = (start.y() + end.y()) * 0.5 - (distance * sagFactor);
        final double midZ = (start.z() + end.z()) * 0.5;

        double prevX = start.x();
        double prevY = start.y();
        double prevZ = start.z();

        for (int i = 1; i <= segments; i++) {
            final double t = (double) i / segments;
            final double invT = 1.0 - t;

            // Quadratic Bezier interpolation
            final double currentX = invT * invT * start.x() + 2 * invT * t * midX + t * t * end.x();
            final double currentY = invT * invT * start.y() + 2 * invT * t * midY + t * t * end.y();
            final double currentZ = invT * invT * start.z() + 2 * invT * t * midZ + t * t * end.z();

            addLine(world, prevX, prevY, prevZ, currentX, currentY, currentZ, color, thickness, time, flags);

            prevX = currentX;
            prevY = currentY;
            prevZ = currentZ;
        }
    }


}
