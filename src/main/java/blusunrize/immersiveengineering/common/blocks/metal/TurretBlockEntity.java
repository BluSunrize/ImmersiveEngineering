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
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

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
	protected final static int[] IDLE_YAW_UPDATE_INTERVAL_TICKS;
	protected final static float[] IDLE_YAWS;

	public MutableEnergyStorage energyStorage = new MutableEnergyStorage(ENERGY_CAPACITY);

	// TODO move to UUID?
	public String owner;
	public TurretConfig config = TurretConfig.DEFAULT;

	protected int tick = 0;
	protected LivingEntity target;
	public float rotationYaw;
	public float rotationPitch;

	private UUID targetId;

	static
	{
		// time intervals
		final int TICKS_PER_SECOND = 20;
		final int MIN_SECOND = 3;
		final int MAX_SECOND = 7;
		int secondIntervals = MAX_SECOND - MIN_SECOND + 1;
		int totalTick = secondIntervals * TICKS_PER_SECOND;
		IDLE_YAW_UPDATE_INTERVAL_TICKS = new int[totalTick];
		int startingTick = MIN_SECOND * TICKS_PER_SECOND;
		for (int i = 0; i < totalTick; i++)
		{
			IDLE_YAW_UPDATE_INTERVAL_TICKS[i] = startingTick + i;
		}

		// yaws
		final float ANGLE_INTERVAL_DEG = 0.5f;
		final float MIN_ANGLE_DEG = 10f;
		final float MAX_ANGLE_DEG = 30f;

		// if minAngleDeg is 0 then 0 is duplicated here, not a problem really but ever so slightly annoying
		int totalYawOn1Side = (int) ((MAX_ANGLE_DEG - MIN_ANGLE_DEG) / ANGLE_INTERVAL_DEG) + 1;
		IDLE_YAWS = new float[totalYawOn1Side * 2];
		for (int i = 0; i < totalYawOn1Side; i++)
		{
			float yaw = MIN_ANGLE_DEG + ANGLE_INTERVAL_DEG * i;
			IDLE_YAWS[2 * i] = yaw;
			IDLE_YAWS[2 * i + 1] = -yaw;
		}
	}

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
				yaw = normalizeToHalfPiesRange((float)yaw);
				this.rotationPitch = (float)(Math.atan2(Math.sqrt(delta.x*delta.x+delta.z*delta.z), delta.y)*(180/Math.PI))-90;
				float defaultYaw = 180-getFacing().toYRot();
				defaultYaw = normalizeToHalfPiesRange((float)defaultYaw);
				if(this.rotationYaw==0)//moving from default
					this.rotationYaw = (float)(yaw-defaultYaw) * 0.5f;
				else
					this.rotationYaw = (float)yaw-defaultYaw;

				this.rotationYaw = normalizeToHalfPiesRange(this.rotationYaw);
				this.rotationPitch = normalizeToHalfPiesRange(this.rotationPitch);
			}
		}
		else if(level.isClientSide)
		{
			final float LERP_DECAY_FACTOR = 0.25f;
			final float LERP_SNAP_THRESHOLD_DEG = 10f;

			float currentYaw = this.rotationYaw;
			float idleYaw = getTargetIdleYaw();
			float yawDiff = idleYaw - currentYaw;
			if (Math.abs(yawDiff) < LERP_SNAP_THRESHOLD_DEG)
			{
				currentYaw = idleYaw;
			}
			else
			{
				currentYaw = currentYaw + yawDiff * LERP_DECAY_FACTOR;
			}
			this.rotationYaw = currentYaw;

			float currentPitch = this.rotationPitch;
			float idlePitch = getTargetIdlePitch();
			float pitchDiff = idlePitch - currentPitch;
			if (Math.abs(pitchDiff) < LERP_SNAP_THRESHOLD_DEG)
			{
				currentPitch = idlePitch;
			}
			else
			{
				currentPitch = currentPitch + pitchDiff * LERP_DECAY_FACTOR;
			}
			this.rotationPitch = currentPitch;
		}
	}

	@Override
	public void tickServer()
	{
		tickCommon();
		if(level.getGameTime()%64==((getBlockPos().getX()^getBlockPos().getZ())&63))
			markContainingBlockForUpdate(null);

		int energy = IEServerConfig.MACHINES.turret_consumption.get();
		if(isRSPowered()^config.redstoneControlInverted)
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
		boolean isListed = isListedName(config.targetList, entity.getName().getString())||isListedName(config.targetList, entity.getType().getDescription().getString());
		if(config.whitelist^isListed)
			return false;
		//Same as above but for the owner of the pet, to prevent shooting wolves
		if(entity instanceof TamableAnimal)
		{
			Entity entityOwner = ((TamableAnimal)entity).getOwner();
			if(entityOwner!=null&&(config.whitelist^isListedName(config.targetList, entityOwner.getName().getString())))
				return false;
		}

		if(entity instanceof Animal&&!config.attackAnimals)
			return false;
		if(entity instanceof Player&&!config.attackPlayers)
			return false;
		if(!(entity instanceof Player)&&!(entity instanceof Animal)&&!(entity instanceof Enemy)&&!config.attackNeutrals)
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
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		EnergyHelper.deserializeFrom(energyStorage, nbt, provider);

		if(nbt.contains("owner"))
			owner = nbt.getString("owner");
		else
			owner = null;
		this.config = TurretConfig.CODECS.codec().decode(NbtOps.INSTANCE, nbt.get("config")).getOrThrow().getFirst();

		target = null;
		if(nbt.contains("target", Tag.TAG_STRING))
			targetId = UUID.fromString(nbt.getString("target"));
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		EnergyHelper.serializeTo(energyStorage, nbt, provider);

		if(owner!=null)
			nbt.putString("owner", owner);

		nbt.put("config", TurretConfig.CODECS.codec().encodeStart(NbtOps.INSTANCE, config).getOrThrow());

		if(target!=null)
			nbt.putString("target", target.getUUID().toString());
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(!isDummy())
			return Shapes.block();
		return switch(getFacing())
		{
			case NORTH -> Shapes.box(.125f, .0625f, .125f, .875f, .875f, 1);
			case SOUTH -> Shapes.box(.125f, .0625f, 0, .875f, .875f, .875f);
			case WEST -> Shapes.box(.125f, .0625f, .125f, 1, .875f, .875f);
			case EAST -> Shapes.box(0, .0625f, .125f, .875f, .875f, .875f);
			default -> Shapes.block();
		};
	}

	public AABB renderBB;

	@Override
	public ItemInteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(isDummy())
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().below());
			if(te instanceof TurretBlockEntity<?>)
				return ((TurretBlockEntity<?>)te).screwdriverUseSide(side, player, hand, hitVec);
			return ItemInteractionResult.FAIL;
		}
		if(player.isShiftKeyDown()&&!level.isClientSide)
		{
			config = new TurretConfig(
					config.targetList, config.whitelist, config.attackAnimals, config.attackPlayers, config.attackNeutrals, !config.redstoneControlInverted
			);
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"rsControl."+(config.redstoneControlInverted?"invertedOn": "invertedOff")),
					true
			);
			setChanged();
			this.markContainingBlockForUpdate(null);
		}
		return ItemInteractionResult.SUCCESS;
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
		stack.set(IEDataComponents.TURRET_DATA, config);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		final Player placer = ctx.getPlayer();
		final var configFromStack = stack.get(IEDataComponents.TURRET_DATA);
		if(configFromStack!=null)
			config = configFromStack;
		if(placer!=null)
		{
			this.owner = placer.getName().getString();
			config = config.addToList(this.owner);
		}
	}

	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, TurretBlockEntity::master, makeEnergyInput(energyStorage)
	);

	protected static <T extends TurretBlockEntity<T>>
	void registerCapabilitiesBase(BECapabilityRegistrar<T> registrar)
	{
		registrar.register(EnergyStorage.BLOCK, (be, side) -> {
			if(side!=null||!be.isDummy())
				return ((TurretBlockEntity<?>)be).energyCap.get();
			else
				return null;
		});
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

	protected float getTargetIdleYaw()
	{
		final float IDLE_YAW_DEG = 0f;
		boolean isOn = isRSPowered() ^ config.redstoneControlInverted;
		if (!isOn)
		{
			return IDLE_YAW_DEG;
		}
		int energyPassiveConsumptionCost = IEServerConfig.MACHINES.turret_consumption.get();
		if (energyStorage == null || energyStorage.getEnergyStored() < energyPassiveConsumptionCost)
		{
			return IDLE_YAW_DEG;
		}
		int worldTick =
				(int) (getLevelNonnull().getGameTime() % (long) Integer.MAX_VALUE);
		BlockPos bp = getBlockPos();
		int gridIndex =
				Math.abs(hashNonNegative(bp.getX()) + hashNonNegative(bp.getY()) - hashNonNegative((bp.getZ() * 3)));
		int idleYawUpdateInterval = IDLE_YAW_UPDATE_INTERVAL_TICKS[gridIndex % IDLE_YAW_UPDATE_INTERVAL_TICKS.length];
		int bucket = worldTick / idleYawUpdateInterval;
		return IDLE_YAWS[hashNonNegative(bucket) % IDLE_YAWS.length];
	}

	protected float getTargetIdlePitch()
	{
		final float INACTIVE_PITCH_DEG = 20f;
		final float IDLE_PITCH_DEG = 0f;
		boolean isOn = isRSPowered() ^ config.redstoneControlInverted;
		if (!isOn)
		{
			return INACTIVE_PITCH_DEG;
		}
		int energyPassiveConsumptionCost = IEServerConfig.MACHINES.turret_consumption.get();
		if (energyStorage == null || energyStorage.getEnergyStored() < energyPassiveConsumptionCost)
		{
			return INACTIVE_PITCH_DEG;
		}
		return IDLE_PITCH_DEG;
	}

	protected static float normalizeToHalfPiesRange(float deg)
	{
		while (deg <= -180f)
		{
			deg += 360f;
		}
		while (deg > 180f)
		{
			deg -= 360f;
		}
		return deg;
	}

	// Source - https://stackoverflow.com/a/12996028
	// Posted by Thomas Mueller, modified by community. See post 'Timeline' for change history
	// Retrieved 2026-08-25, License - CC BY-SA 4.0
	private static int hashNonNegative(int x)
	{
		x = ~((x >>> 16) ^ x) * 0x45d9f3b;
		x = ((x >>> 16) ^ x) * 0x45d9f3b;
		x = (x >>> 16) ^ x;
		// added to ensure positive output for indexing purposes
		x = x & ~(1 << 31);
		return x;
	}

	public record TurretConfig(
			List<String> targetList,
			boolean whitelist,
			boolean attackAnimals,
			boolean attackPlayers,
			boolean attackNeutrals,
			boolean redstoneControlInverted
	)
	{
		public static final DualCodec<ByteBuf, TurretConfig> CODECS = DualCompositeCodecs.composite(
				DualCodecs.STRING.listOf().fieldOf("targetList"), TurretConfig::targetList,
				DualCodecs.BOOL.fieldOf("whitelist"), TurretConfig::whitelist,
				DualCodecs.BOOL.fieldOf("attackAnimals"), TurretConfig::attackAnimals,
				DualCodecs.BOOL.fieldOf("attackPlayers"), TurretConfig::attackPlayers,
				DualCodecs.BOOL.fieldOf("attackNeutrals"), TurretConfig::attackNeutrals,
				DualCodecs.BOOL.fieldOf("redstoneControlInverted"), TurretConfig::redstoneControlInverted,
				TurretConfig::new
		);
		public static final TurretConfig DEFAULT = new TurretConfig(
				List.of(), false, false, false, false, false
		);

		public TurretConfig
		{
			targetList = List.copyOf(targetList);
		}

		public TurretConfig addToList(String name)
		{
			if(targetList.contains(name))
				return this;
			else
			{
				List<String> newTargets = new ArrayList<>(targetList);
				newTargets.add(name);
				return withTargetList(newTargets);
			}
		}

		public TurretConfig withWhitelist(boolean whitelist)
		{
			return new TurretConfig(targetList, whitelist, attackAnimals, attackPlayers, attackNeutrals, redstoneControlInverted);
		}

		public TurretConfig withAttackAnimals(boolean attackAnimals)
		{
			return new TurretConfig(targetList, whitelist, attackAnimals, attackPlayers, attackNeutrals, redstoneControlInverted);
		}

		public TurretConfig withAttackPlayers(boolean attackPlayers)
		{
			return new TurretConfig(targetList, whitelist, attackAnimals, attackPlayers, attackNeutrals, redstoneControlInverted);
		}

		public TurretConfig withAttackNeutrals(boolean attackNeutrals)
		{
			return new TurretConfig(targetList, whitelist, attackAnimals, attackPlayers, attackNeutrals, redstoneControlInverted);
		}

		public TurretConfig withTargetList(List<String> targetList)
		{
			return new TurretConfig(targetList, whitelist, attackAnimals, attackPlayers, attackNeutrals, redstoneControlInverted);
		}
	}
}