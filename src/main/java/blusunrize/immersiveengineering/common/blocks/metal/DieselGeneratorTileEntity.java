/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundTile;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.ScaffoldingBlock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class DieselGeneratorTileEntity extends MultiblockPartTileEntity<DieselGeneratorTileEntity>
		implements IBlockBounds, ISoundTile
{
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	public boolean active = false;

	public float animation_fanRotationStep = 0;
	public float animation_fanRotation = 0;
	public int animation_fanFadeIn = 0;
	public int animation_fanFadeOut = 0;

	public DieselGeneratorTileEntity()
	{
		super(IEMultiblocks.DIESEL_GENERATOR, IETileTypes.DIESEL_GENERATOR.get(), true);
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
		checkForNeedlessTicking();
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
				world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
						exhaust.getX()+.5+(fl.getXOffset()*.3125f)+(-fw.getXOffset()*.3125f), exhaust.getY()+1.25, exhaust.getZ()+.5+(fl.getZOffset()*.3125f)+(-fw.getZOffset()*.3125f), 0.015625-(0.03125*Math.random()), 0.0625, 0.015625-(0.03125*Math.random()));
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

	public static AxisAlignedBB getBlockBounds(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 0, 4).equals(posInMultiblock))
			return new AxisAlignedBB(0, .5f, -.625f, 1, 1.5f, 1);
		if(ImmutableSet.of(
				new BlockPos(0, 0, 4),
				new BlockPos(2, 1, 0),
				new BlockPos(2, 2, 0)
		).contains(posInMultiblock))
			return new AxisAlignedBB(0, 0, 0, .5f, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?1.125f: 1);
		if(ImmutableSet.of(
				new BlockPos(2, 0, 4),
				new BlockPos(0, 1, 0),
				new BlockPos(0, 2, 0)
		).contains(posInMultiblock))
			return new AxisAlignedBB(.5f, 0, 0, 1, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?1.125f: 1);
		if(new BlockPos(1, 2, 0).equals(posInMultiblock))
		{
			posInMultiblock.getZ();
			posInMultiblock.getZ();
			posInMultiblock.getZ();
			return new AxisAlignedBB(0, 0, 0, 1, posInMultiblock.getY()==2?.8125f: 1, posInMultiblock.getZ()==0?.625f: 1);
		}

		if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==4)
			return new AxisAlignedBB(0, .5f, 0, 1, 1, 1);

		if(posInMultiblock.getX()==1&&posInMultiblock.getY() > 0&&posInMultiblock.getZ()==3)
			return new AxisAlignedBB(.0625f, 0, 0, .9375f, posInMultiblock.getY()==2?.3125f: 1, .625f);
		if(new MutableBoundingBox(1, 2, 1, 1, 2, 2).isVecInside(posInMultiblock))
			return new AxisAlignedBB(.0625f, 0, 0, .9375f, .3125f, 1);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0)
			return new AxisAlignedBB(0, 0, 0, 1, .5f, 1);

		//TODO more sensible name
		boolean lessThan21 = posInMultiblock.getY()==0||(posInMultiblock.getY()==1&&posInMultiblock.getZ() > 2);
		if(posInMultiblock.getX()==0&&posInMultiblock.getY() < 2)
			return new AxisAlignedBB(.9375f, -.5f, 0, 1, .625f, lessThan21?.625f: 1);
		if(posInMultiblock.getX()==2&&posInMultiblock.getY() < 2)
			return new AxisAlignedBB(0, -.5f, 0, .0625f, .625f, lessThan21?.625f: 1);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==2&&posInMultiblock.getZ()==2)
			return Utils.flipBox(false, posInMultiblock.getX()==2,
					new AxisAlignedBB(
							0.5625, 0, -0.0625,
							1.0625, posInMultiblock.getX()==2?1.125f: .75f, 0.4375
					));

		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(DieselGeneratorTileEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		VoxelShape base = CachedShapesWithTransform.get(SHAPES, this);
		if(isRedstonePos()&&ctx!=null&&!ctx.func_216378_a(ScaffoldingBlock.CHECK_SHAPE, pos, false))
		{
			AxisAlignedBB box = CachedShapesWithTransform.withFacingAndMirror(
					new AxisAlignedBB(0.9375, 0, 0, 1, 1, 1),
					getFacing(),
					getIsMirrored()
			);
			base = VoxelShapes.or(base, VoxelShapes.create(box));
		}
		return base;
	}

	private static List<AxisAlignedBB> getShape(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 1, 4).equals(posInMultiblock))
			return Lists.newArrayList(new AxisAlignedBB(0, .5f, 0, 1, 1, 1),
					new AxisAlignedBB(0, -.5f, -.625f, 1, .5f, 1));
		else if(posInMultiblock.getY()==1&&posInMultiblock.getZ()==4)
			return Utils.flipBoxes(false, posInMultiblock.getX()==2,
					new AxisAlignedBB(0, .5f, 0, 1, 1, 1),
					new AxisAlignedBB(.125f, 0, .125f, .375f, .5f, .375f),
					new AxisAlignedBB(.125f, 0, .625f, .375f, .5f, .875f)
			);
		if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return ImmutableList.of(getBlockBounds(posInMultiblock),
					new AxisAlignedBB(.5f, .25f, .3125f, 1, .75f, .6875f),
					new AxisAlignedBB(.6875f, -.5f, .4375f, .8125f, .25f, .5625f)
			);

		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getZ() < 4)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(getBlockBounds(posInMultiblock));
			if(posInMultiblock.getZ() > 2)
			{
				list.add(new AxisAlignedBB(0.125, .5625f, 0.25, 1, .8125f, 0.5));
				list.add(new AxisAlignedBB(0.125, .5625f, 0.5, 0.375, .8125f, 1));
			}
			else if(posInMultiblock.getZ() > 0)
			{
				final double offset = posInMultiblock.getZ() > 1?0: 1;
				list.add(new AxisAlignedBB(0.4375, .5f, -0.5625+offset, 1, 1, 0.75+offset));
			}
			if(posInMultiblock.getZ() < 2)
			{
				final double offset = posInMultiblock.getZ()==1?0: 1;
				list.add(new AxisAlignedBB(0.375, .5625f, 0.5625+offset, 0.4375, .8125f, 0.8125+offset));
				list.add(new AxisAlignedBB(0.375, .5625f, -0.875+offset, 0.5, .8125f, -0.625+offset));
				list.add(new AxisAlignedBB(0.125, .5625f, -0.875+offset, 0.375, .8125f, 0.8125+offset));
			}
			return Utils.flipBoxes(false, posInMultiblock.getX()==2, list);
		}
		return ImmutableList.of(getBlockBounds(posInMultiblock));
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
	public boolean shouldPlaySound(String sound)
	{
		return active;
	}

	@Override
	public float getSoundRadiusSq()
	{
		return 1024;
	}
}
