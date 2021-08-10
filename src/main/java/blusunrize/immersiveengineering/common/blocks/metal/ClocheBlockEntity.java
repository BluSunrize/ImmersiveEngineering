/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.utils.DirectionalBlockPos;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.fx.CustomParticleManager;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.utils.DistField;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.BEContainer;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ClocheBlockEntity extends IEBaseBlockEntity implements IETickableBlockEntity, IStateBasedDirectional, IBlockBounds, IHasDummyBlocks,
		IIEInventory, IIEInternalFluxHandler, IInteractionObjectIE<ClocheBlockEntity>, IOBJModelCallback<BlockState>,
		IModelOffsetProvider
{
	public static final int SLOT_SOIL = 0;
	public static final int SLOT_SEED = 1;
	public static final int SLOT_FERTILIZER = 2;

	public int dummy = 0;
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);
	public final FluidTank tank = new FluidTank(4*FluidAttributes.BUCKET_VOLUME)
	{
		@Override
		protected void onContentsChanged()
		{
			ClocheBlockEntity.this.sendSyncPacket(2);
		}

		@Override
		public boolean isFluidValid(FluidStack fluid)
		{
			return FluidTags.WATER.contains(fluid.getFluid());
		}
	};
	public FluxStorage energyStorage = new FluxStorage(16000, Math.max(256, IEServerConfig.MACHINES.cloche_consumption.get()));
	public final DistField<CustomParticleManager> particles = DistField.client(() -> CustomParticleManager::new);
	public final Supplier<ClocheRecipe> cachedRecipe = CachedRecipe.cached(
			ClocheRecipe::findRecipe, () -> inventory.get(SLOT_SEED), () -> inventory.get(SLOT_SOIL)
	);

	public int fertilizerAmount = 0;
	public float fertilizerMod = 1;
	private float growth = 0;
	public float renderGrowth = 0;
	public boolean renderActive = false;

	public ClocheBlockEntity(BlockEntityType<ClocheBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	private final CapabilityReference<IItemHandler> output = CapabilityReference.forBlockEntityAt(this,
			() -> new DirectionalBlockPos(worldPosition.above().relative(getFacing().getOpposite()), getFacing()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public boolean canTickAny()
	{
		return dummy == 0 && !isRSPowered();
	}

	@Override
	public void tickClient()
	{
		particles.get().clientTick();
		ItemStack seed = inventory.get(SLOT_SEED);
		ItemStack soil = inventory.get(SLOT_SOIL);
		if(energyStorage.getEnergyStored() > IEServerConfig.MACHINES.cloche_consumption.get()&&fertilizerAmount > 0&&renderActive)
		{
			ClocheRecipe recipe = cachedRecipe.get();
			if(recipe!=null&&fertilizerAmount > 0)
			{
				if(renderGrowth < recipe.getTime(seed, soil)+IEServerConfig.MACHINES.cloche_growth_mod.get()*fertilizerMod)
				{
					renderGrowth += IEServerConfig.MACHINES.cloche_growth_mod.get()*fertilizerMod;
					fertilizerAmount--;
				}
				else
					renderGrowth = 0;
				if(Utils.RAND.nextInt(8)==0)
					particles.get().add(new DustParticleOptions(new Vector3f(.55f, .1f, .1f), 1), .5, 2.6875, .5, .25, .25, .25, 20);
			}
		}
	}

	@Override
	public void tickServer()
	{
		ItemStack seed = inventory.get(SLOT_SEED);
		ItemStack soil = inventory.get(SLOT_SOIL);
		if(!seed.isEmpty())
		{
			ClocheRecipe recipe = cachedRecipe.get();
			int consumption = IEServerConfig.MACHINES.cloche_consumption.get();
			if(recipe!=null&&fertilizerAmount > 0&&energyStorage.extractEnergy(consumption, true)==consumption)
			{
				boolean consume = false;
				if(growth >= recipe.getTime(seed, soil))
				{
					List<ItemStack> outputs = recipe.getOutputs(seed, soil);
					int canFit = 0;
					boolean[] emptySlotsUsed = new boolean[4];
					for(ItemStack output : outputs)
						if(!output.isEmpty())
							for(int j = 3; j < 7; j++)
							{
								ItemStack existing = inventory.get(j);
								if((existing.isEmpty()&&!emptySlotsUsed[j-3])||(ItemHandlerHelper.canItemStacksStack(existing, output)&&existing.getCount()+output.getCount() <= existing.getMaxStackSize()))
								{
									canFit++;
									if(existing.isEmpty())
										emptySlotsUsed[j-3] = true;
									break;
								}
							}
					if(canFit >= outputs.size())
					{
						for(ItemStack output : outputs)
							for(int j = 3; j < 7; j++)
							{
								ItemStack existing = inventory.get(j);
								if(existing.isEmpty())
								{
									inventory.set(j, output.copy());
									break;
								}
								else if(ItemHandlerHelper.canItemStacksStack(existing, output)&&existing.getCount()+output.getCount() <= existing.getMaxStackSize())
								{
									existing.grow(output.getCount());
									break;
								}
							}
						growth = 0;
						consume = true;
					}
				}
				else
				{
					growth += IEServerConfig.MACHINES.cloche_growth_mod.get()*fertilizerMod;
					consume = true;
					if(level.getGameTime()%32==((getBlockPos().getX()^getBlockPos().getZ())&31))
						sendSyncPacket(0);
				}
				if(consume)
				{
					energyStorage.extractEnergy(consumption, false);
					fertilizerAmount--;
					if(!renderActive)
					{
						renderActive = true;
						sendSyncPacket(0);
					}
				}
				else if(renderActive)
				{
					renderActive = false;
					sendSyncPacket(0);
				}
			}
			else
				growth = 0;

			int fluidConsumption = IEServerConfig.MACHINES.cloche_fluid.get();
			if(fertilizerAmount <= 0&&tank.getFluidAmount() >= fluidConsumption)
			{
				fertilizerMod = 1;
				tank.drain(fluidConsumption, FluidAction.EXECUTE);
				ItemStack fertilizer = inventory.get(SLOT_FERTILIZER);
				if(!fertilizer.isEmpty())
				{
					float itemMod = ClocheFertilizer.getFertilizerGrowthModifier(fertilizer);
					if(itemMod > 0)
					{
						fertilizerMod *= itemMod;
						fertilizer.shrink(1);
						if(fertilizer.getCount() <= 0)
							inventory.set(2, ItemStack.EMPTY);
					}
				}
				fertilizerAmount = IEServerConfig.MACHINES.cloche_fertilizer.get();
				sendSyncPacket(1);
			}
		}
		else
			growth = 0;

		if(level.getGameTime()%8==0)
		{
			if(output.isPresent())
				for(int j = 3; j < 7; j++)
				{
					ItemStack outStack = inventory.get(j);
					if(!outStack.isEmpty())
					{
						int outCount = Math.min(outStack.getCount(), 16);
						ItemStack stack = ItemHandlerHelper.copyStackWithSize(outStack, outCount);
						stack = Utils.insertStackIntoInventory(output, stack, false);
						if(!stack.isEmpty())
							outCount -= stack.getCount();
						outStack.shrink(outCount);
						if(outStack.getCount() <= 0)
							this.inventory.set(j, ItemStack.EMPTY);
					}
				}
		}
	}

	protected void sendSyncPacket(int type)
	{
		CompoundTag nbt = new CompoundTag();
		if(type==0)
		{
			nbt.putFloat("growth", growth);
			nbt.putInt("energy", energyStorage.getEnergyStored());
			nbt.putBoolean("renderActive", renderActive);
		}
		else if(type==1)
		{
			nbt.putInt("fertilizerAmount", fertilizerAmount);
			nbt.putFloat("fertilizerMod", fertilizerMod);
		}
		else if(type==2)
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
		ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
				new MessageBlockEntitySync(this, nbt));
	}

	@Override
	public void receiveMessageFromServer(CompoundTag message)
	{
		if(message.contains("growth", NBT.TAG_FLOAT))
			renderGrowth = message.getFloat("growth");
		if(message.contains("renderActive", NBT.TAG_BYTE))
			renderActive = message.getBoolean("renderActive");
		if(message.contains("energy", NBT.TAG_INT))
			energyStorage.setEnergy(message.getInt("energy"));
		if(message.contains("fertilizerAmount", NBT.TAG_INT))
			fertilizerAmount = message.getInt("fertilizerAmount");
		if(message.contains("fertilizerMod", NBT.TAG_FLOAT))
			fertilizerMod = message.getFloat("fertilizerMod");
		if(message.contains("tank", NBT.TAG_COMPOUND))
			tank.readFromNBT(message.getCompound("tank"));
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		dummy = nbt.getInt("dummy");
		ContainerHelper.loadAllItems(nbt, inventory);
		energyStorage.readFromNBT(nbt);
		tank.readFromNBT(nbt.getCompound("tank"));
		fertilizerAmount = nbt.getInt("fertilizerAmount");
		fertilizerMod = nbt.getFloat("fertilizerMod");
		growth = nbt.getFloat("growth");
		renderBB = null;
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putInt("dummy", dummy);
		ContainerHelper.saveAllItems(nbt, inventory);
		energyStorage.writeToNBT(nbt);
		CompoundTag tankTag = tank.writeToNBT(new CompoundTag());
		nbt.put("tank", tankTag);
		nbt.putInt("fertilizerAmount", fertilizerAmount);
		nbt.putFloat("fertilizerMod", fertilizerMod);
		nbt.putFloat("growth", growth);
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public void setFacing(Direction facing)
	{
		BlockPos lowest = worldPosition.below(dummy);
		for(int i = 0; i < 3; ++i)
		{
			BlockPos pos = lowest.above(i);
			BlockState state = getLevelNonnull().getBlockState(pos);
			if(state.getBlock()==MetalDevices.cloche.get())
				getLevelNonnull().setBlockAndUpdate(pos, state.setValue(getFacingProperty(), facing));
		}
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.block();
	}

	@Override
	public boolean isDummy()
	{
		return dummy!=0;
	}

	@Nullable
	@Override
	public ClocheBlockEntity master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterBE instanceof ClocheBlockEntity)
			return (ClocheBlockEntity)tempMasterBE;
		BlockPos masterPos = getBlockPos().below(dummy);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return te instanceof ClocheBlockEntity?(ClocheBlockEntity)te: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			level.setBlockAndUpdate(worldPosition.above(i), state);
			((ClocheBlockEntity)level.getBlockEntity(worldPosition.above(i))).dummy = i;
			((ClocheBlockEntity)level.getBlockEntity(worldPosition.above(i))).setFacing(getFacing());
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		tempMasterBE = master();
		for(int i = 0; i <= 2; i++)
		{
			BlockPos p = getBlockPos().below(dummy).above(i);
			if(level.getBlockEntity(p) instanceof ClocheBlockEntity)
				level.removeBlock(p, false);
		}
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(slot==SLOT_FERTILIZER)
			return ClocheFertilizer.isValidFertilizer(stack);
		else
			return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return slot < 2?1: 64;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
		if(slot==0)
		{
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
	}


	private LazyOptional<IItemHandler> inputHandler = registerConstantCap(
			new IEInventoryHandler(1, this, 2, true, false)
	);

	private LazyOptional<IItemHandler> outputHandler = registerConstantCap(
			new IEInventoryHandler(4, this, 3, false, true)
	);

	private LazyOptional<IFluidHandler> tankCap = registerConstantCap(tank);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			if(dummy==0&&(facing==null||facing.getAxis()!=this.getFacing().getClockWise().getAxis()))
				return inputHandler.cast();
			if(dummy==1&&(facing==null||facing==this.getFacing().getOpposite()))
			{
				ClocheBlockEntity te = getGuiMaster();
				if(te!=null)
					return te.outputHandler.cast();
			}
		}
		else if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&dummy==0&&(facing==null||facing.getAxis()!=this.getFacing().getClockWise().getAxis()))
			return tankCap.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean canUseGui(Player player)
	{
		return true;
	}

	@Override
	public ClocheBlockEntity getGuiMaster()
	{
		if(dummy==0)
			return this;
		BlockEntity te = level.getBlockEntity(getBlockPos().below(dummy));
		if(te instanceof ClocheBlockEntity)
			return (ClocheBlockEntity)te;
		return null;
	}

	@Override
	public BEContainer<ClocheBlockEntity, ?> getContainerType()
	{
		return IEContainerTypes.CLOCHE;
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	@Nullable
	public TextureAtlasSprite getTextureReplacement(BlockState object, String group, String material)
	{
		ClocheBlockEntity master = master();
		if(master==null)
			return null;
		ItemStack soil = master.inventory.get(SLOT_SOIL);
		if(!soil.isEmpty()&&"farmland".equals(material))
		{
			ResourceLocation rl = getSoilTexture(soil);
			if(rl!=null)
				return ClientUtils.getSprite(rl);
		}
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderGroup(BlockState object, String group)
	{
		return "glass".equals(group)==(MinecraftForgeClient.getRenderLayer()==RenderType.translucent());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Transformation applyTransformations(BlockState object, String group, Transformation transform)
	{
		return transform;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public String getCacheKey(BlockState object)
	{
		ResourceLocation rl = getSoilTexture();
		if(rl!=null)
			return rl.toString();
		else
			return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	private ResourceLocation getSoilTexture()
	{
		ClocheBlockEntity master = master();
		if(master!=null)
			return getSoilTexture(master.inventory.get(SLOT_SOIL));
		else
			return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	private static ResourceLocation getSoilTexture(ItemStack soil)
	{
		ResourceLocation rl = ClocheRecipe.getSoilTexture(soil);
		if(rl==null)
		{
			try
			{
				BlockState state = Utils.getStateFromItemStack(soil);
				if(state!=null)
					rl = ModelUtils.getSideTexture(state, Direction.UP);
			} catch(Exception e)
			{
				rl = ModelUtils.getSideTexture(soil, Direction.UP);
			}
		}
		if(rl==null&&!soil.isEmpty()&&Utils.isFluidRelatedItemStack(soil))
			rl = FluidUtil.getFluidContained(soil).map(fs -> fs.getFluid().getAttributes().getStillTexture(fs)).orElse(rl);
		return rl;
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy!=0)
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().below(dummy));
			if(te instanceof ClocheBlockEntity)
				return ((ClocheBlockEntity)te).energyStorage;
		}
		return this.energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return facing==null||(dummy==0&&facing.getAxis()==this.getFacing().getClockWise().getAxis())||(dummy==2&&facing==Direction.UP)?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper energyWrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing==null||(dummy==0&&facing.getAxis()==this.getFacing().getClockWise().getAxis())||(dummy==2&&facing==Direction.UP))
			return energyWrapper;
		return null;
	}

	AABB renderBB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderBB==null)
			renderBB = new AABB(0, 0, 0, 1, 2, 1).move(worldPosition);
		return renderBB;
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		return new BlockPos(0, dummy, 0);
	}
}