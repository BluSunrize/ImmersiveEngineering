/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorAttachable;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class AutoWorkbenchBlockEntity extends PoweredMultiblockBlockEntity<AutoWorkbenchBlockEntity, MultiblockRecipe>
		implements IInteractionObjectIE<AutoWorkbenchBlockEntity>, IConveyorAttachable, IBlockBounds
{
	public static final int BLUEPRINT_SLOT = 0;
	private static final int FIRST_INPUT_SLOT = 1;
	private static final int NUM_INPUT_SLOTS = 16;
	public static final int NUM_SLOTS = 1+NUM_INPUT_SLOTS;
	public static final int ENERGY_CAPACITY = 32000;

	public final NonNullList<ItemStack> inventory = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	public int selectedRecipe = -1;

	public AutoWorkbenchBlockEntity(BlockEntityType<AutoWorkbenchBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.AUTO_WORKBENCH, ENERGY_CAPACITY, true, type, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		selectedRecipe = nbt.getInt("selectedRecipe");
		if(!descPacket)
			ContainerHelper.loadAllItems(nbt, inventory);
		else
			inventory.set(BLUEPRINT_SLOT, ItemStack.of(nbt.getCompound("syncedBlueprint")));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("selectedRecipe", selectedRecipe);
		if(!descPacket)
			ContainerHelper.saveAllItems(nbt, inventory);
		else
		{
			CompoundTag blueprintNBT = new CompoundTag();
			inventory.get(BLUEPRINT_SLOT).save(blueprintNBT);
			nbt.put("syncedBlueprint", blueprintNBT);
		}
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		if(isRSDisabled()||level.getGameTime()%16!=((getBlockPos().getX()^getBlockPos().getZ())&15)||inventory.get(BLUEPRINT_SLOT).isEmpty())
			return;

		BlueprintCraftingRecipe[] recipes = getAvailableRecipes();
		if(recipes.length > 0&&(this.selectedRecipe >= 0&&this.selectedRecipe < recipes.length))
		{
			BlueprintCraftingRecipe recipe = recipes[this.selectedRecipe];
			if(recipe!=null&&!recipe.output.get().isEmpty())
			{
				NonNullList<ItemStack> query = NonNullList.withSize(NUM_INPUT_SLOTS, ItemStack.EMPTY);
				for(int i = 0; i < query.size(); i++)
					query.set(i, inventory.get(i+FIRST_INPUT_SLOT));
				int crafted = recipe.getMaxCrafted(query);
				if(crafted > 0)
				{
					if(this.addProcessToQueue(new MultiblockProcessInWorld<>(recipe, this::getRecipeForId, 0.78f, NonNullList.create()), true))
					{
						this.addProcessToQueue(new MultiblockProcessInWorld<>(recipe, this::getRecipeForId, 0.78f, recipe.consumeInputs(query, 1)), false);
						for(int i = 0; i < query.size(); i++)
							inventory.set(i+FIRST_INPUT_SLOT, query.get(i));
						this.setChanged();
						this.markContainingBlockForUpdate(null);
					}
				}
			}
		}
	}

	public BlueprintCraftingRecipe[] getAvailableRecipes()
	{
		return EngineersBlueprintItem.getRecipes(level, inventory.get(BLUEPRINT_SLOT));
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		Set<BlockPos> highFullBlocks = ImmutableSet.of(
				new BlockPos(0, 1, 2),
				new BlockPos(0, 1, 1)
		);
		if(posInMultiblock.getY()==0||highFullBlocks.contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, 1, 1);
		Set<BlockPos> conveyors = ImmutableSet.of(
				new BlockPos(1, 1, 1),
				new BlockPos(2, 1, 1),
				new BlockPos(0, 1, 0),
				new BlockPos(1, 1, 0)
		);
		if(conveyors.contains(posInMultiblock))
			return Shapes.box(0, 0, 0, 1, .125f, 1);
		float xMin = 0;
		float yMin = 0;
		float zMin = 0;
		float xMax = 1;
		float yMax = 1;
		float zMax = 1;
		if(ImmutableSet.of(
				new BlockPos(1, 1, 2),
				new BlockPos(2, 1, 2)
		).contains(posInMultiblock))
		{
			//TODO more sensible name
			boolean is11 = new BlockPos(2, 1, 2).equals(posInMultiblock);
			yMax = .8125f;
			if(getFacing()==Direction.NORTH)
			{
				zMin = .1875f;
				if(is11)
					xMax = .875f;
			}
			else if(getFacing()==Direction.SOUTH)
			{
				zMax = .8125f;
				if(is11)
					xMin = .125f;
			}
			else if(getFacing()==Direction.WEST)
			{
				xMin = .1875f;
				if(is11)
					zMin = .125f;
			}
			else if(getFacing()==Direction.EAST)
			{
				xMax = .8125f;
				if(is11)
					zMax = .875f;
			}
		}
		if(new BlockPos(2, 1, 0).equals(posInMultiblock))
		{
			yMax = .3125f;
			if(getFacing()==Direction.NORTH)
			{
				zMin = .25f;
				xMax = .875f;
			}
			else if(getFacing()==Direction.SOUTH)
			{
				zMax = .75f;
				xMin = .125f;
			}
			else if(getFacing()==Direction.WEST)
			{
				xMin = .25f;
				zMin = .125f;
			}
			else if(getFacing()==Direction.EAST)
			{
				xMax = .75f;
				zMax = .875f;
			}
		}
		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(0, 1, 2, RelativeBlockFace.UP));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(new BlockPos(1, 0, 2));
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return true;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process)
	{
		return true;
	}

	private final CapabilityReference<IItemHandler> output = CapabilityReference.forBlockEntityAt(
			this, this::getOutputPos, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(this.output, output, false);
		if(!output.isEmpty())
		{
			DirectionalBlockPos outputPos = getOutputPos();
			Utils.dropStackAtPos(level, outputPos.position(), output, outputPos.side().getOpposite());
		}
	}

	private DirectionalBlockPos getOutputPos()
	{
		Direction outDir = getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise();
		return new DirectionalBlockPos(worldPosition.relative(outDir, 2), outDir.getOpposite());
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 3;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 3;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process)
	{
		return .4375f;
	}


	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
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
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return null;
	}

	@Override
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, AutoWorkbenchBlockEntity::master,
			registerCapability(new IEInventoryHandler(16, this, 1, true, false))
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(new BlockPos(0, 1, 2).equals(posInMultiblock)&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionHandler.getAndCast();
		return super.getCapability(capability, facing);
	}

	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected BlueprintCraftingRecipe getRecipeForId(Level level, ResourceLocation id)
	{
		return BlueprintCraftingRecipe.RECIPES.getById(level, id);
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public AutoWorkbenchBlockEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public BEContainer<AutoWorkbenchBlockEntity, ?> getContainerType()
	{
		return IEContainerTypes.AUTO_WORKBENCH;
	}

	@Override
	public Direction[] sigOutputDirections()
	{
		if(new BlockPos(1, 1, 2).equals(posInMultiblock))
			return new Direction[]{this.getFacing().getClockWise()};
		return new Direction[0];
	}
}
