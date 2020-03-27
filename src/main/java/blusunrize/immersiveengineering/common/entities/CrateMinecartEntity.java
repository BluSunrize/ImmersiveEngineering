package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
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

	public BlockState getDisplayTile()
	{
		return IEBlocks.WoodenDevices.crate.getDefaultState();
	}

}
