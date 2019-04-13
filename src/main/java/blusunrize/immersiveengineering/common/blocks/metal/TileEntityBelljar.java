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
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.FluidFertilizerHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.ItemFertilizerHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static blusunrize.immersiveengineering.common.Config.IEConfig.Machines.belljar_growth_mod;

public class TileEntityBelljar extends TileEntityIEBase implements ITickable, IDirectionalTile, IBlockBounds, IHasDummyBlocks, IIEInventory, IIEInternalFluxHandler, IGuiTile, IOBJModelCallback<IBlockState>
{
	public static final int SLOT_SOIL = 0;
	public static final int SLOT_SEED = 1;
	public static final int SLOT_FERTILIZER = 2;

	public EnumFacing facing = EnumFacing.NORTH;
	public int dummy = 0;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);
	public FluidTank tank = new FluidTank(4000)
	{
		@Override
		protected void onContentsChanged()
		{
			TileEntityBelljar.this.sendSyncPacket(2);
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid)
		{
			return BelljarHandler.getFluidFertilizerHandler(fluid)!=null;
		}
	};
	public FluxStorage energyStorage = new FluxStorage(16000, Math.max(256, IEConfig.Machines.belljar_consumption));

	private IPlantHandler curPlantHandler;
	public int fertilizerAmount = 0;
	public float fertilizerMod = 1;
	private float growth = 0;
	public float renderGrowth = 0;
	public boolean renderActive = false;

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(dummy!=0||world.getRedstonePowerFromNeighbors(getPos()) > 0)
			return;
		ItemStack soil = inventory.get(SLOT_SOIL);
		ItemStack seed = inventory.get(SLOT_SEED);
		if(getWorld().isRemote)
		{
			if(energyStorage.getEnergyStored() > IEConfig.Machines.belljar_consumption&&fertilizerAmount > 0&&renderActive)
			{
				IPlantHandler handler = getCurrentPlantHandler();
				if(handler!=null&&handler.isCorrectSoil(seed, soil)&&fertilizerAmount > 0)
				{
					if(renderGrowth < 1)
					{
						renderGrowth += belljar_growth_mod*handler.getGrowthStep(seed, soil, renderGrowth, this, fertilizerMod, true);
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
				IPlantHandler handler = getCurrentPlantHandler();
				if(handler!=null&&handler.isCorrectSoil(seed, soil)&&fertilizerAmount > 0&&energyStorage.extractEnergy(IEConfig.Machines.belljar_consumption, true)==IEConfig.Machines.belljar_consumption)
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
						growth += belljar_growth_mod*handler.getGrowthStep(seed, soil, growth, this, fertilizerMod, false);
						consume = true;
						if(world.getTotalWorldTime()%32==((getPos().getX()^getPos().getZ())&31))
							sendSyncPacket(0);
					}
					if(consume)
					{
						energyStorage.extractEnergy(IEConfig.Machines.belljar_consumption, false);
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

				if(fertilizerAmount <= 0&&tank.getFluidAmount() >= IEConfig.Machines.belljar_fluid)
				{
					FluidFertilizerHandler fluidFert = BelljarHandler.getFluidFertilizerHandler(tank.getFluid());
					if(fluidFert!=null)
					{
						fertilizerMod = fluidFert.getGrowthMultiplier(tank.getFluid(), seed, soil, this);
						tank.drain(IEConfig.Machines.belljar_fluid, true);
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
						fertilizerAmount = IEConfig.Machines.belljar_fertilizer;
						sendSyncPacket(1);
					}
				}
			}
			else
				growth = 0;

			if(world.getTotalWorldTime()%8==0)
			{
				BlockPos outputPos = getPos().up().offset(facing.getOpposite());
				TileEntity outputTile = Utils.getExistingTileEntity(world, outputPos);
				if(outputTile!=null)
					for(int j = 3; j < 7; j++)
					{
						ItemStack outStack = inventory.get(j);
						if(!outStack.isEmpty())
						{
							int outCount = Math.min(outStack.getCount(), 16);
							ItemStack stack = Utils.copyStackWithAmount(outStack, outCount);
							stack = Utils.insertStackIntoInventory(outputTile, stack, facing);
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

	public IPlantHandler getCurrentPlantHandler()
	{
		ItemStack seed = inventory.get(SLOT_SEED);
		if(curPlantHandler==null||!curPlantHandler.isValid(seed))
			curPlantHandler = BelljarHandler.getHandler(seed);
		return curPlantHandler;
	}

	protected void sendSyncPacket(int type)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		if(type==0)
		{
			nbt.setFloat("growth", growth);
			nbt.setInteger("energy", energyStorage.getEnergyStored());
			nbt.setBoolean("renderActive", renderActive);
		}
		else if(type==1)
		{
			nbt.setInteger("fertilizerAmount", fertilizerAmount);
			nbt.setFloat("fertilizerMod", fertilizerMod);
		}
		else if(type==2)
			nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		ImmersiveEngineering.packetHandler.sendToAllAround(new MessageTileSync(this, nbt), new TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 128));
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message)
	{
		if(message.hasKey("growth"))
			renderGrowth = message.getFloat("growth");
		if(message.hasKey("renderActive"))
			renderActive = message.getBoolean("renderActive");
		if(message.hasKey("energy"))
			energyStorage.setEnergy(message.getInteger("energy"));
		if(message.hasKey("fertilizerAmount"))
			fertilizerAmount = message.getInteger("fertilizerAmount");
		if(message.hasKey("fertilizerMod"))
			fertilizerMod = message.getFloat("fertilizerMod");
		if(message.hasKey("tank"))
			tank.readFromNBT(message.getCompoundTag("tank"));
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.byIndex(nbt.getInteger("facing"));
		dummy = nbt.getInteger("dummy");
		inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 7);
		energyStorage.readFromNBT(nbt);
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		fertilizerAmount = nbt.getInteger("fertilizerAmount");
		fertilizerMod = nbt.getFloat("fertilizerMod");
		growth = nbt.getFloat("growth");
		renderBB = null;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setInteger("dummy", dummy);
		nbt.setTag("inventory", Utils.writeInventory(inventory));
		energyStorage.writeToNBT(nbt);
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
		nbt.setInteger("fertilizerAmount", fertilizerAmount);
		nbt.setFloat("fertilizerMod", fertilizerMod);
		nbt.setFloat("growth", growth);
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
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
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		for(int i = 1; i <= 2; i++)
		{
			world.setBlockState(pos.up(i), state);
			((TileEntityBelljar)world.getTileEntity(pos.up(i))).dummy = i;
			((TileEntityBelljar)world.getTileEntity(pos.up(i))).facing = facing;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i = 0; i <= 2; i++)
		{
			BlockPos p = getPos().down(dummy).up(i);
			if(world.getTileEntity(p) instanceof TileEntityBelljar)
				world.setBlockToAir(p);
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


	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(getGuiMaster()!=null)
		{
			if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return dummy==0?(facing==null||facing.getAxis()!=this.facing.rotateY().getAxis()): dummy==1&&(facing==null||facing==this.facing.getOpposite());
			else if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
				return dummy==0&&(facing==null||facing.getAxis()!=this.facing.rotateY().getAxis());
		}
		return super.hasCapability(capability, facing);
	}

	IItemHandler inputHandler = new IEInventoryHandler(1, this, 2, true, false);
	IItemHandler outputHandler = new IEInventoryHandler(4, this, 3, false, true);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			if(dummy==0&&(facing==null||facing.getAxis()!=this.facing.rotateY().getAxis()))
				return (T)inputHandler;
			if(dummy==1&&(facing==null||facing==this.facing.getOpposite()))
			{
				TileEntityBelljar te = getGuiMaster();
				if(te!=null)
					return (T)te.outputHandler;
			}
		}
		else if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&dummy==0&&(facing==null||facing.getAxis()!=this.facing.rotateY().getAxis()))
			return (T)tank;
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean canOpenGui()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return Lib.GUIID_Belljar;
	}

	@Override
	public TileEntityBelljar getGuiMaster()
	{
		if(dummy==0)
			return this;
		TileEntity te = world.getTileEntity(getPos().down(dummy));
		if(te instanceof TileEntityBelljar)
			return (TileEntityBelljar)te;
		return null;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTextureReplacement(IBlockState object, String material)
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
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		return !"glass".equals(group);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Optional<TRSRTransformation> applyTransformations(IBlockState object, String group, Optional<TRSRTransformation> transform)
	{
		return transform;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getCacheKey(IBlockState object)
	{
		if(!inventory.get(SLOT_SOIL).isEmpty())
		{
			ResourceLocation rl = getSoilTexture();
			if(rl!=null)
				return rl.toString();
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
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
				IBlockState state = Utils.getStateFromItemStack(soil);
				if(state!=null)
					rl = ClientUtils.getSideTexture(state, EnumFacing.UP);
			} catch(Exception e)
			{
				rl = ClientUtils.getSideTexture(soil, EnumFacing.UP);
			}
		}
		if(rl==null&&!soil.isEmpty()&&Utils.isFluidRelatedItemStack(soil))
		{
			FluidStack fs = FluidUtil.getFluidContained(soil);
			if(fs!=null)
				rl = fs.getFluid().getStill(fs);
		}
		return rl;
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy!=0)
		{
			TileEntity te = world.getTileEntity(getPos().down(dummy));
			if(te instanceof TileEntityBelljar)
				return ((TileEntityBelljar)te).energyStorage;
		}
		return this.energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable EnumFacing facing)
	{
		return facing==null||(dummy==0&&facing.getAxis()==this.facing.rotateY().getAxis())||(dummy==2&&facing==EnumFacing.UP)?SideConfig.INPUT: SideConfig.NONE;
	}

	IEForgeEnergyWrapper energyWrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(facing==null||(dummy==0&&facing.getAxis()==this.facing.rotateY().getAxis())||(dummy==2&&facing==EnumFacing.UP))
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