/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.ImmersiveConnectableTileEntity;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class ConnectorBlock extends IETileProviderBlock
{

	public ConnectorBlock(String name, IProperty... additional)
	{
		super(name, Block.Properties.create(Material.IRON).hardnessAndResistance(3.0F, 15.0F),
				BlockItemIE.class, additional);
		lightOpacity = 0;
		setBlockLayer(BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		setNotNormalBlock();
		/*setMetaBlockLayer(BlockTypes_Connector.CONNECTOR_PROBE.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT);
		setMetaBlockLayer(BlockTypes_Connector.FEEDTHROUGH.getMeta(), BlockRenderLayer.SOLID,
				BlockRenderLayer.CUTOUT, BlockRenderLayer.CUTOUT_MIPPED, BlockRenderLayer.TRANSLUCENT);
		setMetaMobilityFlag(BlockTypes_Connector.TRANSFORMER.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.TRANSFORMER_HV.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.ENERGY_METER.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.FEEDTHROUGH.getMeta(), EnumPushReaction.BLOCK);*/
	}

		/*
		TODO when unlisted properties are back
	@Override
	protected void fillStateContainer(Builder<Block, IBlockState> builder)
	{
		IUnlistedProperty[] unlisted = (base instanceof ExtendedBlockState)?((ExtendedBlockState)base).getUnlistedProperties().toArray(new IUnlistedProperty[0]): new IUnlistedProperty[0];
		unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
		unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getExtendedState(state, world, pos);
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState ext = (IExtendedBlockState)state;
			TileEntity te = world.getTileEntity(pos);
			if(!(te instanceof ImmersiveConnectableTileEntity))
				return state;
			state = ext.with(IEProperties.CONNECTIONS, ((ImmersiveConnectableTileEntity)te).genConnBlockstate());
		}
		return state;
	}
	*/

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, world, pos, block, fromPos, isMoving);
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof EnergyConnectorTileEntity)
		{
			EnergyConnectorTileEntity connector = (EnergyConnectorTileEntity)te;
			if(world.isAirBlock(pos.offset(connector.getFacing())))
			{
				spawnAsEntity(world, pos, new ItemStack(this));
				connector.getWorldNonnull().removeBlock(pos, false);
			}
		}
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult targetIn, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		//Select the wire if the player is sneaking
		//TODO alternative to world instaceof World
		if(player!=null&&player.isSneaking()&&world instanceof World&&targetIn instanceof BlockRayTraceResult)
		{
			BlockRayTraceResult target = (BlockRayTraceResult)targetIn;
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof ImmersiveConnectableTileEntity)
			{
				TargetingInfo subTarget = new TargetingInfo(target.getFace(), (float)target.getHitVec().x-pos.getX(),
						(float)target.getHitVec().y-pos.getY(), (float)target.getHitVec().z-pos.getZ());
				BlockPos masterPos = ((ImmersiveConnectableTileEntity)te).getConnectionMaster(null, subTarget);
				if(masterPos!=pos)
					te = world.getTileEntity(masterPos);
				if(te instanceof ImmersiveConnectableTileEntity)
				{
					ConnectionPoint cp = ((ImmersiveConnectableTileEntity)te).getTargetedPoint(subTarget, masterPos.subtract(pos));
					if(cp!=null)
						for(Connection c : GlobalWireNetwork.getNetwork((World)world).getLocalNet(cp).getConnections(cp))
							if(!c.isInternal())
								return c.type.getWireCoil(c);
				}
			}
		}
		return super.getPickBlock(state, targetIn, world, pos, player);
	}

	@Override
	public boolean allowHammerHarvest(BlockState state)
	{
		return true;
	}
}