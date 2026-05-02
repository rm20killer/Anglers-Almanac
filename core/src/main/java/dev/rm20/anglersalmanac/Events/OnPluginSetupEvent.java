package dev.rm20.anglersalmanac.Events;

import com.hypixel.hytale.server.core.event.events.BootEvent;
import dev.rm20.anglersalmanac.AlmanacBook.AlmanacBook;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.Registration.EventInfo;
import dev.rm20.anglersalmanac.Utils.Intergration.MMOSkillTree;

@EventInfo(BootEvent.class)
public class OnPluginSetupEvent {

    public static void handle(BootEvent event) {
        AlmanacBook.reloadAllItem();
        if(AnglersAlmanac.getInstance().skillTree==null){
            AnglersAlmanac.getInstance().skillTree= new MMOSkillTree();
        }
    }
}
