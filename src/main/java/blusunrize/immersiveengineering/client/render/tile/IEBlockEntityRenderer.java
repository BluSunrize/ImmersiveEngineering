package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.config.IEClientConfig;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class IEBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T>
{
	@Override
	public int getViewDistance()
	{
		double increase = IEClientConfig.increasedTileRenderdistance.get();
		return (int)(BlockEntityRenderer.super.getViewDistance()*increase);
	}

}
