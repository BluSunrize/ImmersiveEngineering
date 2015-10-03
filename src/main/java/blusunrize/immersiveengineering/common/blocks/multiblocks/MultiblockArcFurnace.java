package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockArcFurnace implements IMultiblock
{
	public static MultiblockArcFurnace instance = new MultiblockArcFurnace();

	static ItemStack[][][] structure = new ItemStack[5][5][5];
	static{
		for(int h=0;h<5;h++)
			for(int l=0;l<5;l++)
				for(int w=0;w<5;w++)
				{
					int m = -1;
					if(h==0)
					{
						if(l==0&&w==2)
							structure[h][w][l] = new ItemStack(Items.cauldron);
						else if(l==2&&(w==0||w==4))
							structure[h][w][l] = new ItemStack(IEContent.blockStorage,1,7);
						else if((l==0&&w==0)||(l>2&&(w==0||w==4)))
							m = BlockMetalDecoration.META_scaffolding;
						else if(l==4&& w>0&&w<4)
							m = BlockMetalDecoration.META_heavyEngineering;
						else
							structure[h][w][l] = new ItemStack(IEContent.blockStorageSlabs,1,7);
					}
					else if(h==1)
					{
						if((l==0&&w==0)||(l==4&&w>0&&w<4))
							m = BlockMetalDecoration.META_lightEngineering;
						else if((w==0||w==4)&&l>1)
							m = BlockMetalDecoration.META_heavyEngineering;
					}
					else if(h==2)
					{
						if(l==4)
							m = BlockMetalDecoration.META_lightEngineering;
						else if((w>0&&w<4)||l==2)
							structure[h][w][l] = new ItemStack(IEContent.blockStorage,1,7);
					}
					else if(h==3)
					{
						if(l==4 && w==2)
							m = BlockMetalDecoration.META_lightEngineering;
						else if(l==4 && (w==1||w==3))
							m = BlockMetalDecoration.META_scaffolding;
						else if(l>0&&w>0&&w<4)
							structure[h][w][l] = new ItemStack(IEContent.blockStorage,1,7);
					}
					else if(h==4)
					{
						if(l==4 && (w==1||w==3))
							m = BlockMetalDecoration.META_scaffolding;
						else if(l>1 && w==2)
							m = BlockMetalDecoration.META_lightEngineering;
					}
					if(m>=0)
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
		if(stack.getItem()==Items.cauldron)
		{
			ImmersiveEngineering.proxy.draw3DBlockCauldron();
			return true;
		}
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
		TileEntityArcFurnace te = new TileEntityArcFurnace();
		te.formed=true;
		te.pos=62;
		te.facing=4;
		ClientUtils.tes().startDrawingQuads();
		ClientUtils.tes().setTranslation(-.5,0,-.5);
		ClientUtils.handleStaticTileRenderer(te, false);
		ClientUtils.tes().setTranslation(0,0,0);
		ClientUtils.tes().draw();
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==Blocks.cauldron;
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side==0||side==1)
		{
			int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;
			side = f;
		}

		int startX=x+(side==4?2: side==5?-2: 0);
		int startY=y+2;
		int startZ=z+(side==2?2: side==3?-2: 0);


		boolean mirrored = false;
		boolean b = structureCheck(world,startX,startY,startZ, side, mirrored);
		if(!b)
		{
			mirrored = true;
			b = structureCheck(world,startX,startY,startZ, side, mirrored);
		}

		if(b)
		{
			for(int h=-2;h<=2;h++)
				for(int l=-2;l<=2;l++)
					for(int w=-2;w<=2;w++)
						if(structure[h+2][w+2][l+2] !=null)
						{
							//						if(l>0&&w==0)
							//							continue;
							//						if((l==3||l==5) && w==-1 && h==1)
							//							continue;

							int ww = mirrored?-w:w;
							int xx = startX+ (side==4?l: side==5?-l: side==2?-ww : ww);
							int yy = startY+ h;
							int zz = startZ+ (side==2?l: side==3?-l: side==5?-ww : ww);

							world.setBlock(xx, yy, zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_arcFurnace, 0x3);
							TileEntity curr = world.getTileEntity(xx, yy, zz);
							if(curr instanceof TileEntityArcFurnace)
							{
								TileEntityArcFurnace tile = (TileEntityArcFurnace)curr;
								tile.facing=side;
								tile.formed=true;
								tile.pos = (h+2)*25 + (l+2)*5 + (w+2);
								tile.offset = new int[]{(side==4?l: side==5?-l: side==2?-ww: ww),h,(side==2?l: side==3?-l: side==5?-ww: ww)};
								tile.mirrored = mirrored;
							}
						}
			player.triggerAchievement(IEAchievements.mbArcFurnace);
		}
		return b;
	}


	boolean structureCheck(World world, int startX, int startY, int startZ, int side, boolean mirror)
	{
		for(int h=-2;h<=2;h++)
			for(int l=-2;l<=2;l++)
				for(int w=-2;w<=2;w++)
					if(structure[h+2][w+2][l+2]!=null)
					{
						int ww = mirror?-w:w;
						int xx = startX+ (side==4?l: side==5?-l: side==2?-ww : ww);
						int yy = startY+ h;
						int zz = startZ+ (side==2?l: side==3?-l: side==5?-ww : ww);

						if(world.isAirBlock(xx, yy, zz))
							return false;
						ItemStack checkStack = new ItemStack(world.getBlock(xx,yy,zz),1,world.getBlockMetadata(xx,yy,zz));
						if(OreDictionary.itemMatches(structure[h+2][w+2][l+2], new ItemStack(Items.cauldron), true))
						{
							if(!Blocks.cauldron.equals(world.getBlock(xx,yy,zz)))
								return false;
						}
						else if(OreDictionary.itemMatches(structure[h+2][w+2][l+2], new ItemStack(IEContent.blockStorage,1,7), true))
						{
							//Steelblocks should have OreDict checks
							if(!Utils.compareToOreName(checkStack, "blockSteel"))
								return false;
						}
						else
							if(!OreDictionary.itemMatches(structure[h+2][w+2][l+2], checkStack, true))
								return false;
					}
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(Items.cauldron),
				new ItemStack(IEContent.blockStorageSlabs,14,7),
				new ItemStack(IEContent.blockStorage,25,7),
				new ItemStack(IEContent.blockMetalDecoration,13,BlockMetalDecoration.META_lightEngineering),
				new ItemStack(IEContent.blockMetalDecoration,9,BlockMetalDecoration.META_heavyEngineering),
				new ItemStack(IEContent.blockMetalDecoration,9,BlockMetalDecoration.META_scaffolding)};
	}
	@Override
	public float getManualScale()
	{
		return 12;
	}
}