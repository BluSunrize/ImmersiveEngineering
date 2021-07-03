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
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.Property;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
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
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ClocheTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IStateBasedDirectional, IBlockBounds, IHasDummyBlocks,
		IIEInventory, IIEInternalFluxHandler, IInteractionObjectIE, IOBJModelCallback<BlockState>, IModelOffsetProvider
{
	public static final int SLOT_SOIL = 0;
	public static final int SLOT_SEED = 1;
	public static final int SLOT_FERTILIZER = 2;

	public int dummy = 0;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);
	public final FluidTank tank = new FluidTank(4*FluidAttributes.BUCKET_VOLUME)
	{
		@Override
		protected void onContentsChanged()
		{
			ClocheTileEntity.this.sendSyncPacket(2);
		}

		@Override
		public boolean isFluidValid(FluidStack fluid)
		{
			return FluidTags.WATER.contains(fluid.getFluid());
		}
	};
	public FluxStorage energyStorage = new FluxStorage(16000, Math.max(256, IEServerConfig.MACHINES.cloche_consumption.get()));
	public final CustomParticleManager particles = new CustomParticleManager();

	public int fertilizerAmount = 0;
	public float fertilizerMod = 1;
	private float growth = 0;
	public float renderGrowth = 0;
	public boolean renderActive = false;

	public ClocheTileEntity()
	{
		super(IETileTypes.CLOCHE.get());
	}

	private final CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntityAt(this,
			() -> new DirectionalBlockPos(pos.up().offset(getFacing().getOpposite()), getFacing()),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tick()
	{
		checkForNeedlessTicking();
		if(dummy!=0||isRSPowered())
			return;
		ItemStack seed = inventory.get(SLOT_SEED);
		ItemStack soil = inventory.get(SLOT_SOIL);
		if(world.isRemote)
		{
			particles.clientTick();
			if(energyStorage.getEnergyStored() > IEServerConfig.MACHINES.cloche_consumption.get()&&fertilizerAmount > 0&&renderActive)
			{
				ClocheRecipe recipe = getRecipe();
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
						particles.add(new RedstoneParticleData(.55f, .1f, .1f, 1), .5, 2.6875, .5, .25, .25, .25, 20);
				}
			}
		}
		else
		{
			if(!seed.isEmpty())
			{
				ClocheRecipe recipe = getRecipe();
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
						if(world.getGameTime()%32==((getPos().getX()^getPos().getZ())&31))
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

			if(world.getGameTime()%8==0)
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
	}

	@Nullable
	public ClocheRecipe getRecipe()
	{
		ItemStack soil = inventory.get(SLOT_SOIL);
		ItemStack seed = inventory.get(SLOT_SEED);
		return ClocheRecipe.findRecipe(seed, soil);
	}

	protected void sendSyncPacket(int type)
	{
		CompoundNBT nbt = new CompoundNBT();
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
			nbt.put("tank", tank.writeToNBT(new CompoundNBT()));
		ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)),
				new MessageTileSync(this, nbt));
	}

	@Override
	public void receiveMessageFromServer(CompoundNBT message)
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
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		dummy = nbt.getInt("dummy");
		inventory = Utils.readInventory(nbt.getList("inventory", 10), 7);
		energyStorage.readFromNBT(nbt);
		tank.readFromNBT(nbt.getCompound("tank"));
		fertilizerAmount = nbt.getInt("fertilizerAmount");
		fertilizerMod = nbt.getFloat("fertilizerMod");
		growth = nbt.getFloat("growth");
		renderBB = null;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("dummy", dummy);
		nbt.put("inventory", Utils.writeInventory(inventory));
		energyStorage.writeToNBT(nbt);
		CompoundNBT tankTag = tank.writeToNBT(new CompoundNBT());
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
	public boolean canHammerRotate(Direction side, Vector3d hit, LivingEntity entity)
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
		BlockPos lowest = pos.down(dummy);
		for(int i = 0; i < 3; ++i)
		{
			BlockPos pos = lowest.up(i);
			BlockState state = getWorldNonnull().getBlockState(pos);
			if(state.getBlock()==MetalDevices.cloche)
				getWorldNonnull().setBlockState(pos, state.with(getFacingProperty(), facing));
		}
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		return VoxelShapes.fullCube();
	}

	@Override
	public boolean isDummy()
	{
		return dummy!=0;
	}

	@Nullable
	@Override
	public ClocheTileEntity master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterTE instanceof ClocheTileEntity)
			return (ClocheTileEntity)tempMasterTE;
		BlockPos masterPos = getPos().down(dummy);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return te instanceof ClocheTileEntity?(ClocheTileEntity)te: null;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			world.setBlockState(pos.up(i), state);
			((ClocheTileEntity)world.getTileEntity(pos.up(i))).dummy = i;
			((ClocheTileEntity)world.getTileEntity(pos.up(i))).setFacing(getFacing());
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		tempMasterTE = master();
		for(int i = 0; i <= 2; i++)
		{
			BlockPos p = getPos().down(dummy).up(i);
			if(world.getTileEntity(p) instanceof ClocheTileEntity)
				world.removeBlock(p, false);
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
			this.markDirty();
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
			if(dummy==0&&(facing==null||facing.getAxis()!=this.getFacing().rotateY().getAxis()))
				return inputHandler.cast();
			if(dummy==1&&(facing==null||facing==this.getFacing().getOpposite()))
			{
				ClocheTileEntity te = getGuiMaster();
				if(te!=null)
					return te.outputHandler.cast();
			}
		}
		else if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&dummy==0&&(facing==null||facing.getAxis()!=this.getFacing().rotateY().getAxis()))
			return tankCap.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return true;
	}

	@Override
	public ClocheTileEntity getGuiMaster()
	{
		if(dummy==0)
			return this;
		TileEntity te = world.getTileEntity(getPos().down(dummy));
		if(te instanceof ClocheTileEntity)
			return (ClocheTileEntity)te;
		return null;
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	@Nullable
	public TextureAtlasSprite getTextureReplacement(BlockState object, String group, String material)
	{
		ClocheTileEntity master = master();
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
		return "glass".equals(group)==(MinecraftForgeClient.getRenderLayer()==RenderType.getTranslucent());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TransformationMatrix applyTransformations(BlockState object, String group, TransformationMatrix transform)
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
		ClocheTileEntity master = master();
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
			TileEntity te = world.getTileEntity(getPos().down(dummy));
			if(te instanceof ClocheTileEntity)
				return ((ClocheTileEntity)te).energyStorage;
		}
		return this.energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return facing==null||(dummy==0&&facing.getAxis()==this.getFacing().rotateY().getAxis())||(dummy==2&&facing==Direction.UP)?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper energyWrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing==null||(dummy==0&&facing.getAxis()==this.getFacing().rotateY().getAxis())||(dummy==2&&facing==Direction.UP))
			return energyWrapper;
		return null;
	}

	AxisAlignedBB renderBB;

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderBB==null)
			renderBB = new AxisAlignedBB(0, 0, 0, 1, 2, 1).offset(pos);
		return renderBB;
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vector3i size)
	{
		return new BlockPos(0, dummy, 0);
	}
}