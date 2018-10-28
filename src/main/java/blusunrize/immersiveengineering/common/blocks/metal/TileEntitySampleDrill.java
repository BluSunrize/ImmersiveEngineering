/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralWorldInfo;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class TileEntitySampleDrill extends TileEntityIEBase implements ITickable, IIEInternalFluxHandler, IHasDummyBlocks, IPlayerInteraction, IHasObjProperty
{
	public FluxStorage energyStorage = new FluxStorage(8000);
	public int dummy = 0;
	public int process = 0;
	public boolean active = false;
	@Nonnull
	public ItemStack sample = ItemStack.EMPTY;

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public void update()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(dummy!=0||world.isAirBlock(getPos().add(0, -1, 0))||!sample.isEmpty())
			return;
		if(world.isRemote&&active)
		{
			process++;
			return;
		}

		boolean powered = world.getRedstonePowerFromNeighbors(getPos()) > 0;
		final boolean prevActive = active;
		if(!active&&powered)
			active = true;
		else if(active&&!powered&&process >= IEConfig.Machines.coredrill_time)
			active = false;


		if(active&&process < IEConfig.Machines.coredrill_time)
			if(energyStorage.extractEnergy(IEConfig.Machines.coredrill_consumption, false)==IEConfig.Machines.coredrill_consumption)
			{
				process++;
				if(process >= IEConfig.Machines.coredrill_time)
				{
					int cx = getPos().getX() >> 4;
					int cz = getPos().getZ() >> 4;
					MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(world, cx, cz);
					this.sample = createCoreSample(world, (getPos().getX() >> 4), (getPos().getZ() >> 4), info);
				}
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		if(prevActive!=active)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	public float getSampleProgress()
	{
		return process/(float)IEConfig.Machines.coredrill_time;
	}

	public boolean isSamplingFinished()
	{
		return process >= IEConfig.Machines.coredrill_time;
	}

	public String getVein()
	{
		if(sample.isEmpty())
			return "";
		return sample.getTagCompound().getString("mineral");
	}

	public int getExpectedVeinYield()
	{
		if(sample.isEmpty())
			return -1;
		return ExcavatorHandler.mineralVeinCapacity-sample.getTagCompound().getInteger("depletion");
	}

	@Nonnull
	public ItemStack createCoreSample(World world, int chunkX, int chunkZ, @Nullable MineralWorldInfo info)
	{
		ItemStack stack = new ItemStack(IEContent.itemCoresample);
		ItemNBTHelper.setLong(stack, "timestamp", world.getTotalWorldTime());
		ItemNBTHelper.setIntArray(stack, "coords", new int[]{world.provider.getDimension(), chunkX, chunkZ});
		if(info==null)
			return stack;
		if(info.mineralOverride!=null)
			ItemNBTHelper.setString(stack, "mineral", info.mineralOverride.name);
		else if(info.mineral!=null)
			ItemNBTHelper.setString(stack, "mineral", info.mineral.name);
		else
			return stack;
		if(ExcavatorHandler.mineralVeinCapacity < 0||info.depletion < 0)
			ItemNBTHelper.setBoolean(stack, "infinite", true);
		else
			ItemNBTHelper.setInt(stack, "depletion", info.depletion);
		return stack;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.setInteger("dummy", dummy);
		nbt.setInteger("process", process);
		nbt.setBoolean("active", active);
		if(!sample.isEmpty())
			nbt.setTag("sample", sample.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		dummy = nbt.getInteger("dummy");
		process = nbt.getInteger("process");
		active = nbt.getBoolean("active");
		if(nbt.hasKey("sample"))
			sample = new ItemStack(nbt.getCompoundTag("sample"));

	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(dummy==0)
				renderAABB = new AxisAlignedBB(getPos(), getPos().add(1, 3, 1));
			else
				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}


	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy > 0)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(te instanceof TileEntitySampleDrill)
				return ((TileEntitySampleDrill)te).getFluxStorage();
		}
		return energyStorage;
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return dummy==0&&facing!=null&&facing.getAxis()!=Axis.Y?SideConfig.INPUT: SideConfig.NONE;
	}

	IEForgeEnergyWrapper[] wrappers = {
			new IEForgeEnergyWrapper(this, EnumFacing.NORTH),
			new IEForgeEnergyWrapper(this, EnumFacing.SOUTH),
			new IEForgeEnergyWrapper(this, EnumFacing.WEST),
			new IEForgeEnergyWrapper(this, EnumFacing.EAST)
	};

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(dummy==0&&facing!=null&&facing.getAxis()!=Axis.Y)
			return wrappers[facing.ordinal()-2];
		return null;
	}

	@Override
	public boolean isDummy()
	{
		return dummy > 0;
	}

	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		for(int i = 1; i <= 2; i++)
		{
			world.setBlockState(pos.add(0, i, 0), state);
			((TileEntitySampleDrill)world.getTileEntity(pos.add(0, i, 0))).dummy = i;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		for(int i = 0; i <= 2; i++)
			if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof TileEntitySampleDrill)
				world.setBlockToAir(getPos().add(0, -dummy, 0).add(0, i, 0));
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(dummy!=0)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(te instanceof TileEntitySampleDrill)
				return ((TileEntitySampleDrill)te).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
		}

		if(!this.sample.isEmpty())
		{
			if(!world.isRemote)
				player.entityDropItem(this.sample.copy(), .5f);
			this.sample = ItemStack.EMPTY;
			this.active = false;
			markDirty();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		else if(!this.active)
		{
			if(energyStorage.getEnergyStored() >= IEConfig.Machines.coredrill_consumption)
			{
				this.active = true;
				markDirty();
				this.markContainingBlockForUpdate(null);
			}
			return true;
		}
		return false;
		//		int off = ((TileEntitySampleDrill)te).pos;
		//		TileEntity te2 = world.getTileEntity(x, y-off, z);
		//		if(!world.isRemote && te2 instanceof TileEntitySampleDrill)
		//		{
		//			TileEntitySampleDrill drill = (TileEntitySampleDrill)te2;
		//			int process = drill.process;
		//			int chunkX = (x>>4);
		//			int chunkZ = (z>>4);
		//			String s0 = (chunkX*16)+", "+(chunkZ*16);
		//			String s1 = (chunkX*16+16)+", "+(chunkZ*16+16);
		//			player.sendMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"forChunk", s0,s1).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_GRAY)));
		//			if(process<Config.getInt("coredrill_time"))
		//			{
		//				float f = process/(float)Config.getInt("coredrill_time");
		//				player.sendMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"coreDrill.progress",(int)(f*100)+"%").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
		//			}
		//			else
		//			{
		//				ExcavatorHandler.MineralMix mineral = ExcavatorHandler.getRandomMineral(world, chunkX, chunkZ);
		//				if(mineral==null)
		//					player.sendMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"coreDrill.result.none").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
		//				else
		//				{
		//					String name = Lib.DESC_INFO+"mineral."+mineral.name;
		//					String localizedName = StatCollector.translateToLocal(name);
		//					if(name.equals(localizedName))
		//						localizedName = mineral.name;
		//					MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(world, chunkX, chunkZ);
		//					boolean deplOverride = info.depletion<0;
		//					if(ExcavatorHandler.mineralVeinCapacity<0||deplOverride)
		//						localizedName = StatCollector.translateToLocal(Lib.CHAT_INFO+"coreDrill.infinite")+" "+localizedName;
		//					player.sendMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"coreDrill.result.mineral",localizedName));
		//					if(ExcavatorHandler.mineralVeinCapacity>0&&!deplOverride)
		//					{
		//						String f = Utils.formatDouble((Config.getInt("excavator_depletion")-info.depletion)/(float)Config.getInt("excavator_depletion")*100,"0.##")+"%";
		//						player.sendMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"coreDrill.result.depl",f));
		//					}
		//				}
		//			}
		//		}
		//		return true;
	}

	static ArrayList<String> displayList = Lists.newArrayList("drill_base");

	@Override
	public ArrayList<String> compileDisplayList()
	{
		return displayList;
	}

	@Nullable
	public String getVeinLocalizedName()
	{
		String name = getVein();
		if(name==null||name.isEmpty())
			return null;
		String unlocalizedName = Lib.DESC_INFO+"mineral."+name;
		String localizedName = I18n.format(unlocalizedName);
		if(unlocalizedName.equals(localizedName))
			return name;
		return localizedName;
	}

	public float getVeinIntegrity()
	{
		if(sample.isEmpty())
			return 0;
		else if(ItemNBTHelper.hasKey(sample, "infinite"))
			return -1;
		else if(ItemNBTHelper.hasKey(sample, "depletion"))
			return 1-ItemNBTHelper.getInt(sample, "depletion")/(float)ExcavatorHandler.mineralVeinCapacity;
		return 0;
	}
}