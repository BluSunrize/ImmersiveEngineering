/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.bullets;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.CodecsAndDefault;
import blusunrize.immersiveengineering.common.util.IESounds;
import malte0811.dualcodecs.DualCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class FireworkBullet implements BulletHandler.IBullet<Fireworks>
{
	static ResourceLocation[] textures = {IEApi.ieLoc("item/bullet_firework")};
	private static final CodecsAndDefault<Fireworks> CODEC = new CodecsAndDefault<>(
			new DualCodec<>(Fireworks.CODEC, Fireworks.STREAM_CODEC), new Fireworks(1, List.of()), DataComponents.FIREWORKS
	);

	public FireworkBullet()
	{
	}

	@Override
	public CodecsAndDefault<Fireworks> getCodec()
	{
		return CODEC;
	}

	@Override
	public Entity getProjectile(Player shooter, Fireworks data, Entity projectile, boolean electro)
	{
		ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
		fireworkStack.set(DataComponents.FIREWORKS, data);
		FireworkRocketEntity firework = new FireworkRocketEntity(projectile.level(), fireworkStack, projectile.getX(), projectile.getY(), projectile.getZ(), true);
		Vec3 vector = projectile.getDeltaMovement();
		firework.shoot(vector.x(), vector.y(), vector.z(), 1.6f, 1.0f);
		return firework;
	}

	@Override
	public SoundEvent getSound()
	{
		return IESounds.revolverFireThump.value();
	}

	@Override
	public void onHitTarget(Level world, HitResult target, UUID shooter, Entity projectile, boolean headshot, Fireworks bulletData)
	{
	}

	@Override
	public ItemStack getCasing(ItemStack stack)
	{
		return BulletHandler.emptyShell.asItem().getDefaultInstance();
	}

	@Override
	public ResourceLocation[] getTextures()
	{
		return textures;
	}

	@Override
	public void addTooltip(Fireworks data, TooltipContext world, List<Component> list, TooltipFlag flag)
	{
		if(data!=null)
			data.addToTooltip(world, list::add, flag);
	}

	@Override
	public boolean isValidForTurret()
	{
		return true;
	}
}
