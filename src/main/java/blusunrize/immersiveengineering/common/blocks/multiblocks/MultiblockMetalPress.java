package blusunrize.immersiveengineering.common.blocks.multiblocks;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Conveyor;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMetalPress;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockMetalPress implements IMultiblock
{
	public static MultiblockMetalPress instance = new MultiblockMetalPress();

	static ItemStack[][][] structure = new ItemStack[3][3][1];
	static{
		for(int h=0;h<3;h++)
			for(int l=0;l<3;l++)
				if(h==0)
				{
					if(l==1)
						structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
					else
						structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
				}
				else if(h==1)
				{
					if(l==1)
						structure[h][l][0] = new ItemStack(Blocks.piston,1,0);
					else
						structure[h][l][0] = new ItemStack(IEContent.blockConveyor,1, BlockTypes_Conveyor.CONVEYOR.getMeta());
				}
				else if(h==2&&l==1)
					structure[h][l][0] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
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
		if(iterator==3||iterator==5)
		{
			GL11.glRotatef(-90, 0, 0, 1);
			GL11.glRotatef(90, 1, 0, 0);
			GL11.glTranslatef(-1, -1, 0);
		}
		return false;
	}
	@Override
	public float getManualScale()
	{
		return 13;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	@SideOnly(Side.CLIENT)
	static ItemStack renderStack;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null)
			renderStack = new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.METAL_PRESS.getMeta());
		GlStateManager.scale(4.57,4.57,4.57);
		GlStateManager.rotate(90, 0, 1, 0);
		GlStateManager.translate(0, .11, 0);
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
	}

	@Override
	public String getUniqueName()
	{
		return "IE:MetalPress";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==Blocks.piston && (state.getBlock().getMetaFromState(state)==0);
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		EnumFacing dir = side.rotateY();
		if(world.getTileEntity(pos.offset(dir)) instanceof TileEntityConveyorBelt)
			dir = ((TileEntityConveyorBelt)world.getTileEntity(pos.offset(dir))).getFacing();

		for(int l=-1;l<=1;l++)
			for(int h=-1;h<=1;h++)
			{
				if(h==1&&l!=0)
					continue;
				BlockPos pos2 = pos.offset(dir, l).add(0,h,0);

				if(h==-1)
				{
					if(l==0)
					{
						if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
							return false;
					}
					else
					{
						if(!Utils.isOreBlockAt(world, pos2, "scaffoldingSteel"))
							return false;
					}
				}
				else if(h==0)
				{
					if(l==0)
					{
						if(!Utils.isBlockAt(world, pos2, Blocks.piston, 0))
							return false;
					}
					else
					{
						if(!Utils.isBlockAt(world, pos2, IEContent.blockConveyor, BlockTypes_Conveyor.CONVEYOR.getMeta()))
							return false;
						if( ((TileEntityConveyorBelt)world.getTileEntity(pos2)).facing!=dir )
							return false;
					}
				}
				else
				{
					if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()))
						return false;
				}
			}
		for(int l=-1;l<=1;l++)
			for(int h=-1;h<=1;h++)
			{
				if(h==1&&l!=0)
					continue;
				BlockPos pos2 = pos.offset(dir, l).add(0,h,0);
				world.setBlockState(pos2, IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.METAL_PRESS.getMeta()));
				TileEntity curr = world.getTileEntity(pos2);
				if(curr instanceof TileEntityMetalPress)
				{
					TileEntityMetalPress tile = (TileEntityMetalPress)curr;
					tile.facing=dir;
					tile.formed=true;
					tile.pos = (h+1)*3 + (l+1);
					tile.offset = new int[]{(dir==EnumFacing.WEST?-l: dir==EnumFacing.EAST?l: 0),h,(dir==EnumFacing.NORTH?-l: dir==EnumFacing.SOUTH?l: 0)};
				}
			}
		player.triggerAchievement(IEAchievements.mbMetalPress);
		return true;
	}

	static final ItemStack[] materials = new ItemStack[]{
			new ItemStack(IEContent.blockMetalDecoration1,2,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()),
			new ItemStack(Blocks.piston),
			new ItemStack(IEContent.blockConveyor,2,BlockTypes_Conveyor.CONVEYOR.getMeta()),
			new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())};
	@Override
	public ItemStack[] getTotalMaterials()
	{
		return materials;
	}
}