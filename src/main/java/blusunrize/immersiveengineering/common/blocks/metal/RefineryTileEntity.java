/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.TileContainer;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RefineryTileEntity extends PoweredMultiblockTileEntity<RefineryTileEntity, RefineryRecipe> implements
		ISelectionBounds, ICollisionBounds, IInteractionObjectIE<RefineryTileEntity>, IBlockBounds
{
	public static final int OUTPUT_EMPTY = 4;
	public static final int OUTPUT_FILLED = 5;
	public FluidTank[] tanks = new FluidTank[]{
			new FluidTank(24*FluidAttributes.BUCKET_VOLUME),
			new FluidTank(24*FluidAttributes.BUCKET_VOLUME),
			new FluidTank(24*FluidAttributes.BUCKET_VOLUME)
	};
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(6, ItemStack.EMPTY);

	public RefineryTileEntity(BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.REFINERY, 16000, true, IETileTypes.REFINERY.get(), pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompound("tank0"));
		tanks[1].readFromNBT(nbt.getCompound("tank1"));
		tanks[2].readFromNBT(nbt.getCompound("tank2"));
		if(!descPacket)
			ContainerHelper.loadAllItems(nbt, inventory);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("tank0", tanks[0].writeToNBT(new CompoundTag()));
		nbt.put("tank1", tanks[1].writeToNBT(new CompoundTag()));
		nbt.put("tank2", tanks[2].writeToNBT(new CompoundTag()));
		if(!descPacket)
			ContainerHelper.saveAllItems(nbt, inventory);
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
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
				BlockPos outputPos = this.getBlockPos().offset(0, -1, 0).relative(getFacing().getOpposite());
				update |= FluidUtil.getFluidHandler(level, outputPos, getFacing()).map(output -> {
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

		for(int tank = 0; tank < 2; tank++)
		{
			final int inputSlot = 2*tank;
			final int outputSlot = inputSlot+1;
			final int amountPrev = tanks[tank].getFluidAmount();
			ItemStack outputStack = inventory.get(outputSlot);
			ItemStack emptyContainer = Utils.drainFluidContainer(tanks[tank], inventory.get(inputSlot), outputStack);
			if(amountPrev!=tanks[tank].getFluidAmount())
			{
				if(ItemHandlerHelper.canItemStacksStack(outputStack, emptyContainer))
					outputStack.grow(emptyContainer.getCount());
				else if(outputStack.isEmpty())
					inventory.set(outputSlot, emptyContainer.copy());
				inventory.get(inputSlot).shrink(outputSlot);
				update = true;
			}
		}

		if(update)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(RefineryTileEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return getShape(SHAPES);
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		if(posInMultiblock.getZ()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getX()%4==0)
		{
			List<AABB> list = Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==0,
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0.25, .5f, 0, 0.5, 1.375f, 0.25)
			);
			if(new BlockPos(4, 0, 2).equals(posInMultiblock))
			{
				list.add(new AABB(0.125, .5f, 0.625, 0.25, 1, 0.875));
				list.add(new AABB(0.75, .5f, 0.625, 0.875, 1, 0.875));
			}
			return list;
		}
		if(posInMultiblock.getZ()%2==0&&posInMultiblock.getY()==0&&posInMultiblock.getX()%2==1)
			return Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==1,
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0, .5f, 0, 0.25, 1.375f, 0.25)
			);

		if(posInMultiblock.getZ() < 2&&posInMultiblock.getY() > 0&&posInMultiblock.getX()%4==0)
		{
			float minZ = -.25f;
			float maxZ = 1.25f;
			float minY = posInMultiblock.getY()==1?.5f: -.5f;
			float maxY = posInMultiblock.getY()==1?2f: 1f;
			if(posInMultiblock.getZ()==0)
			{
				minZ += 1;
				maxZ += 1;
			}
			return Utils.flipBoxes(false, posInMultiblock.getX()==4,
					new AABB(0.5, minY, minZ, 2, maxY, maxZ)
			);
		}
		if(posInMultiblock.getZ() < 2&&posInMultiblock.getY() > 0&&posInMultiblock.getX()%2==1)
		{
			float minZ = -.25f;
			float maxZ = 1.25f;
			float minY = posInMultiblock.getY()==1?.5f: -.5f;
			float maxY = posInMultiblock.getY()==1?2f: 1f;
			if(posInMultiblock.getZ()==0)
			{
				minZ += 1;
				maxZ += 1;
			}
			return Utils.flipBoxes(false, posInMultiblock.getX()==3,
					new AABB(-0.5, minY, minZ, 1, maxY, maxZ)
			);
		}
		else if(ImmutableSet.of(
				new BlockPos(0, 0, 2),
				new BlockPos(1, 0, 2),
				new BlockPos(3, 0, 2)
		).contains(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, 0, 1, .5f, 1));
		else if(new BlockPos(4, 1, 2).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, 0.5, 1, 1, 1));
		else if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return ImmutableList.of(new AABB(.0625f, 0, .0625f, .9375f, 1, .9375f));
		else
			return ImmutableList.of(new AABB(0, 0, 0, 1, 1, 1));
	}

	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 0)
		);
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(4, 1, 2)
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


	private static final BlockPos outputOffset = new BlockPos(2, 0, 2);
	private static final Set<BlockPos> inputOffsets = ImmutableSet.of(
			new BlockPos(0, 0, 1),
			new BlockPos(4, 0, 1)
	);

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		RefineryTileEntity master = this.master();
		if(master!=null)
		{
			if(outputOffset.equals(posInMultiblock)&&(side==null||side==getFacing().getOpposite()))
				return new FluidTank[]{master.tanks[2]};
			if(inputOffsets.contains(posInMultiblock)&&(side==null||side.getAxis()==getFacing().getCounterClockWise().getAxis()))
				return new FluidTank[]{master.tanks[0], master.tanks[1]};
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		if(inputOffsets.contains(posInMultiblock)&&(side==null||side.getAxis()==getFacing().getCounterClockWise().getAxis()))
		{
			RefineryTileEntity master = this.master();
			if(master==null||master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity())
				return false;
			if(master.tanks[0].getFluid().isEmpty()&&master.tanks[1].getFluid().isEmpty())
			{
				Optional<RefineryRecipe> incompleteRecipes = RefineryRecipe.findIncompleteRefineryRecipe(resource, FluidStack.EMPTY);
				return incompleteRecipes.isPresent();
			}
			else
			{
				FluidStack otherFluid = master.tanks[iTank==0?1: 0].getFluid();
				Optional<RefineryRecipe> incompleteRecipes = RefineryRecipe.findIncompleteRefineryRecipe(resource, otherFluid);
				return incompleteRecipes.isPresent();
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
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public RefineryRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected RefineryRecipe getRecipeForId(ResourceLocation id)
	{
		return RefineryRecipe.recipeList.get(id);
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public RefineryTileEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public TileContainer<RefineryTileEntity, ?> getContainerType()
	{
		return IEContainerTypes.REFINERY;
	}
}