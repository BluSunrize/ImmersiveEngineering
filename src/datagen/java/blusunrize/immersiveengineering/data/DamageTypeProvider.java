/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.Lib.DamageTypes;
import blusunrize.immersiveengineering.api.Lib.TurretDamageType;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.damagesource.DamageType;

public class DamageTypeProvider
{
	private static final float DEFAULT_EXHAUSTION = 0.1f;

	public static void bootstrap(BootstapContext<DamageType> ctx)
	{
		registerTurretCapable(ctx, DamageTypes.REVOLVER_CASULL, "ieRevolver_casull");
		registerTurretCapable(ctx, DamageTypes.REVOLVER_ARMORPIERCING, "ieRevolver_armorPiercing");
		registerTurretCapable(ctx, DamageTypes.REVOLVER_BUCKSHOT, "ieRevolver_buckshot");
		registerTurretCapable(ctx, DamageTypes.REVOLVER_DRAGONSBREATH, "ieRevolver_dragonsbreath");
		registerTurretCapable(ctx, DamageTypes.REVOLVER_HOMING, "ieRevolver_homing");
		registerTurretCapable(ctx, DamageTypes.REVOLVER_WOLFPACK, "ieRevolver_wolfpack");
		registerTurretCapable(ctx, DamageTypes.REVOLVER_SILVER, "ieRevolver_silver");
		registerTurretCapable(ctx, DamageTypes.REVOLVER_POTION, "ieRevolver_potion");
		ctx.register(DamageTypes.CRUSHER, new DamageType("ieCrushed", DEFAULT_EXHAUSTION));
		ctx.register(DamageTypes.SAWMILL, new DamageType("ieSawmill", DEFAULT_EXHAUSTION));
		ctx.register(DamageTypes.TESLA, new DamageType("ieTesla", DEFAULT_EXHAUSTION));
		ctx.register(DamageTypes.ACID, new DamageType("ieAcid", DEFAULT_EXHAUSTION));
		registerTurretCapable(ctx, DamageTypes.RAILGUN, "ieRailgun");
		registerTurretCapable(ctx, DamageTypes.SAWBLADE, "ieSawblade");
		ctx.register(DamageTypes.TESLA_PRIMARY, new DamageType("ieTeslaPrimary", DEFAULT_EXHAUSTION));
		ctx.register(DamageTypes.RAZOR_WIRE, new DamageType("ieRazorWire", DEFAULT_EXHAUSTION));
		ctx.register(DamageTypes.RAZOR_SHOCK, new DamageType("ieRazorShock", DEFAULT_EXHAUSTION));
		ctx.register(DamageTypes.WIRE_SHOCK, new DamageType("ieWireShock", DEFAULT_EXHAUSTION));
	}

	private static void registerTurretCapable(BootstapContext<DamageType> ctx, TurretDamageType type, String path)
	{
		ctx.register(type.playerType(), new DamageType(path, DEFAULT_EXHAUSTION));
		ctx.register(type.turretType(), new DamageType(path+".turret", DEFAULT_EXHAUSTION));
	}
}
