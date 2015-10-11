package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.Vertex;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderExcavator extends TileRenderIE
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/excavator.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_excavator);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityExcavator excavator = (TileEntityExcavator)tile;
		
		translationMatrix.translate(excavator.facing==4?4.5: excavator.facing==5?-3.5: .5, .5, excavator.facing==2?4.5: excavator.facing==3?-3.5: .5);
		rotationMatrix.rotate(Math.toRadians(excavator.facing==4?180: excavator.facing==3?-90: excavator.facing==2?90: 0), 0,1,0);
		if(excavator.mirrored)
			translationMatrix.scale(new Vertex(excavator.facing<4?-1:1, 1, excavator.facing>3?-1:1));

		model.render(tile, tes, translationMatrix, rotationMatrix, 0, excavator.mirrored);
	}
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
	}
}