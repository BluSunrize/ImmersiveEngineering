/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderAndCase;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.client.render.tooltip.RevolverServerTooltip;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.network.MessageSpeedloaderSync;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ItemContainerType;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.*;

import static blusunrize.immersiveengineering.common.register.IEDataComponents.REVOLVER_COOLDOWN;

public class RevolverItem extends UpgradeableToolItem implements IBulletContainer, IZoomTool
{
	public RevolverItem()
	{
		super(
				new Properties().stacksTo(1)
						.component(IEDataComponents.REVOLVER_PERKS, Perks.EMPTY)
						.component(IEDataComponents.REVOLVER_ELITE, "")
						.component(REVOLVER_COOLDOWN, RevolverCooldowns.DEFAULT),
				"REVOLVER"
		);
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(ItemCallback.USE_IEOBJ_RENDER);
	}

	public static final ResourceLocation speedModUUID = IEApi.ieLoc("speed_modifier");
	public static final ResourceLocation luckModUUID = IEApi.ieLoc("luck_modifier");

	/* ------------- CORE ITEM METHODS ------------- */

	public static void registerCapabilities(ItemCapabilityRegistrar registrar)
	{
		registrar.register(CapabilityShader.ITEM, stack -> new ShaderWrapper_Item(IEApi.ieLoc("revolver"), stack));
		InternalStorageItem.registerCapabilitiesISI(registrar);
	}

	/* ------------- INTERNAL INVENTORY ------------- */

	@Override
	public int getSlotCount()
	{
		return 18+2+1;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]
				{
						new IESlot.Upgrades(container, toolInventory, 18+0, 80, 32, "REVOLVER", stack, true, level, getPlayer),
						new IESlot.Upgrades(container, toolInventory, 18+1, 100, 32, "REVOLVER", stack, true, level, getPlayer)
				};
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(ItemHandler.ITEM);
		if(inv!=null&&!inv.getStackInSlot(18).isEmpty()&&!inv.getStackInSlot(19).isEmpty())
			Utils.unlockIEAdvancement(player, "tools/upgrade_revolver");
	}

	/* ------------- NAME, TOOLTIP, SUB-ITEMS ------------- */

	@Nonnull
	@Override
	public String getDescriptionId(@Nonnull ItemStack stack)
	{
		String tag = getRevolverDisplayTag(stack);
		if(!tag.isEmpty())
			return this.getDescriptionId()+"."+tag;
		return super.getDescriptionId(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		String tag = getRevolverDisplayTag(stack);
		if(!tag.isEmpty())
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver."+tag));
		else if(stack.has(IEDataComponents.REVOLVER_FLAVOUR))
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver."+stack.get(IEDataComponents.REVOLVER_FLAVOUR)));
		else
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver"));

		for(var entry : getPerks(stack).perks().entrySet())
			list.add(Component.literal("  ").append(entry.getKey().getDisplayString(entry.getValue())));
	}

	@Nonnull
	@Override
	public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack pStack)
	{
		return Optional.of(new RevolverServerTooltip(getBullets(pStack), getBulletCount(pStack)));
	}

	/* ------------- ATTRIBUTES, UPDATE, RIGHTCLICK ------------- */

	@Override
	public ItemAttributeModifiers getAttributeModifiers(ItemStack stack)
	{
		var builder = ItemAttributeModifiers.builder();
		if(getUpgrades(stack).getBoolean("fancyAnimation"))
			builder.add(
					Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
			);
		double melee = getUpgradeValue_d(stack, "melee");
		if(melee!=0)
		{
			builder.add(
					Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
			);
			builder.add(
					Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, melee, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
			);
		}
		double speed = getUpgradeValue_d(stack, "speed");
		if(speed!=0)
			builder.add(
					Attributes.MOVEMENT_SPEED, new AttributeModifier(speedModUUID, speed, Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.HAND
			);

		double luck = getUpgradeValue_d(stack, RevolverPerk.LUCK.getNBTKey());
		if(luck!=0)
			builder.add(
					Attributes.LUCK, new AttributeModifier(luckModUUID, luck, Operation.ADD_VALUE), EquipmentSlotGroup.HAND
			);
		return builder.build();
	}

	@Override
	public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull Entity ent, int slot, boolean inHand)
	{
		super.inventoryTick(stack, world, ent, slot, inHand);
		final var cooldowns = getCooldowns(stack);
		if(cooldowns.reloadTimer > 0||cooldowns.fireCooldown > 0)
			stack.set(
					REVOLVER_COOLDOWN,
					new RevolverCooldowns(Math.max(cooldowns.reloadTimer-1, 0), Math.max(cooldowns.fireCooldown-1, 0))
			);
	}

	@Nonnull
	@Override
	public UseAnim getUseAnimation(@Nonnull ItemStack stack)
	{
		return UseAnim.BOW;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		ItemStack revolver = player.getItemInHand(hand);
		final var cooldowns = getCooldowns(revolver);
		if(!world.isClientSide)
		{
			if(player.isShiftKeyDown())
			{
				openGui(player, hand);
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
			}
			else if(player.getAttackStrengthScale(1) >= 1)
			{
				if(this.getUpgrades(revolver).getBoolean("nerf"))
					world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1f, 0.6f);
				else
				{
					if(cooldowns.fireCooldown > 0||cooldowns.reloadTimer > 0)
						return new InteractionResultHolder<>(InteractionResult.PASS, revolver);

					NonNullList<ItemStack> bullets = getBullets(revolver);

					// Try reloading
					if(isEmpty(revolver, false))
						for(int i = 0; i < player.getInventory().getContainerSize(); i++)
						{
							ItemStack stack = player.getInventory().getItem(i);
							if(stack.getItem() instanceof SpeedloaderItem&&!((SpeedloaderItem)stack.getItem()).isEmpty(stack))
							{
								for(ItemStack b : bullets)
									if(!b.isEmpty())
										world.addFreshEntity(new ItemEntity(world, player.getX(), player.getY(), player.getZ(), b));
								var bulletList = getContainedItems(stack).stream().collect(ListUtils.collector());
								setBullets(revolver, bulletList, true);
								((SpeedloaderItem)stack.getItem()).setContainedItems(stack, NonNullList.withSize(8, ItemStack.EMPTY));
								player.getInventory().setChanged();
								if(player instanceof ServerPlayer)
									PacketDistributor.sendToPlayer((ServerPlayer)player, new MessageSpeedloaderSync(i, hand));

								revolver.set(REVOLVER_COOLDOWN, new RevolverCooldowns(60, cooldowns.fireCooldown));
								return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
							}
						}

					ItemStack bulletStack = bullets.get(0);
					Item bullet0 = bulletStack.getItem();
					if(bullet0 instanceof BulletItem)
					{
						IBullet<?> bullet = ((BulletItem<?>)bullet0).getType();
						if(bullet!=null)
						{
							float noise = fireProjectile(world, player, revolver, bullet, bulletStack);
							bullets.set(0, bullet.getCasing(bullets.get(0)).copy());
							// alert nearby enemies
							Utils.attractEnemies(player, 64*noise);
							// Revolvers with more than 60% noise reduction do not trigger sculk sensors
							if(noise > .2f)
							{
								// anything louder than default is considered an explosion
								var eventTriggered = noise > 0.5?GameEvent.EXPLODE: GameEvent.PROJECTILE_SHOOT;
								world.gameEvent(eventTriggered, player.position(), GameEvent.Context.of(player));
							}
						}
						else
							world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 1f, 1f);
					}
					else
						world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 1f, 1f);

					rotateCylinder(revolver, player, true, bullets);
					revolver.set(REVOLVER_COOLDOWN, new RevolverCooldowns(cooldowns.reloadTimer, getMaxShootCooldown(revolver)));
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
				}
			}
		}
		else if(!player.isShiftKeyDown())
		{
			if(cooldowns.reloadTimer > 0||cooldowns.fireCooldown > 0)
				return new InteractionResultHolder<>(InteractionResult.PASS, revolver);
			NonNullList<ItemStack> bullets = getBullets(revolver);
			if(!bullets.get(0).isEmpty()&&bullets.get(0).getItem() instanceof BulletItem)
			{
				ShaderAndCase shader = ShaderRegistry.getStoredShaderAndCase(revolver);
				if(shader!=null)
				{

					Vec3 pos = Utils.getLivingFrontPos(player, .75, player.getBbHeight()*.75, ItemUtils.getLivingHand(player, hand), false, 1);
					shader.registryEntry().getEffectFunction().execute(world, revolver,
							shader.sCase().getShaderType().toString(), pos,
							Vec3.directionFromRotation(player.getRotationVector()), .125f);
				}
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, revolver);
	}

	public static float fireProjectile(Level world, LivingEntity shooter, ItemStack revolver, IBullet bullet, ItemStack bulletStack)
	{
		Player player = shooter instanceof Player p?p: null;
		Vec3 vec = shooter.getLookAngle();
		boolean electro = getUpgradesStatic(revolver).getBoolean("electro");
		int count = bullet.getProjectileCount(player);
		if(count==1)
		{
			Entity entBullet = getBullet(shooter, vec, bullet, electro);
			shooter.level().addFreshEntity(bullet.getProjectile(player, bulletStack, entBullet, electro));
		}
		else
			for(int i = 0; i < count; i++)
			{
				Vec3 vecDir = vec.add(shooter.getRandom().nextGaussian()*.1, shooter.getRandom().nextGaussian()*.1, shooter.getRandom().nextGaussian()*.1);
				Entity entBullet = getBullet(shooter, vecDir, bullet, electro);
				shooter.level().addFreshEntity(bullet.getProjectile(player, bulletStack, entBullet, electro));
			}

		float noise = 0.5f;
		if(hasUpgradeValue(revolver, RevolverPerk.NOISE.getNBTKey()))
			noise *= (float)getUpgradeValue_d(revolver, RevolverPerk.NOISE.getNBTKey());
		SoundEvent sound = bullet.getSound();
		if(sound==null)
			sound = IESounds.revolverFire.value();
		world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), sound, SoundSource.PLAYERS, noise, 1f);
		return noise;
	}

	public int getMaxShootCooldown(ItemStack stack)
	{
		if(hasUpgradeValue(stack, RevolverPerk.COOLDOWN.getNBTKey()))
			return (int)Math.ceil(15*getUpgradeValue_d(stack, RevolverPerk.COOLDOWN.getNBTKey()));
		return 15;
	}

	/* ------------- IBulletContainer ------------- */

	@Override
	public int getBulletCount(ItemStack revolver)
	{
		return 8+this.getUpgrades(revolver).getInt("bullets");
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver)
	{
		return getContainedItems(revolver).stream()
				.limit(getBulletCount(revolver))
				.collect(ListUtils.collector());
	}

	/* ------------- BULLET UTILITY ------------- */

	private static RevolvershotEntity getBullet(LivingEntity living, Vec3 vecDir, IBullet type, boolean electro)
	{
		RevolvershotEntity bullet = new RevolvershotEntity(living.level(), living, vecDir.x*1.5, vecDir.y*1.5, vecDir.z*1.5, type);
		bullet.setDeltaMovement(vecDir.scale(2));
		bullet.bulletElectro = electro;
		return bullet;
	}

	public void setBullets(ItemStack revolver, NonNullList<ItemStack> bullets, boolean ignoreExtendedMag)
	{
		IItemHandlerModifiable inv = (IItemHandlerModifiable)revolver.getCapability(ItemHandler.ITEM);
		for(int i = 0; i < 18; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		if(ignoreExtendedMag&&getUpgrades(revolver).getInt("bullets") > 0)
			for(int i = 0; i < bullets.size(); i++)
				inv.setStackInSlot(i < 2?i: i+getUpgrades(revolver).getInt("bullets"), bullets.get(i));
		else
			for(int i = 0; i < bullets.size(); i++)
				inv.setStackInSlot(i, bullets.get(i));
	}

	public void rotateCylinder(ItemStack revolver, Player player, boolean forward, NonNullList<ItemStack> bullets)
	{
		NonNullList<ItemStack> cycled = NonNullList.withSize(getBulletCount(revolver), ItemStack.EMPTY);
		int offset = forward?-1: 1;
		for(int i = 0; i < cycled.size(); i++)
			cycled.set((i+offset+cycled.size())%cycled.size(), bullets.get(i));
		setBullets(revolver, cycled, false);
		player.getInventory().setChanged();
	}

	public void rotateCylinder(ItemStack revolver, Player player, boolean forward)
	{
		NonNullList<ItemStack> bullets = getBullets(revolver);
		rotateCylinder(revolver, player, forward, bullets);
	}

	public boolean isEmpty(ItemStack stack, boolean allowCasing)
	{
		IItemHandler inv = stack.getCapability(ItemHandler.ITEM);
		if(inv==null)
			return true;
		for(int i = 0; i < inv.getSlots(); i++)
		{
			ItemStack b = inv.getStackInSlot(i);
			boolean isValid = true;
			if(!allowCasing)
				isValid = b.getItem() instanceof BulletItem;
			if(!b.isEmpty()&&isValid)
				return false;
		}
		return true;
	}

	/* ------------- UPGRADES & PERKS ------------- */

	@Override
	public CompoundTag getUpgradeBase(ItemStack stack)
	{
		return ItemNBTHelper.getTagCompound(stack, "baseUpgrades");
	}

	public String getRevolverDisplayTag(ItemStack revolver)
	{
		String tag = revolver.get(IEDataComponents.REVOLVER_ELITE);
		if(!tag.isEmpty())
		{
			int split = tag.lastIndexOf("_");
			if(split < 0)
				split = tag.length();
			return tag.substring(0, split);
		}
		return "";
	}

	public static Perks getPerks(ItemStack stack)
	{
		return stack.getOrDefault(IEDataComponents.REVOLVER_PERKS, Perks.EMPTY);
	}

	public static boolean hasUpgradeValue(ItemStack stack, String key)
	{
		if(getUpgradesStatic(stack).contains(key))
			return true;
		var perk = RevolverPerk.get(key);
		return perk!=null&&getPerks(stack).perks.containsKey(perk);
	}

	public static double getUpgradeValue_d(ItemStack stack, String key)
	{
		double baseValue = getUpgradesStatic(stack).getDouble(key);
		var perk = RevolverPerk.get(key);
		if(perk!=null)
			baseValue += getPerks(stack).perks.getOrDefault(perk, 0.);
		return baseValue;
	}

	@Override
	public boolean canZoom(ItemStack stack, Player player)
	{
		return hasUpgradeValue(stack, "scope");
	}

	float[] zoomSteps = new float[]{.3125f, .4f, .5f, .625f};

	@Override
	public float[] getZoomSteps(ItemStack stack, Player player)
	{
		return zoomSteps;
	}

	/* ------------- CRAFTING ------------- */

	@Override
	public void onCraftedBy(ItemStack stack, @Nonnull Level world, @Nonnull Player player)
	{
		if(stack.isEmpty()||player==null)
			return;

		String uuid = player.getUUID().toString();
		if(specialRevolvers.containsKey(uuid))
		{
			ArrayList<SpecialRevolver> list = new ArrayList<>(specialRevolvers.get(uuid));
			if(!list.isEmpty())
			{
				list.add(null);
				String existingTag = stack.get(IEDataComponents.REVOLVER_ELITE);
				if(existingTag.isEmpty())
					applySpecialCrafting(stack, list.get(0));
				else
				{
					int i = 0;
					for(; i < list.size(); i++)
						if(list.get(i)!=null&&existingTag.equals(list.get(i).tag))
							break;
					int next = (i+1)%list.size();
					applySpecialCrafting(stack, list.get(next));
				}
			}
		}
		this.recalculateUpgrades(stack, world, player);
	}

	public void applySpecialCrafting(ItemStack stack, SpecialRevolver r)
	{
		if(r==null)
		{
			stack.set(IEDataComponents.REVOLVER_ELITE, "");
			stack.remove(IEDataComponents.REVOLVER_FLAVOUR);
			ItemNBTHelper.remove(stack, "baseUpgrades");
			return;
		}
		if(r.tag!=null&&!r.tag.isEmpty())
			stack.set(IEDataComponents.REVOLVER_ELITE, r.tag);
		if(r.flavour!=null&&!r.flavour.isEmpty())
			stack.set(IEDataComponents.REVOLVER_FLAVOUR, r.flavour);
		CompoundTag baseUpgrades = new CompoundTag();
		for(Map.Entry<String, Object> e : r.baseUpgrades.entrySet())
		{
			if(e.getValue() instanceof Boolean)
				baseUpgrades.putBoolean(e.getKey(), (Boolean)e.getValue());
			else if(e.getValue() instanceof Integer)
				baseUpgrades.putInt(e.getKey(), (Integer)e.getValue());
			else if(e.getValue() instanceof Float)
				baseUpgrades.putDouble(e.getKey(), (Float)e.getValue());
			else if(e.getValue() instanceof Double)
				baseUpgrades.putDouble(e.getKey(), (Double)e.getValue());
			else if(e.getValue() instanceof String)
				baseUpgrades.putString(e.getKey(), (String)e.getValue());
		}
		ItemNBTHelper.setTagCompound(stack, "baseUpgrades", baseUpgrades);
	}

	/* ------------- RENDERING ------------- */

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged||CapabilityShader.shouldReequipDueToShader(oldStack, newStack))
			return true;

		return false;
	}

	@Nullable
	@Override
	protected ItemContainerType<?> getContainerType()
	{
		return IEMenuTypes.REVOLVER;
	}

	/* ------------- INNER CLASSES ------------- */

	public static final Multimap<String, SpecialRevolver> specialRevolvers = ArrayListMultimap.create();
	public static final Map<String, SpecialRevolver> specialRevolversByTag = new HashMap<>();

	public record SpecialRevolver(
			String[] uuid,
			String tag,
			String flavour,
			HashMap<String, Object> baseUpgrades,
			String[] renderAdditions
	)
	{
	}

	@ParametersAreNonnullByDefault
	public enum RevolverPerk
	{
		COOLDOWN(f -> f > 1,
				f -> Utils.NUMBERFORMAT_PREFIXED.format((1-f)*100),
				(l, r) -> l*r,
				1, -0.75, -0.05),
		NOISE(f -> f > 1,
				f -> Utils.NUMBERFORMAT_PREFIXED.format((f-1)*100),
				(l, r) -> l*r,
				1, -.9, -0.1),
		LUCK(f -> f < 0,
				f -> Utils.NUMBERFORMAT_PREFIXED.format(f*100),
				(l, r) -> l+r,
				0, 3, 0.5);

		private final DoublePredicate isBadValue;
		private final Function<Double, String> valueFormatter;
		private final DoubleBinaryOperator valueConcat;
		private final double generate_median;
		private final double generate_deviation;
		private final double generate_luckScale;

		RevolverPerk(DoublePredicate isBadValue, Function<Double, String> valueFormatter, DoubleBinaryOperator valueConcat, double generate_median, double generate_deviation, double generate_luckScale)
		{
			this.isBadValue = isBadValue;
			this.valueFormatter = valueFormatter;
			this.valueConcat = valueConcat;
			this.generate_median = generate_median;
			this.generate_deviation = generate_deviation;
			this.generate_luckScale = generate_luckScale;
		}

		public String getNBTKey()
		{
			return name().toLowerCase(Locale.US);
		}

		public Component getDisplayString(double value)
		{
			String key = Lib.DESC_INFO+"revolver.perk."+this.toString();
			return Component.translatable(key, valueFormatter.apply(value))
					.withStyle(isBadValue.test(value)?ChatFormatting.RED: ChatFormatting.BLUE);
		}

		public static Component getFormattedName(Component name, Perks perks)
		{
			double averageTier = 0;
			for(var entry : perks.perks.entrySet())
			{
				RevolverItem.RevolverPerk perk = entry.getKey();
				double value = entry.getValue();
				double dTier = (value-perk.generate_median)/perk.generate_deviation*3;
				averageTier += dTier;
				int iTier = (int)Mth.clamp((dTier < 0?Math.floor(dTier): Math.ceil(dTier)), -3, 3);
				if(iTier==0)
					iTier = 1;
				String translate = Lib.DESC_INFO+"revolver.perk."+perk.name().toLowerCase(Locale.US)+".tier"+iTier;
				name = Component.translatable(translate, name);
			}

			int rarityTier = (int)Math.ceil(Mth.clamp(averageTier+3, 0, 6)/6*5);
			Rarity rarity = rarityTier==5?Lib.RARITY_MASTERWORK: rarityTier==4?Rarity.EPIC: rarityTier==3?Rarity.RARE: rarityTier==2?Rarity.UNCOMMON: Rarity.COMMON;
			return name.copy().withStyle(rarity.color());
		}

		public static int calculateTier(Perks perks)
		{
			double averageTier = 0;
			for(var entry : perks.perks.entrySet())
			{
				RevolverItem.RevolverPerk perk = entry.getKey();
				double value = entry.getValue();
				double dTier = (value-perk.generate_median)/perk.generate_deviation*3;
				averageTier += dTier;
			}
			return (int)Math.ceil(Mth.clamp(averageTier+3, 0, 6)/6*5);
		}

		public double concat(double left, double right)
		{
			return this.valueConcat.applyAsDouble(left, right);
		}

		public double generateValue(RandomSource rand, boolean isBad, float luck)
		{
			double d = Utils.generateLuckInfluencedDouble(generate_median, generate_deviation, luck, rand, isBad, generate_luckScale);
			int i = (int)(d*100);
			d = i/100d;
			return d;
		}

		@Override
		public String toString()
		{
			return this.name().toLowerCase(Locale.US);
		}

		public static RevolverPerk get(String name)
		{
			try
			{
				return valueOf(name.toUpperCase(Locale.US));
			} catch(Exception e)
			{
				return null;
			}
		}

		public static RevolverPerk getRandom(RandomSource rand)
		{
			int i = rand.nextInt(values().length);
			return values()[i];
		}

		public static Perks generatePerkSet(RandomSource rand, float luck)
		{
			RevolverPerk goodPerk = RevolverPerk.getRandom(rand);
			RevolverPerk badPerk = RevolverPerk.LUCK;
			//RevolverPerk.getRandom(rand);
			double val = goodPerk.generateValue(rand, false, luck);

			EnumMap<RevolverPerk, Double> perks = new EnumMap<>(RevolverPerk.class);
			if(goodPerk==badPerk)
				val = (val+badPerk.generateValue(rand, true, luck))/2;
			else
				perks.put(badPerk, badPerk.generateValue(rand, true, luck));
			perks.put(goodPerk, val);

			return new Perks(perks);
		}
	}

	public static RevolverCooldowns getCooldowns(ItemStack revolver)
	{
		return revolver.getOrDefault(REVOLVER_COOLDOWN, RevolverCooldowns.DEFAULT);
	}

	public record Perks(EnumMap<RevolverPerk, Double> perks)
	{
		public static final DualCodec<ByteBuf, Perks> CODECS = DualCodecs.forEnumMap(
				RevolverPerk.values(), DualCodecs.DOUBLE
		).map(Perks::new, Perks::perks);
		public static final Perks EMPTY = new Perks(new EnumMap<>(RevolverPerk.class));
	}

	public record RevolverCooldowns(int reloadTimer, int fireCooldown)
	{
		public static final DualCodec<ByteBuf, RevolverCooldowns> CODECS = DualCodecs.composite(
				DualCodecs.INT.fieldOf("reloadTimer"), RevolverCooldowns::reloadTimer,
				DualCodecs.INT.fieldOf("fireCooldown"), RevolverCooldowns::fireCooldown,
				RevolverCooldowns::new
		);
		public static final RevolverCooldowns DEFAULT = new RevolverCooldowns(0, 0);
	}
}