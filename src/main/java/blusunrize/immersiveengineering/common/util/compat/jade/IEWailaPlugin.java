/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class IEWailaPlugin implements IWailaPlugin
{
	@Override
	public void registerClient(IWailaClientRegistration registrar)
	{
		registrar.registerBlockComponent(new HempDataProvider(), HempBlock.class);
		registrar.registerBlockIcon(new MultiblockIconProvider(), MultiblockPartBlock.class);
	}

	@Override
	public void register(IWailaCommonRegistration registration)
	{
		registration.registerFluidStorage(new SheetmetalTankDataProvider(), MultiblockBlockEntityDummy.class);
		registration.registerFluidStorage(new SheetmetalTankDataProvider(), MultiblockBlockEntityMaster.class);
	}
}