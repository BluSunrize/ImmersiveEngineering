/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.util.shapes.MultiblockCacheKey;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class DieselGeneratorTileEntity extends MultiblockPartTileEntity<DieselGeneratorTileEntity>
		implements IBlockBounds, ISoundTile
{
	public static TileEntityType<DieselGeneratorTileEntity> TYPE;

	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	public boolean active = false;

	public float animation_fanRotationStep = 0;
	public float animation_fanRotation = 0;
	public int animation_fanFadeIn = 0;
	public int animation_fanFadeOut = 0;

	public DieselGeneratorTileEntity()
	{
		super(IEMultiblocks.DIESEL_GENERATOR, TYPE, true);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		active = nbt.getBoolean("active");
		animation_fanRotation = nbt.getFloat("animation_fanRotation");
		animation_fanFadeIn = nbt.getInt("animation_fanFadeIn");
		animation_fanFadeOut = nbt.getInt("animation_fanFadeOut");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[0].writeToNBT(new CompoundNBT()));
		nbt.putBoolean("active", active);
		nbt.putFloat("animation_fanRotation", animation_fanRotation);
		nbt.putInt("animation_fanFadeIn", animation_fanFadeIn);
		nbt.putInt("animation_fanFadeOut", animation_fanFadeOut);
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(isDummy())
			return;

		if(active||animation_fanFadeIn > 0||animation_fanFadeOut > 0)
		{
			float base = 18f;
			float step = active?base: 0;
			if(animation_fanFadeIn > 0)
			{
				step -= (animation_fanFadeIn/80f)*base;
				animation_fanFadeIn--;
			}
			if(animation_fanFadeOut > 0)
			{
				step += (animation_fanFadeOut/80f)*base;
				animation_fanFadeOut--;
			}
			animation_fanRotationStep = step;
			animation_fanRotation += step;
			animation_fanRotation %= 360;
		}

		if(world.isRemote)
		{
			ImmersiveEngineering.proxy.handleTileSound(IESounds.dieselGenerator, this, active, .5f, 1);
			if(active&&world.getGameTime()%4==0)
			{
				BlockPos exhaust = this.getBlockPosForPos(new BlockPos(2, 2, 2));
				Direction fl = getFacing();
				Direction fw = getFacing().rotateY();
				if(getIsMirrored())
					fw = fw.getOpposite();
				world.addParticle(ParticleTypes.LARGE_SMOKE,
						exhaust.getX()+.5+(fl.getXOffset()*.3125f)+(-fw.getXOffset()*.3125f), exhaust.getY()+1.25, exhaust.getZ()+.5+(fl.getZOffset()*.3125f)+(-fw.getZOffset()*.3125f), 0, 0, 0);
			}
		}
		else
		{
			boolean prevActive = active;

			if(!isRSDisabled()&&!tanks[0].getFluid().isEmpty())
			{
				int burnTime = DieselHandler.getBurnTime(tanks[0].getFluid().getFluid());
				if(burnTime > 0)
				{
					int fluidConsumed = 1000/burnTime;
					int output = IEConfig.MACHINES.dieselGen_output.get();
					int connected = 0;
					TileEntity[] receivers = new TileEntity[3];
					for(int i = 0; i < 3; i++)
					{
						receivers[i] = getEnergyOutput(i==1?-1: i==2?1: 0);
						if(receivers[i]!=null)
						{
							if(EnergyHelper.insertFlux(receivers[i], Direction.DOWN, 4096, true) > 0)
								connected++;
						}
					}
					if(connected > 0&&tanks[0].getFluidAmount() >= fluidConsumed)
					{
						if(!active)
						{
							active = true;
							animation_fanFadeIn = 80;
						}
						tanks[0].drain(fluidConsumed, FluidAction.EXECUTE);
						int splitOutput = output/connected;
						int leftover = output%connected;
						for(int i = 0; i < 3; i++)
							if(receivers[i]!=null)
								EnergyHelper.insertFlux(receivers[i], Direction.DOWN, splitOutput+(leftover-- > 0?1: 0), false);
					}
					else if(active)
					{
						active = false;
						animation_fanFadeOut = 80;
					}
				}
			}
			else if(active)
			{
				active = false;
				animation_fanFadeOut = 80;
			}

			if(prevActive!=active)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		}
	}

	//TODO move to CapRef's
	@Nullable
	TileEntity getEnergyOutput(int w)
	{
		BlockPos outPos = this.getBlockPosForPos(new BlockPos(1+w, 1, 4)).add(0, 1, 0);
		TileEntity eTile = Utils.getExistingTileEntity(world, outPos);
		if(EnergyHelper.isFluxReceiver(eTile, Direction.DOWN))
			return eTile;
		return null;
	}

	public static AxisAlignedBB getBlockBounds(MultiblockCacheKey key)
	{
		Direction fl = key.facing;
		Direction fw = fl.rotateY();
		if(key.mirrored)
			fw = fw.getOpposite();
		BlockPos posInMultiblock = key.posInMultiblock;

		if(new BlockPos(1, 0, 4).equals(posInMultiblock))
			return new AxisAlignedBB(fl==Direction.WEST?-.625f: 0, .5f, fl==Direction.NORTH?-.625f: 0, fl==Direction.EAST?1.375f: 1, 1.5f, fl==Direction.SOUTH?1.375f: 1);
		if(ImmutableSet.of(
				new BlockPos(0, 0, 4),
				new BlockPos(2, 1, 0),
				new BlockPos(2, 2, 0)
		).contains(posInMultiblock))
			return new AxisAlignedBB(fw==Direction.WEST?.5f: (posInMultiblock.getZ()==0&&fl==Direction.EAST)?-.125f: 0, 0, fw==Direction.NORTH?.5f: (posInMultiblock.getZ()==0&&fl==Direction.SOUTH)?-.125f: 0, fw==Direction.EAST?.5f: (posInMultiblock.getZ()==0&&fl==Direction.WEST)?1.125f: 1, posInMultiblock.getY()==2?.8125f: 1, fw==Direction.SOUTH?.5f: (posInMultiblock.getZ()==0&&fl==Direction.NORTH)?1.125f: 1);
		if(ImmutableSet.of(
				new BlockPos(2, 0, 4),
				new BlockPos(0, 1, 0),
				new BlockPos(0, 2, 0)
		).contains(posInMultiblock))
			return new AxisAlignedBB(fw==Direction.EAST?.5f: (posInMultiblock.getZ()==0&&fl==Direction.EAST)?-.125f: 0, 0, fw==Direction.SOUTH?.5f: (posInMultiblock.getZ()==0&&fl==Direction.SOUTH)?-.125f: 0, fw==Direction.WEST?.5f: (posInMultiblock.getZ()==0&&fl==Direction.WEST)?1.125f: 1, posInMultiblock.getY()==2?.8125f: 1, fw==Direction.NORTH?.5f: (posInMultiblock.getZ()==0&&fl==Direction.NORTH)?1.125f: 1);
		if(new BlockPos(1, 2, 0).equals(posInMultiblock))
			return new AxisAlignedBB(posInMultiblock.getZ()==0&&fl==Direction.EAST?.375f: 0, 0, posInMultiblock.getZ()==0&&fl==Direction.SOUTH?.375f: 0, posInMultiblock.getZ()==0&&fl==Direction.WEST?.625f: 1, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0&&fl==Direction.NORTH?.625f: 1);

		if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==4)
			return new AxisAlignedBB(0, .5f, 0, 1, 1, 1);

		if(posInMultiblock.getX()==1&&posInMultiblock.getY() > 0&&posInMultiblock.getZ()==3)
			return new AxisAlignedBB(fl==Direction.EAST?.375f: fl.getAxis()==Axis.Z?.0625f: 0, 0, fl==Direction.SOUTH?.375f: fl.getAxis()==Axis.X?.0625f: 0, fl==Direction.WEST?.625f: fl.getAxis()==Axis.Z?.9375f: 1, posInMultiblock.getY()==2?.3125f: 1, fl==Direction.NORTH?.625f: fl.getAxis()==Axis.X?.9375f: 1);
		if(new MutableBoundingBox(1, 2, 1, 1, 2, 2).isVecInside(posInMultiblock))
			return new AxisAlignedBB(fl.getAxis()==Axis.Z?.0625f: 0, 0, fl.getAxis()==Axis.X?.0625f: 0, fl.getAxis()==Axis.Z?.9375f: 1, .3125f, fl.getAxis()==Axis.X?.9375f: 1);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0)
			return new AxisAlignedBB(0, 0, 0, 1, .5f, 1);

		//TODO more sensible name
		boolean lessThan21 = posInMultiblock.getY()==0||(posInMultiblock.getY()==1&&posInMultiblock.getZ() > 2);
		if(posInMultiblock.getX()==0&&posInMultiblock.getY() < 2)
			return new AxisAlignedBB(fw==Direction.EAST?.9375f: (lessThan21&&fl==Direction.EAST)?.375f: 0, -.5f, fw==Direction.SOUTH?.9375f: (lessThan21&&fl==Direction.SOUTH)?.375f: 0, fw==Direction.WEST?.0625f: (lessThan21&&fl==Direction.WEST)?.625f: 1, .625f, fw==Direction.NORTH?.0625f: (lessThan21&&fl==Direction.NORTH)?.625f: 1);
		if(posInMultiblock.getX()==2&&posInMultiblock.getY() < 2)
			return new AxisAlignedBB(fw==Direction.WEST?.9375f: (lessThan21&&fl==Direction.EAST)?.375f: 0, -.5f, fw==Direction.NORTH?.9375f: (lessThan21&&fl==Direction.SOUTH)?.375f: 0, fw==Direction.EAST?.0625f: (lessThan21&&fl==Direction.WEST)?.625f: 1, .625f, fw==Direction.SOUTH?.0625f: (lessThan21&&fl==Direction.NORTH)?.625f: 1);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==2&&posInMultiblock.getZ()==2)
		{
			if(posInMultiblock.getX()==2)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?-.0625f: fl==Direction.EAST?.5625f: fw==Direction.WEST?-.0625f: .5625f;
			float maxX = fl==Direction.WEST?.4375f: fl==Direction.EAST?1.0625f: fw==Direction.WEST?.4375f: 1.0625f;
			float minZ = fl==Direction.NORTH?-.0625f: fl==Direction.SOUTH?.5625f: fw==Direction.NORTH?-.0625f: .5625f;
			float maxZ = fl==Direction.NORTH?.4375f: fl==Direction.SOUTH?1.0625f: fw==Direction.NORTH?.4375f: 1.0625f;
			return new AxisAlignedBB(minX, 0, minZ, maxX, posInMultiblock.getX()==2?1.125f: .75f, maxZ);
		}

		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);

	}

	private static final CachedVoxelShapes<MultiblockCacheKey> SHAPES = new CachedVoxelShapes<>(DieselGeneratorTileEntity::getShape);

	@Override
	public VoxelShape getBlockBounds()
	{
		return SHAPES.get(new MultiblockCacheKey(this));
	}

	private static List<AxisAlignedBB> getShape(MultiblockCacheKey key)
	{
		BlockPos posInMultiblock = key.posInMultiblock;
		Direction fl = key.facing;
		Direction fw = fl.rotateY();
		if(key.mirrored)
			fw = fw.getOpposite();

		if(new BlockPos(1, 1, 4).equals(posInMultiblock))
			return Lists.newArrayList(new AxisAlignedBB(0, .5f, 0, 1, 1, 1),
					new AxisAlignedBB(fl==Direction.WEST?-.625f: 0, -.5f, fl==Direction.NORTH?-.625f: 0, fl==Direction.EAST?1.375f: 1, .5f, fl==Direction.SOUTH?1.375f: 1));
		else if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==4)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, .5f, 0, 1, 1, 1));
			if(posInMultiblock.getX()==2)
				fw = fw.getOpposite();
			list.add(new AxisAlignedBB(fw==Direction.EAST?.125f: fw==Direction.WEST?.625f: .125f, 0, fw==Direction.SOUTH?.125f: fw==Direction.NORTH?.625f: .125f, fw==Direction.EAST?.375f: fw==Direction.WEST?.875f: .375f, .5f, fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.875f: .375f));
			list.add(new AxisAlignedBB(fw==Direction.EAST?.125f: fw==Direction.WEST?.625f: .625f, 0, fw==Direction.SOUTH?.125f: fw==Direction.NORTH?.625f: .625f, fw==Direction.EAST?.375f: fw==Direction.WEST?.875f: .875f, .5f, fw==Direction.SOUTH?.375f: fw==Direction.NORTH?.875f: .875f));
			return list;
		}


		if(new BlockPos(2, 1, 2).equals(posInMultiblock))
		{
			List<AxisAlignedBB> list = Lists.newArrayList(getBlockBounds(key));
			list.add(new AxisAlignedBB(fw==Direction.EAST?.5f: fw==Direction.WEST?0: .3125f, .25f, fw==Direction.SOUTH?.5f: fw==Direction.NORTH?0: .3125f, fw==Direction.EAST?1: fw==Direction.WEST?.5f: .6875f, .75f, fw==Direction.SOUTH?1: fw==Direction.NORTH?.5f: .6875f));
			list.add(new AxisAlignedBB(fw==Direction.EAST?.6875f: fw==Direction.WEST?.1875f: .4375f, -.5f, fw==Direction.SOUTH?.6875f: fw==Direction.NORTH?.1875f: .4375f, fw==Direction.EAST?.8125f: fw==Direction.WEST?.3125f: .5625f, .25f, fw==Direction.SOUTH?.8125f: fw==Direction.NORTH?.3125f: .5625f));
			return list;
		}

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getZ() < 4)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(getBlockBounds(key));
			if(posInMultiblock.getX()==2)
				fw = fw.getOpposite();

			if(posInMultiblock.getZ() > 2)
			{
				float minX = fw==Direction.WEST?0: fw==Direction.EAST?.125f: fl==Direction.WEST?.25f: .5f;
				float maxX = fw==Direction.WEST?.875f: fw==Direction.EAST?1: fl==Direction.EAST?.75f: .5f;
				float minZ = fw==Direction.NORTH?0: fw==Direction.SOUTH?.125f: fl==Direction.NORTH?.25f: .5f;
				float maxZ = fw==Direction.NORTH?.875f: fw==Direction.SOUTH?1: fl==Direction.SOUTH?.75f: .5f;
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ));

				minX = fw==Direction.WEST?.625f: fw==Direction.EAST?.125f: fl==Direction.EAST?0: .5f;
				maxX = fw==Direction.WEST?.875f: fw==Direction.EAST?.375f: fl==Direction.WEST?1: .5f;
				minZ = fw==Direction.NORTH?.625f: fw==Direction.SOUTH?.125f: fl==Direction.SOUTH?0: .5f;
				maxZ = fw==Direction.NORTH?.875f: fw==Direction.SOUTH?.375f: fl==Direction.NORTH?1: .5f;
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ));
			}
			else if(posInMultiblock.getZ() > 0)
			{
				float minX = (fw==Direction.WEST?0: fw==Direction.EAST?.4375f: fl==Direction.EAST?.25f: -.5625f)+(posInMultiblock.getZ() > 1?0: fl==Direction.WEST?1: fl==Direction.EAST?-1: 0);
				float maxX = (fw==Direction.WEST?.5626f: fw==Direction.EAST?1: fl==Direction.WEST?.75f: 1.4375f)+(posInMultiblock.getZ() > 1?0: fl==Direction.WEST?1: fl==Direction.EAST?-1: 0);
				float minZ = (fw==Direction.NORTH?0: fw==Direction.SOUTH?.4375f: fl==Direction.SOUTH?.25f: -.5625f)+(posInMultiblock.getZ() > 1?0: fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0);
				float maxZ = (fw==Direction.NORTH?.5625f: fw==Direction.SOUTH?1: fl==Direction.NORTH?.75f: 1.4375f)+(posInMultiblock.getZ() > 1?0: fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0);
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ));
			}
			if(posInMultiblock.getZ() < 2)
			{
				float minX = (fw==Direction.WEST?.5625f: fw==Direction.EAST?.375f: fl==Direction.WEST?.5625f: .1875f)+(posInMultiblock.getZ()==1?0: fl==Direction.WEST?1: fl==Direction.EAST?-1: 0);
				float maxX = (fw==Direction.WEST?.625f: fw==Direction.EAST?.4375f: fl==Direction.EAST?.4375f: .8125f)+(posInMultiblock.getZ()==1?0: fl==Direction.WEST?1: fl==Direction.EAST?-1: 0);
				float minZ = (fw==Direction.NORTH?.5625f: fw==Direction.SOUTH?.375f: fl==Direction.NORTH?.5625f: .1875f)+(posInMultiblock.getZ()==1?0: fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0);
				float maxZ = (fw==Direction.NORTH?.625f: fw==Direction.SOUTH?.4375f: fl==Direction.SOUTH?.4375f: .8125f)+(posInMultiblock.getZ()==1?0: fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0);
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ));
				minX = (fw==Direction.WEST?.5f: fw==Direction.EAST?.375f: fl==Direction.WEST?-.875f: 1.625f)+(posInMultiblock.getZ()==1?0: fl==Direction.WEST?1: fl==Direction.EAST?-1: 0);
				maxX = (fw==Direction.WEST?.625f: fw==Direction.EAST?.5f: fl==Direction.EAST?1.875f: -.625f)+(posInMultiblock.getZ()==1?0: fl==Direction.WEST?1: fl==Direction.EAST?-1: 0);
				minZ = (fw==Direction.NORTH?.5f: fw==Direction.SOUTH?.375f: fl==Direction.NORTH?-.875f: 1.625f)+(posInMultiblock.getZ()==1?0: fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0);
				maxZ = (fw==Direction.NORTH?.625f: fw==Direction.SOUTH?.5f: fl==Direction.SOUTH?1.875f: -.625f)+(posInMultiblock.getZ()==1?0: fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0);
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ));
				minX = (fw==Direction.WEST?.625f: fw==Direction.EAST?.125f: fl==Direction.EAST?.1875f: -.875f)+(posInMultiblock.getZ()==1?0: fl==Direction.WEST?1: fl==Direction.EAST?-1: 0);
				maxX = (fw==Direction.WEST?.875f: fw==Direction.EAST?.375f: fl==Direction.WEST?.8125f: 1.875f)+(posInMultiblock.getZ()==1?0: fl==Direction.WEST?1: fl==Direction.EAST?-1: 0);
				minZ = (fw==Direction.NORTH?.625f: fw==Direction.SOUTH?.125f: fl==Direction.SOUTH?.1875f: -.875f)+(posInMultiblock.getZ()==1?0: fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0);
				maxZ = (fw==Direction.NORTH?.875f: fw==Direction.SOUTH?.375f: fl==Direction.NORTH?.8125f: 1.875f)+(posInMultiblock.getZ()==1?0: fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0);
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ));
			}
			return list;
		}
		return ImmutableList.of(getBlockBounds(key));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 2)
		);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		DieselGeneratorTileEntity master = master();
		if(master!=null&&(posInMultiblock.getZ()==4&&posInMultiblock.getY()==0&&posInMultiblock.getX()%2==0)
				&&(side==null||side.getAxis()==getFacing().rotateYCCW().getAxis()))
			return master.tanks;
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		if(resources==null)
			return false;
		return DieselHandler.isValidFuel(resources.getFluid());
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Override
	public boolean shoudlPlaySound(String sound)
	{
		return active;
	}
}
