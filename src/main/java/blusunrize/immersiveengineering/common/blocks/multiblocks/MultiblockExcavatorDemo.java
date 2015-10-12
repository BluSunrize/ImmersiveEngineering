package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityExcavator;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockExcavatorDemo implements IMultiblock
{
	public static MultiblockExcavatorDemo instance = new MultiblockExcavatorDemo();

	static ItemStack[][][] structure = new ItemStack[7][8][3];
	static{
		for(int l=0;l<6;l++)
			for(int w=0;w<3;w++)
				for(int h=0;h<3;h++)
				{
					if(l<5&&w==1)
						continue;
					if((l==2||l==0) && w==0 && h==2)
						continue;
					int m = -1;
					if(l==5)
					{
						if(w!=0 && (h!=1||w!=1))
							structure[h+2][l+2][w]=new ItemStack(IEContent.blockStorage,1,7);
						else
							m = h==1&&w==0?BlockMetalDecoration.META_lightEngineering: BlockMetalDecoration.META_heavyEngineering;
					}
					else if(l==4)
					{
						if(w==0 && h==1)
							structure[h+2][l+2][w]=new ItemStack(IEContent.blockStorage,1,7);
						else
							m = w==2?BlockMetalDecoration.META_lightEngineering: BlockMetalDecoration.META_heavyEngineering;
					}
					else if(l==3)
						structure[h+2][l+2][w]=new ItemStack(IEContent.blockStorage,1,7);
					else if(w==2)
						m = h<2?BlockMetalDecoration.META_heavyEngineering: BlockMetalDecoration.META_scaffolding;
					else if(l!=1)
						m = h==0?BlockMetalDecoration.META_heavyEngineering: BlockMetalDecoration.META_lightEngineering;
					else
						m = BlockMetalDecoration.META_lightEngineering;
					if(m>=0)
						structure[h+2][l+2][w]= new ItemStack(IEContent.blockMetalDecoration,1,m);
				}

		for(int h=0;h<7;h++)
			for(int l=0;l<7;l++)
			{
				if((h==0||h==6) && l!=3)
					continue;
				if((l==0||l==6) && h!=3)
					continue;
				if(l==0||h==0||l==6||h==6 || ((l==1||l==5) && (h==1||h==5)) || (h==3&&l==3))
					structure[h][l][1]= new ItemStack(IEContent.blockStorage,1,7);
				else
					structure[h][l][1]= new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding);
			}
	}
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
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
		TileEntityExcavator te = new TileEntityExcavator();
		te.formed=true;
		te.pos=4;
		te.facing=3;
		ClientUtils.bindAtlas(0);
		ClientUtils.tes().startDrawingQuads();
		ClientUtils.tes().setTranslation(-.5,-.5,3.5);
		ClientUtils.handleStaticTileRenderer(te, false);
		ClientUtils.tes().setTranslation(0,0,0);
		ClientUtils.tes().draw();

		TileEntityBucketWheel te2 = new TileEntityBucketWheel();
		te2.formed=true;
		te2.pos=24;
		te2.facing=4;
		TileEntityRendererDispatcher.instance.renderTileEntityAt(te2, -.5D, -.5D, -.5D, 0.0F);
	}
	@Override
	public float getManualScale()
	{
		return 10;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:ExcavatorDemo";
	}
	
	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return false;
		}
	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		return false;
	}
	
	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(IEContent.blockStorage,21,7),
				new ItemStack(IEContent.blockMetalDecoration,9,BlockMetalDecoration.META_lightEngineering),
				new ItemStack(IEContent.blockMetalDecoration,13,BlockMetalDecoration.META_heavyEngineering),
				new ItemStack(IEContent.blockMetalDecoration,23,BlockMetalDecoration.META_scaffolding)};
	}
}