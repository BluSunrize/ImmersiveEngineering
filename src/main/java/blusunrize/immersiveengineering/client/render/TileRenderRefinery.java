package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.Vertex;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderRefinery extends TileRenderIE
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/refinery.obj")
	{
		@Override
		public IIcon getBlockIcon()
		{
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_refinery);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityRefinery refinery = (TileEntityRefinery)tile;
		
		translationMatrix.translate(.5, 1.5, .5);
		rotationMatrix.rotate(Math.toRadians(refinery.facing==2?180: refinery.facing==4?-90: refinery.facing==5?90: 0), 0,1,0);
		if(refinery.mirrored)
			translationMatrix.scale(new Vertex(1,1,-1));

		model.render(tile, tes, translationMatrix, rotationMatrix, 0, refinery.mirrored);
	}
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
	}

}