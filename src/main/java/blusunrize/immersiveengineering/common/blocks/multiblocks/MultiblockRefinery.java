package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockRefinery implements IMultiblock
{
	public static MultiblockRefinery instance = new MultiblockRefinery();
	static ItemStack[][][] structure = new ItemStack[3][5][3];
	static{
		for(int w=0;w<5;w++)
			for(int l=0;l<3;l++)
				for(int h=0;h<3;h++)
				{
					if(w==2&&(h==2||(h==1&&l==1)))
						continue;
					int m = BlockMetalDecoration.META_heavyEngineering;
					if(h==0)
					{
						if(l!=1 && w!=2)
							m = BlockMetalDecoration.META_scaffolding;
						else if(w>0 && w<4)
							m = BlockMetalDecoration.META_lightEngineering;
					}
					else if(h==1 && w==2 && l==2)
						m = BlockMetalDecoration.META_lightEngineering;


					structure[h][w][l]= new ItemStack(IEContent.blockMetalDecoration,1,m);
				}
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}
	@Override
	public boolean overwriteBlockRender(ItemStack stack)
	{
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		TileEntityRefinery te = new TileEntityRefinery();
		te.formed=true;
		te.pos=17;
		te.facing=4;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(te, -1D, -1D, .0D, 0.0F);
	}
	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalDecoration && (meta==BlockMetalDecoration.META_heavyEngineering);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side==0||side==1)
			return false;

		int startX=x;
		int startY=y;
		int startZ=z;

		for(int l=0;l<3;l++)
			for(int w=-2;w<=2;w++)
				for(int h=-1;h<=1;h++)
				{
					if(w==0&&(h==1||(h==0&&l==1)))
						continue;
					int m = BlockMetalDecoration.META_heavyEngineering;
					if(h==-1)
					{
						if(l!=1 && w!=0)
							m = BlockMetalDecoration.META_scaffolding;
						else if(w>-2 && w<2)
							m = BlockMetalDecoration.META_lightEngineering;
					}
					else if(h==0 && w==0 && l==2)
						m = BlockMetalDecoration.META_lightEngineering;

					int xx = startX+ (side==4?l: side==5?-l: side==2?-w : w);
					int yy = startY+ h;
					int zz = startZ+ (side==2?l: side==3?-l: side==5?-w : w);
					if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==m))
						return false;
				}

		for(int l=0;l<3;l++)
			for(int w=-2;w<=2;w++)
				for(int h=-1;h<=1;h++)
				{
					if(w==0&&(h==1||(h==0&&l==1)))
						continue;
					int xx = (side==4?l: side==5?-l: side==2?-w : w);
					int yy = h;
					int zz = (side==2?l: side==3?-l: side==5?-w : w);

					world.setBlock(startX+xx, startY+yy, startZ+zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_refinery, 0x3);
					if(world.getTileEntity(startX+xx, startY+yy, startZ+zz) instanceof TileEntityRefinery)
					{
						TileEntityRefinery tile = (TileEntityRefinery)world.getTileEntity(startX+xx,startY+yy,startZ+zz);
						tile.facing=side;
						tile.formed=true;
						tile.pos = l*15 + (h+1)*5 + (w+2);
						tile.offset = new int[]{(side==4?l-1: side==5?1-l: side==2?-w: w),h+1,(side==2?l-1: side==3?1-l: side==5?-w: w)};
					}
				}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(IEContent.blockMetalDecoration,8,BlockMetalDecoration.META_scaffolding),
				new ItemStack(IEContent.blockMetalDecoration,6,BlockMetalDecoration.META_lightEngineering),
				new ItemStack(IEContent.blockMetalDecoration,27,BlockMetalDecoration.META_heavyEngineering)};
	}
}