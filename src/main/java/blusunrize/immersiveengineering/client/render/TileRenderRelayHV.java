package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderRelayHV extends TileRenderImmersiveConnectable
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/relayHV.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalDevice.getIcon(0, BlockMetalDevices.META_relayHV);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		translationMatrix.translate(.5, .5, .5);
		model.render(tile, tes, translationMatrix, rotationMatrix, 1, false);
	}

}