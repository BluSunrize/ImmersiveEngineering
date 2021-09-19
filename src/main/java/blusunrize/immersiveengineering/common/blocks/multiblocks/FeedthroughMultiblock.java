/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.blocks.metal.FeedthroughTileEntity;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static blusunrize.immersiveengineering.api.IEProperties.FACING_ALL;

public class FeedthroughMultiblock implements IMultiblock
{
	private static final Component ARBITRARY_SOLID = new TranslatableComponent("block.immersiveengineering.arb_solid");
	public static FeedthroughMultiblock instance = new FeedthroughMultiblock();
	static List<StructureBlockInfo> structure = new ArrayList<>();

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	private ItemStack renderStack;

	private Block getDemoConnector()
	{
		return Connectors.getEnergyConnector(WireType.LV_CATEGORY, false);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure(PoseStack transform, MultiBufferSource buffer)
	{
		if(renderStack==null||renderStack.isEmpty())
			renderStack = new ItemStack(Connectors.feedthrough);

		transform.translate(1.5, .5, .5);
		transform.mulPose(new Quaternion(0, 45, 0, true));
		transform.mulPose(new Quaternion(-30, 0, 0, true));
		transform.scale(1.75F, 1.75F, 1.75F);

		ClientUtils.mc().getItemRenderer().renderStatic(
				renderStack,
				TransformType.GUI,
				0xf000f0,
				OverlayTexture.NO_OVERLAY,
				transform, buffer
		);
	}

	@Override
	public Vec3i getSize(@Nullable Level world)
	{
		return new Vec3i(3, 1, 1);
	}

	@Override
	public void disassemble(Level world, BlockPos startPos, boolean mirrored, Direction clickDirectionAtCreation)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public BlockPos getTriggerOffset()
	{
		return new BlockPos(-1, 0, 0);
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}

	@Override
	public ResourceLocation getUniqueName()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, "feedthrough");
	}

	@Override
	public boolean isBlockTrigger(BlockState state, Direction side, @Nullable Level world)
	{
		return WireApi.getWireType(state)!=null;
	}

	@Nullable
	WireType checkValidConnector(Level w, BlockPos pos, GlobalWireNetwork globalNet, Direction expectedDirection)
	{
		LocalWireNetwork localNet = globalNet.getNullableLocalNet(pos);
		if(localNet==null)
			return null;
		Collection<Connection> connsHere = localNet.getConnections(pos);
		IImmersiveConnectable connHere = localNet.getConnector(pos);
		if(connsHere==null)
			connsHere = ImmutableSet.of();
		if(connsHere.size() > 1||connHere.getConnectionPoints().size()!=1)
			return null;
		BlockState state = w.getBlockState(pos);
		if(!state.hasProperty(FACING_ALL)||state.getValue(FACING_ALL)!=expectedDirection)
			return null;
		return WireApi.getWireType(state);
	}

	@Override
	public boolean createStructure(Level world, BlockPos pos, Direction side, Player player)
	{
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(world);
		//Check
		BlockState stateHere = world.getBlockState(pos);
		if(stateHere.hasProperty(FACING_ALL))
			side = stateHere.getValue(FACING_ALL);
		WireType wire = checkValidConnector(world, pos, globalNet, side);
		if(wire==null)
			return false;
		BlockPos middlePos = pos.relative(side);
		BlockState middle = world.getBlockState(middlePos);
		if(!middle.getShape(world, pos).equals(Shapes.block())
				||middle.getBlock().hasTileEntity(middle)
				||middle.getRenderShape()!=RenderShape.MODEL)
			return false;
		BlockPos otherPos = pos.relative(side, 2);
		WireType otherWire = checkValidConnector(world, otherPos, globalNet, side.getOpposite());
		if(otherWire!=wire)
			return false;
		LocalWireNetwork localHere = globalNet.getLocalNet(pos);
		ConnectionPoint cpHere = localHere.getConnector(pos).getConnectionPoints().iterator().next();
		Collection<Connection> connsHere = localHere.getConnections(cpHere);
		LocalWireNetwork localOther = globalNet.getLocalNet(otherPos);
		ConnectionPoint cpOther = localOther.getConnector(otherPos).getConnectionPoints().iterator().next();
		Collection<Connection> connsOther = localOther.getConnections(cpOther);
		if(connsOther.stream().anyMatch(c -> c.isEnd(cpHere)))
			return false;
		for(Connection c : connsOther)
		{
			ConnectionPoint otherEnd = c.getOtherEnd(cpOther);
			if(connsHere.stream().anyMatch(c2 -> c2.isEnd(otherEnd)))
				return false;
		}
		//Form
		if(!world.isClientSide)
		{
			BlockState state = Connectors.feedthrough.defaultBlockState().setValue(FACING_ALL, side);
			BlockPos masterPos = pos.relative(side);
			FeedthroughTileEntity master = setBlock(world, masterPos, state, wire, middle, 0);
			if(master!=null)
			{
				moveConnectionsToMaster(connsOther, cpOther, world, master.getPositivePoint());
				moveConnectionsToMaster(connsHere, cpHere, world, master.getNegativePoint());
			}
			setBlock(world, pos, state, wire, middle, -1);
			setBlock(world, pos.relative(side, 2), state, wire, middle, 1);
		}
		return true;
	}

	@Override
	public List<StructureBlockInfo> getStructure(@Nullable Level world)
	{
		if(structure.isEmpty())
		{
			//Along x axis
			structure.add(new StructureBlockInfo(
					BlockPos.ZERO,
					getDemoConnector().defaultBlockState().setValue(FACING_ALL, Direction.EAST),
					null
			));
			structure.add(new StructureBlockInfo(
					new BlockPos(1, 0, 0),
					Blocks.BOOKSHELF.defaultBlockState(),
					null
			));
			structure.add(new StructureBlockInfo(
					new BlockPos(2, 0, 0),
					getDemoConnector().defaultBlockState().setValue(FACING_ALL, Direction.WEST),
					null
			));
		}
		return structure;
	}

	private void moveConnectionsToMaster(Collection<Connection> conns, ConnectionPoint oldCommon, Level world,
										 ConnectionPoint newCommon)
	{
		for(Connection c : ImmutableSet.copyOf(conns))
			WireUtils.moveConnectionEnd(c, oldCommon, newCommon, world);
	}

	@Nullable
	private FeedthroughTileEntity setBlock(Level world, BlockPos here, BlockState newState, WireType wire, BlockState middle,
										   int offset)
	{
		world.setBlockAndUpdate(here, newState);
		BlockEntity te = world.getBlockEntity(here);
		if(te instanceof FeedthroughTileEntity)
		{
			FeedthroughTileEntity feedthrough = (FeedthroughTileEntity)te;
			feedthrough.reference = wire;
			feedthrough.stateForMiddle = middle;
			feedthrough.offset = offset;
			world.blockEvent(here, feedthrough.getBlockState().getBlock(), 253, 0);
			return feedthrough;
		}
		return null;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
				new ItemStack(getDemoConnector(), 2),
				new ItemStack(Blocks.BOOKSHELF, 1).setHoverName(ARBITRARY_SOLID)
		};
	}

	@Override
	public boolean overwriteBlockRender(BlockState state, int iterator)
	{
		return false;
	}
}