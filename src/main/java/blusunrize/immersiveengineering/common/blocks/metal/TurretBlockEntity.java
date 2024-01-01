/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class TurretBlockEntity<T extends TurretBlockEntity<T>> extends IEBaseBlockEntity implements
		IEServerTickableBE, IEClientTickableBE, IIEInventory, IHasDummyBlocks, IBlockEntityDrop,
		IStateBasedDirectional, IBlockBounds, IInteractionObjectIE<T>, IEntityProof, IScrewdriverInteraction,
		IModelOffsetProvider
{
	public static final int ENERGY_CAPACITY = 16000;

	public MutableEnergyStorage energyStorage = new MutableEnergyStorage(ENERGY_CAPACITY);
	public boolean redstoneControlInverted = false;

	// TODO move to UUID?
	public String owner;
	public List<String> targetList = new ArrayList<>();
	public boolean whitelist = false;
	public boolean attackAnimals = false;
	public boolean attackPlayers = false;
	public boolean attackNeutrals = false;

	protected int tick = 0;
	protected LivingEntity target;
	public float rotationYaw;
	public float rotationPitch;

	private UUID targetId;

	public TurretBlockEntity(BlockEntityType<T> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void tickClient()
	{
		tickCommon();
	}

	//TODO split sides more
	public void tickCommon()
	{
		double range = getRange();
		if(targetId!=null)
		{
			AABB validBox = Shapes.block().bounds().move(worldPosition).inflate(range);
			List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, validBox);
			for(LivingEntity entity : entities)
				if(entity.getUUID().equals(targetId)&&isValidTarget(entity, true))
				{
					target = entity;
					break;
				}
			targetId = null;
		}

		if(target!=null)
		{
			Vec3 delta = getGunToTargetVec(target);
			double dSq = delta.lengthSqr();
			if(dSq > range*range)
				this.target = null;
			else if(level.isClientSide)
			{
				double yaw = (Mth.atan2(delta.x, delta.z)*(180/Math.PI))-180;
				this.rotationPitch = (float)(Math.atan2(Math.sqrt(delta.x*delta.x+delta.z*delta.z), delta.y)*(180/Math.PI))-90;
				float defaultYaw = 180-getFacing().toYRot();
				if(this.rotationYaw==0)//moving from default
					this.rotationYaw = (float)(yaw*.5)-defaultYaw;
				else
					this.rotationYaw = (float)yaw-defaultYaw;
			}
		}
		else if(level.isClientSide)
		{
			this.rotationYaw *= .75;
			if(Math.abs(rotationYaw) < 10)
				this.rotationYaw = 0;
			this.rotationPitch *= .75;
			if(Math.abs(rotationPitch) < 10)
				this.rotationPitch = 0;
		}
	}

	@Override
	public void tickServer()
	{
		tickCommon();
		if(level.getGameTime()%64==((getBlockPos().getX()^getBlockPos().getZ())&63))
			markContainingBlockForUpdate(null);

		int energy = IEServerConfig.MACHINES.turret_consumption.get();
		if(isRSPowered()^redstoneControlInverted)
		{
			if(energyStorage.extractEnergy(energy, true)==energy)
			{
				energyStorage.extractEnergy(energy, false);
				if(target==null||!target.isAlive()||level.getEntity(target.getId())==null||target.getHealth() <= 0||!canShootEntity(target))
				{
					target = getTarget();
					if(target!=null)
					{
						this.setChanged();
						markContainingBlockForUpdate(null);
					}
				}

				//has target, Redstone control check and has power+ammo
				if(target!=null&&canActivate())
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
				}
				else
					tick = 0;
			}
		}
		else if(target!=null)
			target = null;
	}

	private boolean canShootEntity(LivingEntity entity)
	{
		Vec3 start = getGunPosition();
		Vec3 end = getTargetVector(entity);
		//Don't shoot through walls
		if(Utils.rayTraceForFirst(start, end, level, Collections.singleton(getBlockPos().above()))
				!=null)
			return false;
		//Don't shoot non-targeted entities between the turret and the target
		AABB potentialCollateralArea = entity.getBoundingBox().minmax(new AABB(worldPosition.above()));
		List<LivingEntity> potentialCollateral = level.getEntitiesOfClass(LivingEntity.class, potentialCollateralArea);
		for(LivingEntity coll : potentialCollateral)
		{
			AABB entityBB = coll.getBoundingBox().inflate(.125f/2+.4);//Add the range of a revolver bullet in all directions
			if(!isValidTarget(coll, false)&&entityBB.clip(start, end).isPresent())
				return false;
		}
		return true;
	}

	protected Vec3 getTargetVector(LivingEntity e)
	{
		return new Vec3(e.getX(), e.getY()+.5*e.getEyeHeight(), e.getZ());
	}

	protected Vec3 getGunPosition()
	{
		return new Vec3(worldPosition.getX()+.5, worldPosition.getY()+1.375, worldPosition.getZ()+.5);
	}

	protected Vec3 getGunToTargetVec(LivingEntity target)
	{
		//target-gun
		return getGunPosition().vectorTo(getTargetVector(target));
	}

	@Nullable
	private LivingEntity getTarget()
	{
		double range = getRange();
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, new AABB(getBlockPos().getX()-range, getBlockPos().getY(), getBlockPos().getZ()-range, getBlockPos().getX()+range, getBlockPos().getY()+3, getBlockPos().getZ()+range));
		if(list.isEmpty())
			return null;
		for(LivingEntity entity : list)
			if(isValidTarget(entity, true))
				return entity;
		return null;
	}

	public boolean isValidTarget(LivingEntity entity, boolean checkCanShoot)
	{
		if(entity==null||!entity.isAlive()||entity.getHealth() <= 0)
			return false;
		//Continue if blacklist and name is in list, or whitelist and name is not in list
		boolean isListed = isListedName(targetList, entity.getName().getString()) || isListedName(targetList, entity.getType().getDescription().getString());
		if(whitelist^isListed)
			return false;
		//Same as above but for the owner of the pet, to prevent shooting wolves
		if(entity instanceof TamableAnimal)
		{
			Entity entityOwner = ((TamableAnimal)entity).getOwner();
			if(entityOwner!=null&&(whitelist^isListedName(targetList, entityOwner.getName().getString())))
				return false;
		}

		if(entity instanceof Animal&&!attackAnimals)
			return false;
		if(entity instanceof Player&&!attackPlayers)
			return false;
		if(!(entity instanceof Player)&&!(entity instanceof Animal)&&!(entity instanceof Enemy)&&!attackNeutrals)
			return false;

		if(target==null||getBlockPos().distToCenterSqr(entity.position()) < getBlockPos().distToCenterSqr(target.position()))
			return true;
		return !checkCanShoot||canShootEntity(entity);
	}

	private boolean isListedName(List<String> list, String name)
	{
		for(String s : list)
			if(name.equalsIgnoreCase(s))
				return true;
		return false;
	}

	protected abstract double getRange();

	protected abstract boolean canActivate();

	protected abstract int getChargeupTicks();

	protected abstract int getActiveTicks();

	protected abstract boolean loopActivation();

	protected abstract void activate();

	protected boolean hasOwnerRights(Player player)
	{
		if(player.getAbilities().instabuild||owner==null||owner.isEmpty())
			return true;
		return owner.equalsIgnoreCase(player.getName().getString());
	}

	public void resetTarget()
	{
		this.target = null;
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		EnergyHelper.deserializeFrom(energyStorage, nbt);

		if(nbt.contains("owner", Tag.TAG_STRING))
			owner = nbt.getString("owner");
		ListTag list = nbt.getList("targetList", 8);
		targetList.clear();
		for(int i = 0; i < list.size(); i++)
			targetList.add(list.getString(i));
		whitelist = nbt.getBoolean("whitelist");
		attackAnimals = nbt.getBoolean("attackAnimals");
		attackPlayers = nbt.getBoolean("attackPlayers");
		attackNeutrals = nbt.getBoolean("attackNeutrals");

		target = null;
		if(nbt.contains("target", Tag.TAG_STRING))
			targetId = UUID.fromString(nbt.getString("target"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putBoolean("redstoneInverted", redstoneControlInverted);
		EnergyHelper.serializeTo(energyStorage, nbt);

		if(owner!=null)
			nbt.putString("owner", owner);
		ListTag list = new ListTag();
		for(String s : targetList)
			list.add(StringTag.valueOf(s));
		nbt.put("targetList", list);
		nbt.putBoolean("whitelist", whitelist);
		nbt.putBoolean("attackAnimals", attackAnimals);
		nbt.putBoolean("attackPlayers", attackPlayers);
		nbt.putBoolean("attackNeutrals", attackNeutrals);

		if(target!=null)
			nbt.putString("target", target.getUUID().toString());
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(!isDummy())
			return Shapes.block();
		switch(getFacing())
		{
			case NORTH:
				return Shapes.box(.125f, .0625f, .125f, .875f, .875f, 1);
			case SOUTH:
				return Shapes.box(.125f, .0625f, 0, .875f, .875f, .875f);
			case WEST:
				return Shapes.box(.125f, .0625f, .125f, 1, .875f, .875f);
			case EAST:
				return Shapes.box(0, .0625f, .125f, .875f, .875f, .875f);
		}
		return Shapes.block();
	}

	AABB renderBB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderBB==null)
			renderBB = new AABB(getBlockPos().offset(-8, -8, -8), getBlockPos().offset(8, 8, 8));
		return renderBB;
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(isDummy())
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().below());
			if(te instanceof TurretBlockEntity<?>)
				return ((TurretBlockEntity<?>)te).screwdriverUseSide(side, player, hand, hitVec);
			return InteractionResult.FAIL;
		}
		if(player.isShiftKeyDown()&&!level.isClientSide)
		{
			redstoneControlInverted = !redstoneControlInverted;
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn": "invertedOff")),
					true
			);
			setChanged();
			this.markContainingBlockForUpdate(null);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return NonNullList.create();
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
	public void doGraphicalUpdates()
	{
	}

	@Override
	public boolean canUseGui(Player player)
	{
		if(hasOwnerRights(player))
			return true;
		player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"notOwner", owner), true);
		return false;
	}

	@Override
	public T getGuiMaster()
	{
		if(!isDummy())
			return (T)this;
		BlockEntity te = level.getBlockEntity(getBlockPos().below());
		if(te instanceof TurretBlockEntity<?>)
			return (T)te;
		return null;
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
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canEntityDestroy(Entity entity)
	{
		if(isDummy())
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().below());
			if(te instanceof TurretBlockEntity<?>)
				return ((TurretBlockEntity<?>)te).canEntityDestroy(entity);
		}
		if(entity instanceof Player)
			return hasOwnerRights((Player)entity);
		return true;
	}

	@Override
	public boolean isDummy()
	{
		return getBlockState().getValue(IEProperties.MULTIBLOCKSLAVE);
	}

	@Nullable
	@Override
	public TurretBlockEntity<T> master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterBE instanceof TurretBlockEntity<?> turret)
			return (TurretBlockEntity<T>)turret;
		BlockPos masterPos = getBlockPos().below();
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return this.getClass().isInstance(te)?(TurretBlockEntity<T>)te: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		level.setBlockAndUpdate(worldPosition.above(), getBlockState().setValue(IEProperties.MULTIBLOCKSLAVE, true));
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		tempMasterBE = master();
		BlockPos otherPos = isDummy()?getBlockPos().below(): getBlockPos().above();
		if(level.getBlockEntity(otherPos) instanceof TurretBlockEntity<?>)
			level.removeBlock(otherPos, false);
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
		Entity player = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		ItemStack stack = new ItemStack(state.getBlock(), 1);
		TurretBlockEntity<?> turret = this;
		if(isDummy())
		{
			turret = master();
			if(turret==null)
			{
				drop.accept(stack);
				return;
			}
		}

		CompoundTag tag = new CompoundTag();
		//Only writing values when they are different from defaults
		if(turret.owner!=null&&(player==null||!player.getName().getString().equalsIgnoreCase(turret.owner)))
			tag.putString("owner", turret.owner);
		if(turret.targetList.size()!=1||!isListedName(turret.targetList, turret.owner))
		{
			ListTag list = new ListTag();
			for(String s : turret.targetList)
				list.add(StringTag.valueOf(s));
			tag.put("targetList", list);
		}
		if(turret.whitelist)
			tag.putBoolean("whitelist", turret.whitelist);
		if(turret.attackAnimals)
			tag.putBoolean("attackAnimals", turret.attackAnimals);
		if(!turret.attackPlayers)
			tag.putBoolean("attackPlayers", turret.attackPlayers);
		if(turret.attackNeutrals)
			tag.putBoolean("attackNeutrals", turret.attackNeutrals);
		if(turret.redstoneControlInverted)
			tag.putBoolean("redstoneControlInverted", turret.redstoneControlInverted);

		if(!tag.isEmpty())
			stack.setTag(tag);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		final Player placer = ctx.getPlayer();
		if(stack.hasTag())
		{
			CompoundTag tag = stack.getOrCreateTag();
			if(tag.contains("owner", Tag.TAG_STRING))
				this.owner = tag.getString("owner");
			else if(placer!=null)
				this.owner = placer.getName().getString();
			if(tag.contains("targetList", Tag.TAG_LIST))
			{
				ListTag list = tag.getList("targetList", 8);
				targetList.clear();
				for(int i = 0; i < list.size(); i++)
					targetList.add(list.getString(i));
			}
			else if(owner!=null)
				targetList.add(owner);
			if(tag.contains("whitelist", Tag.TAG_BYTE))
				whitelist = tag.getBoolean("whitelist");
			if(tag.contains("attackAnimals", Tag.TAG_BYTE))
				attackAnimals = tag.getBoolean("attackAnimals");
			if(tag.contains("attackPlayers", Tag.TAG_BYTE))
				attackPlayers = tag.getBoolean("attackPlayers");
			if(tag.contains("attackNeutrals", Tag.TAG_BYTE))
				attackNeutrals = tag.getBoolean("attackNeutrals");
			if(tag.contains("redstoneControlInverted", Tag.TAG_BYTE))
				redstoneControlInverted = tag.getBoolean("redstoneControlInverted");
		}
		else if(placer!=null)
		{
			this.owner = placer.getName().getString();
			targetList.add(owner);
		}
	}

	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, TurretBlockEntity::master, registerEnergyInput(energyStorage)
	);

	@Nonnull
	@Override
	public <T2> LazyOptional<T2> getCapability(@Nonnull Capability<T2> cap, @Nullable Direction side)
	{
		if(cap==ForgeCapabilities.ENERGY&&(side!=null||!isDummy()))
			return energyCap.getAndCast();
		return super.getCapability(cap, side);
	}

	public void setDummy(boolean dummy)
	{
		BlockState old = getBlockState();
		BlockState newState = old.setValue(IEProperties.MULTIBLOCKSLAVE, dummy);
		level.setBlockAndUpdate(worldPosition, newState);
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		if(isDummy())
			return new BlockPos(0, 1, 0);
		else
			return BlockPos.ZERO;
	}
}