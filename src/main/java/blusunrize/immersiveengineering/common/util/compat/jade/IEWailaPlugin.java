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
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@SuppressWarnings({"unchecked", "rawtypes"})
@WailaPlugin
public class IEWailaPlugin implements IWailaPlugin
{
	@Override
	public void registerClient(IWailaClientRegistration registration)
	{
		registration.registerBlockIcon(new MultiblockIconProvider(), MultiblockPartBlock.class);

		registration.registerFluidStorageClient(new MultiblockTankDataProvider());
		registration.registerItemStorageClient(new MultiblockInventoryDataProvider());
	}

	@Override
	public void register(IWailaCommonRegistration registration)
	{
		registration.registerFluidStorage(new MultiblockTankDataProvider(), MultiblockBlockEntityDummy.class);
		registration.registerFluidStorage(new MultiblockTankDataProvider(), MultiblockBlockEntityMaster.class);
		registration.registerItemStorage(new MultiblockInventoryDataProvider(), MultiblockBlockEntityDummy.class);
		registration.registerItemStorage(new MultiblockInventoryDataProvider(), MultiblockBlockEntityMaster.class);
	}
}