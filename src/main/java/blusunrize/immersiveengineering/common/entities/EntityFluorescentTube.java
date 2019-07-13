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
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilTileEntity;
import blusunrize.immersiveengineering.common.items.ItemFluorescentTube;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityFluorescentTube extends Entity implements ITeslaEntity
{
	public static final EntityType<EntityFluorescentTube> TYPE = new Builder<>(EntityFluorescentTube.class, EntityFluorescentTube::new)
			.build(ImmersiveEngineering.MODID+":fluorescent_tube");

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
		super(TYPE, world);
		setSize(tubeLength/2, 1+tubeLength/2);
	}


	@Override
	public void tick()
	{
		super.tick();
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
	protected void registerData()
	{
		dataManager.register(dataMarker_r, 1F);
		dataManager.register(dataMarker_g, 1F);
		dataManager.register(dataMarker_b, 1F);
		dataManager.register(dataMarker_active, false);
		dataManager.register(dataMarker_angleHorizontal, 0F);
	}

	@Override
	protected void readAdditional(CompoundNBT nbt)
	{
		CompoundNBT comp = nbt.getCompound("nbt");
		rgb = new float[]{comp.getFloat("r"), comp.getFloat("g"), comp.getFloat("b")};
		angleHorizontal = nbt.getFloat("angleHor");

	}

	@Override
	protected void writeAdditional(CompoundNBT nbt)
	{
		CompoundNBT comp = new CompoundNBT();
		comp.putFloat("r", rgb[0]);
		comp.putFloat("g", rgb[1]);
		comp.putFloat("b", rgb[2]);
		nbt.put("nbt", comp);
		nbt.putFloat("angleHor", angleHorizontal);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if(isAlive()&&!world.isRemote)
		{
			ItemStack tube = new ItemStack(IEContent.itemFluorescentTube);
			ItemFluorescentTube.setRGB(tube, rgb);
			ItemEntity ent = new ItemEntity(world, posX, posY, posZ, tube);
			world.spawnEntity(ent);
			remove();
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return isAlive();
	}

	@Override
	public void onHit(TileEntity te, boolean lowPower)
	{
		if(te instanceof TeslaCoilTileEntity&&((TeslaCoilTileEntity)te).energyStorage.extractEnergy(1, false) > 0)
		{
			timer = 35;
			dataManager.set(dataMarker_active, true);
		}
	}

	@Nonnull
	@Override
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d targetVec3, Hand hand)
	{
		if(Utils.isHammer(player.getHeldItem(hand)))
		{
			angleHorizontal += (player.isSneaking()?10: 1);
			angleHorizontal %= 360;
			dataManager.set(dataMarker_angleHorizontal, angleHorizontal);
			return ActionResultType.SUCCESS;
		}
		return super.applyPlayerInteraction(player, targetVec3, hand);
	}
}
