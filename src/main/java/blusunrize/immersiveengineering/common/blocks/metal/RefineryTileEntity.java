/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RefineryTileEntity extends PoweredMultiblockTileEntity<RefineryTileEntity, RefineryRecipe> implements
		IAdvancedSelectionBounds, IAdvancedCollisionBounds, IInteractionObjectIE
{
	public static TileEntityType<RefineryTileEntity> TYPE;

	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000), new FluidTank(24000), new FluidTank(24000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(6, ItemStack.EMPTY);

	public RefineryTileEntity()
	{
		super(IEMultiblocks.REFINERY, 16000, true, TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		tanks[1].readFromNBT(nbt.getCompound("tank1"));
		tanks[2].readFromNBT(nbt.getCompound("tank2"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 6);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[0].writeToNBT(new CompoundNBT()));
		nbt.put("tank1", tanks[1].writeToNBT(new CompoundNBT()));
		nbt.put("tank2", tanks[2].writeToNBT(new CompoundNBT()));
		if(!descPacket)
			nbt.put("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void tick()
	{
		super.tick();
		if(world.isRemote||isDummy())
			return;

		boolean update = false;
		if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
		{
			if(tanks[0].getFluidAmount() > 0||tanks[1].getFluidAmount() > 0)
			{
				RefineryRecipe recipe = RefineryRecipe.findRecipe(tanks[0].getFluid(), tanks[1].getFluid());
				if(recipe!=null)
				{
					MultiblockProcessInMachine<RefineryRecipe> process = new MultiblockProcessInMachine<>(recipe)
							.setInputTanks((tanks[0].getFluidAmount() > 0&&tanks[1].getFluidAmount() > 0)?new int[]{0, 1}: tanks[0].getFluidAmount() > 0?new int[]{0}: new int[]{1});
					if(this.addProcessToQueue(process, true))
					{
						this.addProcessToQueue(process, false);
						update = true;
					}
				}
			}
		}

		if(this.tanks[2].getFluidAmount() > 0)
		{
			ItemStack filledContainer = Utils.fillFluidContainer(tanks[2], inventory.get(4), inventory.get(5), null);
			if(!filledContainer.isEmpty())
			{
				if(inventory.get(4).getCount()==1&&!Utils.isFluidContainerFull(filledContainer))
					inventory.set(4, filledContainer.copy());
				else
				{
					if(!inventory.get(5).isEmpty()&&ItemHandlerHelper.canItemStacksStack(inventory.get(5), filledContainer))
						inventory.get(5).grow(filledContainer.getCount());
					else if(inventory.get(5).isEmpty())
						inventory.set(5, filledContainer.copy());
					inventory.get(4).shrink(1);
					if(inventory.get(4).getCount() <= 0)
						inventory.set(4, ItemStack.EMPTY);
				}
				update = true;
			}
			if(this.tanks[2].getFluidAmount() > 0)
			{
				FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[2].getFluid(), Math.min(this.tanks[2].getFluidAmount(), 80), false);
				BlockPos outputPos = this.getPos().add(0, -1, 0).offset(getFacing().getOpposite());
				update |= FluidUtil.getFluidHandler(world, outputPos, getFacing()).map(output -> {
					int accepted = output.fill(out, FluidAction.SIMULATE);
					if(accepted > 0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
						this.tanks[2].drain(drained, FluidAction.EXECUTE);
						return true;
					}
					return false;
				}).orElse(false);
			}
		}

		int amount_prev = tanks[0].getFluidAmount();
		ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
		if(amount_prev!=tanks[0].getFluidAmount())
		{
			if(!inventory.get(1).isEmpty()&&ItemHandlerHelper.canItemStacksStack(inventory.get(1), emptyContainer))
				inventory.get(1).grow(emptyContainer.getCount());
			else if(inventory.get(1).isEmpty())
				inventory.set(1, emptyContainer.copy());
			inventory.get(0).shrink(1);
			if(inventory.get(0).getCount() <= 0)
				inventory.set(0, ItemStack.EMPTY);
			update = true;
		}
		amount_prev = tanks[1].getFluidAmount();
		emptyContainer = Utils.drainFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
		if(amount_prev!=tanks[1].getFluidAmount())
		{
			if(!inventory.get(3).isEmpty()&&ItemHandlerHelper.canItemStacksStack(inventory.get(3), emptyContainer))
				inventory.get(3).grow(emptyContainer.getCount());
			else if(inventory.get(3).isEmpty())
				inventory.set(3, emptyContainer.copy());
			inventory.get(2).shrink(1);
			if(inventory.get(2).getCount() <= 0)
				inventory.set(2, ItemStack.EMPTY);
			update = true;
		}

		if(update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		if(ImmutableSet.of(
				new BlockPos(0, 0, 0),
				new BlockPos(0, 0, 1),
				new BlockPos(0, 0, 3)
		).contains(posInMultiblock))
			return new float[]{0, 0, 0, 1, .5f, 1};
		if(new BlockPos(0, 1, 4).equals(posInMultiblock))
			return new float[]{getFacing()==Direction.WEST?.5f: 0, 0, getFacing()==Direction.NORTH?.5f: 0, getFacing()==Direction.EAST?.5f: 1, 1, getFacing()==Direction.SOUTH?.5f: 1};
		if(new BlockPos(0, 1, 2).equals(posInMultiblock))
			return new float[]{.0625f, 0, .0625f, .9375f, 1, .9375f};

		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		Direction fl = getFacing();
		Direction fw = getFacing().rotateY();
		if(getIsMirrored())
			fw = fw.getOpposite();
		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getZ()%4==0)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .5f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock.getX()==2)
				fl = fl.getOpposite();
			if(posInMultiblock.getZ()==0)
				fw = fw.getOpposite();

			float minX = fl==Direction.WEST?0: fl==Direction.EAST?.75f: fw==Direction.WEST?.5f: .25f;
			float maxX = fl==Direction.EAST?1: fl==Direction.WEST?.25f: fw==Direction.EAST?.5f: .75f;
			float minZ = fl==Direction.NORTH?0: fl==Direction.SOUTH?.75f: fw==Direction.NORTH?.5f: .25f;
			float maxZ = fl==Direction.SOUTH?1: fl==Direction.NORTH?.25f: fw==Direction.SOUTH?.5f: .75f;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1.375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			if(new BlockPos(0, 0, 4).equals(posInMultiblock))
			{
				minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .125f;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .25f;
				minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .125f;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .25f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

				minX = fl==Direction.WEST?.625f: fl==Direction.EAST?.125f: .75f;
				maxX = fl==Direction.EAST?.375f: fl==Direction.WEST?.875f: .875f;
				minZ = fl==Direction.NORTH?.625f: fl==Direction.SOUTH?.125f: .75f;
				maxZ = fl==Direction.SOUTH?.375f: fl==Direction.NORTH?.875f: .875f;
				list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}

			return list;
		}
		if(posInMultiblock.getX()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getZ()%2==1)
		{
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, 0, 0, 1, .0f, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(posInMultiblock.getX()==2)
				fl = fl.getOpposite();
			if(posInMultiblock.getZ()==1)
				fw = fw.getOpposite();

			float minX = fl==Direction.WEST?0: fl==Direction.EAST?.75f: fw==Direction.WEST?.75f: 0;
			float maxX = fl==Direction.EAST?1: fl==Direction.WEST?.25f: fw==Direction.EAST?.25f: 1;
			float minZ = fl==Direction.NORTH?0: fl==Direction.SOUTH?.75f: fw==Direction.NORTH?.75f: 0;
			float maxZ = fl==Direction.SOUTH?1: fl==Direction.NORTH?.25f: fw==Direction.SOUTH?.25f: 1;
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1.375f, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}

		if(posInMultiblock.getX() > 0&&posInMultiblock.getY() > 0&&posInMultiblock.getZ()%4==0)
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(posInMultiblock.getZ()==4)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?-.25f: fl==Direction.EAST?-.25f: fw==Direction.WEST?-1f: .5f;
			float maxX = fl==Direction.EAST?1.25f: fl==Direction.WEST?1.25f: fw==Direction.EAST?2: .5f;
			float minZ = fl==Direction.NORTH?-.25f: fl==Direction.SOUTH?-.25f: fw==Direction.NORTH?-1f: .5f;
			float maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?1.25f: fw==Direction.SOUTH?2: .5f;
			float minY = posInMultiblock.getY()==1?.5f: -.5f;
			float maxY = posInMultiblock.getY()==1?2f: 1f;
			if(posInMultiblock.getX()==2)
			{
				minX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				maxX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				minZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
				maxZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(posInMultiblock.getX() > 0&&posInMultiblock.getY() > 0&&posInMultiblock.getZ()%2==1)
		{
			List<AxisAlignedBB> list = Lists.newArrayList();
			if(posInMultiblock.getZ()==3)
				fw = fw.getOpposite();
			float minX = fl==Direction.WEST?-.25f: fl==Direction.EAST?-.25f: fw==Direction.WEST?0f: -.5f;
			float maxX = fl==Direction.EAST?1.25f: fl==Direction.WEST?1.25f: fw==Direction.EAST?1f: 1.5f;
			float minZ = fl==Direction.NORTH?-.25f: fl==Direction.SOUTH?-.25f: fw==Direction.NORTH?0: -.5f;
			float maxZ = fl==Direction.SOUTH?1.25f: fl==Direction.NORTH?1.25f: fw==Direction.SOUTH?1f: 1.5f;
			float minY = posInMultiblock.getY()==1?.5f: -.5f;
			float maxY = posInMultiblock.getY()==1?2f: 1f;
			if(posInMultiblock.getX()==2)
			{
				minX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				maxX += fl==Direction.WEST?1: fl==Direction.EAST?-1: 0;
				minZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
				maxZ += fl==Direction.NORTH?1: fl==Direction.SOUTH?-1: 0;
			}
			list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, PlayerEntity player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		return getAdvancedSelectionBounds();
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 2)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 4)
		);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<RefineryRecipe> process)
	{
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<RefineryRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<RefineryRecipe> process)
	{
		return 0;
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
		return new int[0];
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[]{2};
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return tanks;
	}


	private static final BlockPos outputOffset = new BlockPos(0, 0, 2);
	private static final Set<BlockPos> inputOffsets = ImmutableSet.of(
			new BlockPos(1, 0, 0),
			new BlockPos(1, 0, 4)
	);

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		RefineryTileEntity master = this.master();
		if(master!=null)
		{
			if(outputOffset.equals(posInMultiblock)&&(side==null||side==getFacing().getOpposite()))
				return new FluidTank[]{master.tanks[2]};
			if(inputOffsets.contains(posInMultiblock)&&(side==null||side.getAxis()==getFacing().rotateYCCW().getAxis()))
				return new FluidTank[]{master.tanks[0], master.tanks[1]};
		}
		return tanks;
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		if(inputOffsets.contains(posInMultiblock)&&(side==null||side.getAxis()==getFacing().rotateYCCW().getAxis()))
		{
			RefineryTileEntity master = this.master();
			if(master==null||master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity())
				return false;
			if(master.tanks[0].getFluid()==null&&master.tanks[1].getFluid()==null)
			{
				List<RefineryRecipe> incompleteRecipes = RefineryRecipe.findIncompleteRefineryRecipe(resource, null);
				return incompleteRecipes!=null&&!incompleteRecipes.isEmpty();
			}
			else
			{
				List<RefineryRecipe> incompleteRecipes = RefineryRecipe.findIncompleteRefineryRecipe(resource, master.tanks[iTank==0?1: 0].getFluid());
				return incompleteRecipes!=null&&!incompleteRecipes.isEmpty();
			}
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return outputOffset.equals(posInMultiblock)&&(side==null||side==getFacing().getOpposite());
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public RefineryRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected RefineryRecipe readRecipeFromNBT(CompoundNBT tag)
	{
		return RefineryRecipe.loadFromNBT(tag);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return formed;
	}

	@Override
	public IInteractionObjectIE getGuiMaster()
	{
		return master();
	}
}