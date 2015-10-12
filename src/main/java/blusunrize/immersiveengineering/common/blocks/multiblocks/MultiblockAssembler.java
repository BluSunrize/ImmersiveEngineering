package blusunrize.immersiveengineering.common.blocks.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityStructuralArm;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultiblockAssembler implements IMultiblock
{
	public static MultiblockAssembler instance = new MultiblockAssembler();
	static ItemStack[][][] structure = new ItemStack[3][3][3];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<3;l++)
				for(int w=0;w<3;w++)
				{
					if(h==0)
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_scaffolding);
					else if(h==1)
					{
						if(w==0 || w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1, BlockMetalDecoration.META_sheetMetal);
						else if(l==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1, BlockMetalDecoration.META_lightEngineering);
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice,1, BlockMetalDevices.META_conveyorBelt);
					}
					else if(h==2)
					{
						if(w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1, BlockMetalDecoration.META_lightEngineering);
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration,1, BlockMetalDecoration.META_structuralArm);
					}
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
		if(iterator==10 || iterator==16)
			GL11.glRotatef(-90, 0, 1, 0);
		if(iterator==20||iterator==23||iterator==26)
			GL11.glRotatef(-90, 0, 1, 0);
		if(iterator==18||iterator==21||iterator==24)
			GL11.glRotatef(90, 0, 1, 0);

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
		TileEntityAssembler te = new TileEntityAssembler();
		ClientUtils.bindAtlas(0);
		ClientUtils.tes().startDrawingQuads();
		ClientUtils.tes().setTranslation(-.5f,-1.5f,-.5f);
		ClientUtils.handleStaticTileRenderer(te, false);
		ClientUtils.tes().draw();
		ClientUtils.tes().setTranslation(0,0,0);
	}
	@Override
	public float getManualScale()
	{
		return 13;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:Assembler";
	}
	
	@Override
	public boolean isBlockTrigger(Block b, int meta)
	{
		return b==IEContent.blockMetalDevice && (meta==BlockMetalDevices.META_conveyorBelt);
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
		int startX=x;
		int startY=y;
		int startZ=z;
		for(int l=0;l<3;l++)
			for(int h=-1;h<=1;h++)
				for(int w=-1;w<=1;w++)
				{
					int xx = startX+ (side==4?l: side==5?-l: side==2?-w : w);
					int yy = startY+ h;
					int zz = startZ+ (side==2?l: side==3?-l: side==5?-w : w);

					if(h==-1)
					{
						if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_scaffolding))
							return false;
					}
					else if(h==0)
					{
						if(w==-1 || w==1)
						{
							if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_sheetMetal))
								return false;
						}
						else if(l==1)
						{
							if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_lightEngineering))
								return false;
						}
						else
						{
							if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDevice) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDevices.META_conveyorBelt))
								return false;
						}
					}
					else if(h==1)
					{
						if(w==0)
						{
							if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_lightEngineering))
								return false;
						}
						else
						{
							if(!(world.getBlock(xx, yy, zz).equals(IEContent.blockMetalDecoration) && world.getBlockMetadata(xx, yy, zz)==BlockMetalDecoration.META_structuralArm))
								return false;
							TileEntity tile = world.getTileEntity(xx, yy, zz);
							int f = tile instanceof TileEntityStructuralArm && !((TileEntityStructuralArm)tile).inverted? ((TileEntityStructuralArm)tile).facing : -1;
							if(f != ForgeDirection.ROTATION_MATRIX[w==-1?1:0][side])
								return false;
						}
					}
				}

		for(int l=0;l<3;l++)
			for(int h=-1;h<=1;h++)
				for(int w=-1;w<=1;w++)
				{
					int xx = startX+ (side==4?l: side==5?-l: side==2?-w : w);
					int yy = startY+ h;
					int zz = startZ+ (side==2?l: side==3?-l: side==5?-w : w);

						world.setBlock(xx, yy, zz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_assembler, 0x3);
						TileEntity curr = world.getTileEntity(xx, yy, zz);
						if(curr instanceof TileEntityAssembler)
						{
							TileEntityAssembler tile = (TileEntityAssembler)curr;
							tile.facing=side;
							tile.formed=true;
							tile.pos = (h+1)*9 + l*3 + (w+1);
							tile.offset = new int[]{(side==4?l-1: side==5?1-l: side==2?-w: w),h,(side==2?l-1: side==3?1-l: side==5?-w: w)};
						}
					}
//			player.triggerAchievement(IEAchievements.mbCrusher);
		return true;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(IEContent.blockMetalDecoration,9,BlockMetalDecoration.META_scaffolding),
				new ItemStack(IEContent.blockMetalDecoration,4,BlockMetalDecoration.META_lightEngineering),
				new ItemStack(IEContent.blockMetalDecoration,6,BlockMetalDecoration.META_sheetMetal),
				new ItemStack(IEContent.blockMetalDecoration,6,BlockMetalDecoration.META_structuralArm),
				new ItemStack(IEContent.blockMetalDevice,2,BlockMetalDevices.META_conveyorBelt)};
	}
}