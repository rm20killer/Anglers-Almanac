package dev.rm20.anglersalmanac.Commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.Config.ConfigUI;
import dev.rm20.anglersalmanac.Registration.CommandInfo;

import javax.annotation.Nonnull;

import static com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime.CanDismissOrCloseThroughInteraction;

@CommandInfo(
        name = "OpenConfig",
        description = "Opens the config"
)
public class OpenConfig extends AbstractPlayerCommand {

    public OpenConfig(@Nonnull String name, @Nonnull String description) {
        super(name, description);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (!(commandContext.sender() instanceof Player player)) {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.notPlayer"));
            return;
        }
        if(!((Player) commandContext.sender()).hasPermission("AnglersAlmanac.admin"))
        {
            commandContext.sendMessage(Message.translation("anglersalmanac.cmd.error.noPerms"));
            return;
        }

        ((Player) commandContext.sender()).getPageManager().openCustomPage(ref,store,new ConfigUI(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction));
    }
}
