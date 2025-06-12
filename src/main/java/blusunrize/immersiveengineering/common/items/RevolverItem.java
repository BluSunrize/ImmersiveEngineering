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
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderAndCase;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeData;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import blusunrize.immersiveengineering.client.render.tooltip.RevolverServerTooltip;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.network.MessageSpeedloaderSync;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ItemContainerType;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ListUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.register.IEDataComponents.BASE_UPGRADES;
import static blusunrize.immersiveengineering.common.register.IEDataComponents.REVOLVER_COOLDOWN;

public class RevolverItem extends UpgradeableToolItem implements IBulletContainer, IZoomTool
{
	public static final String TYPE = "REVOLVER";

	public RevolverItem()
	{
		super(
				new Properties().stacksTo(1)
						.component(IEDataComponents.REVOLVER_PERKS, Perks.EMPTY)
						.component(IEDataComponents.REVOLVER_ELITE, "")
						.component(REVOLVER_COOLDOWN, RevolverCooldowns.DEFAULT),
				TYPE,
				18+2+1
		);
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
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]
				{
						new IESlot.Upgrades(container, toolInventory, 18+0, 80, 32, TYPE, stack, true, level, getPlayer),
						new IESlot.Upgrades(container, toolInventory, 18+1, 100, 32, TYPE, stack, true, level, getPlayer)
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
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver."+tag).withStyle(ChatFormatting.GRAY));
		else if(stack.has(IEDataComponents.REVOLVER_FLAVOUR))
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver."+stack.get(IEDataComponents.REVOLVER_FLAVOUR)).withStyle(ChatFormatting.GRAY));
		else
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"revolver").withStyle(ChatFormatting.GRAY));

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
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)
	{
		var builder = ItemAttributeModifiers.builder();
		if(getUpgrades(stack).has(UpgradeEffect.FANCY_ANIMATION))
			builder.add(
					Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
			);
		final var upgrades = getUpgradesStatic(stack);
		double melee = upgrades.get(UpgradeEffect.MELEE);
		if(melee!=0)
		{
			builder.add(
					Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
			);
			builder.add(
					Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, melee, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
			);
		}
		double speed = upgrades.get(UpgradeEffect.SPEED);
		if(speed!=0)
			builder.add(
					Attributes.MOVEMENT_SPEED, new AttributeModifier(speedModUUID, speed, Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.HAND
			);

		double luck = upgrades.get(UpgradeEffect.LUCK);
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
		if(player.isShiftKeyDown())
		{
			openGui(player, hand);
			return InteractionResultHolder.sidedSuccess(revolver, world.isClientSide());
		}

		// not yet fully drawn
		if(player.getAttackStrengthScale(1) < 1)
			return InteractionResultHolder.pass(revolver);

		if(this.getUpgrades(revolver).has(UpgradeEffect.NERF))
		{
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1f, 0.6f);
			return InteractionResultHolder.sidedSuccess(revolver, world.isClientSide());
		}

		// on cooldown, can't be used
		if(player.getCooldowns().isOnCooldown(this))
			return InteractionResultHolder.pass(revolver);

		NonNullList<ItemStack> bullets = getBullets(revolver);
		// check if empty and try to use speedloader
		if(bullets.stream().noneMatch(stack -> stack.getItem() instanceof BulletItem))
		{
			if(useSpeedloader(world, player, revolver, hand, bullets))
				return InteractionResultHolder.sidedSuccess(revolver, world.isClientSide());
		}

		ItemStack bulletStack = bullets.get(0);
					Item bullet0 = bulletStack.getItem();
					if(bullet0 instanceof BulletItem)
					{
						IBullet<?> bullet = ((BulletItem<?>)bullet0).getType();
						if(bullet!=null)
						{
							if(!world.isClientSide())
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
								player.gameEvent(eventTriggered);
							}


							// fancy particle effects for shaders
							ShaderAndCase shader = ShaderRegistry.getStoredShaderAndCase(revolver);
							if(shader!=null)
							{
								Vec3 pos = Utils.getLivingFrontPos(player, .75, player.getBbHeight()*.75, ItemUtils.getLivingHand(player, hand), false, 1);
								shader.registryEntry().getEffectFunction().execute(world, revolver,
										shader.sCase().getShaderType().toString(), pos,
										Vec3.directionFromRotation(player.getRotationVector()), .125f);
							}
						}
						}
						else
							world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 1f, 1f);
					}
					else
						world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 1f, 1f);

		rotateCylinder(revolver, player, true, bullets);
		player.getCooldowns().addCooldown(this, getMaxShootCooldown(revolver));
		return InteractionResultHolder.sidedSuccess(revolver, world.isClientSide());
	}

public boolean useSpeedloader(Level level, Player player, ItemStack revolver, InteractionHand hand, NonNullList<ItemStack> bullets)
{
	for(int i = 0; i < player.getInventory().getContainerSize(); i++)
		{
			ItemStack stack = player.getInventory().getItem(i);
			if(stack.getItem() instanceof SpeedloaderItem speedloader&&!speedloader.isEmpty(stack))
			{
				if(!level.isClientSide())
				{
					for(ItemStack b : bullets)
						if(!b.isEmpty())
							level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), b));
					var bulletList = speedloader.getBullets(stack);
					setBullets(revolver, bulletList, true);
					((SpeedloaderItem)stack.getItem()).setContainedItems(stack, NonNullList.withSize(8, ItemStack.EMPTY));
					player.getInventory().setChanged();
					if(player instanceof ServerPlayer)
						PacketDistributor.sendToPlayer((ServerPlayer)player, new MessageSpeedloaderSync(i, hand));
				}
				// set cooldown & animation timer
				var oldCooldowns = getCooldowns(revolver);
				revolver.set(REVOLVER_COOLDOWN, new RevolverCooldowns(60, oldCooldowns.fireCooldown));
				player.getCooldowns().addCooldown(this, 60);
				return true;
			}
		}
	return false;
	}

	public static float fireProjectile(Level world, LivingEntity shooter, ItemStack revolver, IBullet bullet, ItemStack bulletStack)
	{
		Player player = shooter instanceof Player p?p: null;
		Vec3 vec = shooter.getLookAngle();
		boolean electro = getUpgradesStatic(revolver).has(UpgradeEffect.ELECTRO);
		int count = bullet.getProjectileCount(player);
		if(count==1)
			shooter.level().addFreshEntity(getBullet(shooter, player, vec, bulletStack, electro));
		else
			for(int i = 0; i < count; i++)
			{
				Vec3 vecDir = vec.add(shooter.getRandom().nextGaussian()*.1, shooter.getRandom().nextGaussian()*.1, shooter.getRandom().nextGaussian()*.1);
				shooter.level().addFreshEntity(getBullet(shooter, player, vecDir, bulletStack, electro));
			}

		var upgrades = getUpgradesStatic(revolver);
		float noise = upgrades.get(UpgradeEffect.NOISE)/2;
		SoundEvent sound = bullet.getSound();
		if(sound==null)
			sound = IESounds.revolverFire.value();
		world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), sound, SoundSource.PLAYERS, noise, 1f);
		return noise;
	}

	public int getMaxShootCooldown(ItemStack stack)
	{
		return (int)Math.ceil(15*getUpgradesStatic(stack).get(UpgradeEffect.COOLDOWN));
	}

	/* ------------- IBulletContainer ------------- */

	@Override
	public int getBulletCount(ItemStack revolver)
	{
		return 8+this.getUpgrades(revolver).get(UpgradeEffect.BULLETS);
	}

	@Override
	public NonNullList<ItemStack> getBullets(ItemStack revolver)
	{
		return ListUtils.fromStream(getContainedItems(revolver).stream(), getBulletCount(revolver));
	}

	/* ------------- BULLET UTILITY ------------- */

	private static Entity getBullet(
			LivingEntity living, @Nullable Player player, Vec3 vecDir, ItemStack bulletStack, boolean electro
	)
	{
		return ((BulletItem<?>)bulletStack.getItem()).createBullet(
				living.level(), player, living.getEyePosition(), vecDir, bulletStack, electro
		);
	}

	public void setBullets(ItemStack revolver, NonNullList<ItemStack> bullets, boolean ignoreExtendedMag)
	{
		IItemHandlerModifiable inv = (IItemHandlerModifiable)revolver.getCapability(ItemHandler.ITEM);
		for(int i = 0; i < 18; i++)
			inv.setStackInSlot(i, ItemStack.EMPTY);
		if(ignoreExtendedMag&&getUpgrades(revolver).get(UpgradeEffect.BULLETS) > 0)
			for(int i = 0; i < bullets.size(); i++)
				inv.setStackInSlot(i < 2?i: i+getUpgrades(revolver).get(UpgradeEffect.BULLETS), bullets.get(i));
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

	/* ------------- UPGRADES & PERKS ------------- */

	@Override
	public UpgradeData getUpgradeBase(ItemStack stack)
	{
		return stack.getOrDefault(IEDataComponents.BASE_UPGRADES, UpgradeData.EMPTY);
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

	@Override
	public boolean canZoom(ItemStack stack, Player player)
	{
		return getUpgradesStatic(stack).has(UpgradeEffect.SCOPE);
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
			stack.remove(IEDataComponents.UPGRADE_DATA);
			return;
		}
		if(r.tag!=null&&!r.tag.isEmpty())
			stack.set(IEDataComponents.REVOLVER_ELITE, r.tag);
		if(r.flavour!=null&&!r.flavour.isEmpty())
			stack.set(IEDataComponents.REVOLVER_FLAVOUR, r.flavour);
		stack.set(BASE_UPGRADES, r.baseUpgrades);
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
			List<String> uuid,
			String tag,
			String flavour,
			UpgradeData baseUpgrades,
			List<String> renderAdditions
	)
	{
		public static final DualCodec<ByteBuf, SpecialRevolver> CODECS = DualCompositeCodecs.composite(
				DualCodecs.STRING.listOf().fieldOf("uuid"), SpecialRevolver::uuid,
				DualCodecs.STRING.fieldOf("tag"), SpecialRevolver::tag,
				DualCodecs.STRING.fieldOf("flavour"), SpecialRevolver::flavour,
				UpgradeData.SPECIAL_REVOLVER_CODEC.fieldOf("baseUpgrades"), SpecialRevolver::baseUpgrades,
				DualCodecs.STRING.listOf().fieldOf("renderAdditions"), SpecialRevolver::renderAdditions,
				SpecialRevolver::new
		);
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
				Double::sum,
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
			Rarity rarity = switch(rarityTier)
			{
				case 5 -> Lib.RARITY_MASTERWORK.getValue();
				case 4 -> Rarity.EPIC;
				case 3 -> Rarity.RARE;
				case 2 -> Rarity.UNCOMMON;
				default -> Rarity.COMMON;
			};
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

	public record Perks(Map<RevolverPerk, Double> perks)
	{
		public static final DualCodec<ByteBuf, Perks> CODECS = IEDualCodecs.forMap(
				IEDualCodecs.forEnum(RevolverPerk.values()), DualCodecs.DOUBLE
		).map(Perks::new, Perks::perks);
		public static final Perks EMPTY = new Perks(new EnumMap<>(RevolverPerk.class));
	}

	public record RevolverCooldowns(int reloadTimer, int fireCooldown)
	{
		public static final DualCodec<ByteBuf, RevolverCooldowns> CODECS = DualCompositeCodecs.composite(
				DualCodecs.INT.fieldOf("reloadTimer"), RevolverCooldowns::reloadTimer,
				DualCodecs.INT.fieldOf("fireCooldown"), RevolverCooldowns::fireCooldown,
				RevolverCooldowns::new
		);
		public static final RevolverCooldowns DEFAULT = new RevolverCooldowns(0, 0);
	}
}