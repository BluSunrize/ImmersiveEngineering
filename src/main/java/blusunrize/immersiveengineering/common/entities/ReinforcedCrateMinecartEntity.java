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
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ReinforcedCrateMinecartEntity extends CrateMinecartEntity
{
	public static final EntityType<ReinforcedCrateMinecartEntity> TYPE = Builder
			.<ReinforcedCrateMinecartEntity>create(ReinforcedCrateMinecartEntity::new, EntityClassification.MISC)
			.size(0.98F, 0.7F)
			.build(ImmersiveEngineering.MODID+":cart_reinforcedcrate");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "cart_reinforcedcrate");
	}

	public ReinforcedCrateMinecartEntity(EntityType<?> type, World world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public ReinforcedCrateMinecartEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}

	@Override
	public boolean isImmuneToExplosions()
	{
		return true;
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Misc.cartReinforcedCrate);
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
		return () -> new WoodenCrateTileEntity(true);
	}

	@Override
	public BlockState getDisplayTile()
	{
		return IEBlocks.WoodenDevices.reinforcedCrate.getDefaultState();
	}

}
