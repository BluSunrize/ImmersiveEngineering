package blusunrize.immersiveengineering.common.blocks.multiblocks;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnaceAdvanced;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class MultiblockBlastFurnaceAdvanced implements IMultiblock
{

	public static MultiblockBlastFurnaceAdvanced instance = new MultiblockBlastFurnaceAdvanced();

	static ItemStack[][][] structure = new ItemStack[4][3][3];
	static{
		for(int h=0;h<4;h++)
			for(int l=0;l<3;l++)
				for(int w=0;w<3;w++)
					if(h==3 && w==1 && l==1)
						structure[h][l][w]=new ItemStack(Blocks.hopper);
					else if(h<3)
						structure[h][l][w]=new ItemStack(IEContent.blockStoneDecoration,1,6);
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
		GL11.glTranslated(0, .5, 0);
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
		ClientUtils.bindAtlas(0);
		ClientUtils.tes().startDrawingQuads();
		ClientUtils.tes().setTranslation(-.5, -1, .5);
		TileEntityBlastFurnaceAdvanced tile = new TileEntityBlastFurnaceAdvanced();
		tile.facing = 3;
		ClientUtils.handleStaticTileRenderer(tile, false);
		ClientUtils.tes().setTranslation(0,0,0);
		ClientUtils.tes().draw();
	}

	@Override
	public String getUniqueName()
	{
		return "IE:BlastFurnaceAdvanced";
	}

	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockStoneDecoration && (meta==6);
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		int playerViewQuarter = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int f = playerViewQuarter==0 ? 2:playerViewQuarter==1 ? 5:playerViewQuarter==2 ? 3: 4;

		for(int h=-1;h<=2;h++)
			for(int l=0;l<=2;l++)
				for(int w=-1;w<=1;w++)
					if(h!=2 || (w==0 && l==1))
					{
						int xx = f==4?l: f==5?-l: f==2?-w:w;
						int zz = f==2?l: f==3?-l: f==4?w:-w;

						if(h==2)
						{
							if(!world.getBlock(x+xx,y+h,z+zz).equals(Blocks.hopper))
								return false;
						}
						else
						{
							if(!world.getBlock(x+xx,y+h,z+zz).equals(IEContent.blockStoneDecoration) || world.getBlockMetadata(x+xx,y+h,z+zz)!=6)
								return false;
						}
					}
		for(int h=-1;h<=2;h++)
			for(int l=0;l<=2;l++)
				for(int w=-1;w<=1;w++)
					if(h!=2 || (w==0 && l==1))
					{
						int xx = f==4?l: f==5?-l: f==2?-w:w;
						int zz = f==2?l: f==3?-l: f==4?w:-w;

						world.setBlock(x+xx, y+h, z+zz, IEContent.blockStoneDevice, 5, 0x3);
						TileEntity curr = world.getTileEntity(x+xx, y+h, z+zz);
						if(curr instanceof TileEntityBlastFurnace)
						{
							TileEntityBlastFurnace currBlast = (TileEntityBlastFurnace) curr;
							currBlast.offset=new int[]{xx,h,zz};
							currBlast.pos = (h+1)*9 + l*3 + (w+1);
							currBlast.facing=f;
							currBlast.formed=true;
							currBlast.markDirty();
						}
					}
		player.triggerAchievement(IEAchievements.mbImprovedBlastFurnace);
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{new ItemStack(IEContent.blockStoneDecoration,27,6),new ItemStack(Blocks.hopper)};
	}
	@Override
	public float getManualScale()
	{
		return 14;
	}
}