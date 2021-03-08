package blusunrize.immersiveengineering.common.util.compat.computers.generic.impl;

import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import net.minecraft.item.ItemStack;

public class ArcFurnaceCallbacks extends MultiblockCallbackOwner<ArcFurnaceTileEntity>
{
	public ArcFurnaceCallbacks()
	{
		super(ArcFurnaceTileEntity.class, "arc_furnace");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new InventoryCallbacks<>(
				ArcFurnaceTileEntity::getInventory,
				ArcFurnaceTileEntity.FIRST_ELECTRODE_SLOT, ArcFurnaceTileEntity.ELECTRODE_COUNT, "electrode"
		));
		addAdditional(new InventoryCallbacks<>(
				ArcFurnaceTileEntity::getInventory,
				ArcFurnaceTileEntity.FIRST_OUT_SLOT, ArcFurnaceTileEntity.OUT_SLOT_COUNT, "output"
		));
		addAdditional(new InventoryCallbacks<>(
				ArcFurnaceTileEntity::getInventory,
				ArcFurnaceTileEntity.FIRST_IN_SLOT, ArcFurnaceTileEntity.IN_SLOT_COUNT, "input"
		));
		addAdditional(new InventoryCallbacks<>(
				ArcFurnaceTileEntity::getInventory,
				ArcFurnaceTileEntity.FIRST_ADDITIVE_SLOT, ArcFurnaceTileEntity.ADDITIVE_SLOT_COUNT, "additive"
		));
	}

	@ComputerCallable
	public ItemStack getSlag(CallbackEnvironment<ArcFurnaceTileEntity> env)
	{
		return env.getObject().getInventory().get(ArcFurnaceTileEntity.SLAG_SLOT);
	}
}
