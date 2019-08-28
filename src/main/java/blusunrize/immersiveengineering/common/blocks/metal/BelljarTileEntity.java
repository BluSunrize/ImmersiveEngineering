/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DirectionalBlockPos;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.FluidFertilizerHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.ItemFertilizerHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class BelljarTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IDirectionalTile, IBlockBounds, IHasDummyBlocks,
		IIEInventory, IIEInternalFluxHandler, IInteractionObjectIE, IOBJModelCallback<BlockState>
{
	public static TileEntityType<BelljarTileEntity> TYPE;

	public static final int SLOT_SOIL = 0;
	public static final int SLOT_SEED = 1;
	public static final int SLOT_FERTILIZER = 2;

	public Direction facing = Direction.NORTH;
	public int dummy = 0;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);
	public final FluidTank tank = new FluidTank(4000)
	{
		@Override
		protected void onContentsChanged()
		{
			BelljarTileEntity.this.sendSyncPacket(2);
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid)
		{
			return BelljarHandler.getFluidFertilizerHandler(fluid)!=null;
		}
	};
	public FluxStorage energyStorage = new FluxStorage(16000, Math.max(256, IEConfig.MACHINES.belljar_consumption.get()));

	private IPlantHandler curPlantHandler;
	public int fertilizerAmount = 0;
	public float fertilizerMod = 1;
	private float growth = 0;
	public float renderGrowth = 0;
	public boolean renderActive = false;

	public BelljarTileEntity()
	{
		super(TYPE);
	}

	private CapabilityReference<IItemHandler> output = CapabilityReference.forTileEntity(this,
			() -> new DirectionalBlockPos(pos.up().offset(facing.getOpposite()), facing),
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(dummy!=0||world.getRedstonePowerFromNeighbors(getPos()) > 0)
			return;
		ItemStack soil = inventory.get(SLOT_SOIL);
		ItemStack seed = inventory.get(SLOT_SEED);
		if(world.isRemote)
		{
			if(energyStorage.getEnergyStored() > IEConfig.MACHINES.belljar_consumption.get()&&fertilizerAmount > 0&&renderActive)
			{
				IPlantHandler handler = getCurrentPlantHandler();
				if(handler!=null&&handler.isCorrectSoil(seed, soil)&&fertilizerAmount > 0)
				{
					if(renderGrowth < 1)
					{
						renderGrowth += handler.getGrowthStep(seed, soil, renderGrowth, this, fertilizerMod, true);
						fertilizerAmount--;
					}
					else
						renderGrowth = handler.resetGrowth(seed, soil, renderGrowth, this, true);
					if(Utils.RAND.nextInt(8)==0)
					{
						double partX = getPos().getX()+.5;
						double partY = getPos().getY()+2.6875;
						double partZ = getPos().getZ()+.5;
						ImmersiveEngineering.proxy.spawnRedstoneFX(getWorld(), partX, partY, partZ, .25, .25, .25, 1f, .55f, .1f, .1f);
					}
				}
			}
		}
		else
		{
			if(!seed.isEmpty())
			{
				int consumption = IEConfig.MACHINES.belljar_consumption.get();
				IPlantHandler handler = getCurrentPlantHandler();
				if(handler!=null&&handler.isCorrectSoil(seed, soil)&&fertilizerAmount > 0&&
						energyStorage.extractEnergy(consumption, true)==consumption)
				{
					boolean consume = false;
					if(growth >= 1)
					{
						ItemStack[] outputs = handler.getOutput(seed, soil, this);
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
						if(canFit >= outputs.length)
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
							growth = handler.resetGrowth(seed, soil, growth, this, false);
							consume = true;
						}
					}
					else if(growth < 1)
					{
						growth += IEConfig.MACHINES.belljar_growth_mod.get()*handler.getGrowthStep(seed, soil, growth, this, fertilizerMod, false);
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

				int fluidConsumption = IEConfig.MACHINES.belljar_fluid.get();
				if(fertilizerAmount <= 0&&tank.getFluidAmount() >= fluidConsumption)
				{
					FluidFertilizerHandler fluidFert = BelljarHandler.getFluidFertilizerHandler(tank.getFluid());
					if(fluidFert!=null)
					{
						fertilizerMod = fluidFert.getGrowthMultiplier(tank.getFluid(), seed, soil, this);
						tank.drain(fluidConsumption, true);
						ItemStack fertilizer = inventory.get(SLOT_FERTILIZER);
						if(!fertilizer.isEmpty())
						{
							ItemFertilizerHandler itemFert = BelljarHandler.getItemFertilizerHandler(fertilizer);
							if(itemFert!=null)
								fertilizerMod *= itemFert.getGrowthMultiplier(fertilizer, seed, soil, this);
							fertilizer.shrink(1);
							if(fertilizer.getCount() <= 0)
								inventory.set(2, ItemStack.EMPTY);
						}
						fertilizerAmount = IEConfig.MACHINES.belljar_fertilizer.get();
						sendSyncPacket(1);
					}
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
							ItemStack stack = Utils.copyStackWithAmount(outStack, outCount);
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
	public IPlantHandler getCurrentPlantHandler()
	{
		ItemStack seed = inventory.get(SLOT_SEED);
		if(curPlantHandler==null||!curPlantHandler.isValid(seed))
			curPlantHandler = BelljarHandler.getHandler(seed);
		return curPlantHandler;
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
		facing = Direction.byIndex(nbt.getInt("facing"));
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
		nbt.putInt("facing", facing.ordinal());
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
	public Direction getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return true;
	}

	@Override
	public float[] getBlockBounds()
	{
		return null;//new float[]{facing==EnumFacing.EAST?0:.25f,facing==EnumFacing.UP?0:facing==EnumFacing.DOWN?.125f:.0625f,facing==EnumFacing.SOUTH?0:.25f, facing==EnumFacing.WEST?1:.75f,facing==EnumFacing.DOWN?1:.875f,facing==EnumFacing.NORTH?1:.75f};
	}

	@Override
	public boolean isDummy()
	{
		return dummy!=0;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		for(int i = 1; i <= 2; i++)
		{
			world.setBlockState(pos.up(i), state);
			((BelljarTileEntity)world.getTileEntity(pos.up(i))).dummy = i;
			((BelljarTileEntity)world.getTileEntity(pos.up(i))).facing = facing;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 2; i++)
		{
			BlockPos p = getPos().down(dummy).up(i);
			if(world.getTileEntity(p) instanceof BelljarTileEntity)
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
			return BelljarHandler.getItemFertilizerHandler(stack)!=null;
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
			if(dummy==0&&(facing==null||facing.getAxis()!=this.facing.rotateY().getAxis()))
				return inputHandler.cast();
			if(dummy==1&&(facing==null||facing==this.facing.getOpposite()))
			{
				BelljarTileEntity te = getGuiMaster();
				if(te!=null)
					return te.outputHandler.cast();
			}
		}
		else if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&dummy==0&&(facing==null||facing.getAxis()!=this.facing.rotateY().getAxis()))
			return tankCap.cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return true;
	}

	@Override
	public ResourceLocation getGuiName()
	{
		return Lib.GUIID_Belljar;
	}

	@Override
	public BelljarTileEntity getGuiMaster()
	{
		if(dummy==0)
			return this;
		TileEntity te = world.getTileEntity(getPos().down(dummy));
		if(te instanceof BelljarTileEntity)
			return (BelljarTileEntity)te;
		return null;
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public TextureAtlasSprite getTextureReplacement(BlockState object, String material)
	{
		if(!inventory.get(SLOT_SOIL).isEmpty()&&"farmland".equals(material))
		{
			ResourceLocation rl = getSoilTexture();
			if(rl!=null)
				return ClientUtils.getSprite(rl);
		}
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderGroup(BlockState object, String group)
	{
		return !"glass".equals(group);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Optional<TRSRTransformation> applyTransformations(BlockState object, String group, Optional<TRSRTransformation> transform)
	{
		return transform;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public String getCacheKey(BlockState object)
	{
		if(!inventory.get(SLOT_SOIL).isEmpty())
		{
			ResourceLocation rl = getSoilTexture();
			if(rl!=null)
				return rl.toString();
		}
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	private ResourceLocation getSoilTexture()
	{
		ItemStack soil = inventory.get(SLOT_SOIL);
		ResourceLocation rl = curPlantHandler!=null?curPlantHandler.getSoilTexture(inventory.get(SLOT_SEED), soil, this): null;
		if(rl==null)
			rl = BelljarHandler.getSoilTexture(soil);
		if(rl==null)
		{
			try
			{
				BlockState state = Utils.getStateFromItemStack(soil);
				if(state!=null)
					rl = ClientUtils.getSideTexture(state, Direction.UP);
			} catch(Exception e)
			{
				rl = ClientUtils.getSideTexture(soil, Direction.UP);
			}
		}
		if(rl==null&&!soil.isEmpty()&&Utils.isFluidRelatedItemStack(soil))
			rl = FluidUtil.getFluidContained(soil).map(fs -> fs.getFluid().getStill(fs)).orElse(rl);
		return rl;
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy!=0)
		{
			TileEntity te = world.getTileEntity(getPos().down(dummy));
			if(te instanceof BelljarTileEntity)
				return ((BelljarTileEntity)te).energyStorage;
		}
		return this.energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return facing==null||(dummy==0&&facing.getAxis()==this.facing.rotateY().getAxis())||(dummy==2&&facing==Direction.UP)?SideConfig.INPUT: SideConfig.NONE;
	}

	IEForgeEnergyWrapper energyWrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing==null||(dummy==0&&facing.getAxis()==this.facing.rotateY().getAxis())||(dummy==2&&facing==Direction.UP))
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
}