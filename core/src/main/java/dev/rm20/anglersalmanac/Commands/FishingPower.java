package dev.rm20.anglersalmanac.Commands;


import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Registration.CommandInfo;
import dev.rm20.anglersalmanac.Utils.FishingPowerUtils;
import org.jspecify.annotations.NonNull;

@CommandInfo(
        name = "fishingpower",
        description = "Initializes fish entries in your Almanac with 0 catches used for testing"
)
public class FishingPower extends AbstractPlayerCommand {

    public FishingPower(@NonNull String name, @NonNull String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNull CommandContext commandContext, @NonNull Store<EntityStore> store, @NonNull Ref<EntityStore> ref, @NonNull PlayerRef playerRef, @NonNull World world) {
        AnglersAlmanac.LOGGER.atInfo().log(String.valueOf(FishingPowerUtils.getTotalFishingPower(store,ref)));
    }
}