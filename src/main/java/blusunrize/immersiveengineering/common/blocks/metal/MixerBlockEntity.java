/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.client.fx.FluidSplashOptions;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISoundBE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.register.IEParticles;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
import blusunrize.immersiveengineering.common.util.orientation.RelativeBlockFace;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

public class MixerBlockEntity extends PoweredMultiblockBlockEntity<MixerBlockEntity, MixerRecipe> implements
		IInteractionObjectIE<MixerBlockEntity>, IBlockBounds, IEClientTickableBE, ISoundBE
{
	public final MultiFluidTank tank = new MultiFluidTank(8*FluidAttributes.BUCKET_VOLUME);
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(8, ItemStack.EMPTY);
	public float animation_agitator = 0;
	public boolean outputAll;

	public MixerBlockEntity(BlockEntityType<MixerBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.MIXER, 16000, true, type, pos, state);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tank.readFromNBT(nbt.getCompound("tank"));
		if(!descPacket)
			ContainerHelper.loadAllItems(nbt, inventory);
		outputAll = nbt.getBoolean("outputAll");
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		CompoundTag tankTag = tank.writeToNBT(new CompoundTag());
		nbt.put("tank", tankTag);
		if(!descPacket)
			ContainerHelper.saveAllItems(nbt, inventory);
		nbt.putBoolean("outputAll", outputAll);
	}

	@Override
	public void receiveMessageFromClient(CompoundTag message)
	{
		super.receiveMessageFromClient(message);
		if(message.contains("outputAll", Tag.TAG_BYTE))
			outputAll = message.getBoolean("outputAll");
	}

	@Override
	public boolean canTickAny()
	{
		return super.canTickAny() && !isRSDisabled();
	}

	@Override
	public void tickClient()
	{
		if(shouldRenderAsActive())
		{
			if(Utils.RAND.nextInt(8)==0&&!tank.fluids.isEmpty())
			{
				FluidStack fs = tank.fluids.get(0);
				float amount = tank.getFluidAmount()/(float)tank.getCapacity()*1.125f;
				Vec3 partPos = new Vec3(getBlockPos().getX()+.5f+getFacing().getStepX()*.5f+(getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise()).getStepX()*.5f, getBlockPos().getY()-.0625f+amount, getBlockPos().getZ()+.5f+getFacing().getStepZ()*.5f+(getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise()).getStepZ()*.5f);
				float r = Utils.RAND.nextFloat()*.8125f;
				float angleRad = (float)Math.toRadians(animation_agitator);
				partPos = partPos.add(r*Math.cos(angleRad), 0, r*Math.sin(angleRad));
				if(Utils.RAND.nextBoolean())
					level.addParticle(IEParticles.IE_BUBBLE.get(), partPos.x, partPos.y, partPos.z, 0, 0, 0);
				else
					level.addParticle(new FluidSplashOptions(fs.getFluid()), partPos.x, partPos.y, partPos.z, 0, 0, 0);
			}
			animation_agitator = (animation_agitator+9)%360;
			ImmersiveEngineering.proxy.handleTileSound(IESounds.mixer, this, shouldRenderAsActive(), 0.075f, 1f);
		}
	}

	@Override
	public void tickServer()
	{
		super.tickServer();
		boolean update = false;
		boolean foundRecipe = false;
		if(energyStorage.getEnergyStored() > 0&&processQueue.size() < this.getProcessQueueMaxLength())
		{
			int tankAmount = tank.getFluidAmount();
			if(tankAmount > 0)
			{
				Set<Integer> usedInvSlots = new HashSet<>();
				for(MultiblockProcess<MixerRecipe> process : processQueue)
					if(process instanceof MultiblockProcessInMachine)
						for(int i : ((MultiblockProcessInMachine<MixerRecipe>)process).getInputSlots())
							usedInvSlots.add(i);
				NonNullList<ItemStack> components = NonNullList.withSize(this.inventory.size(), ItemStack.EMPTY);
				for(int i = 0; i < components.size(); i++)
					if(!usedInvSlots.contains(i))
						components.set(i, inventory.get(i));

				for(FluidStack fs : tank.fluids)
				{
					MixerRecipe recipe = MixerRecipe.findRecipe(level, fs, components);
					if(recipe!=null)
					{
						foundRecipe = true;
						MultiblockProcessInMachine<MixerRecipe> process = new MultiblockProcessMixer(recipe, this::getRecipeForId, recipe.getUsedSlots(fs, components)).setInputTanks(0);
						if(this.addProcessToQueue(process, true))
						{
							this.addProcessToQueue(process, false);
							update = true;
						}
					}
				}
			}
		}

		int fluidTypes = this.tank.getFluidTypes();
		if(fluidTypes>0 &&(fluidTypes> 1||!foundRecipe||outputAll))
		{
			BlockPos outputPos = this.getBlockPos().below().relative(getFacing().getOpposite(), 2);
			update |= FluidUtil.getFluidHandler(level, outputPos, getFacing()).map(output ->
			{
				boolean ret = false;
				if(!outputAll)
				{
					FluidStack inTank = this.tank.getFluid();
					FluidStack out = Utils.copyFluidStackWithAmount(inTank, Math.min(inTank.getAmount(), FluidAttributes.BUCKET_VOLUME), false);
					int accepted = output.fill(out, FluidAction.SIMULATE);
					if(accepted > 0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false),
								FluidAction.EXECUTE);
						this.tank.drain(drained, FluidAction.EXECUTE);
						ret = true;
					}
				}
				else
				{
					int totalOut = 0;
					Iterator<FluidStack> it = this.tank.fluids.iterator();
					while(it.hasNext())
					{
						FluidStack fs = it.next();
						if(fs!=null)
						{
							FluidStack out = Utils.copyFluidStackWithAmount(fs, Math.min(fs.getAmount(), FluidAttributes.BUCKET_VOLUME-totalOut), false);
							int accepted = output.fill(out, FluidAction.SIMULATE);
							if(accepted > 0)
							{
								int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
								MultiFluidTank.drain(drained, fs, it, FluidAction.EXECUTE);
								totalOut += drained;
								ret = true;
							}
							if(totalOut >= FluidAttributes.BUCKET_VOLUME)
								break;
						}
					}
				}
				return ret;
			}).orElse(false);
		}
		if(update)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}

	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES =
			CachedShapesWithTransform.createForMultiblock(MixerBlockEntity::getShape);

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return getShape(SHAPES);
	}

	private static List<AABB> getShape(BlockPos posInMultiblock)
	{
		if(new BlockPos(2, 0, 2).equals(posInMultiblock))
			return ImmutableList.of(
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0.125, .5f, 0.625, 0.25, 1, 0.875),
					new AABB(0.75, .5f, 0.625, 0.875, 1, 0.875)
			);
		else if(posInMultiblock.getX() > 0&&posInMultiblock.getY()==0&&posInMultiblock.getZ() < 2)
		{
			List<AABB> list = Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==2,
					new AABB(0, 0, 0, 1, .5f, 1),
					new AABB(0.0625, .5f, 0.6875, 0.3125, 1, 0.9375)
			);

			if(new BlockPos(1, 0, 1).equals(posInMultiblock))
			{
				list.add(new AABB(0, .5f, 0.375, 1.125, .75f, 0.625));
				list.add(new AABB(0.875, .5f, -0.125, 1.125, .75f, 0.375));
				list.add(new AABB(0.875, .75f, -0.125, 1.125, 1, 0.125));
			}

			return list;
		}
		else if(posInMultiblock.getX() > 0&&posInMultiblock.getY()==1&&posInMultiblock.getZ() < 2)
			return Utils.flipBoxes(posInMultiblock.getZ()==0, posInMultiblock.getX()==2,
					new AABB(0.1875, -.25, 0, 1, 0, 0.8125),
					new AABB(0.0625, 0, 0, 0.1875, 1, 0.9375),
					new AABB(0.1875, 0, 0.8125, 1, 1, 0.9375)
			);
		else if(new BlockPos(0, 2, 1).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0.1875, 0, 0.1875, 1, .625f, 0.6875));
		else if(new BlockPos(1, 2, 1).equals(posInMultiblock))
			return ImmutableList.of(
					new AABB(0.5625, .1875, -0.4375, 1.4375, 1, 0.4375),
					new AABB(0, 0, 0, 0.5625, .875, 0.5)
			);
		else if(posInMultiblock.getY()==0&&!ImmutableSet.of(
				new BlockPos(0, 0, 2),
				new BlockPos(0, 0, 1),
				new BlockPos(1, 0, 2)
		).contains(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, 0, 1, .5f, 1));
		else if(new BlockPos(2, 1, 2).equals(posInMultiblock))
			return ImmutableList.of(new AABB(0, 0, 0.5, 1, 1, 1));
		else
			return ImmutableList.of(new AABB(0, 0, 0, 1, 1, 1));
	}

	@Override
	public Set<MultiblockFace> getEnergyPos()
	{
		return ImmutableSet.of(new MultiblockFace(0, 1, 2, RelativeBlockFace.UP));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(
				new BlockPos(2, 1, 2)
		);
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MixerRecipe> process)
	{
		return true;
	}

	private DirectionalBlockPos getOutputPos()
	{
		return new DirectionalBlockPos(worldPosition.relative(getFacing(), 2), getFacing());
	}

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forBlockEntityAt(
			this, this::getOutputPos, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
	);

	@Override
	public void doProcessOutput(ItemStack output)
	{
		output = Utils.insertStackIntoInventory(outputCap, output, false);
		if(!output.isEmpty())
			Utils.dropStackAtPos(level, getOutputPos(), output);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<MixerRecipe> process)
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
	public float getMinProcessDistance(MultiblockProcess<MixerRecipe> process)
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
		return new int[]{0};
	}

	@Override
	@Nonnull
	public IFluidTank[] getInternalTanks()
	{
		return new IFluidTank[]{tank};
	}

	@Override
	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private final MultiblockCapability<IItemHandler> insertionHandler = MultiblockCapability.make(
			this, be -> be.insertionHandler, MixerBlockEntity::master,
			registerCapability(new IEInventoryHandler(8, this, 0, new boolean[]{true, true, true, true, true, true, true, true}, new boolean[8]))
	);
	private final MultiblockCapability<IFluidHandler> fluidInputCap = MultiblockCapability.make(
			this, be -> be.fluidInputCap, MixerBlockEntity::master, registerFluidInput(tank)
	);
	private final MultiblockCapability<IFluidHandler> fluidOutputCap = MultiblockCapability.make(
			this, be -> be.fluidOutputCap, MixerBlockEntity::master, registerFluidOutput(tank)
	);
	private static final MultiblockFace FLUID_OUTPUT = new MultiblockFace(1, 0, 2, RelativeBlockFace.FRONT);
	private static final MultiblockFace FLUID_INPUT = new MultiblockFace(0, 0, 1, RelativeBlockFace.LEFT);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			if(facing==null)
				return fluidInputCap.getAndCast();
			MultiblockFace relativeFace = asRelativeFace(facing);
			if(FLUID_INPUT.equals(relativeFace))
				return fluidInputCap.getAndCast();
			else if(FLUID_OUTPUT.equals(relativeFace))
				return fluidOutputCap.getAndCast();
		}
		if((facing==null||new BlockPos(1, 1, 0).equals(posInMultiblock))&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return insertionHandler.getAndCast();
		return super.getCapability(capability, facing);
	}

	@Override
	public MixerRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected MixerRecipe getRecipeForId(Level level, ResourceLocation id)
	{
		return MixerRecipe.RECIPES.getById(level, id);
	}

	public static class MultiblockProcessMixer extends MultiblockProcessInMachine<MixerRecipe>
	{
		public MultiblockProcessMixer(MixerRecipe recipe, BiFunction<Level, ResourceLocation, MixerRecipe> getRecipe, int... inputSlots)
		{
			super(recipe, getRecipe, inputSlots);
		}

		public MultiblockProcessMixer(ResourceLocation recipeId, BiFunction<Level, ResourceLocation, MixerRecipe> getRecipe, int... inputSlots)
		{
			super(recipeId, getRecipe, inputSlots);
		}

		@Override
		protected List<FluidStack> getRecipeFluidOutputs(PoweredMultiblockBlockEntity<?, MixerRecipe> multiblock)
		{
			return Collections.emptyList();
		}

		@Override
		protected List<FluidTagInput> getRecipeFluidInputs(PoweredMultiblockBlockEntity<?, MixerRecipe> multiblock)
		{
			return Collections.emptyList();
		}

		@Override
		public boolean canProcess(PoweredMultiblockBlockEntity<?, MixerRecipe> multiblock)
		{
			LevelDependentData<MixerRecipe> levelData = getLevelData(multiblock.getLevel());
			if (levelData.recipe() == null)
				return true;
			if(!(multiblock instanceof MixerBlockEntity mixer))
				return false;
			// we don't need to check filling since after draining 1 mB of input fluid there will be space for 1 mB of output fluid
			return mixer.energyStorage.extractEnergy(levelData.energyPerTick(), true)==levelData.energyPerTick()&&
					!mixer.tank.drain(levelData.recipe().fluidInput.withAmount(1), FluidAction.SIMULATE).isEmpty();
		}

		@Override
		public void doProcessTick(PoweredMultiblockBlockEntity<?, MixerRecipe> multiblock)
		{
			LevelDependentData<MixerRecipe> levelData = getLevelData(multiblock.getLevel());
			if (levelData.recipe() == null)
			{
				this.clearProcess = true;
				return;
			}
			int timerStep = Math.max(levelData.maxTicks()/levelData.recipe().fluidAmount, 1);
			if(timerStep!=0&&this.processTick%timerStep==0)
			{
				int amount = levelData.recipe().fluidAmount/levelData.maxTicks();
				int leftover = levelData.recipe().fluidAmount%levelData.maxTicks();
				if(leftover > 0)
				{
					double distBetweenExtra = levelData.maxTicks()/(double)leftover;
					if(Math.floor(processTick/distBetweenExtra)!=Math.floor((processTick-1)/distBetweenExtra))
						amount++;
				}
				MixerBlockEntity mixer = (MixerBlockEntity)multiblock;
				FluidStack drained = mixer.tank.drain(levelData.recipe().fluidInput.withAmount(amount), FluidAction.EXECUTE);
				if(!drained.isEmpty())
				{
					NonNullList<ItemStack> components = NonNullList.withSize(this.inputSlots.length, ItemStack.EMPTY);
					for(int i = 0; i < components.size(); i++)
						components.set(i, multiblock.getInventory().get(this.inputSlots[i]));
					FluidStack output = levelData.recipe().getFluidOutput(drained, components);

					FluidStack fs = Utils.copyFluidStackWithAmount(output, drained.getAmount(), false);
					((MixerBlockEntity)multiblock).tank.fill(fs, FluidAction.EXECUTE);
				}
			}
			super.doProcessTick(multiblock);
		}
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return formed;
	}

	@Override
	public MixerBlockEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public BEContainer<MixerBlockEntity, ?> getContainerType()
	{
		return IEContainerTypes.MIXER;
	}

	@Override
	public boolean shouldPlaySound(String sound)
	{
		return shouldRenderAsActive();
	}
}