package blusunrize.immersiveengineering.client.render;

import java.util.List;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil.LightningAnimation;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileRenderTeslaCoil extends TileEntitySpecialRenderer<TileEntityTeslaCoil>
{
	@Override
	public void renderTileEntityAt(TileEntityTeslaCoil tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos()))
			return;

		List<LightningAnimation> animations = TileEntityTeslaCoil.effectMap.get(tile.getPos());
		for(LightningAnimation animation : animations)
		{
			
		}
		
	}
}