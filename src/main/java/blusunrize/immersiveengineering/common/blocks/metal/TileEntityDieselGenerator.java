/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockDieselGenerator;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityDieselGenerator extends TileEntityMultiblockMetal<TileEntityDieselGenerator, IMultiblockRecipe>
		implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IGuiTile, ISoundTile, IFaceShape
{
	public TileEntityDieselGenerator()
	{
		super(MultiblockDieselGenerator.instance, new int[]{3, 5, 3}, 0, true);
	}

	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	public boolean active = false;

	public float animation_fanRotationStep = 0;
	public float animation_fanRotation = 0;
	public int animation_fanFadeIn = 0;
	public int animation_fanFadeOut = 0;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		active = nbt.getBoolean("active");
		animation_fanRotation = nbt.getFloat("animation_fanRotation");
		animation_fanFadeIn = nbt.getInteger("animation_fanFadeIn");
		animation_fanFadeOut = nbt.getInteger("animation_fanFadeOut");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setBoolean("active", active);
		nbt.setFloat("animation_fanRotation", animation_fanRotation);
		nbt.setInteger("animation_fanFadeIn", animation_fanFadeIn);
		nbt.setInteger("animation_fanFadeOut", animation_fanFadeOut);
	}

	@Override
	public void update()
	{
		super.update();
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
			if(active&&world.getTotalWorldTime()%4==0)
			{
				BlockPos exhaust = this.getBlockPosForPos(38);
				EnumFacing fl = facing;
				EnumFacing fw = facing.rotateY();
				if(mirrored)
					fw = fw.getOpposite();
				world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, exhaust.getX()+.5+(fl.getXOffset()*.3125f)+(-fw.getXOffset()*.3125f), exhaust.getY()+1.25, exhaust.getZ()+.5+(fl.getZOffset()*.3125f)+(-fw.getZOffset()*.3125f), 0, 0, 0);
			}
		}
		else
		{
			boolean prevActive = active;

			if(!isRSDisabled()&&tanks[0].getFluid()!=null&&tanks[0].getFluid().getFluid()!=null)
			{
				int burnTime = DieselHandler.getBurnTime(tanks[0].getFluid().getFluid());
				if(burnTime > 0)
				{
					int fluidConsumed = 1000/burnTime;
					int output = IEConfig.Machines.dieselGen_output;
					int connected = 0;
					TileEntity[] receivers = new TileEntity[3];
					for(int i = 0; i < 3; i++)
					{
						receivers[i] = getEnergyOutput(i==1?-1: i==2?1: 0);
						if(receivers[i]!=null)
						{
							if(EnergyHelper.insertFlux(receivers[i], EnumFacing.DOWN, 4096, true) > 0)
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
						tanks[0].drain(fluidConsumed, true);
						int splitOutput = output/connected;
						int leftover = output%connected;
						for(int i = 0; i < 3; i++)
							if(receivers[i]!=null)
								EnergyHelper.insertFlux(receivers[i], EnumFacing.DOWN, splitOutput+(leftover-- > 0?1: 0), false);
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

	@Nullable
	TileEntity getEnergyOutput(int w)
	{
		BlockPos outPos = this.getBlockPosForPos(16+w).add(0, 1, 0);
		TileEntity eTile = Utils.getExistingTileEntity(world, outPos);
		if(EnergyHelper.isFluxReceiver(eTile, EnumFacing.DOWN))
			return eTile;
		return null;
	}

	@Override
	public float[] getBlockBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();

		if(pos==1)
			return new float[]{fl==EnumFacing.WEST?-.625f: 0, .5f, fl==EnumFacing.NORTH?-.625f: 0, fl==EnumFacing.EAST?1.375f: 1, 1.5f, fl==EnumFacing.SOUTH?1.375f: 1};
		if(pos==0||pos==29||pos==44)
			return new float[]{fw==EnumFacing.WEST?.5f: (pos%15 > 11&&fl==EnumFacing.EAST)?-.125f: 0, 0, fw==EnumFacing.NORTH?.5f: (pos%15 > 11&&fl==EnumFacing.SOUTH)?-.125f: 0, fw==EnumFacing.EAST?.5f: (pos%15 > 11&&fl==EnumFacing.WEST)?1.125f: 1, pos > 30?.8125f: 1, fw==EnumFacing.SOUTH?.5f: (pos%15 > 11&&fl==EnumFacing.NORTH)?1.125f: 1};
		if(pos==2||pos==27||pos==42)
			return new float[]{fw==EnumFacing.EAST?.5f: (pos%15 > 11&&fl==EnumFacing.EAST)?-.125f: 0, 0, fw==EnumFacing.SOUTH?.5f: (pos%15 > 11&&fl==EnumFacing.SOUTH)?-.125f: 0, fw==EnumFacing.WEST?.5f: (pos%15 > 11&&fl==EnumFacing.WEST)?1.125f: 1, pos > 30?.8125f: 1, fw==EnumFacing.NORTH?.5f: (pos%15 > 11&&fl==EnumFacing.NORTH)?1.125f: 1};
		if(pos==43)
			return new float[]{pos%15 > 11&&fl==EnumFacing.EAST?.375f: 0, 0, pos%15 > 11&&fl==EnumFacing.SOUTH?.375f: 0, pos%15 > 11&&fl==EnumFacing.WEST?.625f: 1, pos > 30?.8125f: 1, pos%15 > 11&&fl==EnumFacing.NORTH?.625f: 1};

		if(pos >= 15&&pos <= 17)
			return new float[]{0, .5f, 0, 1, 1, 1};

		if(pos==19||pos==34)
			return new float[]{fl==EnumFacing.EAST?.375f: fl.getAxis()==Axis.Z?.0625f: 0, 0, fl==EnumFacing.SOUTH?.375f: fl.getAxis()==Axis.X?.0625f: 0, fl==EnumFacing.WEST?.625f: fl.getAxis()==Axis.Z?.9375f: 1, pos > 30?.3125f: 1, fl==EnumFacing.NORTH?.625f: fl.getAxis()==Axis.X?.9375f: 1};
		if(pos==37||pos==40)
			return new float[]{fl.getAxis()==Axis.Z?.0625f: 0, 0, fl.getAxis()==Axis.X?.0625f: 0, fl.getAxis()==Axis.Z?.9375f: 1, .3125f, fl.getAxis()==Axis.X?.9375f: 1};

		if(pos < 15&&pos%3!=1)
			return new float[]{0, 0, 0, 1, .5f, 1};

		if(pos < 30&&pos%3==0)
			return new float[]{fw==EnumFacing.EAST?.9375f: (pos < 21&&fl==EnumFacing.EAST)?.375f: 0, -.5f, fw==EnumFacing.SOUTH?.9375f: (pos < 21&&fl==EnumFacing.SOUTH)?.375f: 0, fw==EnumFacing.WEST?.0625f: (pos < 21&&fl==EnumFacing.WEST)?.625f: 1, .625f, fw==EnumFacing.NORTH?.0625f: (pos < 21&&fl==EnumFacing.NORTH)?.625f: 1};
		if(pos < 30&&pos%3==2)
			return new float[]{fw==EnumFacing.WEST?.9375f: (pos < 21&&fl==EnumFacing.EAST)?.375f: 0, -.5f, fw==EnumFacing.NORTH?.9375f: (pos < 21&&fl==EnumFacing.SOUTH)?.375f: 0, fw==EnumFacing.EAST?.0625f: (pos < 21&&fl==EnumFacing.WEST)?.625f: 1, .625f, fw==EnumFacing.SOUTH?.0625f: (pos < 21&&fl==EnumFacing.NORTH)?.625f: 1};

		if(pos==36||pos==38)
		{
			if(pos==38)
				fw = fw.getOpposite();
			float minX = fl==EnumFacing.WEST?-.0625f: fl==EnumFacing.EAST?.5625f: fw==EnumFacing.WEST?-.0625f: .5625f;
			float maxX = fl==EnumFacing.WEST?.4375f: fl==EnumFacing.EAST?1.0625f: fw==EnumFacing.WEST?.4375f: 1.0625f;
			float minZ = fl==EnumFacing.NORTH?-.0625f: fl==EnumFacing.SOUTH?.5625f: fw==EnumFacing.NORTH?-.0625f: .5625f;
			float maxZ = fl==EnumFacing.NORTH?.4375f: fl==EnumFacing.SOUTH?1.0625f: fw==EnumFacing.NORTH?.4375f: 1.0625f;
			return new float[]{minX, 0, minZ, maxX, pos==38?1.125f: .75f, maxZ};
		}

		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();

		if(pos==16)
		{
			return Lists.newArrayList(new AxisAlignedBB(0, .5f, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()),
					new AxisAlignedBB(fl==EnumFacing.WEST?-.625f: 0, -.5f, fl==EnumFacing.NORTH?-.625f: 0, fl==EnumFacing.EAST?1.375f: 1, .5f, fl==EnumFacing.SOUTH?1.375f: 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos==15||pos==17)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, .5f, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos==17)
				fw = fw.getOpposite();
			list.add(new AxisAlignedBB(fw==EnumFacing.EAST?.125f: fw==EnumFacing.WEST?.625f: .125f, 0, fw==EnumFacing.SOUTH?.125f: fw==EnumFacing.NORTH?.625f: .125f, fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.875f: .375f, .5f, fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.875f: .375f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fw==EnumFacing.EAST?.125f: fw==EnumFacing.WEST?.625f: .625f, 0, fw==EnumFacing.SOUTH?.125f: fw==EnumFacing.NORTH?.625f: .625f, fw==EnumFacing.EAST?.375f: fw==EnumFacing.WEST?.875f: .875f, .5f, fw==EnumFacing.SOUTH?.375f: fw==EnumFacing.NORTH?.875f: .875f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}


		if(pos==23)
		{
			float[] defaultBounds = this.getBlockBounds();
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(defaultBounds[0], defaultBounds[1], defaultBounds[2], defaultBounds[3], defaultBounds[4], defaultBounds[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fw==EnumFacing.EAST?.5f: fw==EnumFacing.WEST?0: .3125f, .25f, fw==EnumFacing.SOUTH?.5f: fw==EnumFacing.NORTH?0: .3125f, fw==EnumFacing.EAST?1: fw==EnumFacing.WEST?.5f: .6875f, .75f, fw==EnumFacing.SOUTH?1: fw==EnumFacing.NORTH?.5f: .6875f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			list.add(new AxisAlignedBB(fw==EnumFacing.EAST?.6875f: fw==EnumFacing.WEST?.1875f: .4375f, -.5f, fw==EnumFacing.SOUTH?.6875f: fw==EnumFacing.NORTH?.1875f: .4375f, fw==EnumFacing.EAST?.8125f: fw==EnumFacing.WEST?.3125f: .5625f, .25f, fw==EnumFacing.SOUTH?.8125f: fw==EnumFacing.NORTH?.3125f: .5625f).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}

		if(pos > 2&&pos < 15&&pos%3!=1)
		{
			float[] defaultBounds = this.getBlockBounds();
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(defaultBounds[0], defaultBounds[1], defaultBounds[2], defaultBounds[3], defaultBounds[4], defaultBounds[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos%3==2)
				fw = fw.getOpposite();

			if(pos < 6)
			{
				float minX = fw==EnumFacing.WEST?0: fw==EnumFacing.EAST?.125f: fl==EnumFacing.WEST?.25f: .5f;
				float maxX = fw==EnumFacing.WEST?.875f: fw==EnumFacing.EAST?1: fl==EnumFacing.EAST?.75f: .5f;
				float minZ = fw==EnumFacing.NORTH?0: fw==EnumFacing.SOUTH?.125f: fl==EnumFacing.NORTH?.25f: .5f;
				float maxZ = fw==EnumFacing.NORTH?.875f: fw==EnumFacing.SOUTH?1: fl==EnumFacing.SOUTH?.75f: .5f;
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fw==EnumFacing.WEST?.625f: fw==EnumFacing.EAST?.125f: fl==EnumFacing.EAST?0: .5f;
				maxX = fw==EnumFacing.WEST?.875f: fw==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?1: .5f;
				minZ = fw==EnumFacing.NORTH?.625f: fw==EnumFacing.SOUTH?.125f: fl==EnumFacing.SOUTH?0: .5f;
				maxZ = fw==EnumFacing.NORTH?.875f: fw==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?1: .5f;
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			else if(pos < 12)
			{
				float minX = (fw==EnumFacing.WEST?0: fw==EnumFacing.EAST?.4375f: fl==EnumFacing.EAST?.25f: -.5625f)+(pos <= 8?0: fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0);
				float maxX = (fw==EnumFacing.WEST?.5626f: fw==EnumFacing.EAST?1: fl==EnumFacing.WEST?.75f: 1.4375f)+(pos <= 8?0: fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0);
				float minZ = (fw==EnumFacing.NORTH?0: fw==EnumFacing.SOUTH?.4375f: fl==EnumFacing.SOUTH?.25f: -.5625f)+(pos <= 8?0: fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0);
				float maxZ = (fw==EnumFacing.NORTH?.5625f: fw==EnumFacing.SOUTH?1: fl==EnumFacing.NORTH?.75f: 1.4375f)+(pos <= 8?0: fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0);
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			if(pos > 8)
			{
				float minX = (fw==EnumFacing.WEST?.5625f: fw==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.5625f: .1875f)+(pos <= 11?0: fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0);
				float maxX = (fw==EnumFacing.WEST?.625f: fw==EnumFacing.EAST?.4375f: fl==EnumFacing.EAST?.4375f: .8125f)+(pos <= 11?0: fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0);
				float minZ = (fw==EnumFacing.NORTH?.5625f: fw==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.5625f: .1875f)+(pos <= 11?0: fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0);
				float maxZ = (fw==EnumFacing.NORTH?.625f: fw==EnumFacing.SOUTH?.4375f: fl==EnumFacing.SOUTH?.4375f: .8125f)+(pos <= 11?0: fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0);
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				minX = (fw==EnumFacing.WEST?.5f: fw==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?-.875f: 1.625f)+(pos <= 11?0: fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0);
				maxX = (fw==EnumFacing.WEST?.625f: fw==EnumFacing.EAST?.5f: fl==EnumFacing.EAST?1.875f: -.625f)+(pos <= 11?0: fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0);
				minZ = (fw==EnumFacing.NORTH?.5f: fw==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?-.875f: 1.625f)+(pos <= 11?0: fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0);
				maxZ = (fw==EnumFacing.NORTH?.625f: fw==EnumFacing.SOUTH?.5f: fl==EnumFacing.SOUTH?1.875f: -.625f)+(pos <= 11?0: fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0);
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				minX = (fw==EnumFacing.WEST?.625f: fw==EnumFacing.EAST?.125f: fl==EnumFacing.EAST?.1875f: -.875f)+(pos <= 11?0: fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0);
				maxX = (fw==EnumFacing.WEST?.875f: fw==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.8125f: 1.875f)+(pos <= 11?0: fl==EnumFacing.WEST?1: fl==EnumFacing.EAST?-1: 0);
				minZ = (fw==EnumFacing.NORTH?.625f: fw==EnumFacing.SOUTH?.125f: fl==EnumFacing.SOUTH?.1875f: -.875f)+(pos <= 11?0: fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0);
				maxZ = (fw==EnumFacing.NORTH?.875f: fw==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.8125f: 1.875f)+(pos <= 11?0: fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?-1: 0);
				list.add(new AxisAlignedBB(minX, .5625f, minZ, maxX, .8125f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{15, 16, 17};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{23};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 0;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 0;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return 0;
	}


	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return null;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return false;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 0;
	}

	@Override
	public int[] getOutputSlots()
	{
		return new int[0];
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntityDieselGenerator master = master();
		if(master!=null&&(pos==0||pos==2)&&(side==null||side.getAxis()==facing.rotateYCCW().getAxis()))
			return master.tanks;
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		if(resources==null)
			return false;
		return DieselHandler.isValidFuel(resources.getFluid());
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}


	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return null;
	}

	@Override
	public boolean canOpenGui()
	{
		return false;
	}

	@Override
	public int getGuiID()
	{
		return -1;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public boolean shoudlPlaySound(String sound)
	{
		return active;
	}

	@Override
	public BlockFaceShape getFaceShape(EnumFacing side)
	{
		if(pos==0||pos==2)
			return side.getAxis()==facing.rotateY().getAxis()?BlockFaceShape.SOLID: BlockFaceShape.UNDEFINED;
		else if(pos >= 15&&pos <= 17)
			return side==EnumFacing.UP?BlockFaceShape.SOLID: BlockFaceShape.UNDEFINED;
		else if(pos==23)
			return side.getAxis()==facing.rotateY().getAxis()?BlockFaceShape.SOLID: BlockFaceShape.UNDEFINED;
		return BlockFaceShape.UNDEFINED;
	}
}