package dev.rm20.anglersalmanac.Utils.Validator;

import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;

public class CustomAssetValidator{

    public static final CommonAssetValidator FISH_LOOT_VALIDATOR = new CommonAssetValidator("json", "FishLootManager");
    public static final CommonAssetValidator LOOT_ID_VALIDATOR = new CommonAssetValidator("json", "Items");
    public static final CommonAssetValidator UI_ZONE_VALIDATOR = new CommonAssetValidator("png", "UI/Custom/Almanac/Fish/Assets");
    public static final CommonAssetValidator UI_TAB_VALIDATOR = new CommonAssetValidator("png", "UI/Custom/Almanac/Utils/Assets/Tabs");


}
