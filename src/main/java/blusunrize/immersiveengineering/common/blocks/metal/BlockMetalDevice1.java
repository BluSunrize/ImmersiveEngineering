/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;

import java.util.Arrays;

public class BlockMetalDevice1 extends BlockIETileProvider<BlockTypes_MetalDevice1>
{
	public BlockMetalDevice1()
	{
		super("metal_device1", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalDevice1.class), ItemBlockIEBase.class, IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IOBJModelCallback.PROPERTY, IEProperties.OBJ_TEXTURE_REMAP);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		lightOpacity = 0;
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.CHARGING_STATION.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FLUID_PIPE.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FLOODLIGHT.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta(), BlockRenderLayer.CUTOUT);

		this.setMetaLightOpacity(BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta(), 255);
		this.setMetaLightOpacity(BlockTypes_MetalDevice1.DYNAMO.getMeta(), 255);
		this.setMetaLightOpacity(BlockTypes_MetalDevice1.THERMOELECTRIC_GEN.getMeta(), 255);
		this.setNotNormalBlock(BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.CHARGING_STATION.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TESLA_COIL.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.FLOODLIGHT.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TURRET_CHEM.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TURRET_GUN.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.BELLJAR.getMeta());

		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TESLA_COIL.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TURRET_CHEM.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TURRET_GUN.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.BELLJAR.getMeta(), EnumPushReaction.BLOCK);
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.ELECTRIC_LANTERN)
			return "lantern";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.CHARGING_STATION)
			return "charging_station";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.FLUID_PIPE)
			return "pipe";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.SAMPLE_DRILL)
			return "core_drill";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.FLOODLIGHT)
			return "floodlight";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.BELLJAR)
			return "belljar";
		//		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.TESLA_COIL)
		//			return "teslaCoil";
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
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityImmersiveConnectable&&state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)tile).genConnBlockstate());
		if(tile instanceof TileEntityElectricLantern)
			state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityElectricLantern)tile).active);
		if(tile instanceof TileEntityFloodlight)
			state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityFloodlight)tile).active);
		return state;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		if(stack.getItemDamage()==BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta()||stack.getItemDamage()==BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta()||stack.getItemDamage()==BlockTypes_MetalDevice1.BELLJAR.getMeta())
		{
			for(int hh = 1; hh <= 2; hh++)
			{
				BlockPos pos2 = pos.add(0, hh, 0);
				if(world.isOutsideBuildHeight(pos2)||!world.getBlockState(pos2).getBlock().isReplaceable(world, pos2))
					return false;
			}
		}
		else if(stack.getItemDamage()==BlockTypes_MetalDevice1.TESLA_COIL.getMeta())
		{
			BlockPos newPos = pos.offset(side);
			return !world.isOutsideBuildHeight(newPos)&&world.getBlockState(newPos).getBlock().isReplaceable(world, newPos);
		}
		else if(stack.getItemDamage()==BlockTypes_MetalDevice1.TURRET_CHEM.getMeta()||stack.getItemDamage()==BlockTypes_MetalDevice1.TURRET_GUN.getMeta())
		{
			BlockPos newPos = pos.up();
			return !world.isOutsideBuildHeight(newPos)&&world.getBlockState(newPos).getBlock().isReplaceable(world, newPos);
		}
		return true;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityTeslaCoil)
			return !((TileEntityTeslaCoil)tile).dummy;
		else if(tile instanceof TileEntityTurret)
			return !((TileEntityTurret)tile).dummy;
		else if(tile instanceof TileEntityFluidPipe)
			return !((TileEntityFluidPipe)tile).pipeCover.isEmpty();
		else if(tile instanceof TileEntityElectricLantern||tile instanceof TileEntityChargingStation||tile instanceof TileEntityFloodlight)
			return side==EnumFacing.DOWN;
		return true;
	}


	@Override
	public TileEntity createBasicTE(World world, BlockTypes_MetalDevice1 type)
	{
		switch(type)
		{
			case BLAST_FURNACE_PREHEATER:
				return new TileEntityBlastFurnacePreheater();
			case FURNACE_HEATER:
				return new TileEntityFurnaceHeater();
			case DYNAMO:
				return new TileEntityDynamo();
			case THERMOELECTRIC_GEN:
				return new TileEntityThermoelectricGen();
			case ELECTRIC_LANTERN:
				return new TileEntityElectricLantern();
			case CHARGING_STATION:
				return new TileEntityChargingStation();
			case FLUID_PIPE:
				return new TileEntityFluidPipe();
			case SAMPLE_DRILL:
				return new TileEntitySampleDrill();
			case TESLA_COIL:
				return new TileEntityTeslaCoil();
			case FLOODLIGHT:
				return new TileEntityFloodlight();
			case TURRET_CHEM:
				return new TileEntityTurretChem();
			case TURRET_GUN:
				return new TileEntityTurretGun();
			case BELLJAR:
				return new TileEntityBelljar();

			//		case 0://CONNECTOR_LV
			//		case 1://CONNECTOR_MV
			//			return new TileEntityConnectorMV();
			//		case 2://CONNECTOR_HV
			//			return new TileEntityConnectorHV();
			//		case 3://RELAY_HV
			//			return new TileEntityRelayHV();
			//		case META_connectorLV:
			//			return new TileEntityConnectorLV();
			//		case META_capacitorLV:
			//			return new TileEntityCapacitorLV();
			//		case META_connectorMV:
			//			return new TileEntityConnectorMV();
			//		case META_capacitorMV:
			//			return new TileEntityCapacitorMV();
			//		case META_transformer:
			//			return new TileEntityTransformer();
			//		case META_relayHV:
			//			return new TileEntityRelayHV();
			//		case META_connectorHV:
			//			return new TileEntityConnectorHV();
			//		case META_capacitorHV:
			//			return new TileEntityCapacitorHV();
			//		case META_transformerHV:
			//			return new TileEntityTransformerHV();
			//		case META_dynamo:
			//			return new TileEntityDynamo();
			//		case META_thermoelectricGen:
			//			return new TileEntityThermoelectricGen();
			//		case META_conveyorBelt:
			//			return new TileEntityConveyorBelt();
			//		case META_furnaceHeater:
			//			return new TileEntityFurnaceHeater();
			//		case META_sorter:
			//			return new TileEntityConveyorSorter();
			//		case META_sampleDrill:
			//			return new TileEntitySampleDrill();
			//		case META_conveyorDropper:
			//			return new TileEntityConveyorBelt(true);
		}
		return null;
	}

	//	@Override
	//	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	//	{
	//		super.breakBlock(world, x, y, z, par5, par6);
	//	}

	//	@Override
	//	public void onNeighborBlockChange(World world, int x, int y, int z, Block nbid)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(te instanceof TileEntityConnectorLV)
	//		{
	//			TileEntityConnectorLV relay = (TileEntityConnectorLV)te;
	//			ForgeDirection fd = ForgeDirection.getOrientation(relay.facing);
	//			if(world.isAirBlock(x+fd.offsetX, y+fd.offsetY, z+fd.offsetZ))
	//			{
	//				dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
	//				world.setBlockToAir(x, y, z);
	//			}
	//		}
	//		if(te instanceof TileEntityTransformer)
	//		{
	//			TileEntityTransformer transf = (TileEntityTransformer)te;
	//			int postX = x+(transf.postAttached==4?1: transf.postAttached==5?-1: 0);
	//			int postZ = z+(transf.postAttached==2?1: transf.postAttached==3?-1: 0);
	//			Block blockPost = world.getBlock(postX, y, postZ);
	//			if(transf.postAttached>0 && !(blockPost instanceof IPostBlock && ((IPostBlock)blockPost).canConnectTransformer(world, postX, y, postZ)))
	//			{
	//				this.dropBlockAsItem(world, x, y, z, new ItemStack(this,1,world.getBlockMetadata(x, y, z)));
	//				world.setBlockToAir(x, y, z);
	//			}
	//			else if(transf.postAttached<=0 && ((transf.dummy && world.isAirBlock(x,y+1,z))|| (!transf.dummy && world.isAirBlock(x,y-1,z))))
	//				world.setBlockToAir(x, y, z);
	//		}
	//		if(te instanceof TileEntitySampleDrill)
	//		{
	//			TileEntitySampleDrill drill = (TileEntitySampleDrill)te;
	//			if((drill.pos==0 && (world.isAirBlock(x,y+1,z)||world.isAirBlock(x,y+2,z)))
	//					||(drill.pos==1 && (world.isAirBlock(x,y-1,z)||world.isAirBlock(x,y+1,z)))
	//					||(drill.pos==2 && (world.isAirBlock(x,y-1,z)||world.isAirBlock(x,y-2,z))))
	//				world.setBlockToAir(x, y, z);
	//		}
	//	}

	//	@Override
	//	public void onEntityCollision(World world, int x, int y, int z, Entity par5Entity)
	//	{
	//		TileEntity te = world.getTileEntity(x, y, z);
	//		if(par5Entity!=null && te instanceof TileEntityConveyorBelt && !par5Entity.isDead && !(par5Entity instanceof EntityPlayer && ((EntityPlayer)par5Entity).isSneaking()))
	//		{
	//			if(world.getRedstonePowerFromNeighbors(x, y, z))
	//				return;
	//			TileEntityConveyorBelt tile = (TileEntityConveyorBelt) te;
	//			int f = tile.facing;
	//			ForgeDirection fd = ForgeDirection.getOrientation(f).getOpposite();
	//			double vBase = 1.15;
	//			double vX = 0.1 * vBase*fd.offsetX;
	//			double vY = par5Entity.motionY;
	//			double vZ = 0.1 * vBase*fd.offsetZ;
	//
	//			if (tile.transportUp)
	//				vY = 0.17D * vBase;
	//			else if (tile.transportDown)
	//				vY = -0.07000000000000001D * vBase;
	//
	//			if (tile.transportUp||tile.transportDown)
	//				par5Entity.onGround = false;
	//
	//			//			if(par5Entity instanceof EntityItem)
	//			if (fd == ForgeDirection.WEST || fd == ForgeDirection.EAST)
	//			{
	//				if (par5Entity.posZ > z + 0.65D)
	//					vZ = -0.1D * vBase;
	//				else if (par5Entity.posZ < z + 0.35D)
	//					vZ = 0.1D * vBase;
	//				//				else
	//				//				{
	//				//					vZ = 0;
	//				//					par5Entity.posZ=z+.5;
	//				//				}
	//			}
	//			else if (fd == ForgeDirection.NORTH || fd == ForgeDirection.SOUTH)
	//			{
	//				if (par5Entity.posX > x + 0.65D)
	//					vX = -0.1D * vBase;
	//				else if (par5Entity.posX < x + 0.35D)
	//					vX = 0.1D * vBase;
	//				//				else
	//				//				{
	//				//					vX = 0;
	//				//					par5Entity.posX=x+.5;
	//				//				}
	//			}
	//
	//			par5Entity.motionX = vX;
	//			par5Entity.motionY = vY;
	//			par5Entity.motionZ = vZ;
	//			if(par5Entity instanceof EntityItem)
	//			{
	//				((EntityItem)par5Entity).age=0;
	//				boolean contact;
	//				boolean dropping = ((TileEntityConveyorBelt) te).dropping;
	//				if(dropping)
	//				{
	//					te = world.getTileEntity(x, y-1, z);
	//					contact = (f==2)&&(par5Entity.posZ-z>=.2) || (f==3)&&(par5Entity.posZ-z<=.8) || (f==4)&&(par5Entity.posX-x>=.2) || (f==5)&&(par5Entity.posX-x<=.8);
	//					fd = ForgeDirection.DOWN;
	//				}
	//				else
	//				{
	//					te = world.getTileEntity(x+fd.offsetX,y+(tile.transportUp?1: tile.transportDown?-1: 0),z+fd.offsetZ);
	//					contact = f==3? (par5Entity.posZ-z<=.2): f==2? (par5Entity.posZ-z>=.8): f==5? (par5Entity.posX-x<=.2): (par5Entity.posX-x>=.8);
	//				}
	//				if (!world.isRemote)
	//					if(contact && te instanceof IInventory)
	//					{
	//						IInventory inv = (IInventory)te;
	//						if(!(inv instanceof TileEntityConveyorBelt))
	//						{
	//							ItemStack stack = ((EntityItem)par5Entity).getEntityItem();
	//							if(stack!=null)
	//							{
	//								ItemStack ret = Utils.insertStackIntoInventory(inv, stack.copy(), fd.getOpposite().ordinal());
	//								if(ret==null)
	//									par5Entity.setDead();
	//								else if(ret.stackSize<stack.stackSize)
	//									((EntityItem)par5Entity).setEntityItemStack(ret);
	//							}
	//						}
	//					}
	//
	//				if(dropping && contact && !(te instanceof IInventory) && world.isAirBlock(x, y-1, z) && !world.isRemote)
	//				{
	//					par5Entity.motionX = 0;
	//					par5Entity.motionZ = 0;
	//					par5Entity.setPosition(x+.5, y-.5, z+.5);
	//				}
	//			}
	//		}
	//	}
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
//	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
//		super.onNeighborChange(world, pos, fromPos);
		super.neighborChanged(state, world, pos, block, fromPos);
		if(!world.isRemote&&world.getBlockState(pos).getValue(property)==BlockTypes_MetalDevice1.FLUID_PIPE)
			TileEntityFluidPipe.indirectConnections.clear();
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		if(!worldIn.isRemote)
			TileEntityFluidPipe.indirectConnections.clear();
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if(state.getValue(property)==BlockTypes_MetalDevice1.FLUID_PIPE)
		{
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof TileEntityFluidPipe)
			{
				TileEntityFluidPipe here = (TileEntityFluidPipe)te;
				for(int i = 0; i < 6; i++)
					if(here.sideConfig[i]==-1)
					{
						EnumFacing f = EnumFacing.VALUES[i];

						TileEntity there = world.getTileEntity(pos.offset(f));
						if(there instanceof TileEntityFluidPipe)
							((TileEntityFluidPipe)there).toggleSide(f.getOpposite().ordinal());
					}
			}
		}
		super.breakBlock(world, pos, state);
	}
}