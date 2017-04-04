package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
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
import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
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

public class TileEntityBelljar extends TileEntityIEBase implements ITickable, IDirectionalTile, IBlockBounds, IHasDummyBlocks, IIEInventory, IIEInternalFluxHandler, IGuiTile, IOBJModelCallback<IBlockState>
{
	public EnumFacing facing = EnumFacing.NORTH;
	public int dummy = 0;
	private ItemStack[] inventory = new ItemStack[7];
	public FluidTank tank = new FluidTank(4000)
	{
		@Override
		protected void onContentsChanged()
		{
			TileEntityBelljar.this.sendSyncPacket(2);
		}
	};
	public FluxStorage energyStorage = new FluxStorage(16000,Math.max(256,IEConfig.Machines.belljar_consumption));

	private IPlantHandler curPlantHandler;
	public int fertilizerAmount = 0;
	public float fertilizerMod = 1;
	private float growth = 0;
	public float renderGrowth = 0;

	@Override
	public void update()
	{
		if(dummy!=0 || worldObj.isBlockIndirectlyGettingPowered(getPos())>0)
			return;
		if(getWorld().isRemote)
		{
			if(energyStorage.getEnergyStored()>IEConfig.Machines.belljar_consumption && fertilizerAmount>0)
			{
				IPlantHandler handler = getCurrentPlantHandler();
				if(handler!=null&&handler.isCorrectSoil(inventory[1], inventory[0]) && fertilizerAmount>0)
				{
					if(renderGrowth<1)
					{
						renderGrowth += handler.getGrowthStep(inventory[1], inventory[0], renderGrowth, this, fertilizerMod, true);
						fertilizerAmount--;
					}
					else
						renderGrowth = handler.resetGrowth(inventory[1], inventory[0], renderGrowth, this, true);
					if(getWorld().rand.nextInt(8)==0)
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
			if(inventory[1]!=null)
			{
				IPlantHandler handler = getCurrentPlantHandler();
				if(handler!=null&&handler.isCorrectSoil(inventory[1], inventory[0]) && fertilizerAmount>0 && energyStorage.extractEnergy(IEConfig.Machines.belljar_consumption, true)==IEConfig.Machines.belljar_consumption)
				{
					energyStorage.extractEnergy(IEConfig.Machines.belljar_consumption, false);
					fertilizerAmount--;
					if(growth >= 1)
					{
						ItemStack[] outputs = handler.getOutput(inventory[1], inventory[0], this);
						int canFit = 0;
						for(int i=0; i<outputs.length; i++)
							if(outputs[i]!=null)
								for(int j=3;j<7;j++)
									if(inventory[j]==null || (ItemHandlerHelper.canItemStacksStack(inventory[j],outputs[i]) && inventory[j].stackSize+outputs[i].stackSize<=inventory[j].getMaxStackSize()))
										canFit++;
						if(canFit>=outputs.length)
						{
							for(ItemStack output : outputs)
								for(int j=3;j<7;j++)
								{
									if(inventory[j]==null)
									{
										inventory[j] = output.copy();
										break;
									}
									else if(ItemHandlerHelper.canItemStacksStack(inventory[j], output)&&inventory[j].stackSize+output.stackSize <= inventory[j].getMaxStackSize())
									{
										inventory[j].stackSize += output.stackSize;
										break;
									}
								}
							growth = handler.resetGrowth(inventory[1], inventory[0], growth, this, false);
						}
					}
					else if(growth < 1)
					{
						growth += handler.getGrowthStep(inventory[1], inventory[0], growth, this, fertilizerMod, false);
						if(worldObj.getTotalWorldTime()%32==((getPos().getX()^getPos().getZ())&31))
							sendSyncPacket(0);
					}
				}
				else
					growth = 0;

				if(fertilizerAmount<=0 && tank.getFluidAmount()>IEConfig.Machines.belljar_fluid)
				{
					FluidFertilizerHandler fluidFert = BelljarHandler.getFluidFertilizerHandler(tank.getFluid());
					if(fluidFert!=null)
					{
						fertilizerMod = fluidFert.getGrowthMultiplier(tank.getFluid(), inventory[1],inventory[0], this);
						tank.drain(IEConfig.Machines.belljar_fluid, true);
						if(inventory[2]!=null)
						{
							ItemFertilizerHandler itemFert = BelljarHandler.getItemFertilizerHandler(inventory[2]);
							if(itemFert!=null)
								fertilizerMod *= itemFert.getGrowthMultiplier(inventory[2], inventory[1], inventory[0], this);
							inventory[2].stackSize--;
							if(inventory[2].stackSize<=0)
								inventory[2]=null;
						}
						fertilizerAmount = IEConfig.Machines.belljar_fertilizer;
						sendSyncPacket(1);
					}
				}
			}
			else
				growth = 0;

			if(worldObj.getTotalWorldTime()%8==0)
			{
				BlockPos outputPos = getPos().up().offset(facing.getOpposite());
				TileEntity outputTile = this.worldObj.getTileEntity(outputPos);
				if(outputTile!=null)
					for(int j=3; j<7; j++)
						if(inventory[j]!=null)
						{
							int out = Math.min(inventory[j].stackSize, 16);
							ItemStack stack = Utils.copyStackWithAmount(inventory[j], out);
							stack = Utils.insertStackIntoInventory(outputTile, stack, facing);
							if(stack != null)
								out -= stack.stackSize;
							if((this.inventory[j].stackSize -= out) <= 0)
								this.inventory[j] = null;
						}
			}
		}
	}

	public IPlantHandler getCurrentPlantHandler()
	{
		if(curPlantHandler==null || !curPlantHandler.isValid(inventory[1]))
			curPlantHandler = BelljarHandler.getHandler(inventory[1]);
		return curPlantHandler;
	}

	protected void sendSyncPacket(int type)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		if(type==0)
		{
			nbt.setFloat("growth", growth);
			nbt.setInteger("energy", energyStorage.getEnergyStored());
		}
		else if(type==1)
		{
			nbt.setInteger("fertilizerAmount", fertilizerAmount);
			nbt.setFloat("fertilizerMod", fertilizerMod);
		}
		else if(type==2)
			nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		ImmersiveEngineering.packetHandler.sendToAllAround(new MessageTileSync(this, nbt), new TargetPoint(worldObj.provider.getDimension(),getPos().getX(), getPos().getY(), getPos().getZ(), 128));
	}
	@Override
	public void receiveMessageFromServer(NBTTagCompound message)
	{
		if(message.hasKey("growth"))
			renderGrowth = message.getFloat("growth");
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
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		dummy = nbt.getInteger("dummy");
		inventory = Utils.readInventory(nbt.getTagList("inventory",10),7);
		energyStorage.readFromNBT(nbt);
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		fertilizerAmount = nbt.getInteger("fertilizerAmount");
		fertilizerMod = nbt.getFloat("fertilizerMod");
		growth = nbt.getFloat("growth");
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
		for(int i=1; i<=2; i++)
		{
			worldObj.setBlockState(pos.up(i), state);
			((TileEntityBelljar)worldObj.getTileEntity(pos.up(i))).dummy = i;
			((TileEntityBelljar)worldObj.getTileEntity(pos.up(i))).facing = facing;
		}
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i=0; i<=2; i++)
		{
			BlockPos p = getPos().down(dummy).up(i);
			if(worldObj.getTileEntity(p) instanceof TileEntityBelljar)
				worldObj.setBlockToAir(p);
		}
	}

	@Override
	public ItemStack[] getInventory()
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
		return slot<2?1:64;
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
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return dummy==0?(facing==null||facing.getAxis()!=this.facing.rotateY().getAxis()): dummy==1&&(facing==null||facing==this.facing.getOpposite());
		else if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return dummy==0 && (facing==null||facing.getAxis()!=this.facing.rotateY().getAxis());
		return super.hasCapability(capability, facing);
	}
	IItemHandler inputHandler = new IEInventoryHandler(1,this,2, true,false);
	IItemHandler outputHandler = new IEInventoryHandler(4,this,3, false,true);
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			if(dummy==0 && (facing==null||facing.getAxis()!=this.facing.rotateY().getAxis()))
				return (T)inputHandler;
			if(dummy==1 && (facing==null||facing==this.facing.getOpposite()))
				return (T)outputHandler;
		}
		else if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dummy==0 && (facing==null||facing.getAxis()!=this.facing.rotateY().getAxis()))
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
	public TileEntity getGuiMaster()
	{
		if(dummy==0)
			return this;
		TileEntity te = worldObj.getTileEntity(getPos().down(dummy));
		if(te instanceof TileEntityBelljar)
			return te;
		return null;
	}


	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getTextureReplacement(IBlockState object, String material)
	{
		if(inventory[0]!=null && "farmland".equals(material))
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
		return !"extension".equals(group) ;//&& !"base_glass".equals(group);
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
		if(inventory[0]!=null)
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
		ResourceLocation rl = curPlantHandler!=null?curPlantHandler.getSoilTexture(inventory[1], inventory[0], this):null;
		if(rl==null)
			rl=BelljarHandler.getSoilTexture(inventory[0]);
		if(rl==null)
		{
			IBlockState state = Utils.getStateFromItemStack(inventory[0]);
			if(state!=null)
				rl = ClientUtils.getSideTexture(state, EnumFacing.UP);
		}
		if(rl==null && inventory[0]!=null && Utils.isFluidRelatedItemStack(inventory[0]))
		{
			FluidStack fs = FluidUtil.getFluidContained(inventory[0]);
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
			TileEntity te = worldObj.getTileEntity(getPos().down(dummy));
			if(te instanceof TileEntityBelljar)
				return ((TileEntityBelljar)te).energyStorage;
		}
		return this.energyStorage;
	}
	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable EnumFacing facing)
	{
		return facing==null||(dummy==0&&facing.getAxis()==this.facing.rotateY().getAxis())||(dummy==2&&facing==EnumFacing.UP)?SideConfig.INPUT:SideConfig.NONE;
	}
	IEForgeEnergyWrapper energyWrapper = new IEForgeEnergyWrapper(this, EnumFacing.UP);
	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(facing==null||(dummy==0&&facing.getAxis()==this.facing.rotateY().getAxis())||(dummy==2&&facing==EnumFacing.UP))
			return energyWrapper;
		return null;
	}
}