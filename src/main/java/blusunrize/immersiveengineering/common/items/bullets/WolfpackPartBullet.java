/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.bullets;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEApiDataComponents.CodecPair;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import com.mojang.datafixers.util.Unit;

public class WolfpackPartBullet extends BulletHandler.DamagingBullet<Unit>
{
	public WolfpackPartBullet()
	{
		super(
				CodecPair.UNIT,
				(projectile, shooter, hit) -> IEDamageSources.causeWolfpackDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_WolfpackPart::get,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				IEApi.ieLoc("item/bullet_wolfpack")
		);
	}

	@Override
	public boolean isProperCartridge()
	{
		return false;
	}
}
