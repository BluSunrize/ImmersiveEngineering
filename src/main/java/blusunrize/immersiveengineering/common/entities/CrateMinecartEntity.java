package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class CrateMinecartEntity extends IEMinecartEntity<WoodenCrateTileEntity>
{
	public static final EntityType<CrateMinecartEntity> TYPE = Builder
			.<CrateMinecartEntity>create(CrateMinecartEntity::new, EntityClassification.MISC)
			.size(0.98F, 0.7F)
			.build(ImmersiveEngineering.MODID+":cart_woodencrate");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "cart_woodencrate");
	}

	public CrateMinecartEntity(EntityType<?> type, World world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public CrateMinecartEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}

	@Override
	public void killMinecart(DamageSource source)
	{
		this.remove();
		if(this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS))
		{
			ItemStack itemstack = new ItemStack(IEItems.Misc.cartWoodenCrate);
			CompoundNBT tag = new CompoundNBT();
			this.containedTileEntity.writeCustomNBT(tag, false);
			if(!tag.isEmpty())
				itemstack.setTag(tag);
			if(this.hasCustomName())
				itemstack.setDisplayName(this.getCustomName());
			this.entityDropItem(itemstack);
		}
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
		return () -> new WoodenCrateTileEntity(false);
	}

	@Override
	protected void invalidateCaps()
	{
		if(this.containedTileEntity!=null)
			this.containedTileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).invalidate();
	}

	@Override
	public BlockState getDisplayTile()
	{
		return IEBlocks.WoodenDevices.crate.getDefaultState();
	}

}
