/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Arrays;

public class BlockConnector extends BlockIETileProvider<BlockTypes_Connector>
{
	public BlockConnector()
	{
		super("connector", Material.IRON, PropertyEnum.create("type", BlockTypes_Connector.class), ItemBlockIEBase.class, IEProperties.FACING_ALL,
				IEProperties.BOOLEANS[0], IEProperties.BOOLEANS[1], IEProperties.MULTIBLOCKSLAVE, IOBJModelCallback.PROPERTY,
				IEProperties.TILEENTITY_PASSTHROUGH);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		setMetaBlockLayer(BlockTypes_Connector.RELAY_HV.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		setMetaBlockLayer(BlockTypes_Connector.CONNECTOR_PROBE.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT);
		setMetaBlockLayer(BlockTypes_Connector.FEEDTHROUGH.getMeta(), BlockRenderLayer.SOLID,
				BlockRenderLayer.CUTOUT, BlockRenderLayer.CUTOUT_MIPPED, BlockRenderLayer.TRANSLUCENT);
		setAllNotNormalBlock();
		setMetaMobilityFlag(BlockTypes_Connector.TRANSFORMER.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.TRANSFORMER_HV.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.ENERGY_METER.getMeta(), EnumPushReaction.BLOCK);
		setMetaMobilityFlag(BlockTypes_Connector.FEEDTHROUGH.getMeta(), EnumPushReaction.BLOCK);
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(meta==BlockTypes_Connector.TRANSFORMER.getMeta())
			return "transformer";
		if(meta==BlockTypes_Connector.TRANSFORMER_HV.getMeta())
			return "transformer_hv";
		if(meta==BlockTypes_Connector.BREAKERSWITCH.getMeta())
			return "breaker_switch";
		if(meta==BlockTypes_Connector.REDSTONE_BREAKER.getMeta())
			return "redstone_breaker";
		if(meta==BlockTypes_Connector.ENERGY_METER.getMeta())
			return "energy_meter";
		return null;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		BlockStateContainer base = super.createBlockState();
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
			if(!(te instanceof TileEntityImmersiveConnectable))
				return state;
			state = ext.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
		}
		return state;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState s = world.getBlockState(pos);
		return s.getValue(property)==BlockTypes_Connector.ENERGY_METER;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		switch(BlockTypes_Connector.values()[stack.getItemDamage()])
		{
			case TRANSFORMER:
			case TRANSFORMER_HV:
				for(int hh = 1; hh <= 2; hh++)
				{
					BlockPos pos2 = pos.up(hh);
					if(world.isOutsideBuildHeight(pos2)||!world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
						return false;
				}
				break;
			case ENERGY_METER:
				BlockPos pos2 = pos.up();
				return !world.isOutsideBuildHeight(pos2)&&world.getBlockState(pos2).getBlock().isReplaceable(world, pos2);
			case FEEDTHROUGH:
				EnumFacing f = new TileEntityFeedthrough().getFacingForPlacement(player, pos, side, hitX, hitY, hitZ);
				BlockPos forward = pos.offset(f, 1);
				BlockPos backward = pos.offset(f, -1);
				return world.getBlockState(forward).getBlock().isReplaceable(world, forward)&&
						world.getBlockState(backward).getBlock().isReplaceable(world, backward);
		}
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityConnectorLV)
		{
			TileEntityConnectorLV connector = (TileEntityConnectorLV)te;
			if(world.isAirBlock(pos.offset(connector.facing)))
			{
				this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
				connector.getWorld().setBlockToAir(pos);
				return;
			}
		}
		if(te instanceof TileEntityConnectorRedstone)
		{
			TileEntityConnectorRedstone connector = (TileEntityConnectorRedstone)te;
			if(world.isAirBlock(pos.offset(connector.facing)))
			{
				this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
				connector.getWorld().setBlockToAir(pos);
				return;
			}
			if(connector.isRSInput())
				connector.rsDirty = true;
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		//Select the wire if the player is sneaking
		if(player!=null&&player.isSneaking())
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityImmersiveConnectable)
			{
				TargetingInfo subTarget = null;
				if(target.hitVec!=null)
					subTarget = new TargetingInfo(target.sideHit, (float)target.hitVec.x-pos.getX(), (float)target.hitVec.y-pos.getY(), (float)target.hitVec.z-pos.getZ());
				else
					subTarget = new TargetingInfo(target.sideHit, 0, 0, 0);
				BlockPos masterPos = ((TileEntityImmersiveConnectable)te).getConnectionMaster(null, subTarget);
				if(masterPos!=pos)
					te = world.getTileEntity(masterPos);
				if(te instanceof TileEntityImmersiveConnectable)
				{
					WireType connected = ((TileEntityImmersiveConnectable)te).getCableLimiter(subTarget);
					if(connected!=null)
						return connected.getWireCoil();
				}
			}
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public TileEntity createBasicTE(World world, BlockTypes_Connector type)
	{
		switch(type)
		{
			case CONNECTOR_LV:
				return new TileEntityConnectorLV();
			case RELAY_LV:
				return new TileEntityRelayLV();
			case CONNECTOR_MV:
				return new TileEntityConnectorMV();
			case RELAY_MV:
				return new TileEntityRelayMV();
			case CONNECTOR_HV:
				return new TileEntityConnectorHV();
			case RELAY_HV:
				return new TileEntityRelayHV();
			case CONNECTOR_STRUCTURAL:
				return new TileEntityConnectorStructural();
			case TRANSFORMER:
				return new TileEntityTransformer();
			case TRANSFORMER_HV:
				return new TileEntityTransformerHV();
			case BREAKERSWITCH:
				return new TileEntityBreakerSwitch();
			case REDSTONE_BREAKER:
				return new TileEntityRedstoneBreaker();
			case ENERGY_METER:
				return new TileEntityEnergyMeter();
			case CONNECTOR_REDSTONE:
				return new TileEntityConnectorRedstone();
			case CONNECTOR_PROBE:
				return new TileEntityConnectorProbe();
			case FEEDTHROUGH:
				return new TileEntityFeedthrough();
		}
		return null;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		IBlockState ret = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
		if(meta==BlockTypes_Connector.TRANSFORMER.getMeta())
		{
			BlockPos pos2 = pos.offset(facing, -1);
			IBlockState placedAgainst = world.getBlockState(pos2);
			Block block = placedAgainst.getBlock();
			if(block instanceof IPostBlock&&((IPostBlock)block).canConnectTransformer(world, pos2))
				ret = ret.withProperty(IEProperties.BOOLEANS[1], true);
			TileEntity tile = world.getTileEntity(pos2);
			if(tile instanceof IPostBlock&&((IPostBlock)tile).canConnectTransformer(world, pos2))
				ret = ret.withProperty(IEProperties.BOOLEANS[1], true);
		}
		return ret;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess w, BlockPos pos)
	{
		if(state.getValue(property)==BlockTypes_Connector.FEEDTHROUGH)
		{
			TileEntity te = w.getTileEntity(pos);
			if(te instanceof TileEntityFeedthrough&&((TileEntityFeedthrough)te).offset==0)
				return 255;
		}
		return super.getLightOpacity(state, w, pos);
	}
}