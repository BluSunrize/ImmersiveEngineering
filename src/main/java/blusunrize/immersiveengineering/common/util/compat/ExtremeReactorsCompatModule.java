package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import net.minecraft.util.ResourceLocation;

public class ExtremeReactorsCompatModule extends IECompatModule{

    @Override
    public void preInit() {

    }

    @Override
    public void registerRecipes() {

    }

    @Override
    public void init() {
        ThermoelectricHandler.registerSourceInKelvin(TagUtils.createBlockWrapper(new ResourceLocation("forge:storage_blocks/cyanite")), 1500);
        ThermoelectricHandler.registerSourceInKelvin(TagUtils.createBlockWrapper(new ResourceLocation("forge:storage_blocks/yellorium")), 2000);
    }

    @Override
    public void postInit() {

    }
}
