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
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilTileEntity;
import blusunrize.immersiveengineering.common.items.FluorescentTubeItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;

public class FluorescentTubeEntity extends Entity implements ITeslaEntity
{
	public static final float TUBE_LENGTH = 1.5F;
	public static final EntityType<FluorescentTubeEntity> TYPE = Builder
			.<FluorescentTubeEntity>create(FluorescentTubeEntity::new, EntityClassification.MISC)
			.size(TUBE_LENGTH/2, 1+TUBE_LENGTH/2)
			.build(ImmersiveEngineering.MODID+":fluorescent_tube");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "fluorescent_tube");
	}

	private static final DataParameter<Boolean> dataMarker_active = EntityDataManager.createKey(FluorescentTubeEntity.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Float> dataMarker_r = EntityDataManager.createKey(FluorescentTubeEntity.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> dataMarker_g = EntityDataManager.createKey(FluorescentTubeEntity.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> dataMarker_b = EntityDataManager.createKey(FluorescentTubeEntity.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> dataMarker_angleHorizontal = EntityDataManager.createKey(FluorescentTubeEntity.class, DataSerializers.FLOAT);

	private int timer = 0;
	public boolean active = false;
	public float[] rgb = new float[4];
	boolean firstTick = true;
	public float angleHorizontal = 0;

	public FluorescentTubeEntity(World world, ItemStack tube, float angleVert)
	{
		this(TYPE, world);
		rotationYaw = angleVert;
		rgb = FluorescentTubeItem.getRGB(tube);
	}

	public FluorescentTubeEntity(EntityType<FluorescentTubeEntity> type, World world)
	{
		super(type, world);
	}


	@Override
	public void tick()
	{
		super.tick();
		//movement logic
		this.prevPosX = this.getPosX();
		this.prevPosY = this.getPosY();
		this.prevPosZ = this.getPosZ();
		Vector3d motion = getMotion();
		motion = motion.add(0, -.4, 0);
		this.move(MoverType.SELF, motion);
		motion = motion.scale(0.98);

		if(this.onGround)
			motion = new Vector3d(motion.x*0.7, motion.y*-0.5, motion.z*0.7);
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
		setMotion(motion);
	}

	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
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
			ItemStack tube = new ItemStack(Misc.fluorescentTube);
			FluorescentTubeItem.setRGB(tube, rgb);
			ItemEntity ent = new ItemEntity(world, getPosX(), getPosY(), getPosZ(), tube);
			world.addEntity(ent);
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
	public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d targetVec3, Hand hand)
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
