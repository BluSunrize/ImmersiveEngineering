/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.old.ImmersiveNetHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.FeedthroughTileEntity;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class MultiblockFeedthrough extends TemplateMultiblock
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
	public List<BlockInfo> getStructureManual()
	{
		return structure;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	private ItemStack renderStack;

	@Override
	@OnlyIn(Dist.CLIENT)
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
	public BlockState getBlockstateFromStack(int index, ItemStack stack)
	{
		BlockState ret = IMultiblock.super.getBlockstateFromStack(index, stack);
		if(stack==structure[0][0][0])
			return ret.with(IEProperties.FACING_ALL, Direction.SOUTH);
		return ret;
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return "IE:Feedthrough";
	}

	@Override
	public boolean isBlockTrigger(BlockState state)
	{
		return WireApi.getWireType(state)!=null;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player)
	{
		//Check
		BlockState stateHere = world.getBlockState(pos).getActualState(world, pos);
		if(stateHere.getProperties().contains(IEProperties.FACING_ALL))
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
		BlockState middle = world.getBlockState(middlePos).getActualState(world, middlePos);
		if(!middle.isFullCube()||middle.getBlock().hasTileEntity(middle)||middle.getRenderType()!=BlockRenderType.MODEL)
			return false;
		BlockPos otherPos = pos.offset(side, 2);
		BlockState otherConn = world.getBlockState(otherPos).getActualState(world, otherPos);
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
		for(ImmersiveNetHandler.Connection c : connOther)
			if(connHere.stream().anyMatch(c2 -> c2.end.equals(c.end)))
				return false;
		//Form
		if(!world.isRemote)
		{
			BlockState state = IEContent.blockConnectors.getDefaultState().with(IEContent.blockConnectors.property,
					BlockTypes_Connector.FEEDTHROUGH).with(IEProperties.FACING_ALL, side);
			BlockPos masterPos = pos.offset(side);
			FeedthroughTileEntity master = setBlock(world, masterPos, state, wire, middle, 0);
			if(master!=null)
			{
				//TODO
				//moveConnectionsToMaster(connOther, world, true, master);
				//moveConnectionsToMaster(connHere, world, false, master);
				master.markContainingBlockForUpdate(null);
			}
			setBlock(world, pos, state, wire, middle, -1);
			setBlock(world, pos.offset(side, 2), state, wire, middle, 1);
		}
		return true;
	}

	//private void moveConnectionsToMaster(Collection<Connection> conns, World world, boolean positive,
	//									 FeedthroughTileEntity master)
	//{
	//	BlockPos masterPos = master.getPos();
	//	for(Connection c : ImmutableSet.copyOf(conns))
	//	{
	//		Connection reverse = ImmersiveNetHandler.INSTANCE.getReverseConnection(world.provider.getDimension(), c);
	//		if(positive)
	//			master.connPositive = c.end;
	//		else
	//			master.hasNegative = true;
	//		ApiUtils.moveConnectionEnd(reverse, masterPos, world);
	//	}
	//}

	@Nullable
	private FeedthroughTileEntity setBlock(World world, BlockPos here, BlockState newState, WireType wire, BlockState middle,
										   int offset)
	{
		world.setBlockState(here, newState);
		TileEntity te = world.getTileEntity(here);
		if(te instanceof FeedthroughTileEntity)
		{
			FeedthroughTileEntity feedthrough = (FeedthroughTileEntity)te;
			feedthrough.reference = wire;
			feedthrough.stateForMiddle = middle;
			feedthrough.offset = offset;
			world.checkLight(here);
			world.addBlockEvent(here, feedthrough.getBlockState(), 253, 0);
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