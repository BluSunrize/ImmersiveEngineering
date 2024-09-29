/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.bullets;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.CodecsAndDefault;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.client.utils.FontUtils;
import blusunrize.immersiveengineering.common.entities.RevolvershotFlareEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.UUID;

public class FlareBullet implements BulletHandler.IBullet<Color4>
{
	static ResourceLocation[] textures = {IEApi.ieLoc("item/bullet_flare"), IEApi.ieLoc("item/bullet_flare_layer")};
	private static final CodecsAndDefault<Color4> CODEC = new CodecsAndDefault<>(Color4.CODECS, Color4.fromRGB(0xcc2e06));

	public FlareBullet()
	{
	}

	@Override
	public CodecsAndDefault<Color4> getCodec()
	{
		return CODEC;
	}

	@Override
	public Entity getProjectile(Player shooter, Color4 color, Entity projectile, boolean electro)
	{
		RevolvershotFlareEntity flare = shooter!=null?new RevolvershotFlareEntity(projectile.level(), shooter,
				projectile.getDeltaMovement().x*1.5,
				projectile.getDeltaMovement().y*1.5,
				projectile.getDeltaMovement().z*1.5, color):
				new RevolvershotFlareEntity(projectile.level(), projectile.getX(), projectile.getY(), projectile.getZ(), 0, 0, 0, color);
		flare.setDeltaMovement(projectile.getDeltaMovement());
		flare.bulletElectro = electro;
		return flare;
	}

	@Override
	public void onHitTarget(Level world, HitResult target, UUID shooter, Entity projectile, boolean headshot, Color4 bulletData)
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
	public void addTooltip(Color4 color, TooltipContext world, List<Component> list, TooltipFlag flag)
	{
		list.add(FontUtils.withAppendColoredColour(Component.translatable(Lib.DESC_INFO+"bullet.flareColour"), color.toInt()));
	}

	@Override
	public Color4 getColour(Color4 color, int layer)
	{
		if(layer!=1)
			return Color4.WHITE;
		return color;
	}

	@Override
	public boolean isValidForTurret()
	{
		return true;
	}
}
