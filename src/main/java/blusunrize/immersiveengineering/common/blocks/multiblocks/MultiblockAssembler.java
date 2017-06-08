package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalMultiblock;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
					{
						if(l==1&&w!=1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
						else if(l==1&&w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if(h==1)
					{
						if(w==0 || w==2)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.IRON.getMeta());
						else if(l==1)
							structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
						else
							structure[h][l][w] = ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID + ":conveyor");
					}
					else if(h==2)
					{
						if(w==1)
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.IRON.getMeta());
						else
							structure[h][l][w] = new ItemStack(IEContent.blockSheetmetalSlabs,1,BlockTypes_MetalsAll.IRON.getMeta());
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
		if(iterator==10||iterator==16)
			return ImmersiveEngineering.proxy.drawConveyorInGui("immersiveengineering:conveyor", EnumFacing.EAST);
		return false;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	//@SideOnly(Side.CLIENT)
	static ItemStack renderStack = ItemStack.EMPTY;
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack.isEmpty())
			renderStack = new ItemStack(IEContent.blockMetalMultiblock,1,BlockTypes_MetalMultiblock.ASSEMBLER.getMeta());
		GlStateManager.translate(1.5, 1.5, 1.5);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-20, 1, 0, 0);
		GlStateManager.scale(4, 4, 4);
		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}
	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:Assembler";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock() == IEContent.blockConveyor;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		if(side.getAxis()==Axis.Y)
			side = EnumFacing.fromAngle(player.rotationYaw);
		else
			side = side.getOpposite();

		boolean mirror = false;
		for(int l=0;l<3;l++)
			for(int h=-1;h<=1;h++)
				for(int w=-1;w<=1;w++)
				{
					int ww = mirror?-w:w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					if(h==-1)
					{
						if(l==1&&w!=0)
						{
							if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
								return false;
						}
						else if(l==1&&w==0)
						{
							if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
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
						if(w==-1 || w==1)
						{
							if(!Utils.isOreBlockAt(world, pos2, "blockSheetmetalIron"))
								return false;
						}
						else if(l==1)
						{
							if(!Utils.isBlockAt(world, pos2, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
								return false;
						}
						else
						{
							if(!ConveyorHandler.isConveyor(world, pos2, ImmersiveEngineering.MODID + ":conveyor", null))
								return false;
						}
					}
					else if(h==1)
					{
						if(w==0)
						{
							if(!Utils.isOreBlockAt(world, pos2, "blockSheetmetalIron"))
								return false;
						}
						else
						{
							if(!Utils.isOreBlockAt(world, pos2, "slabSheetmetalIron"))
								return false;
						}
					}
				}


		for(int l=0;l<3;l++)
			for(int w=-1;w<=1;w++)
				for(int h=-1;h<=1;h++)
				{
					int ww = mirror?-w:w;
					BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

					world.setBlockState(pos2, IEContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.ASSEMBLER.getMeta()));
					TileEntity curr = world.getTileEntity(pos2);
					if(curr instanceof TileEntityAssembler)
					{
						TileEntityAssembler tile = (TileEntityAssembler)curr;
						tile.facing=side;
						tile.formed=true;
						tile.pos = (h+1)*9 + l*3 + (w+1);
						tile.offset = new int[]{(side==EnumFacing.WEST?1-l: side==EnumFacing.EAST?l-1: side==EnumFacing.NORTH?ww: -ww),h,(side==EnumFacing.NORTH?1-l: side==EnumFacing.SOUTH?l-1: side==EnumFacing.EAST?ww : -ww)};
						tile.mirrored=mirror;
						tile.markDirty();
						world.addBlockEvent(pos2, IEContent.blockMetalMultiblock, 255, 0);
					}
				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 6),
			new IngredientStack("blockSheetmetalIron", 9),
			new IngredientStack("slabSheetmetalIron", 6),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack(Utils.copyStackWithAmount(ConveyorHandler.getConveyorStack(ImmersiveEngineering.MODID + ":conveyor"), 2))};
	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}