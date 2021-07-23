/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.utils.shapes.CachedShapesWithTransform;
import blusunrize.immersiveengineering.client.fx.FluidSplashParticle.Data;
import blusunrize.immersiveengineering.client.fx.IEParticles;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.TileContainer;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MixerTileEntity extends PoweredMultiblockTileEntity<MixerTileEntity, MixerRecipe> implements
		IInteractionObjectIE<MixerTileEntity>, IBlockBounds
{
	public final MultiFluidTank tank = new MultiFluidTank(8*FluidAttributes.BUCKET_VOLUME);
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(8, ItemStack.EMPTY);
	public float animation_agitator = 0;
	public boolean outputAll;

	public MixerTileEntity()
	{
		super(IEMultiblocks.MIXER, 16000, true, IETileTypes.MIXER.get());
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
		if(message.contains("outputAll", NBT.TAG_BYTE))
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
		super.tickClient();
		if(shouldRenderAsActive())
		{
			if(Utils.RAND.nextInt(8)==0)
			{
				FluidStack fs = !tank.fluids.isEmpty()?tank.fluids.get(0): null;
				if(fs!=null)
				{
					float amount = tank.getFluidAmount()/(float)tank.getCapacity()*1.125f;
					Vec3 partPos = new Vec3(getBlockPos().getX()+.5f+getFacing().getStepX()*.5f+(getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise()).getStepX()*.5f, getBlockPos().getY()-.0625f+amount, getBlockPos().getZ()+.5f+getFacing().getStepZ()*.5f+(getIsMirrored()?getFacing().getCounterClockWise(): getFacing().getClockWise()).getStepZ()*.5f);
					float r = Utils.RAND.nextFloat()*.8125f;
					float angleRad = (float)Math.toRadians(animation_agitator);
					partPos = partPos.add(r*Math.cos(angleRad), 0, r*Math.sin(angleRad));
					if(Utils.RAND.nextBoolean())
						level.addParticle(IEParticles.IE_BUBBLE.get(), partPos.x, partPos.y, partPos.z, 0, 0, 0);
					else
						level.addParticle(new Data(fs.getFluid()), partPos.x, partPos.y, partPos.z, 0, 0, 0);
				}
			}
			animation_agitator = (animation_agitator+9)%360;
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
					MixerRecipe recipe = MixerRecipe.findRecipe(fs, components);
					if(recipe!=null)
					{
						foundRecipe = true;
						MultiblockProcessInMachine<MixerRecipe> process = new MultiblockProcessMixer(recipe, recipe.getUsedSlots(fs, components)).setInputTanks(0);
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
					FluidStack out = Utils.copyFluidStackWithAmount(inTank, Math.min(inTank.getAmount(), 80), false);
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
							FluidStack out = Utils.copyFluidStackWithAmount(fs, Math.min(fs.getAmount(), 80-totalOut), false);
							int accepted = output.fill(out, FluidAction.SIMULATE);
							if(accepted > 0)
							{
								int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), FluidAction.EXECUTE);
								MultiFluidTank.drain(drained, fs, it, FluidAction.EXECUTE);
								totalOut += drained;
								ret = true;
							}
							if(totalOut >= 80)
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
			CachedShapesWithTransform.createForMultiblock(MixerTileEntity::getShape);

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
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(
				new BlockPos(0, 1, 2)
		);
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

	private CapabilityReference<IItemHandler> outputCap = CapabilityReference.forTileEntityAt(
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
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		MixerTileEntity master = master();
		if(master!=null&&((new BlockPos(1, 0, 2).equals(posInMultiblock)&&(side==null||side==getFacing().getOpposite()))
				||(new BlockPos(0, 0, 1).equals(posInMultiblock)&&(side==null||side==(getIsMirrored()?getFacing().getClockWise(): getFacing().getCounterClockWise())))))
			return master.getInternalTanks();
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resources)
	{
		return side==null||side==(getIsMirrored()?getFacing().getClockWise(): getFacing().getCounterClockWise());
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return side==null||side==getFacing().getOpposite();
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}

	private LazyOptional<IItemHandler> insertionHandler = registerConstantCap(
			new IEInventoryHandler(8, this, 0, new boolean[]{true, true, true, true, true, true, true, true}, new boolean[8])
	);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if((facing==null||new BlockPos(1, 1, 0).equals(posInMultiblock))&&capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			MixerTileEntity master = master();
			if(master!=null)
				return master.insertionHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public MixerRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected MixerRecipe getRecipeForId(ResourceLocation id)
	{
		return MixerRecipe.recipeList.get(id);
	}

	public static class MultiblockProcessMixer extends MultiblockProcessInMachine<MixerRecipe>
	{
		public MultiblockProcessMixer(MixerRecipe recipe, int... inputSlots)
		{
			super(recipe, inputSlots);
		}

		@Override
		protected List<FluidStack> getRecipeFluidOutputs(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			return Collections.emptyList();
		}

		@Override
		protected List<FluidTagInput> getRecipeFluidInputs(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			return Collections.emptyList();
		}

		@Override
		public boolean canProcess(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			if(!(multiblock instanceof MixerTileEntity))
				return false;
			MixerTileEntity mixer = (MixerTileEntity)multiblock;
			// we don't need to check filling since after draining 1 mB of input fluid there will be space for 1 mB of output fluid
			return mixer.energyStorage.extractEnergy(energyPerTick, true)==energyPerTick&&
					!mixer.tank.drain(recipe.fluidInput.withAmount(1), FluidAction.SIMULATE).isEmpty();
		}

		@Override
		public void doProcessTick(PoweredMultiblockTileEntity<?, MixerRecipe> multiblock)
		{
			int timerStep = Math.max(this.maxTicks/this.recipe.fluidAmount, 1);
			if(timerStep!=0&&this.processTick%timerStep==0)
			{
				int amount = this.recipe.fluidAmount/maxTicks;
				int leftover = this.recipe.fluidAmount%maxTicks;
				if(leftover > 0)
				{
					double distBetweenExtra = maxTicks/(double)leftover;
					if(Math.floor(processTick/distBetweenExtra)!=Math.floor((processTick-1)/distBetweenExtra))
						amount++;
				}
				MixerTileEntity mixer = (MixerTileEntity)multiblock;
				FluidStack drained = mixer.tank.drain(recipe.fluidInput.withAmount(amount), FluidAction.EXECUTE);
				if(!drained.isEmpty())
				{
					NonNullList<ItemStack> components = NonNullList.withSize(this.inputSlots.length, ItemStack.EMPTY);
					for(int i = 0; i < components.size(); i++)
						components.set(i, multiblock.getInventory().get(this.inputSlots[i]));
					FluidStack output = this.recipe.getFluidOutput(drained, components);

					FluidStack fs = Utils.copyFluidStackWithAmount(output, drained.getAmount(), false);
					((MixerTileEntity)multiblock).tank.fill(fs, FluidAction.EXECUTE);
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
	public MixerTileEntity getGuiMaster()
	{
		return master();
	}

	@Override
	public TileContainer<MixerTileEntity, ?> getContainerType()
	{
		return IEContainerTypes.MIXER;
	}
}