/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class CrateMinecartEntity extends IEMinecartEntity<WoodenCrateTileEntity>
{
	public static final EntityType<CrateMinecartEntity> TYPE = Builder
			.<CrateMinecartEntity>of(CrateMinecartEntity::new, MobCategory.MISC)
			.sized(0.98F, 0.7F)
			.build(ImmersiveEngineering.MODID+":cart_woodencrate");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "cart_woodencrate");
	}

	public CrateMinecartEntity(EntityType<?> type, Level world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public CrateMinecartEntity(EntityType<?> type, Level world)
	{
		super(type, world);
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Misc.cartWoodenCrate);
	}

	@Override
	public void writeTileToItem(ItemStack itemStack)
	{
		CompoundTag tag = new CompoundTag();
		this.containedTileEntity.writeInv(tag, true);
		if(!tag.isEmpty())
			itemStack.setTag(tag);
	}

	@Override
	public void readTileFromItem(LivingEntity placer, ItemStack itemStack)
	{
		this.containedTileEntity.readOnPlacement(placer, itemStack);
	}

	@Nonnull
	@Override
	public EntityType<?> getType()
	{
		return TYPE;
	}

	@Override
	protected Supplier<WoodenCrateTileEntity> getTileProvider()
	{
		return () -> {
			WoodenCrateTileEntity tile = new WoodenCrateTileEntity();
			tile.setOverrideState(getDisplayBlockState());
			return tile;
		};
	}

	@Override
	protected void invalidateCaps()
	{
		if(this.containedTileEntity!=null)
			this.containedTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).invalidate();
	}

	@Override
	public BlockState getDisplayBlockState()
	{
		return IEBlocks.WoodenDevices.crate.defaultBlockState();
	}

}
