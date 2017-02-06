package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class TileEntityTurret extends TileEntityIEBase implements ITickable, IIEInternalFluxHandler, IIEInventory, IHasDummyBlocks, ITileDrop, IDirectionalTile, IBlockBounds, IGuiTile, IHammerInteraction, IHasObjProperty
{
	public boolean dummy = false;
	public FluxStorage energyStorage = new FluxStorage(16000);
	public boolean redstoneControlInverted = false;
	public EnumFacing facing = EnumFacing.NORTH;

	public String owner;
	public List<String> targetList = new ArrayList<>();
	public boolean whitelist = false;
	public boolean attackAnimals = false;
	public boolean attackPlayers = true;
	public boolean attackNeutrals = false;

	protected int tick = 0;
	protected EntityLivingBase target;
	public float rotationYaw;
	public float rotationPitch;

	@Override
	public void update()
	{
		if(dummy)
			return;

		if(target!=null)
		{
			double dX = target.posX-(getPos().getX()+.5);
			double dY = target.posY-(getPos().getY()+.5);
			double dZ = target.posZ-(getPos().getZ()+.5);
			double dSq = dX*dX+dY*dY+dZ*dZ;
			double r = getRange();
			if(dSq > r*r)
				this.target=null;
			else
			if(worldObj.isRemote)
			{
				float facingYaw = facing==EnumFacing.NORTH?180: facing==EnumFacing.WEST?-90: facing==EnumFacing.EAST?90: 0;
				double yaw = (MathHelper.atan2(dX, dZ)*(180/Math.PI))-facingYaw;
				this.rotationPitch = (float)(Math.atan2(Math.sqrt(dZ*dZ+dX*dX), dY)*(180/Math.PI))-90;
				if(this.rotationYaw==0)//moving from default
					this.rotationYaw = (float)(yaw*.5);
				else
					this.rotationYaw = (float)yaw;
			}
		}
		else if(worldObj.isRemote)
		{
			this.rotationYaw*=.75;
			if(Math.abs(rotationYaw)<10)
				this.rotationYaw = 0;
			this.rotationPitch*=.75;
			if(Math.abs(rotationPitch)<10)
				this.rotationPitch = 0;
		}


		if(worldObj.isRemote)
			return;
		if(worldObj.getTotalWorldTime()%64==((getPos().getX()^getPos().getZ())&63))
			markContainingBlockForUpdate(null);

		int energy = IEConfig.Machines.turret_consumption;
		if(energyStorage.extractEnergy(energy, true)==energy)
		{
			energyStorage.extractEnergy(energy, false);
			if(target==null||target.isDead||worldObj.getEntityByID(target.getEntityId())==null||target.getHealth() <= 0)
			{
				target = getTarget();
				if(target!=null)
				{
					this.markDirty();
					markContainingBlockForUpdate(null);
				}
			}

			//has target, Redstone control check and has power+ammo
			if(target!=null && (worldObj.isBlockIndirectlyGettingPowered(getPos())>0^redstoneControlInverted) && canActivate())
			{
				tick++;
				int chargeup = getChargeupTicks();
				if(tick==chargeup)
					this.activate();
				else if(tick > chargeup)
				{
					if(loopActivation())
						this.activate();
					else if(tick==chargeup+getActiveTicks())
						tick = 0;
				}
			} else
				tick = 0;
		}
	}

	private EntityLivingBase getTarget()
	{
		double range = getRange();
		List<EntityLivingBase> list = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(getPos().getX()-range,getPos().getY(),getPos().getZ()-range, getPos().getX()+range,getPos().getY()+3,getPos().getZ()+range));
		if(list.isEmpty())
			return null;
		EntityLivingBase target = null;
		for(EntityLivingBase entity : list)
		{
			if(entity==null || entity.isDead)
				continue;
			//Continue if blacklist and name is in list, or whitelist and name is not in list
			if(whitelist ^ isListedName(targetList, entity.getName()))
				continue;
			//Same as above but for the owner of the pet, to prevent shooting wolves
			if(entity instanceof IEntityOwnable)
				if(whitelist ^ isListedName(targetList, ((IEntityOwnable)entity).getOwner().getName()))
					continue;

			if(entity instanceof EntityAnimal && !attackAnimals)
				continue;
			if(entity instanceof EntityPlayer && !attackPlayers)
				continue;
			if(!entity.isCreatureType(EnumCreatureType.MONSTER, false) && !attackNeutrals)
				continue;

			if(target==null || entity.getDistanceSq(getPos())<target.getDistanceSq(getPos()))
				target = entity;
		}
		return target;
	}
	private boolean isListedName(List<String> list, String name)
	{
		for(String s : list)
			if(s!=null && s.equalsIgnoreCase(name))
				return true;
		return false;
	}

	protected abstract double getRange();
	protected abstract boolean canActivate();
	protected abstract int getChargeupTicks();
	protected abstract int getActiveTicks();
	protected abstract boolean loopActivation();
	protected abstract void activate();

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		if(message.hasKey("add"))
			targetList.add(message.getString("add"));
		if(message.hasKey("remove"))
			targetList.remove(message.getInteger("remove"));
		if(message.hasKey("whitelist"))
			whitelist = message.getBoolean("whitelist");
		if(message.hasKey("attackAnimals"))
			attackAnimals = message.getBoolean("attackAnimals");
		if(message.hasKey("attackPlayers"))
			attackPlayers = message.getBoolean("attackPlayers");
		if(message.hasKey("attackNeutrals"))
			attackNeutrals = message.getBoolean("attackNeutrals");
		this.markDirty();
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		dummy = nbt.getBoolean("dummy");
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		energyStorage.readFromNBT(nbt);

		if(nbt.hasKey("owner"))
			owner = nbt.getString("owner");
		NBTTagList list = nbt.getTagList("targetList", 8);
		targetList.clear();
		for(int i=0; i<list.tagCount(); i++)
			targetList.add(list.getStringTagAt(i));
		whitelist = nbt.getBoolean("whitelist");
		attackAnimals = nbt.getBoolean("attackAnimals");
		attackPlayers = nbt.getBoolean("attackPlayers");
		attackNeutrals = nbt.getBoolean("attackNeutrals");

		target = null;
		if(nbt.hasKey("target"))
		{
			int targetId = nbt.getInteger("target");
			Entity ent = worldObj.getEntityByID(targetId);
			if(ent instanceof EntityLivingBase && !ent.isDead)
				target = (EntityLivingBase)ent;
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setBoolean("dummy", dummy);
		nbt.setBoolean("redstoneInverted", redstoneControlInverted);
		if(facing!=null)
			nbt.setInteger("facing", facing.ordinal());
		energyStorage.writeToNBT(nbt);

		if(owner!=null)
			nbt.setString("owner", owner);
		NBTTagList list = new NBTTagList();
		for(String s : targetList)
			list.appendTag(new NBTTagString(s));
		nbt.setTag("targetList", list);
		nbt.setBoolean("whitelist",whitelist);
		nbt.setBoolean("attackAnimals",attackAnimals);
		nbt.setBoolean("attackPlayers",attackPlayers);
		nbt.setBoolean("attackNeutrals",attackNeutrals);

		if(target!=null)
			nbt.setInteger("target", target.getEntityId());
	}

	@Override
	public float[] getBlockBounds()
	{
		if(!dummy)
			return null;
		switch(facing)
		{
			case NORTH:
				return new float[]{.125f,.0625f,.125f, .875f,.875f,1};
			case SOUTH:
				return new float[]{.125f,.0625f,0, .875f,.875f,.875f};
			case WEST:
				return new float[]{.125f,.0625f,.125f, 1,.875f,.875f};
			case EAST:
				return new float[]{0,.0625f,.125f, .875f,.875f,.875f};
		}
		return null;
	}

	AxisAlignedBB renderBB;
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderBB==null)
			renderBB = new AxisAlignedBB(getPos().add(-8,-8,-8),getPos().add(8,8,8));
		return renderBB;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
//		if(dummy)
//		{
//			TileEntity te = worldObj.getTileEntity(getPos().offset(facing,-1));
//			if(te instanceof TileEntityTurret)
//				return ((TileEntityTurret)te).hammerUseSide(side, player, hitX, hitY, hitZ);
//			return false;
//		}
//		if(!player.isSneaking())
//		{
//			redstoneControlInverted = !redstoneControlInverted;
//			ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn":"invertedOff")));
//			markDirty();
//			this.markContainingBlockForUpdate(null);
//		}
		return true;
	}

	@Override
	public ItemStack[] getInventory()
	{
		return new ItemStack[0];
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
	public void doGraphicalUpdates(int slot)
	{

	}

	@Override
	public boolean canOpenGui()
	{
		return true;
	}
	@Override
	public int getGuiID()
	{
		return Lib.GUIID_Turret;
	}
	@Override
	public TileEntity getGuiMaster()
	{
		if(!dummy)
			return this;
		TileEntity te = worldObj.getTileEntity(getPos().up());
		if(te instanceof TileEntityTurret)
			return te;
		return null;
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
		return false;
	}
	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}

	@Override
	public boolean isDummy()
	{
		return dummy;
	}
	@Override
	public void placeDummies(BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		worldObj.setBlockState(pos.up(), state);
		((TileEntityTurret)worldObj.getTileEntity(pos.up())).dummy = true;
		((TileEntityTurret)worldObj.getTileEntity(pos.up())).facing = facing;
	}
	@Override
	public void breakDummies(BlockPos pos, IBlockState state)
	{
		if(worldObj.getTileEntity(dummy?getPos().down():getPos().up()) instanceof TileEntityTurret)
			worldObj.setBlockToAir(dummy?getPos().down():getPos().up());
	}

	@Override
	public ItemStack getTileDrop(EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		TileEntityTurret turret = this;
		if(dummy)
		{
			TileEntity t = worldObj.getTileEntity(getPos().down());
			if(t instanceof TileEntityTurret)
				turret = (TileEntityTurret)t;
			else
				return stack;
		}

		NBTTagCompound tag = new NBTTagCompound();
		//Only writing values when they are different from defaults
		if(player==null || !player.getName().equalsIgnoreCase(turret.owner))
			tag.setString("owner", turret.owner);
		if(turret.targetList.size()!=1 || !isListedName(turret.targetList, turret.owner))
		{
			NBTTagList list = new NBTTagList();
			for(String s : turret.targetList)
				list.appendTag(new NBTTagString(s));
			tag.setTag("targetList",list);
		}
		if(turret.whitelist)
			tag.setBoolean("whitelist",turret.whitelist);
		if(turret.attackAnimals)
			tag.setBoolean("attackAnimals",turret.attackAnimals);
		if(!turret.attackPlayers)
			tag.setBoolean("attackPlayers",turret.attackPlayers);
		if(turret.attackNeutrals)
			tag.setBoolean("attackNeutrals",turret.attackNeutrals);
		if(turret.redstoneControlInverted)
			tag.setBoolean("redstoneControlInverted",turret.redstoneControlInverted);

		if(!tag.hasNoTags())
			stack.setTagCompound(tag);
		return stack;
	}
	@Override
	public void readOnPlacement(@Nullable EntityLivingBase placer, ItemStack stack)
	{
		if(stack.hasTagCompound())
		{
			NBTTagCompound tag = stack.getTagCompound();
			if(tag.hasKey("owner"))
				this.owner = tag.getString("owner");
			else if(placer!=null)
				this.owner = placer.getName();
			if(tag.hasKey("targetList"))
			{
				NBTTagList list = tag.getTagList("targetList", 8);
				targetList.clear();
				for(int i=0; i<list.tagCount(); i++)
					targetList.add(list.getStringTagAt(i));
			}
			else if(owner!=null)
				targetList.add(owner);
			if(tag.hasKey("whitelist"))
				whitelist = tag.getBoolean("whitelist");
			if(tag.hasKey("attackAnimals"))
				attackAnimals = tag.getBoolean("attackAnimals");
			if(tag.hasKey("attackPlayers"))
				attackPlayers = tag.getBoolean("attackPlayers");
			if(tag.hasKey("attackNeutrals"))
				attackNeutrals = tag.getBoolean("attackNeutrals");
			if(tag.hasKey("redstoneControlInverted"))
				redstoneControlInverted = tag.getBoolean("redstoneControlInverted");
		}
		else if(placer!=null)
		{
			this.owner = placer.getName();
			targetList.add(owner);
		}
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy)
		{
			TileEntity te = worldObj.getTileEntity(getPos().down());
			if(te instanceof TileEntityTurret)
				return ((TileEntityTurret)te).getFluxStorage();
		}
		return energyStorage;
	}
	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(EnumFacing facing)
	{
		return !dummy?SideConfig.INPUT:SideConfig.NONE;
	}
	IEForgeEnergyWrapper[] wrappers = IEForgeEnergyWrapper.getDefaultWrapperArray(this);
	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing)
	{
		if(!dummy)
			return wrappers[facing==null?0:facing.ordinal()];
		return null;
	}

	static ArrayList<String> displayList = Lists.newArrayList("base");
	@Override
	public ArrayList<String> compileDisplayList()
	{
		return displayList;
	}
}