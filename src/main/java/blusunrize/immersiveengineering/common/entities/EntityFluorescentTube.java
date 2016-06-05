package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.ITeslaEntity;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;
import blusunrize.immersiveengineering.common.items.ItemFluorescentTube;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityFluorescentTube extends Entity implements ITeslaEntity
{
	static int marker_active = 12;
	static int marker_r = 13;
	static int marker_g = 14;
	static int marker_b = 15;
	static int marker_angleHorizontal = 16;

	private int timer = 0;
	public boolean active = false;
	public float[] rgb;
	boolean firstTick = true;
	public float angleHorizontal = 0;
	public float tubeLength = 1.5F;

	public EntityFluorescentTube(World world, ItemStack tube, float angleVert)
	{
		this(world);
		rotationYaw = angleVert;
		rgb = ItemFluorescentTube.getRGB(tube);
	}
	public EntityFluorescentTube(World world)
	{
		super(world);
		setSize(tubeLength/2, 1+tubeLength/2);
	}


	@Override
	public void onUpdate()
	{
		super.onUpdate();
		//movement logic
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.03999999910593033D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if (this.onGround)
		{
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;
		}
		if (firstTick&&!worldObj.isRemote&&rgb!=null)
		{
			dataWatcher.updateObject(marker_r, rgb[0]);
			dataWatcher.updateObject(marker_g, rgb[1]);
			dataWatcher.updateObject(marker_b, rgb[2]);
			dataWatcher.updateObject(marker_angleHorizontal, angleHorizontal);
			firstTick = false;
		}
		// tube logic
		if (timer>0&&!worldObj.isRemote)
		{
			timer--;
			if (timer<=0)
				dataWatcher.updateObject(marker_active, (byte)-1);
		}
		if (worldObj.isRemote)
		{
			active = dataWatcher.getWatchableObjectByte(marker_active)>0;
			rgb = new float[]{dataWatcher.getWatchableObjectFloat(marker_r),
					dataWatcher.getWatchableObjectFloat(marker_g),
					dataWatcher.getWatchableObjectFloat(marker_b)};
			angleHorizontal = dataWatcher.getWatchableObjectFloat(marker_angleHorizontal);
		}
	}
	@Override
	protected void entityInit()
	{
		dataWatcher.addObject(marker_r, 1F);
		dataWatcher.addObject(marker_g, 1F);
		dataWatcher.addObject(marker_b, 1F);
		dataWatcher.addObject(marker_active, Byte.valueOf((byte) 0));
		dataWatcher.addObject(marker_angleHorizontal, 0F);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		NBTTagCompound comp = nbt.getCompoundTag("nbt");
		rgb = new float[]{comp.getFloat("r"), comp.getFloat("g"), comp.getFloat("b")};
		angleHorizontal = nbt.getFloat("angleHor");

	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		NBTTagCompound comp = new NBTTagCompound();
		comp.setFloat("r", rgb[0]);
		comp.setFloat("g", rgb[1]);
		comp.setFloat("b", rgb[2]);
		nbt.setTag("nbt", comp);
		nbt.setFloat("angleHor", angleHorizontal);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if (!isDead&&!worldObj.isRemote)
		{
			ItemStack tube = new ItemStack(IEContent.itemFluorescentTube);
			ItemFluorescentTube.setRGB(tube, rgb);
			EntityItem ent = new EntityItem(worldObj, posX, posY, posZ, tube);
			worldObj.spawnEntityInWorld(ent);
			setDead();
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return !isDead;
	}
	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		// TODO Auto-generated method stub
		return super.getEntityBoundingBox();
	}
	@Override
	public void onHit(TileEntityTeslaCoil te, boolean lowPower)
	{
		if (te.energyStorage.extractEnergy(1, false)>0)
		{
			timer = 35;
			dataWatcher.updateObject(marker_active, (byte)1);
		}
	}
	@Override
	public boolean interactAt(EntityPlayer player, Vec3 targetVec3) {
		if (Utils.isHammer(player.getEquipmentInSlot(0)))
		{
			angleHorizontal+=(player.isSneaking()?10:1);
			angleHorizontal%=360;
			dataWatcher.updateObject(marker_angleHorizontal, angleHorizontal);
			return true;
		}
		return super.interactAt(player, targetVec3);
	}
}
