package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.Arrays;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IPostBlock;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockConnector extends BlockIETileProvider<BlockTypes_Connector>
{
	public BlockConnector()
	{
		super("connector",Material.IRON, PropertyEnum.create("type", BlockTypes_Connector.class), ItemBlockIEBase.class, IEProperties.FACING_ALL,IEProperties.BOOLEANS[0],IEProperties.BOOLEANS[1],IEProperties.MULTIBLOCKSLAVE);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
	}

	@Override
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}
	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
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
			return "breakerSwitch";
		if(meta==BlockTypes_Connector.REDSTONE_BREAKER.getMeta())
			return "redstoneBreaker";
		if(meta==BlockTypes_Connector.ENERGY_METER.getMeta())
			return "energyMeter";
		return null;
	}
	@Override
	protected BlockStateContainer createBlockState()
	{
		BlockStateContainer base = super.createBlockState();
		IUnlistedProperty[] unlisted = (IUnlistedProperty[]) ((base instanceof ExtendedBlockState)?((ExtendedBlockState)base).getUnlistedProperties().toArray():new IUnlistedProperty[0]);
		unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
		unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		if (state instanceof IExtendedBlockState)
		{
			IExtendedBlockState ext = (IExtendedBlockState) state;
			TileEntity te = world.getTileEntity(pos);
			if (!(te instanceof TileEntityImmersiveConnectable))
				return state;
			state = ext.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
		}
		return state;
	}
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return false;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityConnectorLV)
		{
			TileEntityConnectorLV relay = (TileEntityConnectorLV)te;
			if(world.isAirBlock(pos.offset(relay.facing)))
			{
				this.dropBlockAsItem(relay.getWorld(), pos, world.getBlockState(pos), 0);
				relay.getWorld().setBlockToAir(pos);
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockTypes_Connector.values()[meta])
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
		}
		return null;
	}
	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer)
	{
		IBlockState ret = super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, placer);
		if (meta==BlockTypes_Connector.TRANSFORMER.getMeta())
		{
			BlockPos pos2 = pos.offset(facing, -1);
			IBlockState placedAgainst = world.getBlockState(pos2);
			Block block = placedAgainst.getBlock();
			if (block instanceof IPostBlock&&((IPostBlock)block).canConnectTransformer(world, pos2))
				ret = ret.withProperty(IEProperties.BOOLEANS[1], true);
		}
		return ret;
	}
}