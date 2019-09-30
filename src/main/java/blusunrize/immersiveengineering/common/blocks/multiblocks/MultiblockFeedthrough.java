/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Connector;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public class MultiblockFeedthrough implements IMultiblock
{
	public static MultiblockFeedthrough instance = new MultiblockFeedthrough();
	static ItemStack[][][] structure = new ItemStack[1][1][3];

	static
	{
		structure[0][0][0] = new ItemStack(IEContent.blockConnectors, 1);
		structure[0][0][1] = new ItemStack(Blocks.BOOKSHELF, 1).setTranslatableName("tile.immersiveengineering.arb_solid.name");
		structure[0][0][2] = new ItemStack(IEContent.blockConnectors, 1);
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

	private ItemStack renderStack;

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if(renderStack==null||renderStack.isEmpty())
			renderStack = new ItemStack(IEContent.blockConnectors, 1, BlockTypes_Connector.FEEDTHROUGH.getMeta());

		GlStateManager.translate(.5, .5, 1.5);
		GlStateManager.rotate(-45, 0, 1, 0);
		GlStateManager.rotate(-30, 1, 0, 0);
		GlStateManager.scale(1.75, 1.75, 1.75);

		GlStateManager.disableCull();
		ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
		GlStateManager.enableCull();
	}

	@Override
	public IBlockState getBlockstateFromStack(int index, ItemStack stack)
	{
		IBlockState ret = IMultiblock.super.getBlockstateFromStack(index, stack);
		if(stack==structure[0][0][0])
			return ret.withProperty(IEProperties.FACING_ALL, EnumFacing.SOUTH);
		return ret;
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public String getUniqueName()
	{
		return "IE:Feedthrough";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return WireApi.getWireType(state)!=null;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		//Check
		IBlockState stateHere = world.getBlockState(pos).getActualState(world, pos);
		if(stateHere.getPropertyKeys().contains(IEProperties.FACING_ALL))
			side = stateHere.getValue(IEProperties.FACING_ALL);
		Set<ImmersiveNetHandler.Connection> connHere = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
		if(connHere==null)
			connHere = ImmutableSet.of();
		if(connHere.size() > 1)
			return false;
		WireType wire = WireApi.getWireType(stateHere);
		if(wire==null)//This shouldn't ever happen
			return false;
		BlockPos middlePos = pos.offset(side);
		IBlockState middle = world.getBlockState(middlePos).getActualState(world, middlePos);
		if(!middle.isFullCube()||middle.getBlock().hasTileEntity(middle)||middle.getRenderType()!=EnumBlockRenderType.MODEL)
			return false;
		BlockPos otherPos = pos.offset(side, 2);
		IBlockState otherConn = world.getBlockState(otherPos).getActualState(world, otherPos);
		if(WireApi.getWireType(otherConn)!=wire)
			return false;
		if(otherConn.getValue(IEProperties.FACING_ALL)!=side.getOpposite())
			return false;
		Set<ImmersiveNetHandler.Connection> connOther = ImmersiveNetHandler.INSTANCE.getConnections(world, otherPos);
		if(connOther==null)
			connOther = ImmutableSet.of();
		if(connOther.size() > 1)
			return false;
		if(connOther.stream().anyMatch(c -> c.end.equals(pos)))
			return false;
		for(Connection c : connOther)
			if(connHere.stream().anyMatch(c2 -> c2.end.equals(c.end)))
				return false;

		ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
		if(MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer).isCanceled())
			return false;
		//Form
		if(!world.isRemote)
		{
			IBlockState state = IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property,
					BlockTypes_Connector.FEEDTHROUGH).withProperty(IEProperties.FACING_ALL, side);
			BlockPos masterPos = pos.offset(side);
			TileEntityFeedthrough master = setBlock(world, masterPos, state, wire, middle, 0);
			if(master!=null)
			{
				moveConnectionsToMaster(connOther, world, true, master);
				moveConnectionsToMaster(connHere, world, false, master);
				master.markContainingBlockForUpdate(null);
			}
			setBlock(world, pos, state, wire, middle, -1);
			setBlock(world, pos.offset(side, 2), state, wire, middle, 1);
		}
		return true;
	}

	private void moveConnectionsToMaster(Collection<Connection> conns, World world, boolean positive,
										 TileEntityFeedthrough master)
	{
		BlockPos masterPos = master.getPos();
		for(Connection c : ImmutableSet.copyOf(conns))
		{
			Connection reverse = ImmersiveNetHandler.INSTANCE.getReverseConnection(world.provider.getDimension(), c);
			if(positive)
				master.connPositive = c.end;
			else
				master.hasNegative = true;
			ApiUtils.moveConnectionEnd(reverse, masterPos, world);
		}
	}

	@Nullable
	private TileEntityFeedthrough setBlock(World world, BlockPos here, IBlockState newState, WireType wire, IBlockState middle,
										   int offset)
	{
		world.setBlockState(here, newState);
		TileEntity te = world.getTileEntity(here);
		if(te instanceof TileEntityFeedthrough)
		{
			TileEntityFeedthrough feedthrough = (TileEntityFeedthrough)te;
			feedthrough.reference = wire;
			feedthrough.stateForMiddle = middle;
			feedthrough.offset = offset;
			world.checkLight(here);
			world.addBlockEvent(here, feedthrough.getBlockType(), 253, 0);
			return feedthrough;
		}
		return null;
	}

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return new IngredientStack[]{
				new IngredientStack(new ItemStack(IEContent.blockConnectors, 2)),
				new IngredientStack(new ItemStack(Blocks.BOOKSHELF, 1).setTranslatableName("Arbitrary solid block"))
		};
	}
}