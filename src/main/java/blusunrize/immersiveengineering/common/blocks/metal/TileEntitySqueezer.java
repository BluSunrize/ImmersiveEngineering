/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPoweredMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockSqueezer;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TileEntitySqueezer extends TileEntityPoweredMultiblock<TileEntitySqueezer, SqueezerRecipe> implements
		IAdvancedSelectionBounds, IAdvancedCollisionBounds, IGuiTile
{
	public static TileEntityType<TileEntitySqueezer> TYPE;
	
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(11, ItemStack.EMPTY);
	public float animation_piston = 0;
	public boolean animation_down = true;
	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntity(this, this::getOutputPos,
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	public TileEntitySqueezer()
	{
		super(MultiblockSqueezer.instance, 16000, true, TYPE);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 11);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		NBTTagCompound tankTag = tanks[0].writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
		if(!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void tick()
	{
		super.tick();
		if(isDummy()||isRSDisabled())
			return;

		if(world.isRemote)
		{
			if(this.processQueue.isEmpty()&&animation_piston < .6875)
				animation_piston = Math.min(.6875f, animation_piston+.03125f);
			else if(shouldRenderAsActive())
			{
				if(animation_down)
					animation_piston = Math.max(0, animation_piston-.03125f);
				else
					animation_piston = Math.min(.6875f, animation_piston+.03125f);
				if(animation_piston <= 0&&animation_down)
					animation_down = false;
				else if(animation_piston >= .6875&&!animation_down)
					animation_down = true;
			}
		}
		else
		{
			boolean update = false;
			if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
			{
				final int[] usedInvSlots = new int[8];
				for(MultiblockProcess process : processQueue)
					if(process instanceof MultiblockProcessInMachine)
						for(int i : ((MultiblockProcessInMachine)process).getInputSlots())
							usedInvSlots[i]++;

				Integer[] preferredSlots = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
				Arrays.sort(preferredSlots, 0, 8, Comparator.comparingInt(arg0 -> usedInvSlots[arg0]));
				for(int slot : preferredSlots)
				{
					ItemStack stack = this.getInventory().get(slot);
					if(!stack.isEmpty())
					{
						stack = stack.copy();
						stack.shrink(usedInvSlots[slot]);
					}
					if(!stack.isEmpty()&&stack.getCount() > 0)
					{
						SqueezerRecipe recipe = this.findRecipeForInsertion(stack);
						if(recipe!=null)
						{
							MultiblockProcessInMachine<SqueezerRecipe> process = new MultiblockProcessInMachine<>(recipe, slot);
							if(this.addProcessToQueue(process, true))
							{
								this.addProcessToQueue(process, false);
								update = true;
							}
						}
					}
				}
			}

			EnumFacing fw = mirrored?facing.rotateYCCW(): facing.rotateY();
			if(this.tanks[0].getFluidAmount() > 0)
			{
				FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[0].getFluid(), Math.min(this.tanks[0].getFluidAmount(), 80), false);
				BlockPos outputPos = this.getPos().add(0, -1, 0).offset(fw, 2);
				update |= FluidUtil.getFluidHandler(world, outputPos, fw.getOpposite()).map(output -> {
					int accepted = output.fill(out, false);
					if(accepted > 0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
						this.tanks[0].drain(drained, true);
						return true;
					}
					return false;
				}).orElse(false);
				ItemStack empty = getInventory().get(9);
				if(!empty.isEmpty()&&tanks[0].getFluidAmount() > 0)
				{
					ItemStack full = Utils.fillFluidContainer(tanks[0], empty, getInventory().get(10), null);
					if(!full.isEmpty())
					{
						if(getInventory().get(9).getCount()==1&&!Utils.isFluidContainerFull(full))
							getInventory().set(9, full.copy());
						else
						{
							if(!getInventory().get(10).isEmpty()&&ItemStack.areItemStacksEqual(full, getInventory().get(10)))
								getInventory().get(10).grow(full.getCount());
							else
								getInventory().set(10, full);
							inventory.get(9).shrink(1);
							if(inventory.get(9).getCount() <= 0)
								inventory.set(9, ItemStack.EMPTY);
						}
						update = true;
					}
				}
			}
			if(!inventory.get(8).isEmpty()&&world.getGameTime()%8==0)
			{
				BlockPos outputPos = this.getPos();
				TileEntity outputTile = Utils.getExistingTileEntity(world, outputPos);
				if(outputTile!=null)
				{
					ItemStack stack = Utils.copyStackWithAmount(inventory.get(8), 1);
					stack = Utils.insertStackIntoInventory(outputCap, stack, false);
					if(stack.isEmpty())
					{
						this.inventory.get(8).shrink(1);
						if(this.inventory.get(8).getCount() <= 0)
							this.inventory.set(8, ItemStack.EMPTY);
					}
				}
			}

			if(update)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		}
	}

	private DirectionalBlockPos getOutputPos()
	{
		EnumFacing fw = mirrored?facing.rotateYCCW(): facing.rotateY();
		return new DirectionalBlockPos(pos.offset(fw), fw.getOpposite());
	}

	@Override
	public float[] getBlockBounds()
	{
		if(posInMultiblock > 0&&posInMultiblock < 9&&posInMultiblock!=5)
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(posInMultiblock==11)
			return new float[]{facing==EnumFacing.WEST?.5f: 0, 0, facing==EnumFacing.NORTH?.5f: 0, facing==EnumFacing.EAST?.5f: 1, 1, facing==EnumFacing.SOUTH?.5f: 1};


		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored)
			fw = fw.getOpposite();
		if(posInMultiblock==2)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			float minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .125f;
			float maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .25f;
			float minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .125f;
			float maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .25f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==EnumFacing.WEST?.625f: fl==EnumFacing.EAST?.125f: .75f;
			maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.875f: .875f;
			minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: .75f;
			maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.875f: .875f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(posInMultiblock==3||posInMultiblock==4||posInMultiblock==6||posInMultiblock==7)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock > 5)
				fl = fl.getOpposite();
			if(posInMultiblock%3==1)
				fw = fw.getOpposite();
			float minX = fl==EnumFacing.WEST?.6875f: fl==EnumFacing.EAST?.0625f: fw==EnumFacing.EAST?.0625f: .6875f;
			float maxX = fl==EnumFacing.EAST?.3125f: fl==EnumFacing.WEST?.9375f: fw==EnumFacing.EAST?.3125f: .9375f;
			float minZ = fl==EnumFacing.NORTH?.6875f: fl==EnumFacing.SOUTH?.0625f: fw==EnumFacing.SOUTH?.0625f: .6875f;
			float maxZ = fl==EnumFacing.SOUTH?.3125f: fl==EnumFacing.NORTH?.9375f: fw==EnumFacing.SOUTH?.3125f: .9375f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(posInMultiblock==4)
			{
				minX = fl==EnumFacing.WEST?.375f: fl==EnumFacing.EAST?.625f: fw==EnumFacing.WEST?-.125f: 0;
				maxX = fl==EnumFacing.EAST?.375f: fl==EnumFacing.WEST?.625f: fw==EnumFacing.EAST?1.125f: 1;
				minZ = fl==EnumFacing.NORTH?.375f: fl==EnumFacing.SOUTH?.625f: fw==EnumFacing.NORTH?-.125f: 0;
				maxZ = fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.NORTH?.625f: fw==EnumFacing.SOUTH?1.125f: 1;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==EnumFacing.WEST?-.125f: fl==EnumFacing.EAST?.625f: fw==EnumFacing.WEST?-.125f: .875f;
				maxX = fl==EnumFacing.EAST?1.125f: fl==EnumFacing.WEST?.375f: fw==EnumFacing.EAST?1.125f: .125f;
				minZ = fl==EnumFacing.NORTH?-.125f: fl==EnumFacing.SOUTH?.625f: fw==EnumFacing.NORTH?-.125f: .875f;
				maxZ = fl==EnumFacing.SOUTH?1.25f: fl==EnumFacing.NORTH?.375f: fw==EnumFacing.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, .75f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==EnumFacing.WEST?-.125f: fl==EnumFacing.EAST?.875f: fw==EnumFacing.WEST?-.125f: .875f;
				maxX = fl==EnumFacing.EAST?1.125f: fl==EnumFacing.WEST?.125f: fw==EnumFacing.EAST?1.125f: .125f;
				minZ = fl==EnumFacing.NORTH?-.125f: fl==EnumFacing.SOUTH?.875f: fw==EnumFacing.NORTH?-.125f: .875f;
				maxZ = fl==EnumFacing.SOUTH?1.25f: fl==EnumFacing.NORTH?.125f: fw==EnumFacing.SOUTH?1.125f: .125f;
				list.add(new AxisAlignedBB(minX, .75f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}

			return list;
		}
		if((posInMultiblock==12||posInMultiblock==13||posInMultiblock==15||posInMultiblock==16)||(posInMultiblock==21||posInMultiblock==22||posInMultiblock==24||posInMultiblock==25))
		{
			List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>(2);
			if(posInMultiblock < 18)
				list.add(new AxisAlignedBB(0, 0, 0, 1, .125f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock%9 > 5)
				fl = fl.getOpposite();
			if(posInMultiblock%3==1)
				fw = fw.getOpposite();
			float minY = posInMultiblock < 18?.125f: -.875f;
			float maxY = posInMultiblock < 18?1.125f: .125f;

			float minX = fl==EnumFacing.WEST?.84375f: fl==EnumFacing.EAST?0f: fw==EnumFacing.EAST?0f: .84375f;
			float maxX = fl==EnumFacing.EAST?.15625f: fl==EnumFacing.WEST?1f: fw==EnumFacing.EAST?.15625f: 1;
			float minZ = fl==EnumFacing.NORTH?.84375f: fl==EnumFacing.SOUTH?0f: fw==EnumFacing.SOUTH?0f: .84375f;
			float maxZ = fl==EnumFacing.SOUTH?.15625f: fl==EnumFacing.NORTH?1f: fw==EnumFacing.SOUTH?.15625f: 1;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==EnumFacing.WEST?0f: fl==EnumFacing.EAST?.15625f: fw==EnumFacing.EAST?.0625f: .8125f;
			maxX = fl==EnumFacing.EAST?1f: fl==EnumFacing.WEST?.84375f: fw==EnumFacing.EAST?.1875f: .9375f;
			minZ = fl==EnumFacing.NORTH?0f: fl==EnumFacing.SOUTH?.15625f: fw==EnumFacing.SOUTH?.0625f: .8125f;
			maxZ = fl==EnumFacing.SOUTH?1f: fl==EnumFacing.NORTH?.84375f: fw==EnumFacing.SOUTH?.1875f: .9375f;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			minX = fl==EnumFacing.WEST?.8125f: fl==EnumFacing.EAST?.0625f: fw==EnumFacing.EAST?.15625f: 0f;
			maxX = fl==EnumFacing.EAST?.1875f: fl==EnumFacing.WEST?.9375f: fw==EnumFacing.EAST?1f: .84375f;
			minZ = fl==EnumFacing.NORTH?.8125f: fl==EnumFacing.SOUTH?.0625f: fw==EnumFacing.SOUTH?.15625f: 0f;
			maxZ = fl==EnumFacing.SOUTH?.1875f: fl==EnumFacing.NORTH?.9375f: fw==EnumFacing.SOUTH?1f: .84375f;
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(posInMultiblock > 18)
			{
				minX = fl==EnumFacing.WEST?-.25f: fl==EnumFacing.EAST?1.25f: fw==EnumFacing.EAST?.75f: -.25f;
				maxX = fl==EnumFacing.EAST?.75f: fl==EnumFacing.WEST?.25f: fw==EnumFacing.EAST?1.25f: .25f;
				minZ = fl==EnumFacing.NORTH?-.25f: fl==EnumFacing.SOUTH?1.25f: fw==EnumFacing.SOUTH?.75f: -.25f;
				maxZ = fl==EnumFacing.SOUTH?.75f: fl==EnumFacing.NORTH?.25f: fw==EnumFacing.SOUTH?1.25f: .25f;
				list.add(new AxisAlignedBB(minX, .375f, minZ, maxX, .9375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
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
		return new int[]{9};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{11};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<SqueezerRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(world, getOutputPos(), output);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<SqueezerRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 8;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 8;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<SqueezerRecipe> process)
	{
		return 0;
	}


	@Override
	public int getComparatedSize()
	{
		return 8;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return new int[]{8};
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[]{0};
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		TileEntitySqueezer master = master();
		if(master!=null&&posInMultiblock==5&&(side==null||side==(mirrored?facing.rotateYCCW(): facing.rotateY())))
			return master.tanks;
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return true;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new IEInventoryHandler(8, this, 0, new boolean[]{true, true, true, true, true, true, true, true}, new boolean[8])
	);
	private LazyOptional<IItemHandler> extractionHandler = registerConstantCap(
			new IEInventoryHandler(1, this, 8, new boolean[1], new boolean[]{true})
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		if((posInMultiblock==15||posInMultiblock==13)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntitySqueezer master = master();
			if(master==null)
				return LazyOptional.empty();
			if(posInMultiblock==15)
				return master.insertionHandler.cast();
			if(posInMultiblock==13)
				return master.extractionHandler.cast();
			return LazyOptional.empty();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public SqueezerRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return SqueezerRecipe.findRecipe(inserting);
	}

	@Override
	protected SqueezerRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return SqueezerRecipe.loadFromNBT(tag);
	}

	@Override
	public boolean canOpenGui()
	{
		return formed;
	}

	@Override
	public int getGuiID()
	{
		return Lib.GUIID_Squeezer;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}
}