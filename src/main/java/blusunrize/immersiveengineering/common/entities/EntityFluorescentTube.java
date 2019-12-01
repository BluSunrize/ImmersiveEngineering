/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ITeslaEntity;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTeslaCoil;
import blusunrize.immersiveengineering.common.items.ItemFluorescentTube;
import blusunrize.immersiveengineering.common.util.Utils;
import elucent.albedo.lighting.ILightProvider;
import elucent.albedo.lighting.Light;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@net.minecraftforge.fml.common.Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo")
public class EntityFluorescentTube extends Entity implements ITeslaEntity, ILightProvider
{
	private static final DataParameter<Boolean> dataMarker_active = EntityDataManager.createKey(EntityFluorescentTube.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Float> dataMarker_r = EntityDataManager.createKey(EntityFluorescentTube.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> dataMarker_g = EntityDataManager.createKey(EntityFluorescentTube.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> dataMarker_b = EntityDataManager.createKey(EntityFluorescentTube.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> dataMarker_angleHorizontal = EntityDataManager.createKey(EntityFluorescentTube.class, DataSerializers.FLOAT);

	private int timer = 0;
	public boolean active = false;
	public float[] rgb = new float[4];
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
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if(this.onGround)
		{
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;
		}
		if(firstTick&&!world.isRemote&&rgb!=null)
		{
			dataManager.set(dataMarker_r, rgb[0]);
			dataManager.set(dataMarker_g, rgb[1]);
			dataManager.set(dataMarker_b, rgb[2]);
			dataManager.set(dataMarker_angleHorizontal, angleHorizontal);
			firstTick = false;
		}
		// tube logic
		if(timer > 0&&!world.isRemote)
		{
			timer--;
			if(timer <= 0)
				dataManager.set(dataMarker_active, false);
		}
		if(world.isRemote)
		{
			active = dataManager.get(dataMarker_active);
			rgb = new float[]{dataManager.get(dataMarker_r),
					dataManager.get(dataMarker_g),
					dataManager.get(dataMarker_b)};
			angleHorizontal = dataManager.get(dataMarker_angleHorizontal);
		}
	}

	@Override
	protected void entityInit()
	{
		dataManager.register(dataMarker_r, 1F);
		dataManager.register(dataMarker_g, 1F);
		dataManager.register(dataMarker_b, 1F);
		dataManager.register(dataMarker_active, false);
		dataManager.register(dataMarker_angleHorizontal, 0F);
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
		if(!isDead&&!world.isRemote)
		{
			ItemStack tube = new ItemStack(IEContent.itemFluorescentTube);
			ItemFluorescentTube.setRGB(tube, rgb);
			EntityItem ent = new EntityItem(world, posX, posY, posZ, tube);
			world.spawnEntity(ent);
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
	public AxisAlignedBB getEntityBoundingBox()
	{
		return super.getEntityBoundingBox();
	}

	@Override
	public void onHit(TileEntity te, boolean lowPower)
	{
		if(te instanceof TileEntityTeslaCoil&&((TileEntityTeslaCoil)te).energyStorage.extractEnergy(1, false) > 0)
		{
			timer = 35;
			dataManager.set(dataMarker_active, true);
		}
	}

	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d targetVec3, EnumHand hand)
	{
		if(Utils.isHammer(player.getHeldItem(hand)))
		{
			angleHorizontal += (player.isSneaking()?10: 1);
			angleHorizontal %= 360;
			dataManager.set(dataMarker_angleHorizontal, angleHorizontal);
			return EnumActionResult.SUCCESS;
		}
		return super.applyPlayerInteraction(player, targetVec3, hand);
	}

	@Override
	public Light provideLight()
	{
		return active?Light.builder().pos(this).radius(10f).color(rgb[0],rgb[1],rgb[2]).build():null;
	}
}
