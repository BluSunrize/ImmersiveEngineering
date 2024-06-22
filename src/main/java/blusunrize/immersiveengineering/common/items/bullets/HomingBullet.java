/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.bullets;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.CodecsAndDefault;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.entities.RevolvershotHomingEntity;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import com.mojang.datafixers.util.Unit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.DoubleSupplier;

public class HomingBullet extends BulletHandler.DamagingBullet<Unit>
{
	public HomingBullet(DoubleSupplier damage, ResourceLocation... textures)
	{
		super(
				CodecsAndDefault.UNIT,
				(projectile, shooter, hit) -> IEDamageSources.causeHomingDamage((RevolvershotEntity)projectile, shooter),
				damage,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				textures
		);
	}

	@Override
	public Entity getProjectile(Player shooter, Unit data, Entity projectile, boolean electro)
	{
		RevolvershotHomingEntity shot = shooter!=null?new RevolvershotHomingEntity(projectile.level(), shooter,
				projectile.getDeltaMovement().x*1.5, projectile.getDeltaMovement().y*1.5, projectile.getDeltaMovement().z*1.5, this): new RevolvershotHomingEntity(projectile.level(), projectile.getX(), projectile.getY(), projectile.getZ(), 0, 0, 0, this);
		shot.setDeltaMovement(projectile.getDeltaMovement());
		shot.bulletElectro = electro;
		return shot;
	}
}
