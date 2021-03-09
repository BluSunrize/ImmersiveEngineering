package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.SqueezerTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.SingleItemCallback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.TankCallbacks;

public class SqueezerCallbacks extends MultiblockCallbackOwner<SqueezerTileEntity>
{
	public SqueezerCallbacks()
	{
		super(SqueezerTileEntity.class, "squeezer");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new TankCallbacks<>(te -> te.tanks[0], ""));
		addAdditional(new InventoryCallbacks<>(te -> te.inventory, 0, 8, "input"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 9, "empty canisters"));
		addAdditional(new SingleItemCallback<>(te -> te.inventory, 10, "filled canisters"));
	}
}
