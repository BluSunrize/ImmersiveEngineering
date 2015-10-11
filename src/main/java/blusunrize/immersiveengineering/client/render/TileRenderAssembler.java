package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.Vertex;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderAssembler extends TileRenderIE
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/assembler.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			if(groupName.equalsIgnoreCase("conveyors"))
				return IEContent.blockMetalDevice.getIcon(0, BlockMetalDevices.META_conveyorBelt);
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_assembler);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityAssembler assembler = (TileEntityAssembler)tile;

		translationMatrix.translate(.5, 0, .5);
		rotationMatrix.rotate(Math.toRadians(assembler.facing==2?180: assembler.facing==4?-90: assembler.facing==5?90: 0), 0,1,0);
		if(assembler.mirrored)
			translationMatrix.scale(new Vertex(1,1,-1));

		model.render(tile, tes, translationMatrix, rotationMatrix, 0, assembler.mirrored);
	}
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
	}

}