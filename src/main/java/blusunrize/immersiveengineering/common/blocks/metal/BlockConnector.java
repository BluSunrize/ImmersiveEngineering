package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.Arrays;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.BlockIETileProvider;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockConnector extends BlockIETileProvider
{
	public BlockConnector()
	{
		super("connector",Material.iron, PropertyEnum.create("type", BlockTypes_Connector.class), ItemBlockIEBase.class, IEProperties.FACING_ALL,IEProperties.BOOLEANS[0],IEProperties.MULTIBLOCKSLAVE);
		setHardness(3.0F);
		setResistance(15.0F);
	}

	@Override
	public boolean isFullBlock()
	{
		return false;
	}
	@Override
	public boolean isFullCube()
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}
	@Override
	public String getCustomStateMapping(int meta)
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
	protected BlockState createBlockState()
	{
		BlockState base = super.createBlockState();
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
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityConnectorLV)
		{
			TileEntityConnectorLV relay = (TileEntityConnectorLV)te;
			if(world.isAirBlock(pos.offset(relay.facing)))
			{
				this.dropBlockAsItem(world, pos, state, 0);
				world.setBlockToAir(pos);
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
}