package dev.rm20.anglersalmanac.Commands;


import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Registration.CommandInfo;
import dev.rm20.anglersalmanac.Models.FishLootManager;

import javax.annotation.Nonnull;
import java.util.Collection;

@CommandInfo(
        name = "removefish",
        description = "Initializes fish entries in your Almanac with 0 catches."
)
public class RemoveFishCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> fishArg;

    public RemoveFishCommand(@Nonnull String name, @Nonnull String description) {
        super(name, description);
        this.fishArg = this.withRequiredArg("fishId", "The fish ID or '*' for all", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String input = this.fishArg.get(commandContext);
        String uuid = playerRef.getUuid().toString();
        Collection<FishLootManager> allFish = FishLootManager.getAllLoot();
        if (!(commandContext.sender() instanceof Player player)) {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.notPlayer"));
            return;
        }
        if(!((Player) commandContext.sender()).hasPermission("AnglersAlmanac.admin"))
        {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.noPerms"));
            return;
        }
        if (input.equals("*")) {
            // Initialize every fish in the game
            for (FishLootManager fish : allFish) {
                AnglersAlmanac.getInstance().database.removeFishEntry(uuid, fish.getId());
            }
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.removeFish.all").param("count",allFish.size()));
        } else {
            // Check if the specific fish ID is valid
            boolean isValid = allFish.stream().anyMatch(f -> f.getId().equalsIgnoreCase(input));

            if (isValid) {
                AnglersAlmanac.getInstance().database.removeFishEntry(uuid, input);
                commandContext.sendMessage(Message.translation("anglersalmanac.cmd.removeFish.fish").param("name",input));
            } else {
                commandContext.sendMessage(Message.translation("anglersalmanac.cmd.invalidFish").param("name",input));
            }
        }
    }
}
