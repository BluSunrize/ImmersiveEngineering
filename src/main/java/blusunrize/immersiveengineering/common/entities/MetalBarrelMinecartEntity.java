package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class MetalBarrelMinecartEntity extends BarrelMinecartEntity
{
	public static final EntityType<MetalBarrelMinecartEntity> TYPE = Builder
			.<MetalBarrelMinecartEntity>create(MetalBarrelMinecartEntity::new, EntityClassification.MISC)
			.size(0.98F, 0.7F)
			.build(ImmersiveEngineering.MODID+":cart_metalbarrel");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "cart_metalbarrel");
	}

	public MetalBarrelMinecartEntity(EntityType<?> type, World world, double x, double y, double z)
	{
		super(type, world, x, y, z);
	}

	public MetalBarrelMinecartEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}

	@Override
	public ItemStack getCartItem()
	{
		return new ItemStack(IEItems.Misc.cartMetalBarrel);
	}

	@Nonnull
	@Override
	public EntityType<?> getType()
	{
		return TYPE;
	}

	@Override
	protected Supplier<WoodenBarrelTileEntity> getTileProvider()
	{
		return MetalBarrelTileEntity::new;
	}

	@Override
	public BlockState getDisplayTile()
	{
		return IEBlocks.MetalDevices.barrel.getDefaultState();
	}

}
